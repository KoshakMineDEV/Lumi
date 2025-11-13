package cn.nukkit.blockentity.impl;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.blockentity.ICommandBlock;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.command.CommandBlockExecuteEvent;
import cn.nukkit.inventory.CommandBlockInventory;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.lang.TextContainer;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.lang.CommandOutputContainer;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachment;
import cn.nukkit.permission.PermissionAttachmentInfo;
import cn.nukkit.permission.PermissibleBase;
import cn.nukkit.permission.Permissible;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import com.google.common.base.Strings;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class BlockEntityCommandBlock extends BlockEntitySpawnable implements ICommandBlock {
    protected boolean conditionalMode;
    protected boolean auto;
    protected String command;
    protected long lastExecution;
    protected boolean trackOutput;
    protected String lastOutput;
    protected ListTag<StringTag> lastOutputParams;
    protected int lastOutputCommandMode;
    protected boolean lastOutputCondionalMode;
    protected boolean lastOutputRedstoneMode;
    protected int successCount;
    protected boolean conditionMet;
    protected boolean powered;
    protected int tickDelay;
    protected boolean executingOnFirstTick;
    protected PermissibleBase perm;
    protected int currentTick;
    protected CommandBlockInventory inventory;

    public BlockEntityCommandBlock(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        inventory = new CommandBlockInventory(this);
    }

    @Override
    protected void initBlockEntity() {
        super.initBlockEntity();
        this.perm = new PermissibleBase(this);
        this.currentTick = 0;

        if (this.namedTag.contains(TAG_POWERED)) {
            this.powered = this.namedTag.getBoolean(TAG_POWERED);
        } else {
            this.powered = false;
        }

        if (this.namedTag.contains(TAG_CONDITIONAL_MODE)) {
            this.conditionalMode = this.namedTag.getBoolean(TAG_CONDITIONAL_MODE);
        } else {
            Block block = this.getLevelBlock();
            if (block != null) {
                int damage = block.getDamage();
                this.conditionalMode = (damage & 0x08) != 0;
            } else {
                this.conditionalMode = false;
            }
        }

        if (this.namedTag.contains(TAG_AUTO)) {
            this.auto = this.namedTag.getBoolean(TAG_AUTO);
        } else {
            this.auto = true;
        }

        if (this.namedTag.contains(TAG_COMMAND)) {
            setCommand(this.namedTag.getString(TAG_COMMAND));
        } else {
            setCommand("");
        }

        if (this.namedTag.contains(TAG_LAST_EXECUTION)) {
            this.lastExecution = this.namedTag.getLong(TAG_LAST_EXECUTION);
        } else {
            this.lastExecution = 0;
        }

        if (this.namedTag.contains(TAG_TRACK_OUTPUT)) {
            this.trackOutput = this.namedTag.getBoolean(TAG_TRACK_OUTPUT);
        } else {
            this.trackOutput = true;
        }

        if (this.namedTag.contains(TAG_LAST_OUTPUT)) {
            this.lastOutput = this.namedTag.getString(TAG_LAST_OUTPUT);
        } else {
            this.lastOutput = null;
        }

        if (this.namedTag.contains(TAG_LAST_OUTPUT_PARAMS)) {
            this.lastOutputParams = this.namedTag.getList(TAG_LAST_OUTPUT_PARAMS, StringTag.class);
        } else {
            this.lastOutputParams = new ListTag<>();
        }

        if (this.namedTag.contains(TAG_LP_COMMAND_MODE)) {
            this.lastOutputCommandMode = this.namedTag.getInt(TAG_LP_COMMAND_MODE);
        } else {
            this.lastOutputCommandMode = 0;
        }

        if (this.namedTag.contains(TAG_LP_CONDIONAL_MODE)) {
            this.lastOutputCondionalMode = this.namedTag.getBoolean(TAG_LP_CONDIONAL_MODE);
        } else {
            this.lastOutputCondionalMode = true;
        }

        if (this.namedTag.contains(TAG_LP_REDSTONE_MODE)) {
            this.lastOutputRedstoneMode = this.namedTag.getBoolean(TAG_LP_REDSTONE_MODE);
        } else {
            this.lastOutputRedstoneMode = true;
        }

        if (this.namedTag.contains(TAG_SUCCESS_COUNT)) {
            this.successCount = this.namedTag.getInt(TAG_SUCCESS_COUNT);
        } else {
            this.successCount = 0;
        }

        if (this.namedTag.contains(TAG_CONDITION_MET)) {
            this.conditionMet = this.namedTag.getBoolean(TAG_CONDITION_MET);
        } else {
            this.conditionMet = false;
        }

        if (this.namedTag.contains(TAG_TICK_DELAY)) {
            this.tickDelay = this.namedTag.getInt(TAG_TICK_DELAY);
        } else {
            this.tickDelay = 0;
        }

        if (this.namedTag.contains(TAG_EXECUTE_ON_FIRST_TICK)) {
            this.executingOnFirstTick = this.namedTag.getBoolean(TAG_EXECUTE_ON_FIRST_TICK);
        } else {
            this.executingOnFirstTick = false;
        }

        if (this.getMode() == MODE_REPEATING) {
            this.scheduleUpdate();
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putBoolean(TAG_POWERED, this.powered);
        this.namedTag.putBoolean(TAG_CONDITIONAL_MODE, this.conditionalMode);
        this.namedTag.putBoolean(TAG_AUTO, this.auto);
        if (this.command != null && !this.command.isEmpty()) {
            this.namedTag.putString(TAG_COMMAND, this.command);
        }
        this.namedTag.putLong(TAG_LAST_EXECUTION, this.lastExecution);
        this.namedTag.putBoolean(TAG_TRACK_OUTPUT, this.trackOutput);
        if (this.lastOutput != null && !this.lastOutput.isEmpty()) {
            this.namedTag.putString(TAG_LAST_OUTPUT, this.lastOutput);
        }
        if (this.lastOutputParams != null) {
            this.namedTag.putList(TAG_LAST_OUTPUT_PARAMS, this.lastOutputParams);
        }
        this.namedTag.putInt(TAG_LP_COMMAND_MODE, this.lastOutputCommandMode);
        this.namedTag.putBoolean(TAG_LP_CONDIONAL_MODE, this.lastOutputCondionalMode);
        this.namedTag.putBoolean(TAG_LP_REDSTONE_MODE, this.lastOutputRedstoneMode);
        this.namedTag.putInt(TAG_SUCCESS_COUNT, this.successCount);
        this.namedTag.putBoolean(TAG_CONDITION_MET, this.conditionMet);
        this.namedTag.putInt(TAG_VERSION, CURRENT_VERSION);
        this.namedTag.putInt(TAG_TICK_DELAY, this.tickDelay);
        this.namedTag.putBoolean(TAG_EXECUTE_ON_FIRST_TICK, this.executingOnFirstTick);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        CompoundTag nbt = new CompoundTag()
                .putString("id", BlockEntity.COMMAND_BLOCK)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z)
                .putBoolean(TAG_CONDITIONAL_MODE, this.conditionalMode)
                .putBoolean(TAG_AUTO, this.auto)
                .putLong(TAG_LAST_EXECUTION, this.lastExecution)
                .putBoolean(TAG_TRACK_OUTPUT, this.trackOutput)
                .putInt(TAG_LP_COMMAND_MODE, this.lastOutputCommandMode)
                .putBoolean(TAG_LP_CONDIONAL_MODE, this.lastOutputCondionalMode)
                .putBoolean(TAG_LP_REDSTONE_MODE, this.lastOutputRedstoneMode)
                .putInt(TAG_SUCCESS_COUNT, this.successCount)
                .putBoolean(TAG_CONDITION_MET, this.conditionMet)
                .putInt(TAG_VERSION, CURRENT_VERSION)
                .putInt(TAG_TICK_DELAY, this.tickDelay)
                .putBoolean(TAG_EXECUTE_ON_FIRST_TICK, this.executingOnFirstTick);
        if (this.command != null) {
            nbt.putString(TAG_COMMAND, this.command);
        }
        if (this.lastOutput != null) {
            nbt.putString(TAG_LAST_OUTPUT, this.lastOutput);
        }
        if (this.lastOutputParams != null) {
            nbt.putList(TAG_LAST_OUTPUT_PARAMS, this.lastOutputParams);
        }
        if (this.hasName()) {
            nbt.putString(TAG_CUSTOM_NAME, this.getName());
        }
        return nbt;
    }

    @Override
    public boolean isBlockEntityValid() {
        int blockId = this.getLevelBlock().getId();
        return blockId == BlockID.COMMAND_BLOCK || blockId == BlockID.CHAIN_COMMAND_BLOCK || blockId == BlockID.REPEATING_COMMAND_BLOCK;
    }

    @NotNull
    @Override
    public String getName() {
        return this.hasName() ? this.namedTag.getString(TAG_CUSTOM_NAME) : "!";
    }

    @Override
    public boolean hasName() {
        return this.namedTag.contains(TAG_CUSTOM_NAME);
    }

    @Override
    public void setName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            this.namedTag.remove(TAG_CUSTOM_NAME);
        } else {
            this.namedTag.putString(TAG_CUSTOM_NAME, name);
        }
    }

    @Override
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public boolean isPowered() {
        return this.powered;
    }

    @Override
    public boolean onUpdate() {
        if (this.getMode() != MODE_REPEATING) {
            return false;
        }

        if (this.currentTick++ < this.getTickDelay()) {
            return true;
        }

        this.execute();
        this.currentTick = 0;

        return true;
    }

    @Override
    public boolean execute(int chain) {
        if (!(this.getServer().getSettings().features().enableCommandBlocks() && this.level.getGameRules().getBoolean(GameRule.COMMAND_BLOCKS_ENABLED))) {
            return false;
        }

        Block block = this.getLevelBlock();
        int facing = block.getDamage() & 0x07;
        BlockFace facingDirection = BlockFace.fromIndex(facing);
        Block behind = block.getSide(facingDirection.getOpposite());
        
        if (this.isConditional() && (behind.getId() == BlockID.COMMAND_BLOCK || behind.getId() == BlockID.REPEATING_COMMAND_BLOCK || behind.getId() == BlockID.CHAIN_COMMAND_BLOCK)) {
            BlockEntity be = behind.getLevel().getBlockEntity(behind);
            if (be instanceof BlockEntityCommandBlock lastCB) {
                if (lastCB.getSuccessCount() == 0) {
                    Block next = block.getSide(facingDirection);
                    if (next.getId() == BlockID.CHAIN_COMMAND_BLOCK) {
                        BlockEntity nextBe = next.getLevel().getBlockEntity(next);
                        if (nextBe instanceof BlockEntityCommandBlock nextChainBlock) {
                            nextChainBlock.trigger(++chain);
                        }
                    }
                    return true;
                }
            }
        }

        if (this.getLastExecution() != this.getLevel().getCurrentTick()) {
            this.setConditionMet();
            
            boolean shouldExecute = this.isConditionMet() && (this.isAuto() || this.isPowered());
            
            if (shouldExecute) {
                String cmd = this.getCommand();
                if (cmd != null && !cmd.trim().isEmpty()) {
                    if (cmd.equalsIgnoreCase("Searge")) {
                        this.lastOutput = "#itzlipofutzli";
                        this.successCount = 1;
                    } else if (cmd.equalsIgnoreCase("Hello PNX!") || cmd.equalsIgnoreCase("Hello Lumi!")) {
                        this.lastOutput = "KoshakMineDEV\nSuperIceCN\nCoolLoong\nwode490390";
                        this.successCount = 1;
                    } else {
                        this.lastOutput = null;
                        CommandBlockExecuteEvent event = new CommandBlockExecuteEvent(this.getLevelBlock(), cmd);
                        Server.getInstance().getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                            return false;
                        }
                        try {
                            boolean result = Server.getInstance().dispatchCommand(this, cmd);
                            this.successCount = result ? 1 : 0;
                        } catch (Exception e) {
                            Server.getInstance().getLogger().error("Error executing command block command: " + cmd, e);
                            this.successCount = 0;
                        }
                    }
                }

                Block next = block.getSide(facingDirection);
                if (next.getId() == BlockID.CHAIN_COMMAND_BLOCK) {
                    BlockEntity nextBe = next.getLevel().getBlockEntity(next);
                    if (nextBe instanceof BlockEntityCommandBlock chainBlock) {
                        chainBlock.trigger(++chain);
                    }
                }
            }

            this.lastExecution = this.getLevel().getCurrentTick();
            this.lastOutputCommandMode = this.getMode();
            this.lastOutputCondionalMode = this.isConditional();
            this.lastOutputRedstoneMode = !this.isAuto();
        } else {
            this.successCount = 0;
        }

        for (int side = 0; side <= 5; side++) {
            Block sideBlock = block.getSide(BlockFace.fromIndex(side));
            sideBlock.onUpdate(Level.BLOCK_UPDATE_REDSTONE);
        }
        return true;
    }

    @Override
    public int getMode() {
        int blockId = this.getLevelBlock().getId();
        if (blockId == BlockID.REPEATING_COMMAND_BLOCK) {
            return MODE_REPEATING;
        } else if (blockId == BlockID.CHAIN_COMMAND_BLOCK) {
            return MODE_CHAIN;
        }
        return MODE_NORMAL;
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public void setCommand(String command) {
        this.command = command;
        this.successCount = 0;
    }

    @Override
    public boolean isAuto() {
        return this.auto;
    }

    @Override
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    @Override
    public boolean isConditional() {
        return this.conditionalMode;
    }

    @Override
    public void setConditional(boolean conditionalMode) {
        this.conditionalMode = conditionalMode;
        this.setConditionMet();
        
        Block block = this.getLevelBlock();
        if (block != null) {
            int damage = block.getDamage();
            int facingBits = damage & 0x07;
            int conditionalBit = conditionalMode ? 0x08 : 0;
            int newDamage = facingBits | conditionalBit;
            
            if (damage != newDamage) {
                block.setDamage(newDamage);
                this.level.setBlock(block, block, false, true);
            }
        }
    }

    @Override
    public boolean isConditionMet() {
        return this.conditionMet;
    }

    @Override
    public boolean setConditionMet() {
        if (this.isConditional()) {
            Block block = this.getLevelBlock();
            int facing = block.getDamage() & 0x07;
            BlockFace facingDirection = BlockFace.fromIndex(facing);
            Block behind = block.getSide(facingDirection.getOpposite());
            
            if (behind.getId() == BlockID.COMMAND_BLOCK || behind.getId() == BlockID.REPEATING_COMMAND_BLOCK || behind.getId() == BlockID.CHAIN_COMMAND_BLOCK) {
                BlockEntity be = behind.getLevel().getBlockEntity(behind);
                if (be instanceof BlockEntityCommandBlock next) {
                    this.conditionMet = next.getSuccessCount() > 0;
                } else {
                    this.conditionMet = false;
                }
            } else {
                this.conditionMet = false;
            }
        } else {
            this.conditionMet = true;
        }
        return this.conditionMet;
    }

    @Override
    public int getSuccessCount() {
        return this.successCount;
    }

    @Override
    public void setSuccessCount(int count) {
        this.successCount = count;
    }

    @Override
    public long getLastExecution() {
        return this.lastExecution;
    }

    @Override
    public void setLastExecution(long time) {
        this.lastExecution = time;
    }

    @Override
    public boolean isTrackingOutput() {
        return this.trackOutput;
    }

    @Override
    public void setTrackOutput(boolean track) {
        this.trackOutput = track;
    }

    @Override
    public String getLastOutput() {
        return this.lastOutput;
    }

    @Override
    public void setLastOutput(String output) {
        if (Strings.isNullOrEmpty(output)) {
            this.lastOutput = null;
        } else {
            this.lastOutput = output;
        }
    }

    @Override
    public int getLastOutputCommandMode() {
        return this.lastOutputCommandMode;
    }

    @Override
    public void setLastOutputCommandMode(int mode) {
        this.lastOutputCommandMode = mode;
    }

    @Override
    public boolean isLastOutputCondionalMode() {
        return this.lastOutputCondionalMode;
    }

    @Override
    public void setLastOutputCondionalMode(boolean condionalMode) {
        this.lastOutputCondionalMode = condionalMode;
    }

    @Override
    public boolean isLastOutputRedstoneMode() {
        return this.lastOutputRedstoneMode;
    }

    @Override
    public void setLastOutputRedstoneMode(boolean redstoneMode) {
        this.lastOutputRedstoneMode = redstoneMode;
    }

    @Override
    public void setLastOutputParams(ListTag<StringTag> params) {
        this.lastOutputParams = params;
    }

    @Override
    public int getTickDelay() {
        return this.tickDelay;
    }

    @Override
    public void setTickDelay(int tickDelay) {
        this.tickDelay = tickDelay;
    }

    @Override
    public boolean isExecutingOnFirstTick() {
        return this.executingOnFirstTick;
    }

    @Override
    public void setExecutingOnFirstTick(boolean executingOnFirstTick) {
        this.executingOnFirstTick = executingOnFirstTick;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return this.perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return this.perm.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String name) {
        return this.perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return this.perm.hasPermission(permission);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return this.perm.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name) {
        return this.perm.addAttachment(plugin, name);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, Boolean value) {
        return this.perm.addAttachment(plugin, name, value);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        this.perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.perm.recalculatePermissions();
    }

    @Override
    public Map<String, PermissionAttachmentInfo> getEffectivePermissions() {
        return this.perm.getEffectivePermissions();
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public Server getServer() {
        return Server.getInstance();
    }

    @Override
    public void sendMessage(String message) {
        this.sendMessage(new TranslationContainer(message));
    }

    @Override
    public void sendMessage(TextContainer message) {
        if (this.isTrackingOutput()) {
            this.lastOutput = message.getText();
            if (message instanceof TranslationContainer translationContainer) {
                ListTag<StringTag> newParams = new ListTag<>();
                for (String param : translationContainer.getParameters()) {
                    newParams.add(new StringTag(param));
                }
                this.lastOutputParams = newParams;
            }
        }

        Level level = this.getLevel();
        if (level == null) {
            Server.getInstance().getLogger().error("CommandBlock message failed: level is null. Location approx at x=" + this.x + " y=" + this.y + " z=" + this.z);
            return;
        }

        if (level.getGameRules().getBoolean(GameRule.COMMAND_BLOCK_OUTPUT)) {
            String translatedText = this.getServer().getLanguage().translate(message);
            String prefix = (!message.getText().equals(translatedText) ? "%" : "");
            message.setText(TextFormat.GRAY + "" + TextFormat.ITALIC + "[" + this.getName() + ": " + TextFormat.RESET + prefix + message.getText() + "]");

            Set<Permissible> users = this.getServer().getPluginManager().getPermissionSubscriptions(Server.BROADCAST_CHANNEL_ADMINISTRATIVE);
            for (var user : users) {
                if (user instanceof Player || user instanceof ConsoleCommandSender) {
                    ((CommandSender) user).sendMessage(message);
                }
            }
        }
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void close() {
        if (!closed) {
            for (Player player : new HashSet<>(this.getInventory().getViewers())) {
                player.removeWindow(this.getInventory());
            }
            super.close();
        }
    }

    @NotNull
    @Override
    public Location getLocation() {
        return new Location(this.x, this.y, this.z, 0, 0, this.level);
    }

    public void sendCommandOutput(CommandOutputContainer container) {
        for (var message : container.getMessages()) {
            this.sendMessage(new TranslationContainer(message.getMessageId(), message.getParameters()));
        }
    }
}


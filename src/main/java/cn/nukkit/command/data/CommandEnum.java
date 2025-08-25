package cn.nukkit.command.data;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.UpdateSoftEnumPacket;
import cn.nukkit.registry.Registries;
import cn.nukkit.utils.CameraPresetManager;
import cn.nukkit.utils.Identifier;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author CreeperFace
 */
@EqualsAndHashCode
public class CommandEnum {

    public static final CommandEnum ENUM_EFFECT = new CommandEnum("Effect", Registries.EFFECT.getEffectStringId2TypeMap()
            .keySet()
            .stream()
            .toList());
    public static final CommandEnum ENUM_ENCHANTMENT = new CommandEnum("enchantmentName", () -> Registries.ENCHANTMENT.getIdentifierToEnchantment()
            .keySet()
            .stream()
            .map(Identifier::toString)
            .map(name -> name.startsWith(Identifier.DEFAULT_NAMESPACE) ? name.substring(10) : name)
            .toList());
    public static final CommandEnum ENUM_BLOCK = new CommandEnum("Block", Collections.emptyList());
    public static final CommandEnum ENUM_ITEM = new CommandEnum("Item", Collections.emptyList());
    public static final CommandEnum ENUM_ENTITY = new CommandEnum("Entity", Collections.emptyList());
    public static final CommandEnum ENUM_BOOLEAN = new CommandEnum("Boolean", ImmutableList.of("true", "false"));
    public static final CommandEnum ENUM_GAMEMODE = new CommandEnum("GameMode", ImmutableList.of("survival", "creative", "s", "c", "adventure", "a", "spectator", "view", "v", "spc"));
    public static final CommandEnum CAMERA_PRESETS = new CommandEnum("preset", () -> CameraPresetManager.getPresets().keySet());
    public static final CommandEnum SCOREBOARD_OBJECTIVES = new CommandEnum("ScoreboardObjectives", () -> Server.getInstance().getScoreboardManager().getScoreboards().keySet());
    public static final CommandEnum CHAINED_COMMAND_ENUM = new CommandEnum("ExecuteChainedOption_0", "run", "as", "at", "positioned", "if", "unless", "in", "align", "anchored", "rotated", "facing");

    private final String name;
    private final List<String> values;

    private final boolean soft;
    private final Supplier<Collection<String>> supplier;

    public CommandEnum(String name, String... values) {
        this(name, Arrays.asList(values));
    }

    public CommandEnum(String name, List<String> values) {
        this(name, values, false);
    }

    public CommandEnum(String name, List<String> values, boolean soft) {
        this.name = name;
        this.values = values;
        this.soft = soft;
        this.supplier = null;
    }

    public CommandEnum(String name, Supplier<Collection<String>> supplier) {
        this.name = name;
        this.values = null;
        this.soft = true;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        if (this.supplier == null) {
            return values;
        } else {
            return supplier.get().stream().toList();
        }
    }

    public boolean isSoft() {
        return soft;
    }

    public void updateSoftEnum(UpdateSoftEnumPacket.Type mode, String... value) {
        if (!this.soft) return;
        UpdateSoftEnumPacket packet = new UpdateSoftEnumPacket();
        packet.name = this.getName();
        packet.values = Arrays.stream(value).toList().toArray(new String[0]);
        packet.type = mode;
        Server.broadcastPacket(Server.getInstance().getOnlinePlayers().values(), packet);
    }

    public void updateSoftEnum() {
        if (!this.soft && this.supplier == null) return;
        UpdateSoftEnumPacket packet = new UpdateSoftEnumPacket();
        packet.name = this.getName();
        packet.values = this.getValues().toArray(new String[0]);
        packet.type = UpdateSoftEnumPacket.Type.SET;
        Server.broadcastPacket(Server.getInstance().getOnlinePlayers().values(), packet);
    }
}

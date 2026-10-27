package cn.nukkit.command.defaults;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.ParamList;
import cn.nukkit.command.utils.CommandLogger;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.GameRules;
import cn.nukkit.level.Level;

import java.util.*;

public class GameruleCommand extends VanillaCommand {

    public GameruleCommand(String name) {
        super(name, "commands.gamerule.description");
        this.setPermission("nukkit.command.gamerule");
        this.commandParameters.clear();

        GameRules rules = GameRules.getDefault();
        List<String> boolGameRules = new ArrayList<>();
        List<String> intGameRules = new ArrayList<>();
        List<String> floatGameRules = new ArrayList<>();
        List<String> unknownGameRules = new ArrayList<>();

        rules.getGameRules().forEach((rule, value) -> {
            switch (value.getType()) {
                case BOOLEAN -> boolGameRules.add(rule.getName().toLowerCase(Locale.ROOT));
                case INTEGER -> intGameRules.add(rule.getName().toLowerCase(Locale.ROOT));
                case FLOAT -> floatGameRules.add(rule.getName().toLowerCase(Locale.ROOT));
                default -> unknownGameRules.add(rule.getName().toLowerCase(Locale.ROOT));
            }
        });
        this.commandParameters.put("default", new CommandParameter[0]);
        if (!boolGameRules.isEmpty()) {
            this.commandParameters.put("boolGameRules", new CommandParameter[]{
                    CommandParameter.newEnum("rule", new CommandEnum("BoolGameRule", boolGameRules)),
                    CommandParameter.newEnum("value", true, CommandEnum.ENUM_BOOLEAN)
            });
        }
        if (!intGameRules.isEmpty()) {
            this.commandParameters.put("intGameRules", new CommandParameter[]{
                    CommandParameter.newEnum("rule", new CommandEnum("IntGameRule", intGameRules)),
                    CommandParameter.newType("value", true, CommandParamType.INT)
            });
        }
        if (!floatGameRules.isEmpty()) {
            this.commandParameters.put("floatGameRules", new CommandParameter[]{
                    CommandParameter.newEnum("rule", new CommandEnum("FloatGameRule", floatGameRules)),
                    CommandParameter.newType("value", true, CommandParamType.FLOAT)
            });
        }
        if (!unknownGameRules.isEmpty()) {
            this.commandParameters.put("unknownGameRules", new CommandParameter[]{
                    CommandParameter.newEnum("rule", new CommandEnum("UnknownGameRule", unknownGameRules)),
                    CommandParameter.newType("value", true, CommandParamType.STRING)
            });
        }
        this.enableParamTree();
    }

    @Override
    public int execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) {
        GameRules rules;
        try {
            var pos = sender.getPosition();
            if (pos != null && pos.level != null) {
                rules = pos.level.getGameRules();
            } else {
                rules = Server.getInstance().getDefaultLevel().getGameRules();
            }
        } catch (Exception e) {
            rules = GameRules.getDefault();
        }
        var list = result.getValue();
        String ruleStr = list.getResult(0);
        if (result.getKey().equals("default")) {
            StringJoiner rulesJoiner = new StringJoiner(", ");
            for (GameRule rule : rules.getRules()) {
                rulesJoiner.add(rule.getName().toLowerCase(Locale.ROOT));
            }
            log.addSuccess(rulesJoiner.toString()).output();
            return 1;
        } else if (!list.hasResult(1)) {
            Optional<GameRule> gameRule = GameRule.parseString(ruleStr);
            if (gameRule.isEmpty() || !rules.hasRule(gameRule.get())) {
                log.addSyntaxErrors(0).output();
                return 0;
            }
            log.addSuccess(gameRule.get().getName().toLowerCase(Locale.ROOT) + " = " + rules.getString(gameRule.get())).output();
            return 1;
        }

        Optional<GameRule> optionalRule = GameRule.parseString(ruleStr);
        if (optionalRule.isEmpty()) {
            log.addSyntaxErrors(0).output();
            return 0;
        }
        GameRule gameRule = optionalRule.get();
        // helper to get sender level for non-piston rules
        Level senderLevel = null;
        try {
            var pos = sender.getPosition();
            if (pos != null && pos.level != null) senderLevel = pos.level;
            else senderLevel = Server.getInstance().getDefaultLevel();
        } catch (Exception ignored) {
            senderLevel = Server.getInstance().getDefaultLevel();
        }
        switch (result.getKey()) {
            case "boolGameRules" -> {
                boolean value = list.getResult(1);
                if (senderLevel != null) {
                    senderLevel.getGameRules().setGameRule(gameRule, value);
                    senderLevel.getProvider().setGameRules(senderLevel.getGameRules());
                }
            }
            case "intGameRules" -> {
                int raw = list.getResult(1);
                int value = raw;
                if (gameRule == GameRule.PISTON_PUSH_LIMIT) {
                    if (raw == -1) value = Integer.MAX_VALUE;
                    // piston limit must affect all 3 dimensions
                    applyToAllLevels(gameRule, value);
                } else {
                    if (senderLevel != null) {
                        senderLevel.getGameRules().setGameRule(gameRule, value);
                        senderLevel.getProvider().setGameRules(senderLevel.getGameRules());
                    }
                }
            }
            case "floatGameRules" -> {
                float value = list.getResult(1);
                if (senderLevel != null) {
                    senderLevel.getGameRules().setGameRule(gameRule, value);
                    senderLevel.getProvider().setGameRules(senderLevel.getGameRules());
                }
            }
            case "unknownGameRules" -> {
                String value = list.getResult(1);
                if (senderLevel != null) {
                    senderLevel.getGameRules().setGameRules(gameRule, value);
                    senderLevel.getProvider().setGameRules(senderLevel.getGameRules());
                }
            }
        }
        Object displayObj = list.getResult(1);
        String display = displayObj == null ? "" : displayObj.toString();
        // for pistonPushLimit show clamped value (-1 -> MAX)
        if (gameRule == GameRule.PISTON_PUSH_LIMIT && result.getKey().equals("intGameRules")) {
            int raw = list.getResult(1);
            if (raw == -1) display = String.valueOf(Integer.MAX_VALUE);
        }
        log.addSuccess("commands.gamerule.success", gameRule.getName().toLowerCase(Locale.ROOT), display).output();
        return 1;
    }

    private void applyToAllLevels(GameRule rule, boolean value) {
        for (Level level : Server.getInstance().getLevels().values()) {
            try {
                level.getGameRules().setGameRule(rule, value);
                level.getProvider().setGameRules(level.getGameRules());
            } catch (Exception ignored) {}
        }
    }

    private void applyToAllLevels(GameRule rule, int value) {
        for (Level level : Server.getInstance().getLevels().values()) {
            try {
                level.getGameRules().setGameRule(rule, value);
                level.getProvider().setGameRules(level.getGameRules());
                // immediate persistence to level.dat int tag
                try { level.getProvider().saveLevelData(); } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        }
        if (rule == GameRule.PISTON_PUSH_LIMIT) {
            // re-check all loaded pistons immediately - covers structures that were blocked by limit or obstacle
            for (Level level : Server.getInstance().getLevels().values()) {
                try {
                    for (cn.nukkit.blockentity.BlockEntity be : new java.util.ArrayList<>(level.getBlockEntities().values())) {
                        if (be instanceof cn.nukkit.blockentity.impl.BlockEntityPistonArm) {
                            try {
                                cn.nukkit.block.Block b = level.getBlock(be.getFloorX(), be.getFloorY(), be.getFloorZ());
                                if (b instanceof cn.nukkit.block.BlockPistonBase) {
                                    level.scheduleUpdate(b, 1);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private void applyToAllLevels(GameRule rule, float value) {
        for (Level level : Server.getInstance().getLevels().values()) {
            try {
                level.getGameRules().setGameRule(rule, value);
                level.getProvider().setGameRules(level.getGameRules());
            } catch (Exception ignored) {}
        }
    }

    private void applyToAllLevelsString(GameRule rule, String value) {
        for (Level level : Server.getInstance().getLevels().values()) {
            try {
                level.getGameRules().setGameRules(rule, value);
                level.getProvider().setGameRules(level.getGameRules());
            } catch (Exception ignored) {}
        }
    }
}
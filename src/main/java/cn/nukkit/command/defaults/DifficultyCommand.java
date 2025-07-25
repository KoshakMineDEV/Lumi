package cn.nukkit.command.defaults;

import cn.nukkit.Difficulty;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandEnum;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.ParamList;
import cn.nukkit.command.utils.CommandLogger;
import cn.nukkit.network.protocol.SetDifficultyPacket;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author xtypr
 * @since 2015/11/12
 */
public class DifficultyCommand extends VanillaCommand {

    public DifficultyCommand(String name) {
        super(name, "commands.difficulty.description", "%commands.difficulty.usage");
        this.setPermission("nukkit.command.difficulty");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("difficulty", CommandParamType.INT)
        });
        this.commandParameters.put("byString", new CommandParameter[]{
                CommandParameter.newEnum("difficulty", new CommandEnum("Difficulty", "peaceful", "p", "easy", "e", "normal", "n", "hard", "h"))
        });
        this.enableParamTree();
    }

    @Override
    public int execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) {
        var list = result.getValue();

        Difficulty difficulty = switch (result.getKey()) {
            case "default" -> Difficulty.byId(list.getResult(0));
            case "byString" -> Difficulty.byName(list.getResult(0));
            default -> null;
        };

        if (difficulty == null) {
            log.addSyntaxErrors(0).output();
            return 0;
        }

        if (Server.getInstance().getSettings().world().enableHardcore()) {
            difficulty = Difficulty.HARD;
        }

        Server.getInstance().setDifficulty(difficulty);

        SetDifficultyPacket packet = new SetDifficultyPacket();
        packet.difficulty = difficulty.ordinal();
        Server.broadcastPacket(new ArrayList<>(Server.getInstance().getOnlinePlayers().values()), packet);

        log.addSuccess("commands.difficulty.success", difficulty.getTranslationKey()).output(true);
        return 1;
    }
}
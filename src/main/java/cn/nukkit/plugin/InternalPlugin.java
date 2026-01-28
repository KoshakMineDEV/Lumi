package cn.nukkit.plugin;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.level.Level;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalPlugin extends PluginBase {
    public static final InternalPlugin INSTANCE = new InternalPlugin();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().subscribeEvent(PlayerChatEvent.class, event -> {
            Player player = event.getPlayer();
            Level level = player.getLevel();
            player.sendMessage(level.getFullLight(player.asVector3f().asVector3()) + "");
        }, this);
    }

    @Override
    public void onDisable() {
        this.getLogger().warning("InternalPlugin is disabled.");
    }
}
package cn.nukkit.plugin;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerChatEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InternalPlugin extends PluginBase {
    public static final InternalPlugin INSTANCE = new InternalPlugin();
    
    @Override
    public void onDisable() {
        this.getLogger().warning("InternalPlugin is disabled.");
    }
}
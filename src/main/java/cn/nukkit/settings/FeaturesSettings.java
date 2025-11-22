package cn.nukkit.settings;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(fluent = true)
public class FeaturesSettings extends OkaeriConfig {

    @CustomKey("enable-experiment-mode")
    @Comment("Enable experimental mode (required for custom items)")
    private boolean enableExperimentMode = true;

    @CustomKey("vanilla-portals")
    @Comment("Enable vanilla portals (player will be teleported to the linked portal)")
    private boolean vanillaPortals = true;

    @CustomKey("enable-new-paintings")
    @Comment("Enable new paintings that have been added in recent versions")
    private boolean enableNewPaintings = true;

    @CustomKey("enable-new-chicken-eggs-laying")
    @Comment("Will the chickens drop new egg variants or only old egg")
    private boolean enableNewChickenEggsLaying = true;

    @CustomKey("enable-command-blocks")
    @Comment("Enable command blocks functionality")
    private boolean enableCommandBlocks = true;
}

package cn.nukkit.settings;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * NetherNet (WebRTC) transport settings under network.nethernet.
 * <p>
 * Runs alongside RakNet; restricted-network clients 1.21.90+ join over HTTP signaling + WebRTC.
 */
@Getter
@Setter
@Accessors(fluent = true)
public class NetherNetSettings extends OkaeriConfig {

    @Comment("Enabled by default alongside RakNet, set false to disable. Signaling shares the server-port over TCP; WebRTC media also shares server-port by default via settings.yml general.server-udp-ports (default 19132, relayed in-process from RakNet's socket; 19134 pins a standalone media port, 0 reverts to system-assigned)")
    @CustomKey("enabled")
    private boolean enabled = true;

    @Comment("Extra ICE STUN servers, e.g. stun:stun.l.google.com:19302. TURN is no longer supported upstream")
    @CustomKey("ice-servers")
    private List<String> iceServers = new ArrayList<>();

    @Comment("Operator identity PEM file, generated on first start, keep it. Changing it makes all returning players re-confirm the trust prompt")
    @CustomKey("identity-file")
    private String identityFile = "keys/nethernet-identity.pem";

    @Comment("Operator name in the client trust prompt, empty uses the MOTD")
    @CustomKey("identity-domain")
    private String identityDomain = "";
}

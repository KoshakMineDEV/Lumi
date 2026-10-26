package cn.nukkit.network;

import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.settings.NetherNetSettings;
import lombok.extern.log4j.Log4j2;
import org.cloudburstmc.netty.util.nethernet.OperatorIdentity;

import java.io.File;
import java.nio.file.Path;

/**
 * The operator identity clients pin; generate once and keep it, replacing it re-prompts every
 * returning player. Share one file across a fleet to present as a single operator.
 */
@Log4j2
final class NetherNetIdentity {

    private NetherNetIdentity() {
    }

    static OperatorIdentity load(Server server, NetherNetSettings settings) {
        File pem = Path.of(server.getDataPath()).resolve(settings.identityFile()).toFile();
        File parent = pem.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IllegalStateException("Unable to create the NetherNet identity directory " + parent);
        }

        try {
            boolean existed = pem.isFile();
            OperatorIdentity identity = OperatorIdentity.fromPemOrCreate(pem, domain(server, settings));
            if (!existed) {
                log.info("Generated a NetherNet identity at {}. Keep it: replacing it re-prompts every returning player", pem);
            }
            return identity;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load or create the NetherNet identity at " + pem, e);
        }
    }

    /**
     * Operator name shown in the client trust prompt; display text only.
     */
    private static String domain(Server server, NetherNetSettings settings) {
        String configured = settings.identityDomain();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return TextFormat.clean(server.getSettings().general().motd());
    }
}

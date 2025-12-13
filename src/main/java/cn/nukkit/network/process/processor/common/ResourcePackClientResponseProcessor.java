package cn.nukkit.network.process.processor.common;

import cn.nukkit.Player;
import cn.nukkit.PlayerHandle;
import cn.nukkit.network.process.DataPacketProcessor;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.network.protocol.ResourcePackClientResponsePacket;
import cn.nukkit.network.protocol.ResourcePackStackPacket;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.Server;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author SocialMoods
 */
public class ResourcePackClientResponseProcessor extends DataPacketProcessor<ResourcePackClientResponsePacket> {

    public static final ResourcePackClientResponseProcessor INSTANCE = new ResourcePackClientResponseProcessor();

    @Override
    public void handle(@NotNull PlayerHandle h, @NotNull ResourcePackClientResponsePacket pk) {
        Player p = h.player;
        Server server = p.getServer();

        switch (pk.responseStatus) {
            case ResourcePackClientResponsePacket.STATUS_REFUSED -> {
                h.close("", "disconnectionScreen.noReason");
            }

            case ResourcePackClientResponsePacket.STATUS_SEND_PACKS -> {
                if (h.isShouldPack()
                        || pk.packEntries.length >
                        server.getResourcePackManager()
                                .getResourceStack().length) {
                    h.close("", "disconnectionScreen.resourcePack");
                    return;
                }

                h.setShouldPack(true);

                Set<String> uniqueIds = new LinkedHashSet<>();
                for (ResourcePackClientResponsePacket.Entry entry : pk.packEntries) {
                    ResourcePack pack =
                            server.getResourcePackManager()
                                    .getPackById(entry.uuid);

                    if (pack == null
                            || !uniqueIds.add(entry.uuid.toString())) {
                        h.close("", "disconnectionScreen.resourcePack");
                        return;
                    }

                    p.dataPacket(pack.toNetwork());
                }
            }

            case ResourcePackClientResponsePacket.STATUS_HAVE_ALL_PACKS -> {
                ResourcePackStackPacket stack = new ResourcePackStackPacket();
                stack.mustAccept =
                        server.getSettings().general().forceResources()
                                && !server.getSettings().general()
                                .forceResourcesAllowClientPacks();

                stack.resourcePackStack =
                        server.getResourcePackManager().getResourceStack();

                stack.experiments.addAll(h.getExperiments());

                p.dataPacket(stack);
            }

            case ResourcePackClientResponsePacket.STATUS_COMPLETED -> {
                h.setShouldLogin(true);

                if (h.getPreLoginEventTask().isFinished()) {
                    h.getPreLoginEventTask().onCompletion(server);
                }
            }
        }
    }

    @Override
    public int getPacketId() {
        return ProtocolInfo.RESOURCE_PACK_CLIENT_RESPONSE_PACKET;
    }

    @Override
    public Class<ResourcePackClientResponsePacket> getPacketClass() {
        return ResourcePackClientResponsePacket.class;
    }
}

package cn.nukkit.block.customblock;

import cn.nukkit.Server;
import cn.nukkit.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

/**
 * A utility for automatically downloading vanilla palettes
 * for blocks required for custom blocks to work.
 */
@Slf4j
public class VanillaPaletteUpdater {

    private static final String URL = "https://github.com/LuminiaDev/Lumi-Vanilla-Palettes";
    private static final String URL_PROTOCOL_LIST = VanillaPaletteUpdater.getFileUrl("vanilla_palette_protocols.json");

    public static void updateProtocol(int protocol) {
        log.info("Downloading the vanilla_palette for protocol {} ({})", protocol, Utils.getVersionByProtocol(protocol));

        String url = VanillaPaletteUpdater.getFileUrl("vanilla_palette_" + protocol + ".nbt");
        Path path = CustomBlockUtil.getVanillaPalettePath(protocol);

        try {
            downloadFile(url, path);
        } catch (Exception e) {
            log.error("Failed to download vanilla_palette for protocol {}: {}", protocol, e.getMessage());
        }
    }

    public static void updateAllProtocols() {
        int minProtocol = Server.getInstance().getSettings().general().multiversion().minProtocol();
        int maxProtocol = Server.getInstance().getSettings().general().multiversion().maxProtocol();
        for (int protocol : VanillaPaletteUpdater.getProtocolList()) {
            if (minProtocol != 0 && protocol < minProtocol || maxProtocol != -1 && protocol > maxProtocol) {
                continue;
            }
            Path path = CustomBlockUtil.getVanillaPalettePath(protocol);
            if (!path.toFile().exists()) {
                updateProtocol(protocol);
            }
        }
    }

    private static String getFileUrl(String uri) {
        return URL + "/raw/refs/heads/master/" + uri;
    }

    private static List<Integer> getProtocolList() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(URL_PROTOCOL_LIST).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode + " when trying to get " + URL_PROTOCOL_LIST);
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                Type listType = new TypeToken<List<Integer>>() {
                }.getType();
                return new Gson().fromJson(reader, listType);
            } finally {
                connection.disconnect();
            }

        } catch (Exception e) {
            log.error("Failed to get JSON with palette protocols: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static void downloadFile(String fileUrl, Path targetPath) throws IOException, URISyntaxException {
        HttpURLConnection connection = (HttpURLConnection) new URI(fileUrl).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new IOException("Vanilla palette not found: " + fileUrl);
            } else {
                throw new IOException("HTTP " + responseCode + " when trying to get " + fileUrl);
            }
        }

        try (InputStream in = connection.getInputStream()) {
            Files.createDirectories(targetPath.getParent());
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }
    }
}

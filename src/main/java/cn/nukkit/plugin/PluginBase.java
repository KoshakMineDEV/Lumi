package cn.nukkit.plugin;

import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;
import com.google.common.base.Preconditions;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * 一般的Nukkit插件需要继承的类。<br>
 * A class to be extended by a normal Nukkit plugin.
 *
 * @author MagicDroidX(code) @ Nukkit Project
 * @author 粉鞋大妈(javadoc) @ Nukkit Project
 * @see cn.nukkit.plugin.PluginDescription
 */
abstract public class PluginBase implements Plugin {

    private PluginLoader loader;

    private ClassLoader classLoader;

    private Server server;

    private boolean isEnabled = false;

    private boolean initialized = false;

    private PluginDescription description;

    private File dataFolder;
    private Config config;
    private File configFile;
    private File file;
    private PluginLogger logger;


    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public final boolean isEnabled() {
        return isEnabled;
    }

    /**
     * 加载这个插件。<br>
     * Enables this plugin.
     * 
     * 如果你需要卸载这个插件，建议使用{@link #setEnabled(boolean)}<br>
     * If you need to disable this plugin, it's recommended to use {@link #setEnabled(boolean)}
     *
     * @since Nukkit 1.0 | Nukkit API 1.0.0
     */
    public final void setEnabled() {
        this.setEnabled(true);
    }

    /**
     * 加载或卸载这个插件。<br>
     * Enables or disables this plugin.
     * 
     * 插件管理器插件常常使用这个方法。<br>
     * It's normally used by a plugin manager plugin to manage plugins.
     *
     * @param value {@code true}为加载，{@code false}为卸载。<br>{@code true} for enable, {@code false} for disable.
     */
    public final void setEnabled(boolean value) {
        if (isEnabled != value) {
            if (!value && InternalPlugin.INSTANCE == this) {
                throw new UnsupportedOperationException("The Lumi Internal Plugin cannot be disabled");
            }
            isEnabled = value;
            if (isEnabled) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    @Override
    public final boolean isDisabled() {
        return !isEnabled;
    }

    @Override
    public final File getDataFolder() {
        return dataFolder;
    }

    @Override
    public final PluginDescription getDescription() {
        return description;
    }

    /**
     * 初始化这个插件。<br>
     * Initialize the plugin.
     * 
     * 这个方法会在加载(load)之前被插件加载器调用，初始化关于插件的一些事项，不能被重写。<br>
     * Called by plugin loader before load, and initialize the plugin. Can't be overridden.
     *
     * @param loader      加载这个插件的插件加载器的{@code PluginLoader}对象。<br>
     *                    The plugin loader ,which loads this plugin, as a {@code PluginLoader} object.
     * @param server      运行这个插件的服务器的{@code Server}对象。<br>
     *                    The server running this plugin, as a {@code Server} object.
     * @param description 描述这个插件的{@code PluginDescription}对象。<br>
     *                    A {@code PluginDescription} object that describes this plugin.
     * @param dataFolder  这个插件的数据的文件夹。<br>
     *                    The data folder of this plugin.
     * @param file        这个插件的文件{@code File}对象。对于jar格式的插件，就是jar文件本身。<br>
     *                    The {@code File} object of this plugin itself. For jar-packed plugins, it is the jar file itself.
     */
    public final void init(PluginLoader loader, ClassLoader classLoader, Server server, PluginDescription description, File dataFolder, File file) {
        if (!initialized) {
            initialized = true;
            this.loader = loader;
            this.classLoader = classLoader;
            this.server = server;
            this.description = description;
            this.dataFolder = dataFolder;
            this.file = file;
            this.configFile = new File(this.dataFolder, "config.yml");
            this.logger = new PluginLogger(this);
        }
    }

    @Override
    public PluginLogger getLogger() {
        return logger;
    }

    /**
     * 返回这个插件是否已经初始化。<br>
     * Returns if this plugin is initialized.
     *
     * @return 这个插件是否已初始化。<br>if this plugin is initialized.
     */
    public final boolean isInitialized() {
        return initialized;
    }

    /**
     * TODO: FINISH JAVADOC
     */
    public PluginIdentifiableCommand getCommand(String name) {
        PluginIdentifiableCommand command = this.server.getPluginCommand(name);
        if (command == null || !command.getPlugin().equals(this)) {
            command = this.server.getPluginCommand(this.description.getName().toLowerCase(Locale.ROOT) + ':' + name);
        }

        if (command != null && command.getPlugin().equals(this)) {
            return command;
        } else {
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public InputStream getResource(String filename) {
        return this.getClass().getClassLoader().getResourceAsStream(filename);
    }

    @Override
    public boolean saveResource(String filename) {
        return saveResource(filename, false);
    }

    @Override
    public boolean saveResource(String filename, boolean replace) {
        return saveResource(filename, filename, replace);
    }

    @Override
    public boolean saveResource(String filename, String outputName, boolean replace) {
        Preconditions.checkArgument(filename != null && outputName != null, "Filename can not be null!");
        Preconditions.checkArgument(!filename.trim().isEmpty() && !outputName.trim().isEmpty(), "Filename can not be empty!");

        File out = new File(dataFolder, outputName);
        if (!out.exists() || replace) {
            try (InputStream resource = getResource(filename)) {
                if (resource != null) {
                    File outFolder = out.getParentFile();
                    if (!outFolder.exists()) {
                        outFolder.mkdirs();
                    }
                    Utils.writeFile(out, resource);

                    return true;
                }
            } catch (IOException e) {
                Server.getInstance().getLogger().logException(e);
            }
        }
        return false;
    }

    @Override
    public Config getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }
        return this.config;
    }

    @Override
    public void saveConfig() {
        if (!this.getConfig().save()) {
            this.logger.critical("Could not save config to " + this.configFile.toString());
        }
    }

    @Override
    public void saveDefaultConfig() {
        if (!this.configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }

    @Override
    public void reloadConfig() {
        this.config = new Config(this.configFile);
        InputStream configStream = this.getResource("config.yml");
        if (configStream != null) {
            LoadSettings settings = LoadSettings.builder()
                    .setParseComments(false)
                    .build();
            Load yaml = new Load(settings);
            try {
                this.config.setDefault((LinkedHashMap<String, Object>) yaml.loadFromString(Utils.readFile(this.configFile)));
            } catch (IOException e) {
                Server.getInstance().getLogger().logException(e);
            }
        }
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public String getName() {
        return this.description.getName();
    }

    /**
     * 返回这个插件完整的名字。<br>
     * Returns the full name of this plugin.
     * 
     * 一个插件完整的名字由{@code 名字+" v"+版本号}组成。比如：<br>
     * A full name of a plugin is composed by {@code name+" v"+version}.for example:
     * {@code HelloWorld v1.0.0}
     *
     * @return 这个插件完整的名字。<br>The full name of this plugin.
     * @see cn.nukkit.plugin.PluginDescription#getFullName
     */
    public final String getFullName() {
        return this.description.getFullName();
    }

    /**
     * 返回这个插件的文件{@code File}对象。对于jar格式的插件，就是jar文件本身。<br>
     * Returns the {@code File} object of this plugin itself. For jar-packed plugins, it is the jar file itself.
     *
     * @return 这个插件的文件 {@code File}对象。<br>The {@code File} object of this plugin itself.
     */
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public PluginLoader getPluginLoader() {
        return this.loader;
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return classLoader;
    }
}

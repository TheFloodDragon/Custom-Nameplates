package net.momirealms.customnameplates.paper.setting;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.customnameplates.api.CustomNameplatesPlugin;
import net.momirealms.customnameplates.api.mechanic.character.CharacterArranger;
import net.momirealms.customnameplates.api.util.FontUtils;
import net.momirealms.customnameplates.api.util.LogUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CNConfig {

    public static String configVersion = "22";
    public static int corePoolSize;
    public static long keepAliveTime;
    public static int maximumPoolSize;
    public static boolean debug;
    public static String language;
    public static boolean updateChecker;
    public static boolean metrics;
    public static boolean legacyColorSupport;
    public static boolean generatePackOnStart;
    public static int sendDelay;
    public static String namespace;
    public static String font;
    public static char initChar;
    public static boolean copyPackIA;
    public static boolean copyPackOraxen;
    public static boolean trChatChannel;
    public static boolean ventureChatChannel;
    public static boolean nameplateModule;
    public static boolean bossBarModule;
    public static boolean actionBarModule;
    public static boolean bubbleModule;
    public static boolean backgroundModule;
    public static boolean imageModule;
    public static boolean tabTeam;
    public static boolean cmiTeam;
    public static String folderNameplate;
    public static String folderImage;
    public static String folderBubble;
    public static String folderBackground;
    public static String folderSplit;
    public static boolean add_1_20_Unicodes;
    public static short defaultCharWidth;

    public static void load() {
        try {
            YamlDocument.create(
                    new File(CustomNameplatesPlugin.getInstance().getDataFolder(), "config.yml"),
                    Objects.requireNonNull(CustomNameplatesPlugin.getInstance().getResource("config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings
                            .builder()
                            .setAutoUpdate(true)
                            .build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings
                            .builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .build()
            );
            loadSettings(CustomNameplatesPlugin.getInstance().getConfig("config.yml"));
        } catch (IOException e) {
            LogUtils.warn(e.getMessage());
        }
    }

    private static void loadSettings(YamlConfiguration config) {
        debug = config.getBoolean("debug", false);

        language = config.getString("lang", "english");
        updateChecker = config.getBoolean("update-checker", true);
        metrics = config.getBoolean("metrics");

        ConfigurationSection moduleSection = config.getConfigurationSection("modules");
        if (moduleSection != null) {
            nameplateModule = moduleSection.getBoolean("nameplates");
            bossBarModule = moduleSection.getBoolean("bossbars");
            actionBarModule = moduleSection.getBoolean("actionbars");
            bubbleModule = moduleSection.getBoolean("bubbles");
            backgroundModule = moduleSection.getBoolean("backgrounds");
            imageModule = moduleSection.getBoolean("images");
        }

        ConfigurationSection integrationSection = config.getConfigurationSection("integrations");
        if (integrationSection != null) {
            copyPackIA = integrationSection.getBoolean("resource-pack.ItemsAdder", false);
            copyPackOraxen = integrationSection.getBoolean("resource-pack.Oraxen", false);
            trChatChannel = integrationSection.getBoolean("chat.TrChat", false);
            ventureChatChannel = integrationSection.getBoolean("chat.VentureChat", false);
            tabTeam = integrationSection.getBoolean("team.TAB", false);
            cmiTeam = integrationSection.getBoolean("team.CMI", false);
        }

        ConfigurationSection packSection = config.getConfigurationSection("resource-pack");
        if (packSection != null) {
            generatePackOnStart = !packSection.getBoolean("disable-generation-on-start", false);
            namespace = packSection.getString("namespace", "nameplates");
            font = packSection.getString("font", "default");
            FontUtils.setNameSpaceAndFont(namespace, font);

            initChar = packSection.getString("initial-char", "뀁").charAt(0);
            CharacterArranger.reset(initChar);

            folderNameplate = packSection.getString("image-path.nameplates","font\\nameplates\\");
            folderBubble = packSection.getString("image-path.bubbles","font\\bubbles\\");
            folderBackground = packSection.getString("image-path.backgrounds","font\\backgrounds\\");
            folderImage = packSection.getString("image-path.images","font\\images\\");
            folderSplit = packSection.getString("image-path.space-split","font\\base\\");

            add_1_20_Unicodes = packSection.getBoolean("support-1_20-unicodes", true);
        }

        corePoolSize = config.getInt("other-settings.thread-pool-settings.corePoolSize", 10);
        maximumPoolSize = config.getInt("other-settings.thread-pool-settings.maximumPoolSize", 10);
        keepAliveTime = config.getInt("other-settings.thread-pool-settings.keepAliveTime", 30);
    }

    public static boolean isOtherTeamPluginHooked() {
        return tabTeam || cmiTeam;
    }
}

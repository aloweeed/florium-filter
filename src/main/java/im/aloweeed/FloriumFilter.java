package im.aloweeed;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.elytrium.limboapi.api.Limbo;
import net.elytrium.limboapi.api.LimboFactory;
import net.elytrium.limboapi.api.chunk.Dimension;
import net.elytrium.limboapi.api.chunk.VirtualWorld;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;
import net.elytrium.limboapi.thirdparty.commons.kyori.serialization.Serializer;
import net.elytrium.limboapi.thirdparty.commons.kyori.serialization.Serializers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.slf4j.Logger;
import org.spongepowered.configurate.yaml.YamlConfigurationFormat;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Plugin(
        id = "floriumfilter",
        name = "FloriumFilter",
        version = "1.0",
        authors = {
                "aloweeed",
        },
        dependencies = {
                @Dependency(id = "limboapi")
        }
)
public class FloriumFilter {

    public static FloriumFilter it;
    public final Logger logger;
    public final ProxyServer server;
    public Serializer serializer;

    private LimboFactory factory;
    private Limbo limbo;
    private Path dataDirectory;
    private File configFile;

    public final ArrayList<UUID> checked = new ArrayList<>();


    @Inject
    public FloriumFilter(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        it = this;

        this.logger = logger;
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configFile = this.dataDirectory.resolve("config.yml").toFile();
        Settings.it.load(configFile);

        this.factory = (LimboFactory) server.getPluginManager()
                .getPlugin("limboapi")
                .flatMap(PluginContainer::getInstance)
                .orElseThrow(() -> new IllegalStateException("LimboAPI instance is not available"));

        this.server.getCommandManager().register("floriumfilterreload", (SimpleCommand) invocation -> {
            CommandSource source = invocation.source();

            if (source instanceof Player player) {
                if (!player.hasPermission("floriumfilter.reload")) {
                    return;
                }
            }

            reload();

            if (source instanceof Player player) {
                player.sendMessage(
                        MiniMessage.miniMessage().deserialize("FloriumFilter reloaded.")
                );
            }

            logger.info("FloriumFilter reloaded");
        });

        reload();

        logger.info("Filter initialized");
    }

    @Subscribe(priority = 1)
    public void onLogin(LoginLimboRegisterEvent event) {
        Player player = event.getPlayer();

        if (!checked.contains(player.getUniqueId())) {
            event.addOnJoinCallback(() -> {
                this.limbo.spawnPlayer(
                        player,
                        new FilterSessionHandler(this.factory, this)
                );
            });
        }
    }

    private void reload() {
        Settings.it.reload(configFile, Settings.it.PREFIX);

        VirtualWorld virtualWorld = this.factory.createVirtualWorld(
                Settings.it.MAIN.DIMENSION_TYPE,
                0, 30, 0,
                0, 0
        );

        this.limbo = factory.createLimbo(virtualWorld);

        ComponentSerializer<Component, Component, String> serializer = Settings.it.SERIALIZER.getSerializer();
        if (serializer == null) {
            this.serializer = new Serializer(Objects.requireNonNull(Serializers.LEGACY_AMPERSAND.getSerializer()));
        } else {
            this.serializer = new Serializer(serializer);
        }
    }
}
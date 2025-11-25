package io.calinea.playground;

import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

import java.io.File;
import java.nio.file.Path;

import io.calinea.Calinea;
import io.calinea.config.CalineaConfig;
import io.calinea.logger.CalineaLogger;
import io.calinea.playground.Commands.CalineaCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * Calinea Playground Plugin
 * 
 * Example Paper plugin demonstrating proper API usage.
 */
public class CalineaPlayground extends JavaPlugin {

    public static final String PLUGIN_NAME = "CalineaPlayground";
    public static final String NAMESPACE = "calinea_playground";
    ComponentLogger LOGGER = ComponentLogger.logger(PLUGIN_NAME);
    private boolean failOnload = false;

    @Override
    public void onLoad() {
        try {
            
            try {
                onLoadCommandAPI();
            } catch (Exception e) {
                LOGGER.error("Failed to initialize command API: " + e.getMessage());
                throw e;
            }

            try  {
                onLoadCalinea();
            } catch (Exception e) {
                LOGGER.error("Failed to load Calinea: " + e.getMessage());
                throw e;
            }
        } catch (Exception e) {
            failOnload = true;
        }
    }

    @Override
    public void onEnable() {
        if (failOnload) {
            LOGGER.error("Calinea Playground failed to load correctly, disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        CommandAPI.onEnable();

        CalineaCommand.register();

        LOGGER.info(Calinea.center(Component.text("=== CALINEA ==="), 320));
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Calinea Playground disabled!");
    }
    
    private void onLoadCalinea() {
        // Path fontInfoPath = getDataFolder().toPath().resolve("font-widths.json");
        // TODO add a config option for this path
        Path fontInfoPath = Path.of("D:\\dev\\Minecraft\\_project\\Calinea\\calinea-output\\font-widths.json");

        CalineaConfig config = new CalineaConfig()
            .fontInfoPath(fontInfoPath)
            .logger(CalineaLogger.fromSlf4jLogger(LOGGER));

        Calinea.onLoad(config);
    }

    private void onLoadCommandAPI() {
        CommandAPIPaperConfig commandApiConfig = new CommandAPIPaperConfig(this);
        commandApiConfig.missingExecutorImplementationMessage("This command has no implementations for %s");
        commandApiConfig.setNamespace(NAMESPACE);
        commandApiConfig.dispatcherFile(new File(getDataFolder(), "command_registration.json"));

        CommandAPI.onLoad(commandApiConfig);
    }
    
    /**
     * Gets a simple greeting for testing.
     */
    public String getGreeting() {
        return "Hello from Calinea Playground!";
    }
}

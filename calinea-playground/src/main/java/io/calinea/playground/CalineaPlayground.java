package io.calinea.playground;

import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

import java.io.File;
import java.nio.file.Path;

import io.calinea.Calinea;
import io.calinea.config.CalineaConfig;
import io.calinea.playground.Commands.CalineaCommand;
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
    
    @Override
    public void onLoad() {
        try {
            onLoadCommandAPI();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize command API: " + e.getMessage());
        }

        try  {
        	onLoadCalinea();
        } catch (Exception e) {
        	LOGGER.error("Failed to load Calinea: " + e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        CalineaCommand.register();

        LOGGER.info(Calinea.center("=== CALINEA TEST ==="));
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Calinea Playground disabled!");
    }
    
    private void onLoadCalinea() {
        // Path fontInfoPath = Path.of(getDataFolder().toString(), "font-widths.json");
        Path fontInfoPath = Path.of("D:\\dev\\Minecraft\\lib\\Calinea\\calinea-output\\font-widths.json");

        CalineaConfig config = new CalineaConfig()
            .fontInfoPath(fontInfoPath);

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

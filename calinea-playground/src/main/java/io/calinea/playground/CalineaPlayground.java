package io.calinea.playground;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import io.calinea.Calinea;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * Calinea Playground Plugin
 * 
 * Example Paper plugin demonstrating proper API usage.
 */
public class CalineaPlayground extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("Calinea Playground enabled! ");
        ComponentLogger logger = ComponentLogger.logger("Calinea");
        logger.info(Calinea.center("=== CALINEA TEST ==="));
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Calinea Playground disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("calinea")) {
            if (args.length > 0 && args[0].equals("center")) {
                // Test Calinea centering
                Component centered = Calinea.center("=== CALINEA TEST ===");
                sender.sendMessage(centered);
                return true;
            } else if (args.length > 0 && args[0].equals("measure")) {
                // Test Calinea measurement
                String testText = "Hello World!";
                int width = Calinea.measureWidth(testText);
                sender.sendMessage("Width of '" + testText + "': " + width + " pixels");
                return true;
            } else if (args.length > 0 && args[0].equals("align")) {
                // Test alignment
                Component left = Calinea.alignLeft("Left", 100);
                Component right = Calinea.alignRight("Right", 100);
                sender.sendMessage(left);
                sender.sendMessage(right);
                return true;
            } else if (args.length > 0 && args[0].equals("separator")) {
                // Test separator
                Component separator = Calinea.separator(200);
                sender.sendMessage(separator);
                return true;
            } else {
                sender.sendMessage("Usage: /calinea <center|measure|align|separator>");
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets a simple greeting for testing.
     */
    public String getGreeting() {
        return "Hello from Calinea Playground!";
    }
}

package io.github.alshain01.TradeShop;

import java.io.*;
import java.util.logging.Level;

import org.bukkit.configuration.file.*;
import org.bukkit.plugin.Plugin;

/**
 * Modified YAML manager from http://wiki.bukkit.org/Configuration_API_Reference
 * 
 * @author bukkit.org
 */
class CustomYML {
	private static Plugin plugin;
	private String dataFile;
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	

	// Construct a new CustomYML file
	protected CustomYML(Plugin newPlugin, String dataFile){
		plugin = newPlugin;
		this.dataFile = dataFile;
	}

	// Reloads the file to the MemorySection
    protected void reload() {
        if (customConfigFile == null) {
        	customConfigFile = new File(plugin.getDataFolder(), dataFile);
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource(dataFile);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

	// Get's the custom config file.
    protected FileConfiguration getConfig() {
        if (customConfig == null) {
            this.reload();
        }
        return customConfig;
    }

    //Saves all changes
    protected void saveConfig() {
        if (customConfig == null || customConfigFile == null) {
        	return;
        }
        try {
            getConfig().save(customConfigFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

	// Save a default config file to the data folder.
    protected void saveDefaultConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder(), dataFile);
        }
        
        if (!customConfigFile.exists()) {            
             plugin.saveResource(dataFile, false);
         }
    }

}

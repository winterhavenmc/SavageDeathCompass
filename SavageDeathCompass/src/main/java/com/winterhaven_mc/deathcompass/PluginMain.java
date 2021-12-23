package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.commands.CommandManager;
import com.winterhaven_mc.deathcompass.listeners.InventoryEventListener;
import com.winterhaven_mc.deathcompass.listeners.PlayerEventListener;

import com.winterhaven_mc.deathcompass.storage.DataStore;
import com.winterhaven_mc.util.LanguageHandler;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.YamlSoundConfiguration;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.winterhaven_mc.util.WorldManager;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;


/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 *
 * @author Tim Savage
 */
public final class PluginMain extends JavaPlugin {

	public LanguageHandler languageHandler;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public DataStore dataStore;


	public PluginMain() {
		super();
	}


	@SuppressWarnings("unused")
	private PluginMain(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
		super(loader, descriptionFile, dataFolder, file);
	}


	@Override
	public void onEnable() {

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// initialize language manager
		languageHandler = new LanguageHandler(this);

		// instantiate sound config
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate datastore
		dataStore = DataStore.create(this);

		// instantiate command handler
		new CommandManager(this);

		// instantiate player event listener
		new PlayerEventListener(this);

		// instantiate inventory event listener
		new InventoryEventListener(this);
	}


	@Override
	public void onDisable() {
		dataStore.close();
	}

}

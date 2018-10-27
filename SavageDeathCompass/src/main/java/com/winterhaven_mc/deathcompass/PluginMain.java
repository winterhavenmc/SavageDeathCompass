package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.commands.CommandManager;
import com.winterhaven_mc.deathcompass.listeners.InventoryEventListener;
import com.winterhaven_mc.deathcompass.listeners.PlayerEventListener;
import com.winterhaven_mc.deathcompass.storage.DataStore;
import com.winterhaven_mc.deathcompass.storage.DataStoreFactory;
import com.winterhaven_mc.deathcompass.messages.MessageManager;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.YamlSoundConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.winterhaven_mc.util.WorldManager;


/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 * 
 * @author      Tim Savage
 * @version		1.13.2
 */
public final class PluginMain extends JavaPlugin {

	// static reference to main class
	public static PluginMain instance;

	// global debug setting read from config file
	public Boolean debug = this.getConfig().getBoolean("debug");

	public MessageManager messageManager;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public DataStore dataStore;


	public void onEnable() {

		// static reference to main class
		instance = this;

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// instantiate message manager
		messageManager = new MessageManager(this);

		// instantiate sound config
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate datastore
		dataStore = DataStoreFactory.create();

		// instantiate command handler
		new CommandManager(this);

		// instantiate player event listener
		new PlayerEventListener(this);

		// instantiate inventory event listener
		new InventoryEventListener(this);
	}


	public void onDisable() {
		dataStore.close();
	}

}

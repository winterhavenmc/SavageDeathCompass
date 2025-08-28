/*
 * Copyright (c) 2022-2025 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathcompass.plugin;

import com.winterhavenmc.deathcompass.plugin.commands.CommandManager;
import com.winterhavenmc.deathcompass.plugin.listeners.InventoryEventListener;
import com.winterhavenmc.deathcompass.plugin.listeners.PlayerEventListener;
import com.winterhavenmc.deathcompass.plugin.storage.DataStore;
import com.winterhavenmc.deathcompass.plugin.util.DeathCompassUtility;
import com.winterhavenmc.deathcompass.plugin.util.MetricsHandler;
import com.winterhavenmc.library.messagebuilder.MessageBuilder;
import com.winterhavenmc.library.soundconfig.SoundConfiguration;
import com.winterhavenmc.library.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.library.worldmanager.WorldManager;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 *
 * @author Tim Savage
 */
public final class PluginMain extends JavaPlugin
{
	public MessageBuilder messageBuilder;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public DataStore dataStore;
	public DeathCompassUtility deathCompassUtility;


	@Override
	public void onEnable()
	{
		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// initialize message builder
		messageBuilder = MessageBuilder.create(this);

		// instantiate sound config
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate datastore
		dataStore = DataStore.connect(this);

		// instantiate death compass utility
		deathCompassUtility = new DeathCompassUtility(this);

		// instantiate command handler
		new CommandManager(this);

		// instantiate player event listeners
		new PlayerEventListener(this);
		new InventoryEventListener(this);

		// instantiate metrics handler
		new MetricsHandler(this);
	}


	@Override
	public void onDisable()
	{
		dataStore.close();
	}

}

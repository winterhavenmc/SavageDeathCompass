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

package com.winterhavenmc.deathcompass.core;

import com.winterhavenmc.deathcompass.core.commands.CommandManager;
import com.winterhavenmc.deathcompass.core.listeners.InventoryEventListener;
import com.winterhavenmc.deathcompass.core.listeners.PlayerEventListener;
import com.winterhavenmc.deathcompass.core.ports.storage.ConnectionProvider;
import com.winterhavenmc.deathcompass.core.storage.DataStore;
import com.winterhavenmc.deathcompass.core.util.DeathCompassUtility;
import com.winterhavenmc.deathcompass.core.util.MetricsHandler;
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
public final class PluginController
{
	public MessageBuilder messageBuilder;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public DataStore dataStore;
	public DeathCompassUtility deathCompassUtility;


	public void startUp(final JavaPlugin plugin, final ConnectionProvider connectionProvider)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// initialize message builder
		messageBuilder = MessageBuilder.create(plugin);

		// instantiate sound config
		soundConfig = new YamlSoundConfiguration(plugin);

		// instantiate world manager
		worldManager = new WorldManager(plugin);

		// instantiate datastore
		dataStore = DataStore.connect(plugin, connectionProvider);

		// instantiate death compass utility
		deathCompassUtility = new DeathCompassUtility(this);

		// instantiate context container
		ContextContainer ctx = new ContextContainer(plugin, messageBuilder, soundConfig, worldManager, dataStore, deathCompassUtility);

		// instantiate command handler
		new CommandManager(ctx);

		// instantiate event listeners
		new PlayerEventListener(ctx);
		new InventoryEventListener(ctx);

		// instantiate metrics handler
		new MetricsHandler(ctx);
	}

	public void shutDown()
	{
		dataStore.close();
	}


	public record ContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder, SoundConfiguration soundConfig,
	                               WorldManager worldManager, DataStore datastore, DeathCompassUtility deathCompassUtility) { }


	public record CommandContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                      SoundConfiguration soundConfig, WorldManager worldManager) { }
}

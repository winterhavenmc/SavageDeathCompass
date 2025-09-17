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
	public ConnectionProvider datastore;
	public DeathCompassUtility deathCompassUtility;
	public CommandManager commandManager;
	public PlayerEventListener playerEventListener;
	public InventoryEventListener inventoryEventListener;


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
		datastore = connectionProvider.connect();

		// instantiate death compass utility
		deathCompassUtility = new DeathCompassUtility(this);

		// instantiate metrics handler
		new MetricsHandler(plugin);

		// instantiate context container
		ListenerContextContainer listenerCtx = new ListenerContextContainer(plugin, messageBuilder, soundConfig, worldManager, datastore, deathCompassUtility);
		CommandContextContainer commandCtx = new CommandContextContainer(plugin, messageBuilder, soundConfig, worldManager);

		// instantiate command handler
		commandManager = new CommandManager(commandCtx);

		// instantiate event listeners
		playerEventListener = new PlayerEventListener(listenerCtx);
		inventoryEventListener = new InventoryEventListener(listenerCtx);
	}


	public void shutDown()
	{
		datastore.close();
	}


	public record ListenerContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                       SoundConfiguration soundConfig, WorldManager worldManager,
	                                       ConnectionProvider datastore, DeathCompassUtility deathCompassUtility) { }


	public record CommandContextContainer(JavaPlugin plugin, MessageBuilder messageBuilder,
	                                      SoundConfiguration soundConfig, WorldManager worldManager) { }
}

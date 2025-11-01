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

package com.winterhavenmc.deathcompass.core.controller;

import com.winterhavenmc.deathcompass.core.context.CommandCtx;
import com.winterhavenmc.deathcompass.core.context.ListenerCtx;
import com.winterhavenmc.deathcompass.core.ports.commands.CommandDispatcher;
import com.winterhavenmc.deathcompass.core.ports.listeners.InventoryEventListener;
import com.winterhavenmc.deathcompass.core.ports.listeners.PlayerEventListener;
import com.winterhavenmc.deathcompass.core.ports.storage.ConnectionProvider;
import com.winterhavenmc.deathcompass.core.util.MetricsHandler;

import com.winterhavenmc.library.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 *
 * @author Tim Savage
 */
public final class DeathCompassPluginController implements PluginController
{
	public MessageBuilder messageBuilder;
	public ConnectionProvider datastore;
	public CommandDispatcher commandDispatcher;
	public InventoryEventListener inventoryEventListener;
	public PlayerEventListener playerEventListener;


	@Override
	public void startUp(final JavaPlugin plugin,
	                    final CommandDispatcher commandDispatcher,
	                    final InventoryEventListener inventoryEventListener,
	                    final PlayerEventListener playerEventListener,
	                    final ConnectionProvider connectionProvider)
	{
		// install default config.yml if not present
		plugin.saveDefaultConfig();

		// initialize message builder
		messageBuilder = MessageBuilder.create(plugin);

		// instantiate datastore
		datastore = connectionProvider.connect();

		// instantiate metrics handler
		new MetricsHandler(plugin);

		// instantiate context containers
		ListenerCtx listenerCtx = new ListenerCtx(plugin, messageBuilder, datastore);
		CommandCtx commandCtx = new CommandCtx(plugin, messageBuilder);

		// initialize command dispatcher
		this.commandDispatcher = commandDispatcher.init(commandCtx);

		// initialize event listeners
		this.playerEventListener = playerEventListener.init(listenerCtx);
		this.inventoryEventListener = inventoryEventListener.init(listenerCtx);
	}


	@Override
	public void shutDown()
	{
		datastore.close();
	}


}

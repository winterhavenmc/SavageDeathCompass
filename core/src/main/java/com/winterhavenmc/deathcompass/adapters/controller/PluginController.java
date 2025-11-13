/*
 * Copyright (c) 2025 Tim Savage.
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

package com.winterhavenmc.deathcompass.adapters.controller;

import com.winterhavenmc.deathcompass.adapters.ports.commands.CommandDispatcher;
import com.winterhavenmc.deathcompass.adapters.ports.listeners.InventoryEventListener;
import com.winterhavenmc.deathcompass.adapters.ports.listeners.PlayerEventListener;
import com.winterhavenmc.deathcompass.adapters.ports.storage.ConnectionProvider;
import org.bukkit.plugin.java.JavaPlugin;


public interface PluginController
{
	void startUp(JavaPlugin plugin, ConnectionProvider connectionProvider, CommandDispatcher commandDispatcher,
	             InventoryEventListener inventoryEventListener, PlayerEventListener playerEventListener);

	void shutDown();
}

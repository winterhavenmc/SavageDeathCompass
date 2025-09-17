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

package com.winterhavenmc.deathcompass.core.ports.listeners;

import com.winterhavenmc.deathcompass.core.DeathCompassPluginController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;

public interface InventoryEventListener extends Listener
{
	InventoryEventListener init(DeathCompassPluginController.ListenerContextContainer ctx);

	/**
	 * Prevent hoppers from inserting DeathCompass items into containers
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onInventoryMoveItem(InventoryMoveItemEvent event);

	/**
	 * Prevent placing items into containers if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onInventoryClick(InventoryClickEvent event);

	/**
	 * Prevent placing items in death chests if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	void onInventoryDrag(InventoryDragEvent event);
}

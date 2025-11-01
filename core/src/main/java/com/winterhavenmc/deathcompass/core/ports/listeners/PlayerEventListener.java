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

import com.winterhavenmc.deathcompass.core.context.ListenerCtx;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public interface PlayerEventListener extends Listener
{
	PlayerEventListener init(ListenerCtx ctx);

	/**
	 * Player death event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	void onPlayerDeath(PlayerDeathEvent event);

	/**
	 * Player respawn event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerRespawn(PlayerRespawnEvent event);

	/**
	 * Player join event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event);

	/**
	 * Player change world event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	void onChangeWorld(PlayerChangedWorldEvent event);

	/**
	 * Player Interact event handler
	 * Remove all death compasses from player inventory on interaction with DeathChestBlocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerInteract(PlayerInteractEvent event);

	/**
	 * Item drop event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	void onItemDrop(PlayerDropItemEvent event);
}

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

package com.winterhavenmc.deathcompass.adapters.storage.sqlite;

import com.winterhavenmc.deathcompass.models.DeathLocation;
import com.winterhavenmc.deathcompass.models.DeathLocationReason;
import com.winterhavenmc.deathcompass.models.InvalidDeathLocation;
import com.winterhavenmc.deathcompass.models.ValidDeathLocation;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;


/**
 * Implements in memory cache for datastore objects
 */
final class SqliteDeathLocationCache implements Listener
{
	private final Map<UUID, Map<UUID, ValidDeathLocation>> deathLocationMap;


	/**
	 * Constructor
	 *
	 * @param plugin instance of plugin main class
	 */
	SqliteDeathLocationCache(final Plugin plugin)
	{
		deathLocationMap = new HashMap<>();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Insert death location into cache keyed by player UUID and world UID
	 *
	 * @param deathLocation object containing player UUID and death location to cache
	 */
	void put(final ValidDeathLocation deathLocation)
	{
		final UUID playerUid = deathLocation.playerUid();
		final UUID worldUid = deathLocation.worldUid();

		Map<UUID, ValidDeathLocation> playerMap = deathLocationMap.get(playerUid);

		if (playerMap == null)
		{
			playerMap = new HashMap<>();
		}

		playerMap.put(worldUid, deathLocation);
		deathLocationMap.put(playerUid, playerMap);
	}


	/**
	 * Fetch death record for player uuid / world uuid
	 *
	 * @param playerUid player UUID to use as key
	 * @param worldUid  world UID to use as key
	 * @return ValidDeathLocation containing playerUid and death location for world,
	 * or InvalidDeathLocation if no record exists
	 */
	DeathLocation get(final UUID playerUid, final UUID worldUid)
	{
		if (playerUid == null) return new InvalidDeathLocation(DeathLocationReason.PARAMETER_PLAYER_UUID_NULL);
		else if (worldUid == null) return new InvalidDeathLocation(DeathLocationReason.PARAMETER_WORLD_UUID_NULL);
		else if (deathLocationMap.get(playerUid) == null) return new InvalidDeathLocation(DeathLocationReason.PLAYER_UUID_NULL);
		else if (deathLocationMap.get(playerUid).get(worldUid) == null) return new InvalidDeathLocation(DeathLocationReason.WORLD_UUID_NULL);
		else return deathLocationMap.get(playerUid).get(worldUid);
	}


	/**
	 * Remove player from cache on player quit event
	 *
	 * @param event the event handled by this listener
	 */
	@EventHandler
	void onPlayerQuit(final PlayerQuitEvent event)
	{
		deathLocationMap.remove(event.getPlayer().getUniqueId());
	}

}

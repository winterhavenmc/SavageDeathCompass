/*
 * Copyright (c) 2022 Tim Savage.
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

import com.winterhavenmc.deathcompass.plugin.model.DeathLocation;
import com.winterhavenmc.deathcompass.plugin.model.InvalidDeathLocation;
import com.winterhavenmc.deathcompass.plugin.model.ValidDeathLocation;
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

	// death location map by player uuid, world uid -> death record
	private final Map<UUID, Map<UUID, ValidDeathLocation>> deathLocationMap;


	/**
	 * Constructor
	 */
	SqliteDeathLocationCache(final Plugin plugin)
	{
		// initialize location map
		deathLocationMap = new HashMap<>();

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Insert death location into cache keyed by player UUID and world UID
	 *
	 * @param deathLocation object containing player UUID and death location to cache
	 */
	void put(final ValidDeathLocation deathLocation)
	{
		// check for null parameter
		Objects.requireNonNull(deathLocation);

		// get player UUID from death record
		final UUID playerUid = deathLocation.playerUid();

		// get world UUID from death record location
		final UUID worldUid = deathLocation.worldUid();

		// get map for player
		Map<UUID, ValidDeathLocation> playerMap = deathLocationMap.get(playerUid);

		// if no cached entry exists for player, create new map
		if (playerMap == null)
		{
			// create empty map
			playerMap = new HashMap<>();
		}

		// put this deathLocation into world map
		playerMap.put(worldUid, deathLocation);

		// put world map into player map
		deathLocationMap.put(playerUid, playerMap);
	}


	/**
	 * Fetch death record for player uuid / world uuid
	 *
	 * @param playerUid player UUID to use as key
	 * @param worldUid  world UID to use as key
	 * @return deathRecord containing playerUid and death location for world, or null if no record exists
	 */
	DeathLocation get(final UUID playerUid, final UUID worldUid)
	{
		if (playerUid == null) { return new InvalidDeathLocation("The parameter 'playerUid' was null."); }
		if (worldUid == null) { return new InvalidDeathLocation("The parameter 'worldUid' was null."); }

		// if map for player does not exist, return invalid death location
		if (deathLocationMap.get(playerUid) == null)
		{
			return new InvalidDeathLocation("The playerUid was null.");
		}

		// if location in map is null, return invalid death location
		if (deathLocationMap.get(playerUid).get(worldUid) == null)
		{
			return new InvalidDeathLocation("The worldUid was null.");
		}

		// return record fetched from cache
		return deathLocationMap.get(playerUid).get(worldUid);
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

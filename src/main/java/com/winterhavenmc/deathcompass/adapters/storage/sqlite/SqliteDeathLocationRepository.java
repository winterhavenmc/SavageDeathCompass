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

package com.winterhavenmc.deathcompass.adapters.storage.sqlite;

import com.winterhavenmc.deathcompass.plugin.model.DeathLocation;
import com.winterhavenmc.deathcompass.plugin.ports.storage.DeathLocationRepository;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class SqliteDeathLocationRepository implements DeathLocationRepository
{
	final static UUID INVALID_UUID = new UUID(0, 0);

	private final Plugin plugin;
	private final Connection connection;
	private final SqliteDeathLocationCache sqliteDeathLocationCache;
	private final SqliteDeathLocationQueryExecutor queryExecutor = new SqliteDeathLocationQueryExecutor();
	private final SqliteDeathLocationRowMapper rowMapper = new SqliteDeathLocationRowMapper();


	public SqliteDeathLocationRepository(final Plugin plugin, final Connection connection)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.sqliteDeathLocationCache = new SqliteDeathLocationCache(plugin);
	}


	@Override
	public Optional<DeathLocation> getDeathLocation(final UUID playerUUID, final UUID worldUID)
	{
		if (playerUUID == null) { return Optional.empty(); }
		if (worldUID == null) { return Optional.empty(); }

		// try cache first
		Optional<DeathLocation> optionalDeathLocation = sqliteDeathLocationCache.get(playerUUID, worldUID);

		// if a record was returned from cache, return the record; otherwise try datastore
		if (optionalDeathLocation.isPresent())
		{
			return optionalDeathLocation;
		}

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectLocation"));
		     final ResultSet resultSet = queryExecutor.selectDeathLocation(playerUUID, worldUID, preparedStatement))
		{
			if (resultSet.next())
			{
				DeathLocation deathLocation = rowMapper.map(plugin, resultSet);
				if (!deathLocation.getWorldUid().equals(INVALID_UUID))
				{
					sqliteDeathLocationCache.put(deathLocation);
					return Optional.of(deathLocation);
				}
				else
				{
					plugin.getLogger().warning("World " + resultSet.getString("WorldName") + " is not loaded!");
				}
			}
			return Optional.empty();
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning("An error occurred while trying to select a record from the SQLite datastore.");
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
			return Optional.empty();
		}
	}


	@Override
	public int saveDeathLocation(final DeathLocation deathLocation)
	{
		if (deathLocation == null) { return 0; }

		final World world = plugin.getServer().getWorld(deathLocation.getWorldUid());
		if (world != null)
		{
			final String worldName = world.getName();
			sqliteDeathLocationCache.put(deathLocation);
			try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertLocation")))
			{
				return queryExecutor.insertDeathLocation(deathLocation, worldName, preparedStatement);
			}
			catch (SQLException sqlException)
			{
				plugin.getLogger().warning("An error occurred while inserting a record into the SQLite database.");
				plugin.getLogger().warning(sqlException.getLocalizedMessage());
			}
		}
		else
		{
			plugin.getLogger().warning("An error occurred while inserting a record in the SQLite datastore. World invalid!");
		}
		return 0;
	}


	@Override
	public int saveDeathLocations(final Collection<DeathLocation> deathLocations)
	{
		if (deathLocations == null) { return 0; }

		int count = 0;
		for (DeathLocation deathLocation : deathLocations)
		{
			count += saveDeathLocation(deathLocation);
		}
		return count;
	}


	@SuppressWarnings("unused")
	public Optional<DeathLocation> deleteDeathLocation(final UUID playerUid, final UUID worldUid)
	{
		if (playerUid == null) { return Optional.empty(); }
		if (worldUid == null) { return Optional.empty(); }

		Optional<DeathLocation> optionalDeathRecord = getDeathLocation(playerUid, worldUid);
		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("DeleteLocation")))
		{
			int rowsAffected = queryExecutor.deleteDeathLocation(playerUid, worldUid, preparedStatement);
			if (plugin.getConfig().getBoolean("debug"))
			{
				plugin.getLogger().info(rowsAffected + " rows deleted.");
			}
		}
		catch (SQLException sqlException)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting to delete a record from the SQLite datastore.");
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return optionalDeathRecord;
	}

}

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

import com.winterhavenmc.deathcompass.models.deathlocation.DeathLocation;
import com.winterhavenmc.deathcompass.models.deathlocation.DeathLocationReason;
import com.winterhavenmc.deathcompass.models.deathlocation.InvalidDeathLocation;
import com.winterhavenmc.deathcompass.models.deathlocation.ValidDeathLocation;
import com.winterhavenmc.deathcompass.adapters.ports.storage.DeathLocationRepository;

import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

import static com.winterhavenmc.deathcompass.adapters.storage.sqlite.SqliteConnectionProvider.DATASTORE_NAME;


public final class SqliteDeathLocationRepository implements DeathLocationRepository
{
	private final Plugin plugin;
	private final Connection connection;
	private final ConfigRepository configRepository;
	private final SqliteDeathLocationCache sqliteDeathLocationCache;
	private final SqliteDeathLocationQueryExecutor queryExecutor = new SqliteDeathLocationQueryExecutor();
	private final SqliteDeathLocationRowMapper rowMapper = new SqliteDeathLocationRowMapper();


	public SqliteDeathLocationRepository(final Plugin plugin, final Connection connection, final ConfigRepository configRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.configRepository = configRepository;
		this.sqliteDeathLocationCache = new SqliteDeathLocationCache(plugin);
	}


	@Override
	public DeathLocation getDeathLocation(final UUID playerUid, final UUID worldUid)
	{
		if (playerUid == null) { return new InvalidDeathLocation(DeathLocationReason.PLAYER_UUID_NULL); }
		if (worldUid == null) { return new InvalidDeathLocation(DeathLocationReason.WORLD_UUID_NULL); }

		// try cache first
		DeathLocation cachedDeathLocation = sqliteDeathLocationCache.get(playerUid, worldUid);

		// if a record was returned from cache, return the record; otherwise try datastore
		if (cachedDeathLocation instanceof ValidDeathLocation validDeathLocation)
		{
			return validDeathLocation;
		}

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectLocation"));
		     final ResultSet resultSet = queryExecutor.selectDeathLocation(playerUid, worldUid, preparedStatement))
		{
			if (resultSet.next())
			{
				DeathLocation deathLocation = rowMapper.map(plugin, resultSet);
				if (deathLocation instanceof ValidDeathLocation validDeathLocation)
				{
					sqliteDeathLocationCache.put(validDeathLocation);
					return validDeathLocation;
				}
				else
				{
					plugin.getLogger().warning(SqliteMessage.SELECT_RECORD_WORLD_INVALID_ERROR
							.getLocalizedMessage(configRepository.locale(), resultSet.getString("WorldName")));
				}
			}
			return new InvalidDeathLocation(DeathLocationReason.RECORD_NOT_FOUND);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SELECT_RECORD_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
			return new InvalidDeathLocation(DeathLocationReason.SQL_EXCEPTION_THROWN);
		}
	}


	@Override
	public int saveDeathLocation(final ValidDeathLocation deathLocation)
	{
		if (deathLocation == null) { return 0; }

		int count = 0;

		final World world = plugin.getServer().getWorld(deathLocation.worldUid());
		if (world != null)
		{
			final String worldName = world.getName();
			sqliteDeathLocationCache.put(deathLocation);
			try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertLocation")))
			{
				count += queryExecutor.insertDeathLocation(deathLocation, worldName, preparedStatement);
			}
			catch (SQLException sqlException)
			{
				plugin.getLogger().warning(SqliteMessage.INSERT_RECORD_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
				plugin.getLogger().warning(sqlException.getLocalizedMessage());
			}
		}
		else
		{
			plugin.getLogger().warning(SqliteMessage.INSERT_RECORD_WORLD_INVALID_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
		}

		return count;
	}


	@Override
	public int saveDeathLocations(final Collection<ValidDeathLocation> deathLocations)
	{
		if (deathLocations == null || deathLocations.isEmpty()) { return 0; }

		int count = 0;

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertLocation")))
		{
			int[] results = queryExecutor.insertDeathLocations(deathLocations, plugin, preparedStatement);
			count = IntStream.of(results).sum();
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.INSERT_RECORD_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return count;
	}


	@SuppressWarnings("unused")
	public DeathLocation deleteDeathLocation(final UUID playerUid, final UUID worldUid)
	{
		if (playerUid == null) { return new InvalidDeathLocation(DeathLocationReason.PLAYER_UUID_NULL); }
		if (worldUid == null) { return new InvalidDeathLocation(DeathLocationReason.WORLD_UUID_NULL); }

		// get stored death record for return
		DeathLocation deathLocation = getDeathLocation(playerUid, worldUid);
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
			plugin.getLogger().warning(SqliteMessage.DELETE_RECORD_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return deathLocation;
	}

}

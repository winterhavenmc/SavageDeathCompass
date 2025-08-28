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
import com.winterhavenmc.deathcompass.plugin.model.InvalidDeathLocation;
import com.winterhavenmc.deathcompass.plugin.model.ValidDeathLocation;
import com.winterhavenmc.deathcompass.plugin.ports.storage.DeathLocationRepository;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
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
	private final LocaleProvider localeProvider;
	private final SqliteDeathLocationCache sqliteDeathLocationCache;
	private final SqliteDeathLocationQueryExecutor queryExecutor = new SqliteDeathLocationQueryExecutor();
	private final SqliteDeathLocationRowMapper rowMapper = new SqliteDeathLocationRowMapper();


	public SqliteDeathLocationRepository(final Plugin plugin, final Connection connection, final LocaleProvider localeProvider)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.localeProvider = localeProvider;
		this.sqliteDeathLocationCache = new SqliteDeathLocationCache(plugin);
	}


	@Override
	public DeathLocation getDeathLocation(final UUID playerUid, final UUID worldUid)
	{
		if (playerUid == null) { return new InvalidDeathLocation("The parameter 'playerUid' was null."); }
		if (worldUid == null) { return new InvalidDeathLocation("The parameter 'worldUid' was null."); }

		// try cache first
		DeathLocation optionalDeathLocation = sqliteDeathLocationCache.get(playerUid, worldUid);

		// if a record was returned from cache, return the record; otherwise try datastore
		if (optionalDeathLocation instanceof ValidDeathLocation validDeathLocation)
		{
			return validDeathLocation;
		}

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectLocation"));
		     final ResultSet resultSet = queryExecutor.selectDeathLocation(playerUid, worldUid, preparedStatement))
		{
			if (resultSet.next())
			{
				DeathLocation deathLocation = rowMapper.map(plugin, resultSet);
				if (deathLocation instanceof ValidDeathLocation validDeathLocation && !validDeathLocation.worldUid().equals(INVALID_UUID))
				{
					sqliteDeathLocationCache.put(validDeathLocation);
					return validDeathLocation;
				}
				else
				{
					plugin.getLogger().warning(SqliteMessage.SELECT_RECORD_WORLD_INVALID_ERROR
							.getLocalizedMessage(localeProvider.getLocale(), resultSet.getString("WorldName")));
				}
			}
			return new InvalidDeathLocation("The death location was not found in the Sqlite datastore.");
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SELECT_RECORD_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
			return new InvalidDeathLocation("An SQL exception was thrown.");
		}
	}


	@Override
	public int saveDeathLocation(final ValidDeathLocation deathLocation)
	{
		if (deathLocation == null) { return 0; }

		final World world = plugin.getServer().getWorld(deathLocation.worldUid());
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
				plugin.getLogger().warning(SqliteMessage.INSERT_RECORD_ERROR.getLocalizedMessage(localeProvider.getLocale()));
				plugin.getLogger().warning(sqlException.getLocalizedMessage());
			}
		}
		else
		{
			plugin.getLogger().warning(SqliteMessage.INSERT_RECORD_WORLD_INVALID_ERROR.getLocalizedMessage(localeProvider.getLocale()));
		}
		return 0;
	}


	@Override
	public int saveDeathLocations(final Collection<ValidDeathLocation> deathLocations)
	{
		if (deathLocations == null) { return 0; }

		int count = 0;
		for (ValidDeathLocation deathLocation : deathLocations)
		{
			count += saveDeathLocation(deathLocation);
		}
		return count;
	}


	@SuppressWarnings("unused")
	public DeathLocation deleteDeathLocation(final UUID playerUid, final UUID worldUid)
	{
		if (playerUid == null) { return new InvalidDeathLocation("The parameter 'playerUid' was null."); }
		if (worldUid == null) { return new InvalidDeathLocation("The parameter 'worldUid' was null."); }

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
			plugin.getLogger().warning(SqliteMessage.DELETE_RECORD_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		return deathLocation;
	}

}

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
	private final Plugin plugin;
	private final Connection connection;
	private final SqliteDeathLocationCache sqliteDeathLocationCache;
	private final SqliteDeathLocationQueryExecutor queryExecutor = new SqliteDeathLocationQueryExecutor();

	private int schemaVersion;


	public SqliteDeathLocationRepository(final Plugin plugin, final Connection connection)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.sqliteDeathLocationCache = new SqliteDeathLocationCache(plugin);
	}


	@Override
	public synchronized Collection<DeathLocation> getAllDeathLocations()
	{
		Collection<DeathLocation> returnSet = new HashSet<>();

		try
		{
			PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllLocations"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next())
			{
				if (schemaVersion == 0)
				{
					String key = rs.getString("playerid");
					String worldName = rs.getString("worldname");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");

					World world = plugin.getServer().getWorld(worldName);

					if (world == null)
					{
						plugin.getLogger().warning("Stored record has invalid world: "
								+ worldName + ". Skipping record.");
						continue;
					}

					// convert key string to UUID
					UUID playerUUID = null;
					try
					{
						playerUUID = UUID.fromString(key);
					}
					catch (IllegalArgumentException argumentException)
					{
						plugin.getLogger().warning("Player UUID in datastore is invalid!");
						plugin.getLogger().warning(argumentException.getLocalizedMessage());
					}

					// if playerUUID is not null, add record to return list
					if (playerUUID != null)
					{
						DeathLocation deathLocation = new DeathLocation(playerUUID, world.getUID(), x, y, z);
						returnSet.add(deathLocation);
					}
				}

				// if schema version 1, try to get world by uuid
				else if (schemaVersion == 1)
				{
					long playerUidMsb = rs.getLong("playerUidMsb");
					long playerUidLsb = rs.getLong("playerUidLsb");
					String worldName = rs.getString("worldname");
					long worldUidMsb = rs.getLong("worldUidMsb");
					long worldUidLsb = rs.getLong("worldUidLsb");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");

					World world = plugin.getServer().getWorld(new UUID(worldUidMsb, worldUidLsb));

					if (world == null)
					{
						plugin.getLogger().warning("Stored record has invalid world: "
								+ worldName + ". Skipping record.");
						continue;
					}

					// convert components to player uuid
					UUID playerUUID = new UUID(playerUidMsb, playerUidLsb);

					DeathLocation deathLocation = new DeathLocation(playerUUID, world.getUID(), x, y, z);
					returnSet.add(deathLocation);
				}
			}
		}
		catch (Exception e)
		{

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying "
					+ "to fetch all records from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.getConfig().getBoolean("debug"))
			{
				e.getStackTrace();
			}
		}

		// return results
		return returnSet;
	}


	@Override
	public synchronized Optional<DeathLocation> getDeathLocation(final UUID playerUUID, final UUID worldUID)
	{
		// if key is null return null record
		if (playerUUID == null)
		{
			return Optional.empty();
		}

		// if world uid is null, return null record
		if (worldUID == null)
		{
			return Optional.empty();
		}

		// get player uuid components
		final long playerUidMsb = playerUUID.getMostSignificantBits();
		final long playerUidLsb = playerUUID.getLeastSignificantBits();

		// get world uid components
		final long worldUidMsb = worldUID.getMostSignificantBits();
		final long worldUidLsb = worldUID.getLeastSignificantBits();

		// try cache first
		Optional<DeathLocation> optionalDeathLocation = sqliteDeathLocationCache.get(playerUUID, worldUID);

		// if a record was returned from cache, return the record; otherwise try datastore
		if (optionalDeathLocation.isPresent())
		{
			return optionalDeathLocation;
		}

		DeathLocation deathLocation = null;

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectLocation")))
		{
			preparedStatement.setLong(1, playerUidMsb);
			preparedStatement.setLong(2, playerUidLsb);
			preparedStatement.setLong(3, worldUidMsb);
			preparedStatement.setLong(4, worldUidLsb);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			// only zero or one record can match the unique key
			if (rs.next())
			{
				// get stored world and coordinates
				String worldName = rs.getString("worldname");
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");

				// get server world by uid
				World world = plugin.getServer().getWorld(worldUID);
				if (world == null)
				{
					plugin.getLogger().warning("World " + worldName + " is not loaded!");
					return Optional.empty();
				}

				deathLocation = new DeathLocation(playerUUID, worldUID, x, y, z);
			}
		}
		catch (SQLException sqlException)
		{
			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to select a record from the SQLite datastore.");
			plugin.getLogger().warning(sqlException.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.getConfig().getBoolean("debug"))
			{
				sqlException.getStackTrace();
			}
			return Optional.empty();
		}

		// if record is not null, put record in cache
		if (deathLocation != null)
		{
			sqliteDeathLocationCache.put(deathLocation);
		}

		return Optional.ofNullable(deathLocation);
	}


	@Override
	public synchronized void saveDeathLocation(final DeathLocation deathLocation)
	{
		// if record is null do nothing and return
		if (deathLocation == null) { return; }

		sqliteDeathLocationCache.put(deathLocation);
		final World world = plugin.getServer().getWorld(deathLocation.getWorldUid());
		if (world != null)
		{
			final String worldName = world.getName();
			try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertLocation")))
			{
				queryExecutor.insertDeathLocation(deathLocation, worldName, preparedStatement);
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
	}


	@Override
	public synchronized int saveDeathLocations(final Collection<DeathLocation> deathLocations)
	{
		if (deathLocations == null) { return 0; }

		int count = 0;
		for (DeathLocation deathLocation : deathLocations)
		{
			sqliteDeathLocationCache.put(deathLocation);
			final World world = plugin.getServer().getWorld(deathLocation.getWorldUid());
			if (world != null)
			{
				final String worldName = world.getName();
				try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("InsertLocation")))
				{
					count += queryExecutor.insertDeathLocation(deathLocation, worldName, preparedStatement);
				}
				catch (Exception e)
				{
					plugin.getLogger().warning("An error occurred while inserting a record into the SQLite datastore.");
					plugin.getLogger().warning(e.getLocalizedMessage());
				}
			}
			else
			{
				plugin.getLogger().warning("An error occurred while inserting a record in the SQLite datastore. World invalid!");
			}
		}
		return count;
	}


	@Override
	public synchronized Optional<DeathLocation> deleteDeathLocation(final UUID playerUid, final UUID worldUid)
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

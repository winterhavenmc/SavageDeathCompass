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

package com.winterhavenmc.deathcompass.adapters.storage.sqlite.schema;

import com.winterhavenmc.deathcompass.adapters.storage.sqlite.SqliteMessage;
import com.winterhavenmc.deathcompass.adapters.storage.sqlite.SqliteQueries;
import com.winterhavenmc.deathcompass.models.DeathLocation;
import com.winterhavenmc.deathcompass.models.ValidDeathLocation;
import com.winterhavenmc.deathcompass.core.ports.storage.DeathLocationRepository;
import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public final class SqliteSchemaUpdaterFromV0 implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final LocaleProvider localeProvider;
	private final DeathLocationRepository deathLocationRepository;
	private int schemaVersion;


	SqliteSchemaUpdaterFromV0(final Plugin plugin,
	                          final Connection connection,
	                          final LocaleProvider localeProvider,
	                          final DeathLocationRepository deathLocationRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.localeProvider = localeProvider;
		this.deathLocationRepository = deathLocationRepository;
	}


	@Override
	public void update()
	{
		schemaVersion = SqliteSchemaUpdater.getSchemaVersion(connection, plugin.getLogger());
		if (schemaVersion == 0)
		{
			if (tableExists(connection, "deathlocations"))
			{
				updateDeathLocationTableSchema(connection, schemaVersion);
			}
		}
	}


	private void updateDeathLocationTableSchema(final Connection connection, final int version)
	{
		Set<ValidDeathLocation> existingDeathLocations = SelectAllRecords();
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropDeathLocationTable"));
			statement.executeUpdate(SqliteQueries.getQuery("CreateDeathLocationTable"));
			setSchemaVersion(connection, plugin.getLogger(), version);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_ERROR.getLocalizedMessage(localeProvider.getLocale()));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = deathLocationRepository.saveDeathLocations(existingDeathLocations);
		plugin.getLogger().info(SqliteMessage.SCHEMA_DEATH_LOCATIONS_MIGRATED_NOTICE
				.getLocalizedMessage(localeProvider.getLocale(), count, schemaVersion));
	}


	private Set<ValidDeathLocation> SelectAllRecords()
	{
		Set<ValidDeathLocation> returnSet = new HashSet<>();

		try (final PreparedStatement preparedStatement = connection.prepareStatement(SqliteQueries.getQuery("SelectAllLocations"));
		     final ResultSet resultSet = preparedStatement.getResultSet())
		{
			while (resultSet.next())
			{
				String playerUidString = resultSet.getString("playerid");
				String worldName = resultSet.getString("worldname");
				double x = resultSet.getDouble("x");
				double y = resultSet.getDouble("y");
				double z = resultSet.getDouble("z");

				World world = plugin.getServer().getWorld(worldName);
				if (world != null)
				{
					UUID playerUUID = null;

					try
					{
						playerUUID = UUID.fromString(playerUidString);
					}
					catch (IllegalArgumentException argumentException)
					{
						plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_PLAYER_UUID_INVALID
								.getLocalizedMessage(localeProvider.getLocale()));
						plugin.getLogger().warning(argumentException.getLocalizedMessage());
					}

					DeathLocation deathLocation = DeathLocation.of(playerUUID, world.getUID(), x, y, z);
					if (deathLocation instanceof ValidDeathLocation validDeathLocation)
					{
						returnSet.add(validDeathLocation);
					}
				}
				else
				{
					plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_WORLD_INVALID
							.getLocalizedMessage(localeProvider.getLocale(), worldName));
				}
			}
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_SELECT_ALL_ERROR
					.getLocalizedMessage(localeProvider.getLocale()));
		}

		return returnSet;
	}

}

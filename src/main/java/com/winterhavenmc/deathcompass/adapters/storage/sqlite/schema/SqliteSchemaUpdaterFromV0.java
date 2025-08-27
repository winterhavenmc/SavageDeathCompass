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
import com.winterhavenmc.deathcompass.plugin.model.DeathLocation;
import com.winterhavenmc.deathcompass.plugin.ports.storage.DeathLocationRepository;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;


public final class SqliteSchemaUpdaterFromV0 implements SqliteSchemaUpdater
{
	private final Plugin plugin;
	private final Connection connection;
	private final DeathLocationRepository deathLocationRepository;


	SqliteSchemaUpdaterFromV0(final Plugin plugin,
	                          final Connection connection,
	                          final DeathLocationRepository deathLocationRepository)
	{
		this.plugin = plugin;
		this.connection = connection;
		this.deathLocationRepository = deathLocationRepository;
	}


	@Override
	public void update()
	{
		int schemaVersion = SqliteSchemaUpdater.getSchemaVersion(connection, plugin.getLogger());
		if (schemaVersion == 0)
		{
			if (tableExists(connection, "deathlocations"))
			{
				updateDiscoveryTableSchema(connection, schemaVersion);
			}
		}
	}


	private void updateDiscoveryTableSchema(final Connection connection, final int version)
	{
		Collection<DeathLocation> existingDeathLocations = deathLocationRepository.getAllDeathLocations();
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("DropDeathLocationTable"));
			statement.executeUpdate(SqliteQueries.getQuery("CreateDeathLocationTable"));
			setSchemaVersion(connection, plugin.getLogger(), version);
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.SCHEMA_UPDATE_ERROR.toString());
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}

		int count = deathLocationRepository.saveDeathLocations(existingDeathLocations);
		plugin.getLogger().info(SqliteMessage.SCHEMA_DEATH_LOCATIONS_MIGRATED_NOTICE.toString());
	}

}

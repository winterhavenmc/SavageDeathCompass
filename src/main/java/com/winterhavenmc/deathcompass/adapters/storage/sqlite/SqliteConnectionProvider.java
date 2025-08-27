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

import com.winterhavenmc.deathcompass.adapters.storage.sqlite.schema.SqliteSchemaUpdater;
import com.winterhavenmc.deathcompass.plugin.ports.storage.ConnectionProvider;
import com.winterhavenmc.deathcompass.plugin.ports.storage.DeathLocationRepository;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


public class SqliteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;

	private SqliteDeathLocationRepository deathLocationRepository;


	/**
	 * Class constructor
	 *
	 * @param plugin instance of main class
	 */
	public SqliteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.dataFilePath = plugin.getDataFolder() + File.separator + "deathlocations.db";
	}


	/**
	 * Initialize datastore
	 */
	@Override
	public void connect() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, log and return
		if (this.initialized)
		{
			plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_ERROR.toString());
			return;
		}

		// register the driver
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + dataFilePath;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);

		// instantiate datastore adapters
		deathLocationRepository = new SqliteDeathLocationRepository(plugin, connection);

		// update schema if necessary
		SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, deathLocationRepository);
		schemaUpdater.update();

		// create tables if necessary
		createDeathLocationTable(connection);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZED_NOTICE.toString());
	}


	/**
	 * Close SQLite datastore connection
	 */
	@Override
	public void close()
	{
		try
		{
			connection.close();
			plugin.getLogger().info("SQLite database connection closed.");
		}
		catch (Exception e)
		{
			plugin.getLogger().warning("An error occurred while closing the SQLite database connection.");
			plugin.getLogger().warning(e.getMessage());
		}

		initialized = false;
	}

	/**
	 * Get instance of DeathLocationRepository
	 *
	 */
	@Override
	public DeathLocationRepository deathLocations()
	{
		return this.deathLocationRepository;
	}


	private void createDeathLocationTable(final Connection connection)
	{
		try (final Statement statement = connection.createStatement())
		{
			statement.executeUpdate(SqliteQueries.getQuery("CreateDeathLocationTable"));
		}
		catch (SQLException sqlException)
		{
			plugin.getLogger().warning(SqliteMessage.CREATE_DEATH_LOCATION_TABLE_ERROR.toString());
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}

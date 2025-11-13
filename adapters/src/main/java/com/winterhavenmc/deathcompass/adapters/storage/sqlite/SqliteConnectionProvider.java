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
import com.winterhavenmc.deathcompass.adapters.ports.storage.ConnectionProvider;
import com.winterhavenmc.deathcompass.adapters.ports.storage.DeathLocationRepository;

import com.winterhavenmc.library.messagebuilder.adapters.resources.configuration.BukkitConfigRepository;
import com.winterhavenmc.library.messagebuilder.models.configuration.ConfigRepository;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;


public final class SqliteConnectionProvider implements ConnectionProvider
{
	private final Plugin plugin;
	private final ConfigRepository configRepository;
	private final String dataFilePath;
	private Connection connection;
	private boolean initialized;

	private SqliteDeathLocationRepository deathLocationRepository;
	final static String DATASTORE_NAME = "SQLite";

	/**
	 * Class constructor
	 *
	 * @param plugin instance of main class
	 */
	public SqliteConnectionProvider(final Plugin plugin)
	{
		this.plugin = plugin;
		this.configRepository = BukkitConfigRepository.create(plugin);
		this.dataFilePath = plugin.getDataFolder() + File.separator + "deathlocations.db";
	}


	public ConnectionProvider connect()
	{
		// initialize data store
		try
		{
			this.initialize();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize the datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}

		// return initialized data store
		return this;
	}


	/**
	 * Initialize datastore
	 */
	private void initialize() throws SQLException, ClassNotFoundException
	{
		// if data store is already initialized, log and return
		if (this.initialized)
		{
			plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
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
		deathLocationRepository = new SqliteDeathLocationRepository(plugin, connection, configRepository);

		// update schema if necessary
		SqliteSchemaUpdater schemaUpdater = SqliteSchemaUpdater.create(plugin, connection, configRepository, deathLocationRepository);
		schemaUpdater.update();

		// create tables if necessary
		createDeathLocationTable(connection);

		// set initialized true
		this.initialized = true;
		plugin.getLogger().info(SqliteMessage.DATASTORE_INITIALIZE_NOTICE.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
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
			plugin.getLogger().info(SqliteMessage.DATASTORE_CLOSE_NOTICE.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
		}
		catch (Exception e)
		{
			plugin.getLogger().warning(SqliteMessage.DATASTORE_CLOSE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
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
			plugin.getLogger().warning(SqliteMessage.CREATE_DEATH_LOCATION_TABLE_ERROR.getLocalizedMessage(configRepository.locale(), DATASTORE_NAME));
			plugin.getLogger().warning(sqlException.getLocalizedMessage());
		}
	}

}

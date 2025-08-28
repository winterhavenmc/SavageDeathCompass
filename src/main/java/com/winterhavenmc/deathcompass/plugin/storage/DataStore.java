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

package com.winterhavenmc.deathcompass.plugin.storage;

import com.winterhavenmc.deathcompass.bootstrap.Bootstrap;
import com.winterhavenmc.deathcompass.plugin.ports.storage.ConnectionProvider;
import com.winterhavenmc.deathcompass.plugin.ports.storage.DeathLocationRepository;
import org.bukkit.plugin.Plugin;


/**
 * DataStore interface
 */
public class DataStore implements AutoCloseable
{
	private final ConnectionProvider connectionProvider;


	/**
	 * Private constructor
	 */
	private DataStore(final ConnectionProvider connectionProvider)
	{
		this.connectionProvider = connectionProvider;
	}


	/**
	 * Close datastore connection
	 */
	public void close()
	{
		connectionProvider.close();
	}


	/**
	 * Create new data store from bootstrap connection provider. Output error to log and disable plugin on failure.
	 *
	 * @param plugin instance of plugin main class
	 * @return a new datastore instance
	 */
	public static DataStore connect(final Plugin plugin)
	{
		ConnectionProvider connectionProvider = Bootstrap.getConnectionProvider(plugin);

		// initialize data store
		try
		{
			connectionProvider.connect();
		}
		catch (Exception exception)
		{
			plugin.getLogger().severe("Could not initialize the datastore!");
			plugin.getLogger().severe(exception.getLocalizedMessage());
		}

		// return initialized data store
		return new DataStore(connectionProvider);
	}


	/**
	 * Passthrough method returns the death location repository
	 *
	 * @return the {@link DeathLocationRepository}
	 */
	public DeathLocationRepository deathLocations()
	{
		return connectionProvider.deathLocations();
	}

}

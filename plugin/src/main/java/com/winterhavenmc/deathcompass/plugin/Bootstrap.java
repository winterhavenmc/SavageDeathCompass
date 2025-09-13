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

package com.winterhavenmc.deathcompass.plugin;

import com.winterhavenmc.deathcompass.adapters.storage.sqlite.SqliteConnectionProvider;
import com.winterhavenmc.deathcompass.core.PluginController;
import com.winterhavenmc.deathcompass.core.ports.storage.ConnectionProvider;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;


public final class Bootstrap extends JavaPlugin
{
	PluginController pluginController;
	SqliteConnectionProvider connectionProvider;


	@Override
	public void onEnable()
	{
		pluginController = new PluginController();
		connectionProvider = new SqliteConnectionProvider(this);
		pluginController.startUp(this, connectionProvider);
	}


	@Override
	public void onDisable()
	{
		pluginController.shutDown();
	}


	public static ConnectionProvider getConnectionProvider(Plugin plugin)
	{
		return new SqliteConnectionProvider(plugin);
	}

}

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

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static com.winterhavenmc.deathcompass.adapters.storage.sqlite.SqliteDeathLocationRepository.INVALID_UUID;


public final class SqliteDeathLocationRowMapper
{
	public DeathLocation map(final Plugin plugin, final ResultSet resultSet) throws SQLException
	{
		final UUID playerUid = new UUID(resultSet.getLong("playerUidMsb"), resultSet.getLong("PlayerUidLsb"));
		final UUID worldUid = new UUID(resultSet.getLong("WorldUidMsb"), resultSet.getLong("WorldUidLsb"));
		final int x = resultSet.getInt("X");
		final int y = resultSet.getInt("Y");
		final int z = resultSet.getInt("Z");

		final World world = plugin.getServer().getWorld(worldUid);

		return (world != null)
				? DeathLocation.of(playerUid, worldUid, x, y, z)
				: DeathLocation.of(playerUid, INVALID_UUID, x, y, z);
	}

}

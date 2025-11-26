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

import com.winterhavenmc.deathcompass.models.deathlocation.ValidDeathLocation;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

public final class SqliteDeathLocationQueryExecutor
{
	ResultSet selectDeathLocation(final UUID playerUid,
	                              final UUID worldUid,
	                              final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, playerUid.getMostSignificantBits());
		preparedStatement.setLong(2, playerUid.getLeastSignificantBits());
		preparedStatement.setLong(3, worldUid.getMostSignificantBits());
		preparedStatement.setLong(4, worldUid.getLeastSignificantBits());
		return preparedStatement.executeQuery();
	}


	int insertDeathLocation(final ValidDeathLocation deathLocation,
	                        final String worldName,
	                        final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(  1, deathLocation.playerUid().getMostSignificantBits());
		preparedStatement.setLong(  2, deathLocation.playerUid().getLeastSignificantBits());
		preparedStatement.setString(3, worldName);
		preparedStatement.setLong(  4, deathLocation.worldUid().getMostSignificantBits());
		preparedStatement.setLong(  5, deathLocation.worldUid().getLeastSignificantBits());
		preparedStatement.setDouble(6, deathLocation.x());
		preparedStatement.setDouble(7, deathLocation.y());
		preparedStatement.setDouble(8, deathLocation.z());
		return preparedStatement.executeUpdate();
	}


	int[] insertDeathLocations(final Collection<ValidDeathLocation> deathLocations,
	                           final Plugin plugin,
	                           final PreparedStatement preparedStatement) throws SQLException
	{
		for (ValidDeathLocation deathLocation : deathLocations)
		{
			World world = plugin.getServer().getWorld(deathLocation.worldUid());
			if (world != null)
			{
				String worldName = world.getName();
				preparedStatement.setLong(  1, deathLocation.playerUid().getMostSignificantBits());
				preparedStatement.setLong(  2, deathLocation.playerUid().getLeastSignificantBits());
				preparedStatement.setString(3, worldName);
				preparedStatement.setLong(  4, deathLocation.worldUid().getMostSignificantBits());
				preparedStatement.setLong(  5, deathLocation.worldUid().getLeastSignificantBits());
				preparedStatement.setDouble(6, deathLocation.x());
				preparedStatement.setDouble(7, deathLocation.y());
				preparedStatement.setDouble(8, deathLocation.z());
				preparedStatement.addBatch();
			}
		}
		return preparedStatement.executeBatch();
	}


	int deleteDeathLocation(final UUID playerUid,
	                        final UUID worldUid,
	                        final PreparedStatement preparedStatement) throws SQLException
	{
		preparedStatement.setLong(1, playerUid.getMostSignificantBits());
		preparedStatement.setLong(2, playerUid.getLeastSignificantBits());
		preparedStatement.setLong(3, worldUid.getMostSignificantBits());
		preparedStatement.setLong(4, worldUid.getLeastSignificantBits());
		return preparedStatement.executeUpdate();
	}

}

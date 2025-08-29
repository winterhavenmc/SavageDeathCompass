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

package com.winterhavenmc.deathcompass.plugin.model;

import org.bukkit.entity.Player;
import java.util.UUID;


public sealed interface DeathLocation permits ValidDeathLocation, InvalidDeathLocation
{
	UUID INVALID_UUID = new UUID(0, 0);


	/**
	 * Creates a DeathLocation from a player, used in event listeners
	 *
	 * @param player the player for whom to create a DeathLocation
	 * @return a ValidDeathLocation if validation checks pass, or an InvalidDeathLocation if validation fails
	 */
	static DeathLocation of(final Player player)
	{
		if (player == null) return new InvalidDeathLocation(DeathLocationReason.PLAYER_NULL);
		else return new ValidDeathLocation(player.getUniqueId(), player.getWorld().getUID(),
				player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
	}


	/**
	 * Create a DeathLocation from a record retrieved from the datastore
	 *
	 * @param playerUid the player UUID
	 * @param worldUid the location world UUID
	 * @param x the location x component
	 * @param y the location y component
	 * @param z the location z component
	 * @return a ValidDeathLocation if validation checks pass, or an InvalidDeathLocation if validation fails
	 */
	static DeathLocation of(final UUID playerUid, final UUID worldUid, final double x, final double y, final double z)
	{
		if (playerUid == null) return new InvalidDeathLocation(DeathLocationReason.PLAYER_UUID_NULL);
		else if (worldUid == null) return new InvalidDeathLocation(DeathLocationReason.WORLD_UUID_NULL);
		else if (worldUid.equals(INVALID_UUID)) return new InvalidDeathLocation(DeathLocationReason.WORLD_UNAVAILABLE);
		else return new ValidDeathLocation(playerUid, worldUid, x, y, z);
	}
}

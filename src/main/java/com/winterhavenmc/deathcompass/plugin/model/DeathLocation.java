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


	static DeathLocation of(UUID playerUid, UUID worldUid, double x, double y, double z)
	{
		if (playerUid == null) return new InvalidDeathLocation(DeathLocationReason.PLAYER_UUID_NULL);
		else if (worldUid == null) return new InvalidDeathLocation(DeathLocationReason.WORLD_UUID_NULL);
		else if (worldUid.equals(INVALID_UUID)) return new InvalidDeathLocation(DeathLocationReason.WORLD_UNAVAILABLE);
		else return new ValidDeathLocation(playerUid, worldUid, x, y, z);
	}


	static DeathLocation of(Player player)
	{
		if (player == null) return new InvalidDeathLocation(DeathLocationReason.PLAYER_NULL);
		else return DeathLocation.of(player.getUniqueId(), player.getWorld().getUID(),
				player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
	}
}

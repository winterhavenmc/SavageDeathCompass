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

package com.winterhavenmc.deathcompass.adapters.tasks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class SetCompassTargetTask extends BukkitRunnable
{
	private final Player player;
	private final Location location;

	public SetCompassTargetTask(final Player player, final Location location)
	{
		this.player = player;
		this.location = location;
	}

	public void run()
	{
		if (location.getWorld() != player.getWorld())
		{
			return;
		}
		player.setCompassTarget(location);
	}
}

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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


public final class ValidDeathLocation implements DeathLocation
{
	private final UUID playerUid;
	private final UUID worldUid;
	private final double x;
	private final double y;
	private final double z;


	/**
	 * Package-private class constructor called by interface static factory method
	 */
	ValidDeathLocation(UUID playerUid, UUID worldUid, double x, double y, double z)
	{
		this.playerUid = playerUid;
		this.worldUid = worldUid;
		this.x = x;
		this.y = y;
		this.z = z;
	}


	/**
	 * Getter for location. Returns an Optional Location that is created from the location components
	 * stored as instance fields of this class. If the World referenced by the worldUid field is not available
	 * at the time this method is called, an empty Optional will be returned.
	 *
	 * @return {@code Optional} Location containing player death location if valid, else empty optional
	 */
	public Optional<Location> location()
	{
		// get world from uid
		final World world = Bukkit.getServer().getWorld(this.worldUid);

		// if world is not null, return optional location, else return empty optional
		return (world != null)
				? Optional.of(new Location(world, this.x, this.y, this.z))
				: Optional.empty();
	}


	public UUID playerUid()
	{
		return playerUid;
	}


	public UUID worldUid()
	{
		return worldUid;
	}


	public double x()
	{
		return x;
	}


	public double y()
	{
		return y;
	}


	public double z()
	{
		return z;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;

		var that = (ValidDeathLocation) obj;

		return Objects.equals(this.playerUid, that.playerUid) &&
				Objects.equals(this.worldUid, that.worldUid) &&
				Double.doubleToLongBits(this.x) == Double.doubleToLongBits(that.x) &&
				Double.doubleToLongBits(this.y) == Double.doubleToLongBits(that.y) &&
				Double.doubleToLongBits(this.z) == Double.doubleToLongBits(that.z);
	}


	@Override
	public int hashCode()
	{
		return Objects.hash(playerUid, worldUid, x, y, z);
	}


	@Override
	public String toString()
	{
		return "ValidDeathLocation[" +
				"playerUid=" + playerUid + ", " +
				"worldUid=" + worldUid + ", " +
				"x=" + x + ", " +
				"y=" + y + ", " +
				"z=" + z + ']';
	}

}

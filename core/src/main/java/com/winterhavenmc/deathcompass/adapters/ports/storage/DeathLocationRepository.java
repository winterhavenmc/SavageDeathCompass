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

package com.winterhavenmc.deathcompass.adapters.ports.storage;

import com.winterhavenmc.deathcompass.adapters.models.DeathLocation;
import com.winterhavenmc.deathcompass.adapters.models.ValidDeathLocation;

import java.util.Collection;
import java.util.UUID;


@SuppressWarnings("UnusedReturnValue")
public interface DeathLocationRepository
{
	DeathLocation getDeathLocation(final UUID playerUUID, final UUID worldUID);
	int saveDeathLocation(final ValidDeathLocation deathLocation);
	int saveDeathLocations(final Collection<ValidDeathLocation> deathLocations);
}

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

package com.winterhavenmc.deathcompass.models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DeathLocationTest
{
	@Mock Player playerMock;
	@Mock World worldMock;
	@Mock Location locationMock;

	UUID playerUid;
	UUID worldUid;


	@BeforeEach
	void setUp()
	{
		playerUid = new UUID(42, 42);
		worldUid = new UUID(64, 64);
	}

	@Test
	void of_with_valid_player_parameter()
	{
		// Arrange
		when(playerMock.getWorld()).thenReturn(worldMock);
		when(playerMock.getUniqueId()).thenReturn(playerUid);
		when(playerMock.getLocation()).thenReturn(locationMock);
		when(worldMock.getUID()).thenReturn(worldUid);
		when(locationMock.getX()).thenReturn(1D);
		when(locationMock.getY()).thenReturn(2D);
		when(locationMock.getZ()).thenReturn(3D);

		// Act
		DeathLocation result = DeathLocation.of(playerMock);
		ValidDeathLocation validResult = (ValidDeathLocation) result;

		// Assert
		assertInstanceOf(ValidDeathLocation.class, result);
		assertEquals(playerUid, validResult.playerUid());
		assertEquals(worldUid, validResult.worldUid());
		assertEquals(1D, validResult.x());
		assertEquals(2D, validResult.y());
		assertEquals(3D, validResult.z());

		// Verify
		verify(playerMock, atLeastOnce()).getWorld();
		verify(playerMock, atLeastOnce()).getUniqueId();
		verify(playerMock, atLeastOnce()).getLocation();
		verify(worldMock, atLeastOnce()).getUID();
		verify(locationMock, atLeastOnce()).getX();
		verify(locationMock, atLeastOnce()).getY();
		verify(locationMock, atLeastOnce()).getZ();
	}


	@Test
	void of_with_null_player_parameter()
	{
		// Act
		DeathLocation result = DeathLocation.of(null);

		// Assert
		assertInstanceOf(InvalidDeathLocation.class, result);
		assertEquals(DeathLocationReason.PLAYER_NULL, ((InvalidDeathLocation) result).reason());
	}


	@Test
	void of_with_valid_record_parameters()
	{
		// Act
		DeathLocation result = DeathLocation.of(playerUid, worldUid, 10, 11, 12);

		// Assert
		assertInstanceOf(ValidDeathLocation.class, result);
	}

}

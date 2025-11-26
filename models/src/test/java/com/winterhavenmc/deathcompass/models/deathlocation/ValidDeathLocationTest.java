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

package com.winterhavenmc.deathcompass.models.deathlocation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;

import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;


@ExtendWith(MockitoExtension.class)
class ValidDeathLocationTest
{
	@Mock Server serverMock;
	@Mock World worldMock;

	static UUID PLAYER_UID = new UUID(42, 42);
	static UUID WORLD_UID = new UUID(12345, 54321);


	@Test
	void get_location_returns_valid_location()
	{
		// Arrange
		DeathLocation deathLocation = DeathLocation.of(PLAYER_UID, WORLD_UID, 10, 11, 12);
		ValidDeathLocation validDeathLocation = (ValidDeathLocation) deathLocation;
		when(serverMock.getWorld(WORLD_UID)).thenReturn(worldMock);
		try (MockedStatic<Bukkit> mockedBukkit = mockStatic(Bukkit.class))
		{
			// Define the behavior for the static method
			mockedBukkit.when(Bukkit::getServer).thenReturn(serverMock);

			// Act
			Optional<Location> location = validDeathLocation.location();

			// Assert
			assertTrue(location.isPresent());

			// verify
			verify(serverMock, atLeastOnce()).getWorld(WORLD_UID);
		}
	}


	@Test
	void get_location_returns_invalid_location()
	{
		// Arrange
		ValidDeathLocation deathLocation = DeathLocation.of(PLAYER_UID, WORLD_UID, 10, 11, 12).isValid().orElseThrow();
		when(serverMock.getWorld(WORLD_UID)).thenReturn(null);
		try (MockedStatic<Bukkit> mockedBukkit = mockStatic(Bukkit.class))
		{
			// Define the behavior for the static method
			mockedBukkit.when(Bukkit::getServer).thenReturn(serverMock);

			// Act
			Optional<Location> location = deathLocation.location();

			// Assert
			assertTrue(location.isEmpty());

			// verify
			verify(serverMock, atLeastOnce()).getWorld(WORLD_UID);
		}
	}


	@Test
	public void hashcode_returns_same_value_for_same_object()
	{
		ValidDeathLocation obj = DeathLocation.of(PLAYER_UID, WORLD_UID, 1D, 2D, 3D).isValid().orElseThrow();
		int hashCode1 = obj.hashCode();
		int hashCode2 = obj.hashCode();
		assertEquals(hashCode1, hashCode2);
	}


	@Test
	public void hashcode_returns_same_value_for_identical_objects() {
		ValidDeathLocation obj1 = DeathLocation.of(PLAYER_UID, WORLD_UID, 1D, 2D, 3D).isValid().orElseThrow();
		ValidDeathLocation obj2 = DeathLocation.of(PLAYER_UID, WORLD_UID, 1D, 2D, 3D).isValid().orElseThrow();
		assertEquals(obj1.hashCode(), obj2.hashCode());
	}


	@Test
	void toString_returns_string_representation_ofValidDeathLocation()
	{
		// Arrange
		ValidDeathLocation deathLocation = DeathLocation.of(PLAYER_UID, WORLD_UID, 1D, 2D, 3D).isValid().orElseThrow();

		// Act
		String result = deathLocation.toString();

		// Assert
		assertEquals("ValidDeathLocation[playerUid=00000000-0000-002a-0000-00000000002a, " +
				"worldUid=00000000-0000-3039-0000-00000000d431, x=1.0, y=2.0, z=3.0]", result);
	}


	private static ValidDeathLocation baseLocation()
	{
		return DeathLocation.of(PLAYER_UID, WORLD_UID, 1.0, 2.0, 3.0).isValid().orElseThrow();
	}


	@Test
	void testEqualsSameObject() {
		ValidDeathLocation loc = baseLocation();
		assertEquals(loc, loc, "Object should equal itself");
	}


	@Test
	void testEqualsEqualObjects() {
		ValidDeathLocation loc1 = baseLocation();
		ValidDeathLocation loc2 = baseLocation();

		assertEquals(loc1, loc2, "Objects with same values should be equal");
		assertEquals(loc1.hashCode(), loc2.hashCode(), "Equal objects must have same hashCode");
	}

	/**
	 * Provides variations that should NOT be equal to the base location.
	 */
	static Stream<ValidDeathLocation> unequalLocations() {
		return Stream.of(
				DeathLocation.of(PLAYER_UID, WORLD_UID, 1.0, 2.0, 4.0).isValid().orElseThrow(),          // different z
				DeathLocation.of(PLAYER_UID, WORLD_UID, 9.0, 2.0, 3.0).isValid().orElseThrow(),          // different x
				DeathLocation.of(PLAYER_UID, WORLD_UID, 1.0, 8.0, 3.0).isValid().orElseThrow(),          // different y
				DeathLocation.of(PLAYER_UID, UUID.randomUUID(), 1.0, 2.0, 3.0).isValid().orElseThrow(),  // different world
				DeathLocation.of(UUID.randomUUID(), WORLD_UID, 1.0, 2.0, 3.0).isValid().orElseThrow()    // different player
		);
	}

	@ParameterizedTest
	@MethodSource("unequalLocations")
	void testNotEquals(ValidDeathLocation other) {
		ValidDeathLocation base = baseLocation();
		assertNotEquals(base, other, "Objects with different fields should not be equal");
	}


	@Test
	void testNotEqualsNullAndDifferentType() {
		ValidDeathLocation loc = baseLocation();

		assertNotEquals(null, loc, "Object should not equal null");
		assertNotEquals("some string", loc, "Object should not equal a different type");
	}


	@Test
	void testHashSetBehavior() {
		ValidDeathLocation loc1 = baseLocation();
		ValidDeathLocation loc2 = baseLocation();

		Set<ValidDeathLocation> set = new HashSet<>();
		set.add(loc1);
		set.add(loc2);

		assertEquals(1, set.size(), "Set should contain only one element when equal objects are added");
	}
}

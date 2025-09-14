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

import org.junit.jupiter.api.Test;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;


class DeathLocationReasonTest
{
	@Test
	void getLocalizedMessage_with_locale_US()
	{
		assertEquals("The player was null.", DeathLocationReason.PLAYER_NULL.getLocalizedMessage(Locale.US));
	}

	@Test
	void getLocalizedMessage_with_locale_GERMAN()
	{
		assertEquals("Der Spieler war null.", DeathLocationReason.PLAYER_NULL.getLocalizedMessage(Locale.GERMAN));
	}

	@Test
	void getLocalizedMessage_with_non_existant_locale()
	{
		assertEquals("The player was null.", DeathLocationReason.PLAYER_NULL.getLocalizedMessage(Locale.of("ru")));
	}

	@Test
	void testToString()
	{
		assertEquals("The player was null.", DeathLocationReason.PLAYER_NULL.toString());
	}

}

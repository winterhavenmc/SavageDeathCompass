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

public enum DeathLocationReason
{
	PLAYER_NULL("The player was null."),
	PLAYER_UUID_NULL("The player UUID was null."),
	WORLD_UUID_NULL("The world UUID was null."),
	WORLD_UNAVAILABLE("The world was not available."),
	RECORD_NOT_FOUND("The death location was not found in the Sqlite datastore."),
	SQL_EXCEPTION_THROWN("An SQL exception was thrown."),
	;

	private final String reason;


	DeathLocationReason(final String reason)
	{
		this.reason = reason;
	}


	public String reason()
	{
		return reason;
	}

}

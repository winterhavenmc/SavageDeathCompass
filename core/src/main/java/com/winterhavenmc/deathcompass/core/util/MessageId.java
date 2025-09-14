/*
 * Copyright (c) 2022-2025 Tim Savage.
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

package com.winterhavenmc.deathcompass.core.util;

/**
 * Provides constant identifiers for configurable messages sent to players
 */
public enum MessageId
{
	EVENT_PLAYER_RESPAWN,
	EVENT_ITEM_DESTROY,
	EVENT_INVENTORY_DENY_TRANSFER,

	COMMAND_FAIL_INVALID_COMMAND,
	COMMAND_FAIL_HELP_PERMISSION,
	COMMAND_FAIL_RELOAD_PERMISSION,
	COMMAND_FAIL_STATUS_PERMISSION,
	COMMAND_SUCCESS_RELOAD,

	COMMAND_HELP_INVALID,
	COMMAND_HELP_HELP,
	COMMAND_HELP_RELOAD,
	COMMAND_HELP_STATUS,
	COMMAND_HELP_USAGE,

	COMMAND_STATUS_HEADER,
	COMMAND_STATUS_FOOTER,
	COMMAND_STATUS_PLUGIN_VERSION,
	COMMAND_STATUS_LANGUAGE,
	COMMAND_STATUS_LOCALE,
	COMMAND_STATUS_TIMEZONE,
	COMMAND_STATUS_DESTROY_ON_DROP,
	COMMAND_STATUS_TARGET_DELAY,
	COMMAND_STATUS_PREVENT_STORAGE,
	COMMAND_STATUS_SOUND_EFFECTS,
	COMMAND_STATUS_ENABLED_WORLDS,
}

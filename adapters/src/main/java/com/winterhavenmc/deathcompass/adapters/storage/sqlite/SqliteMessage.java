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

package com.winterhavenmc.deathcompass.adapters.storage.sqlite;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public enum SqliteMessage
{
	DATASTORE_INITIALIZE_NOTICE("{0} datastore initialized."),
	DATASTORE_INITIALIZE_ERROR("The {0} datastore is already initialized."),
	DATASTORE_FOREIGN_KEYS_ERROR("An error occurred while attempting to enable foreign keys in the {0} datastore."),
	DATASTORE_CLOSE_NOTICE("{0} datastore connection closed."),
	DATASTORE_CLOSE_ERROR("An error occurred while closing the {0} datastore connection."),

	CREATE_DEATH_LOCATION_TABLE_ERROR("An error occurred while trying to create the DeathLocation table in the {0} datastore."),

	SCHEMA_VERSION_ERROR("Could not read schema version."),
	SCHEMA_UPDATE_ERROR("An error occurred while trying to update the datastore schema."),
	SCHEMA_UP_TO_DATE_NOTICE("Current schema is up to date."),
	SCHEMA_DEATH_LOCATIONS_MIGRATED_NOTICE("{0} death location records migrated to schema v{1}."),
	SCHEMA_UPDATE_PLAYER_UUID_INVALID("Player UUID in datastore is invalid!"),
	SCHEMA_UPDATE_WORLD_INVALID("Stored record has invalid world '{0}'. Skipping record."),
	SCHEMA_UPDATE_SELECT_ALL_ERROR("An error occurred while trying to select all records from the SQLite datastore."),

	SELECT_RECORD_ERROR("An error occurred while trying to select a record from the {0} datastore."),
	SELECT_RECORD_WORLD_INVALID_ERROR("World '{0}' is not loaded!"),
	INSERT_RECORD_ERROR("An error occurred while inserting a record into the {0} datastore."),
	INSERT_RECORD_WORLD_INVALID_ERROR("An error occurred while inserting a record in the {0} datastore. World invalid!"),
	DELETE_RECORD_ERROR("An error occurred while attempting to delete a record from the {0} datastore."),
	;

	private final String defaultMessage;


	SqliteMessage(final String defaultMessage)
	{
		this.defaultMessage = defaultMessage;
	}


	@Override
	public String toString()
	{
		return defaultMessage;
	}


	public String getLocalizedMessage(final Locale locale)
	{
		try
		{
			ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			return bundle.getString(name());
		}
		catch (MissingResourceException exception)
		{
			return this.defaultMessage;
		}
	}


	public String getLocalizedMessage(final Locale locale, final Object... objects)
	{
		try
		{
			final ResourceBundle bundle = ResourceBundle.getBundle(getClass().getSimpleName(), locale);
			String pattern = bundle.getString(name());
			return MessageFormat.format(pattern, objects);
		}
		catch (MissingResourceException exception)
		{
			return MessageFormat.format(this.defaultMessage, objects);
		}
	}

}

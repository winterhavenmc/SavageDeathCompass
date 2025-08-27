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
	DATASTORE_INITIALIZED_NOTICE("SQLite datastore initialized."),
	DATASTORE_INITIALIZED_ERROR("The SQLite datastore is already initialized."),
	CREATE_DEATH_LOCATION_TABLE_ERROR("An error occurred while trying to create the DeathLocation table in the SQLite datastore."),

	SCHEMA_VERSION_ERROR("Could not read schema version."),
	SCHEMA_UPDATE_ERROR("An error occurred while trying to update the SQLite datastore schema."),
	SCHEMA_UP_TO_DATE_NOTICE("Current schema is up to date."),
	SCHEMA_DEATH_LOCATIONS_MIGRATED_NOTICE("{0} death location records migrated to schema v{1}."),
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


	public String getLocalizeMessage(final Locale locale, final Object... objects)
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

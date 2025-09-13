/*
 * Copyright (c) 2022 Tim Savage.
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

import com.winterhavenmc.deathcompass.core.PluginController;
import com.winterhavenmc.library.messagebuilder.ItemForge;

import org.bukkit.inventory.ItemStack;


public final class DeathCompassUtility
{
	private final PluginController plugin;


	/**
	 * Constructor
	 *
	 * @param plugin instance of plugin main class
	 */
	public DeathCompassUtility(final PluginController plugin)
	{
		this.plugin = plugin;
	}


	/**
	 * Create a DeathCompass item stack with custom display name and lore
	 *
	 * @return ItemStack of DeathCompass
	 */
	public ItemStack createItem()
	{
		return plugin.messageBuilder.itemForge().createItem("DEATH_COMPASS").orElseThrow();
	}


	/**
	 * Check if itemStack is a DeathCompass item
	 *
	 * @param itemStack the ItemStack to check
	 * @return {@code true} if itemStack is a DeathCompass item, {@code false} if not
	 */
	public boolean isDeathCompass(final ItemStack itemStack)
	{
		return ItemForge.isCustomItem(itemStack);
	}

}

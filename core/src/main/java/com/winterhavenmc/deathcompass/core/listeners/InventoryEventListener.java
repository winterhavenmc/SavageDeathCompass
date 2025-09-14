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

package com.winterhavenmc.deathcompass.core.listeners;

import com.winterhavenmc.deathcompass.core.PluginController;

import com.winterhavenmc.deathcompass.core.util.Macro;
import com.winterhavenmc.deathcompass.core.util.MessageId;
import com.winterhavenmc.deathcompass.core.util.SoundId;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;


/**
 * Implements event handlers for inventory events
 */
public final class InventoryEventListener implements Listener
{
	private final PluginController.ContextContainer ctx;

	// set of inventory types to allow shift-click transfers from hot bar (item goes into player inventory)
	private final static Collection<InventoryType> SHIFT_CLICK_ALLOWED_TYPES = Set.of(
			InventoryType.BEACON,
			InventoryType.BREWING,
			InventoryType.CRAFTING,
			InventoryType.FURNACE,
			InventoryType.WORKBENCH);


	/**
	 * class constructor
	 */
	public InventoryEventListener(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;

		// register event handlers in this class
		ctx.plugin().getServer().getPluginManager().registerEvents(this, ctx.plugin());
	}


	/**
	 * Prevent hoppers from inserting DeathCompass items into containers
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(final InventoryMoveItemEvent event)
	{
		// if prevent-storage is configured false, do nothing and return
		if (!ctx.plugin().getConfig().getBoolean("prevent-storage"))
		{
			return;
		}

		// get item stack involved in event
		final ItemStack itemStack = event.getItem();

		// if item stack is death compass, cancel event
		if (ctx.deathCompassUtility().isDeathCompass(itemStack))
		{
			event.setCancelled(true);
		}
	}


	/**
	 * Prevent placing items into containers if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event)
	{
		// if prevent-storage is configured false, do nothing and return
		if (!ctx.plugin().getConfig().getBoolean("prevent-storage"))
		{
			return;
		}

		switch (event.getAction())
		{
			case MOVE_TO_OTHER_INVENTORY ->
			{
				// check if current item is death compass and inventory type is not in set, cancel event and send player message
				if (ctx.deathCompassUtility().isDeathCompass(event.getCurrentItem())
						&& !SHIFT_CLICK_ALLOWED_TYPES.contains(event.getInventory().getType()))
				{
					cancelInventoryTransfer(event, event.getWhoClicked(), event.getCurrentItem());
				}
			}

			case SWAP_WITH_CURSOR ->
			{
				// check if cursor item or current item is death compass
				// check if slot is in container inventory
				if ((ctx.deathCompassUtility().isDeathCompass(event.getCursor())
						|| ctx.deathCompassUtility().isDeathCompass(event.getCurrentItem()))
						&& event.getRawSlot() < event.getInventory().getSize())
				{
					cancelInventoryTransfer(event, event.getWhoClicked(), event.getCurrentItem());
				}
			}

			case PLACE_ONE, PLACE_SOME, PLACE_ALL ->
			{
				// check if cursor item is a death compass and slot is in container inventory
				if (ctx.deathCompassUtility().isDeathCompass(event.getCursor())
						&& event.getRawSlot() < event.getInventory().getSize())
				{
					cancelInventoryTransfer(event, event.getWhoClicked(), event.getCursor());
				}
			}
		}
	}


	/**
	 * Prevent placing items in death chests if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(final InventoryDragEvent event)
	{
		// if prevent-storage is configured false, do nothing and return
		if (!ctx.plugin().getConfig().getBoolean("prevent-storage"))
		{
			return;
		}

		ItemStack item = event.getOldCursor();

		// if cursor item is a death compass
		if (ctx.deathCompassUtility().isDeathCompass(item))
		{
			// iterate over dragged slots and if any are above max slot, cancel event
			for (int slot : event.getRawSlots())
			{
				if (slot < event.getInventory().getSize())
				{
					cancelInventoryTransfer(event, event.getWhoClicked(), item);
					break;
				}
			}
		}
	}


	/**
	 * Cancel transfer of death compass in inventory, send player message and play sound
	 *
	 * @param event  the event being cancelled
	 * @param player the player involved in the event
	 */
	private void cancelInventoryTransfer(final Cancellable event, final HumanEntity player, final ItemStack item)
	{
		event.setCancelled(true);
		ctx.soundConfig().playSound(player, SoundId.INVENTORY_DENY_TRANSFER);
		ctx.messageBuilder().compose(player, MessageId.EVENT_INVENTORY_DENY_TRANSFER)
				.setMacro(Macro.DEATH_LOCATION, player.getLastDeathLocation())
				.setMacro(Macro.ITEM, item)
				.send();
	}

}

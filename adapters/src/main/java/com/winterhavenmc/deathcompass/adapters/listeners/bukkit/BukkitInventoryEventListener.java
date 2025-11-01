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

package com.winterhavenmc.deathcompass.adapters.listeners.bukkit;

import com.winterhavenmc.deathcompass.core.context.ListenerCtx;
import com.winterhavenmc.deathcompass.core.ports.listeners.InventoryEventListener;
import com.winterhavenmc.deathcompass.core.util.Macro;
import com.winterhavenmc.deathcompass.core.util.MessageId;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
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
public final class BukkitInventoryEventListener implements InventoryEventListener
{
	private final ListenerCtx ctx;

	// set of inventory types to allow shift-click transfers from hot bar (item goes into player inventory)
	private final static Collection<InventoryType> SHIFT_CLICK_ALLOWED_TYPES = Set.of(
			InventoryType.BEACON,
			InventoryType.BREWING,
			InventoryType.CRAFTING,
			InventoryType.FURNACE,
			InventoryType.WORKBENCH);


	public BukkitInventoryEventListener()
	{
		this.ctx = null;
	}

	/**
	 * class constructor
	 */
	private BukkitInventoryEventListener(final ListenerCtx ctx)
	{
		this.ctx = ctx;

		// register event handlers in this class
		ctx.plugin().getServer().getPluginManager().registerEvents(this, ctx.plugin());
	}


	@Override
	public InventoryEventListener init(final ListenerCtx ctx)
	{
		return new BukkitInventoryEventListener(ctx);
	}


	/**
	 * Prevent hoppers from inserting DeathCompass items into containers
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	@Override
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
		if (isDeathCompass(itemStack))
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
	@Override
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
				if (isDeathCompass(event.getCurrentItem())
						&& !SHIFT_CLICK_ALLOWED_TYPES.contains(event.getInventory().getType()))
				{
					cancelInventoryTransfer(event, event.getWhoClicked(), event.getCurrentItem());
				}
			}

			case SWAP_WITH_CURSOR ->
			{
				// check if cursor item or current item is death compass
				// check if slot is in container inventory
				if ((isDeathCompass(event.getCursor())
						|| isDeathCompass(event.getCurrentItem()))
						&& event.getRawSlot() < event.getInventory().getSize())
				{
					cancelInventoryTransfer(event, event.getWhoClicked(), event.getCurrentItem());
				}
			}

			case PLACE_ONE, PLACE_SOME, PLACE_ALL ->
			{
				// check if cursor item is a death compass and slot is in container inventory
				if (isDeathCompass(event.getCursor())
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
	@Override
	public void onInventoryDrag(final InventoryDragEvent event)
	{
		// if prevent-storage is configured false, do nothing and return
		if (!ctx.plugin().getConfig().getBoolean("prevent-storage"))
		{
			return;
		}

		ItemStack item = event.getOldCursor();

		// if cursor item is a death compass
		if (isDeathCompass(item))
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
		ctx.messageBuilder().compose(player, MessageId.EVENT_INVENTORY_DENY_TRANSFER)
				.setMacro(Macro.DEATH_LOCATION, player.getLastDeathLocation())
				.setMacro(Macro.ITEM, item)
				.send();
	}


	/**
	 * Check if itemStack is a DeathCompass item
	 *
	 * @param itemStack the ItemStack to check
	 * @return {@code true} if itemStack is a DeathCompass item, {@code false} if not
	 */
	private boolean isDeathCompass(final ItemStack itemStack)
	{
		return ctx.messageBuilder().items().isItem(itemStack);
	}

}

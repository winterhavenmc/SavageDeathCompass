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

import com.winterhavenmc.deathcompass.core.DeathCompassPluginController;
import com.winterhavenmc.deathcompass.core.ports.listeners.PlayerEventListener;
import com.winterhavenmc.library.messagebuilder.keys.ItemKey;
import com.winterhavenmc.library.messagebuilder.keys.ValidItemKey;
import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import com.winterhavenmc.deathcompass.core.tasks.SetCompassTargetTask;
import com.winterhavenmc.deathcompass.core.util.Macro;
import com.winterhavenmc.deathcompass.core.util.MessageId;
import com.winterhavenmc.deathcompass.core.util.SoundId;
import com.winterhavenmc.deathcompass.models.DeathLocation;
import com.winterhavenmc.deathcompass.models.ValidDeathLocation;
import com.winterhavenmc.deathcompass.models.InvalidDeathLocation;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;


/**
 * Implements event handlers for player events
 */
public final class BukkitPlayerEventListener implements PlayerEventListener
{
	private final static String ITEM_KEY = "DEATH_COMPASS";
	private final DeathCompassPluginController.ListenerContextContainer ctx;
	private final LocaleProvider localeProvider;

	// player death respawn hash set, used to prevent giving compass on non-death respawn events
	private final Set<UUID> deathTriggeredRespawn = new HashSet<>();


	/**
	 * Class constructor
	 */
	public BukkitPlayerEventListener()
	{
		this.ctx = null;
		this.localeProvider = null;
	}


	/**
	 * Class constructor
	 */
	private BukkitPlayerEventListener(final DeathCompassPluginController.ListenerContextContainer ctx)
	{
		this.ctx = ctx;
		this.localeProvider = LocaleProvider.create(ctx.plugin());

		// register event handlers in this class
		ctx.plugin().getServer().getPluginManager().registerEvents(this, ctx.plugin());
	}


	public PlayerEventListener init(final DeathCompassPluginController.ListenerContextContainer ctx)
	{
		return new BukkitPlayerEventListener(ctx);
	}


	/**
	 * Create a DeathCompass item stack with custom display name and lore
	 *
	 * @return ItemStack of DeathCompass
	 */
	public ItemStack createItem()
	{
		ValidItemKey validItemKey = ItemKey.of(ITEM_KEY).isValid().orElseThrow();
		return ctx.messageBuilder().itemForge().createItem(validItemKey).orElseThrow();
	}


	/**
	 * Player death event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	@Override
	public void onPlayerDeath(final PlayerDeathEvent event)
	{
		// if destroy-on-drop is enabled in configuration, remove any death compasses from player drops on death
		if (ctx.plugin().getConfig().getBoolean("destroy-on-drop"))
		{

			// get death drops as list
			List<ItemStack> drops = event.getDrops();

			// get iterator of death drops list
			ListIterator<ItemStack> iterator = drops.listIterator();

			// create death compass stack for comparison
			ItemStack deathCompass = createItem();

			// loop through all dropped items and remove any stacks that are death compasses
			while (iterator.hasNext())
			{
				ItemStack stack = iterator.next();
				if (stack.isSimilar(deathCompass))
				{
					iterator.remove();
				}
			}
		}

		Player player = event.getEntity();

		// if player world is not enabled in config, do nothing and return
		if (!ctx.worldManager().isEnabled(player.getWorld()))
		{
			return;
		}

		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use"))
		{
			return;
		}

		// create new death record for player
		DeathLocation deathLocation = DeathLocation.of(player);

		if (deathLocation instanceof ValidDeathLocation validDeathLocation)
		{
			// insert death record in database
			ctx.datastore().deathLocations().saveDeathLocation(validDeathLocation);

			// put player uuid in deathTriggeredRespawn set
			deathTriggeredRespawn.add(player.getUniqueId());
		}
		else
		{
			// log invalid reason
			ctx.plugin().getLogger().warning("Error: " + ((InvalidDeathLocation) deathLocation).reason()
					.getLocalizedMessage(localeProvider.getLocale()));
		}
	}


	/**
	 * Player respawn event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	@Override
	public void onPlayerRespawn(final PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();

		// if player world is not enabled, do nothing and return
		if (!ctx.worldManager().isEnabled(player.getWorld()))
		{
			return;
		}

		// if deathTriggeredRespawn set does not contain user uuid, do nothing and return
		if (!deathTriggeredRespawn.contains(player.getUniqueId()))
		{
			return;
		}

		// remove player uuid from deathTriggeredRespawn set
		deathTriggeredRespawn.remove(player.getUniqueId());

		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use"))
		{
			return;
		}

		// give player death compass
		ItemStack deathCompass = giveDeathCompass(player);

		// set compass target to player death location
		setDeathCompassTarget(player);

		// get player death location
		Location location = player.getLastDeathLocation();

		// send player respawn message
		ctx.messageBuilder().compose(player, MessageId.EVENT_PLAYER_RESPAWN)
				.setMacro(Macro.ITEM, deathCompass)
				.setMacro(Macro.DEATH_LOCATION, location)
				.send();
	}


	/**
	 * Player join event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	@Override
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		// if player world is not enabled, do nothing and return
		if (!ctx.worldManager().isEnabled(player.getWorld()))
		{
			return;
		}

		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use"))
		{
			return;
		}

		// create 1 compass itemstack with configured settings
		ItemStack deathcompass = createItem();

		// get player last death location
		Location lastDeathLocation = getDeathLocation(player);

		// if player does not have at least one death compass in inventory or
		// saved death location in current world, do nothing and return
		if (!player.getInventory().containsAtLeast(deathcompass, 1) ||
				lastDeathLocation == null)
		{
			return;
		}

		// set player compass target to last death location
		setDeathCompassTarget(player);
	}


	/**
	 * Player change world event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	@Override
	public void onChangeWorld(final PlayerChangedWorldEvent event)
	{
		// get player for event
		Player player = Objects.requireNonNull(event.getPlayer());

		// if player world is not enabled in config, do nothing and return
		if (!ctx.worldManager().isEnabled(player.getWorld()))
		{
			return;
		}

		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use"))
		{
			return;
		}

		// create DeathCompass itemstack
		ItemStack deathcompass = createItem();

		// if player does not have a death compass in inventory, do nothing and return
		if (!player.getInventory().containsAtLeast(deathcompass, 1))
		{
			return;
		}

		// get last death location from datastore
		Location lastDeathLocation = getDeathLocation(player);

		// if player does not have a saved death location, do nothing and return
		if (lastDeathLocation == null)
		{
			return;
		}

		// set death compass target to player last death location
		setDeathCompassTarget(player);
	}


	/**
	 * Player Interact event handler
	 * Remove all death compasses from player inventory on interaction with DeathChestBlocks
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	@Override
	public void onPlayerInteract(final PlayerInteractEvent event)
	{
		// get player
		final Player player = event.getPlayer();

		// get block
		final Block block = event.getClickedBlock();

		// if block is a DeathChestBlock owned by player, remove death compasses from inventory and reset target
		if (block != null
				&& block.hasMetadata("deathchest-owner")
				&& block.getMetadata("deathchest-owner").getFirst().asString()
				.equals(player.getUniqueId().toString()))
		{

			// remove all death compasses from player inventory
			removeDeathCompasses(player.getInventory());

			// reset compass target to world spawn
			resetDeathCompassTarget(player);
		}
	}


	/**
	 * Item drop event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	@Override
	public void onItemDrop(final PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		// get itemstack that was dropped
		ItemStack droppedItemStack = event.getItemDrop().getItemStack();

		// create death compass itemstack for comparison
		ItemStack item = createItem();

		// if droppedItemStack is not a DeathCompass or destroy-on-drop config is not true, do nothing and return
		if (!droppedItemStack.isSimilar(item) || !ctx.plugin().getConfig().getBoolean("destroy-on-drop"))
		{
			return;
		}

		// remove dropped item
		event.getItemDrop().remove();

		// play item_break sound to player if sound effects enabled in config
		ctx.soundConfig().playSound(player, SoundId.PLAYER_DROP_COMPASS);

		// if inventory does not contain at least 1 death compass, reset compass target
		if (!player.getInventory().containsAtLeast(item, 1))
		{
			resetDeathCompassTarget(player);
		}

		// send player compass destroyed message
		ctx.messageBuilder().compose(player, MessageId.EVENT_ITEM_DESTROY)
				.setMacro(Macro.DEATH_LOCATION, player.getLastDeathLocation())
				.setMacro(Macro.ITEM, item)
				.send();
	}


	/**
	 * Give 1 death compass to player
	 *
	 * @param player the player being given a death compass
	 */
	private ItemStack giveDeathCompass(final Player player)
	{
		ItemStack deathcompass = createItem();
		player.getInventory().addItem(deathcompass);

		ctx.plugin().getLogger().info(player.getName() + " was given a death compass in "
				+ ctx.worldManager().getWorldName(player.getWorld()) + ".");

		return deathcompass;
	}


	/**
	 * Remove all death compasses from inventory
	 *
	 * @param inventory the inventory from which to remove all death compasses
	 */
	private void removeDeathCompasses(final Inventory inventory)
	{
		ItemStack deathcompass = createItem();
		inventory.removeItem(deathcompass);
	}


	/**
	 * Set death compass target
	 * delay for configured number of ticks (default 20)  to allow player to respawn
	 *
	 * @param player the player whose death location is being set as the compass target
	 */
	private void setDeathCompassTarget(final Player player)
	{
		Location location = getDeathLocation(player);
		new SetCompassTargetTask(player, location).runTaskLater(ctx.plugin(), ctx.plugin().getConfig().getLong("target-delay"));
	}


	/**
	 * Reset compass target
	 *
	 * @param player the player whose compass target is being reset
	 */
	private void resetDeathCompassTarget(final Player player)
	{
		// set player compass target to world spawn location
		player.setCompassTarget(player.getWorld().getSpawnLocation());
	}


	/**
	 * Retrieve player death location from datastore
	 *
	 * @param player the player whose death location is being retrieved
	 * @return location
	 */
	private Location getDeathLocation(final Player player)
	{
		// check for null parameter
		Objects.requireNonNull(player);

		// get worldUid for player current world
		final UUID worldUid = player.getWorld().getUID();

		// fetch death record from datastore
		final DeathLocation deathLocation = ctx.datastore().deathLocations().getDeathLocation(player.getUniqueId(), worldUid);

		// if fetched record is valid, return location; else use player world spawn location
		return (deathLocation instanceof ValidDeathLocation validDeathLocation && validDeathLocation.location().isPresent())
				? validDeathLocation.location().get()
				: ctx.worldManager().getSpawnLocation(player.getWorld());
	}

}

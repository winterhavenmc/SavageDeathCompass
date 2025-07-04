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

package com.winterhavenmc.deathcompass.listeners;

import com.winterhavenmc.deathcompass.PluginMain;
import com.winterhavenmc.deathcompass.messages.Macro;
import com.winterhavenmc.deathcompass.messages.MessageId;
import com.winterhavenmc.deathcompass.sounds.SoundId;
import com.winterhavenmc.deathcompass.storage.DeathRecord;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Implements event handlers for player events
 */
public final class PlayerEventListener implements Listener
{
	// reference to main class
	private final PluginMain plugin;

	// player death respawn hash set, used to prevent giving compass on non-death respawn events
	private final Set<UUID> deathTriggeredRespawn = ConcurrentHashMap.newKeySet();


	/**
	 * Class constructor
	 *
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(final PluginMain plugin)
	{
		// set reference to main class
		this.plugin = plugin;

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Player death event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(final PlayerDeathEvent event)
	{
		// if destroy-on-drop is enabled in configuration, remove any death compasses from player drops on death
		if (plugin.getConfig().getBoolean("destroy-on-drop"))
		{

			// get death drops as list
			List<ItemStack> drops = event.getDrops();

			// get iterator of death drops list
			ListIterator<ItemStack> iterator = drops.listIterator();

			// create death compass stack for comparison
			ItemStack deathCompass = plugin.deathCompassUtility.createItem();

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
		if (!plugin.worldManager.isEnabled(player.getWorld()))
		{
			return;
		}

		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use"))
		{
			return;
		}

		// create new death record for player
		DeathRecord deathRecord = new DeathRecord(player);

		// insert death record in database
		plugin.dataStore.insertRecord(deathRecord);

		// put player uuid in deathTriggeredRespawn set
		deathTriggeredRespawn.add(player.getUniqueId());
	}


	/**
	 * Player respawn event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerRespawn(final PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();

		// if player world is not enabled, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld()))
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
		giveDeathCompass(player);

		// set compass target to player death location
		setDeathCompassTarget(player);

		// send player respawn message
		plugin.messageBuilder.compose(player, MessageId.ACTION_PLAYER_RESPAWN)
				.setMacro(Macro.PLAYER, player)
				.setMacro(Macro.WORLD, player.getWorld())
				.send();
	}


	/**
	 * Player join event handler
	 *
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		// if player world is not enabled, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld()))
		{
			return;
		}

		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use"))
		{
			return;
		}

		// create 1 compass itemstack with configured settings
		ItemStack deathcompass = plugin.deathCompassUtility.createItem();

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
	public void onChangeWorld(final PlayerChangedWorldEvent event)
	{
		// get player for event
		Player player = Objects.requireNonNull(event.getPlayer());

		// if player world is not enabled in config, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld()))
		{
			return;
		}

		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use"))
		{
			return;
		}

		// create DeathCompass itemstack
		ItemStack deathcompass = plugin.deathCompassUtility.createItem();

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
	public void onItemDrop(final PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();

		// get itemstack that was dropped
		ItemStack droppedItemStack = event.getItemDrop().getItemStack();

		// create death compass itemstack for comparison
		ItemStack dc = plugin.deathCompassUtility.createItem();

		// if droppedItemStack is not a DeathCompass or destroy-on-drop config is not true, do nothing and return
		if (!droppedItemStack.isSimilar(dc) || !plugin.getConfig().getBoolean("destroy-on-drop"))
		{
			return;
		}

		// remove dropped item
		event.getItemDrop().remove();

		// play item_break sound to player if sound effects enabled in config
		plugin.soundConfig.playSound(player, SoundId.PLAYER_DROP_COMPASS);

		// if inventory does not contain at least 1 death compass, reset compass target
		if (!player.getInventory().containsAtLeast(dc, 1))
		{
			resetDeathCompassTarget(player);
		}

		// send player compass destroyed message
		plugin.messageBuilder.compose(player, MessageId.ACTION_ITEM_DESTROY).send();
	}


	/**
	 * Give 1 death compass to player
	 *
	 * @param player the player being given a death compass
	 */
	private void giveDeathCompass(final Player player)
	{
		// create DeathCompass itemstack
		ItemStack deathcompass = plugin.deathCompassUtility.createItem();

		// add DeathCompass itemstack to player inventory
		player.getInventory().addItem(deathcompass);

		// log info
		plugin.getLogger().info(player.getName() + ChatColor.RESET + " was given a death compass in "
				+ plugin.worldManager.getWorldName(player.getWorld()) + ChatColor.RESET + ".");
	}


	/**
	 * Remove all death compasses from inventory
	 *
	 * @param inventory the inventory from which to remove all death compasses
	 */
	private void removeDeathCompasses(final Inventory inventory)
	{
		ItemStack deathcompass = plugin.deathCompassUtility.createItem();
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
		new BukkitRunnable()
		{

			public void run()
			{
				Location location = getDeathLocation(player);
				if (location.getWorld() != player.getWorld())
				{
					return;
				}
				player.setCompassTarget(location);
			}
		}.runTaskLaterAsynchronously(plugin, plugin.getConfig().getLong("target-delay"));
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

		// set location to world spawn location, to be used as default if no stored death record found
		Location location = player.getWorld().getSpawnLocation();

		// fetch death record from datastore
		final Optional<DeathRecord> optionalDeathRecord = plugin.dataStore.selectRecord(player.getUniqueId(), worldUid);

		// if fetched record is not empty, set location
		if (optionalDeathRecord.isPresent() && optionalDeathRecord.get().getLocation().isPresent())
		{
			location = optionalDeathRecord.get().getLocation().get();
		}

		// return location
		return location;
	}

}

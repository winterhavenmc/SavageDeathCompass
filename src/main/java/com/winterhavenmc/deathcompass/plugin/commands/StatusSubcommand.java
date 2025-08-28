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

package com.winterhavenmc.deathcompass.plugin.commands;

import com.winterhavenmc.deathcompass.plugin.PluginMain;
import com.winterhavenmc.deathcompass.plugin.messages.Macro;
import com.winterhavenmc.deathcompass.plugin.messages.MessageId;
import com.winterhavenmc.deathcompass.plugin.sounds.SoundId;

import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.util.List;
import java.util.Objects;


/**
 * Displays configuration settings of the plugin
 */
final class StatusSubcommand extends AbstractSubcommand
{
	private final PluginMain plugin;
	private final LocaleProvider localeProvider;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	StatusSubcommand(final PluginMain plugin)
	{
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "status";
		this.usageString = "/deathcompass status";
		this.description = MessageId.COMMAND_HELP_STATUS;
		this.permissionNode = "deathcompass.status";
		this.localeProvider = LocaleProvider.create(plugin);
	}


	@Override
	public void onCommand(final CommandSender sender, final List<String> args)
	{
		if (!sender.hasPermission(permissionNode))
		{
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_STATUS_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
		}

		displayHeader(sender);
		displayPluginVersion(sender);
		displayDebugSetting(sender);
		displayLanguageSetting(sender);
		displayLocaleSetting(sender);
		displayTimezoneSetting(sender);
		displaySoundEffectsSetting(sender);
		displayDestroyOnDropSetting(sender);
		displayPreventStorageSetting(sender);
		displayTargetDelaySetting(sender);
		displayEnabledWorldsSetting(sender);
		displayFooter(sender);
	}


	private void displayHeader(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_HEADER)
				.setMacro(Macro.PLUGIN, plugin.getDescription().getName())
				.send();
	}

	private void displayFooter(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_FOOTER)
				.setMacro(Macro.PLUGIN, plugin.getDescription().getName())
				.setMacro(Macro.URL, "https://github.com/winterhavenmc/SavageDeathCompass/")
				.send();
	}

	private void displayPluginVersion(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_PLUGIN_VERSION)
				.setMacro(Macro.SETTING, plugin.getDescription().getVersion())
				.send();
	}

	private void displayDebugSetting(final CommandSender sender)
	{
		if (plugin.getConfig().getBoolean("debug"))
		{
			sender.sendMessage(ChatColor.GREEN + "Debug: "
					+ ChatColor.RED + plugin.getConfig().getString("debug"));
		}
	}

	private void displayLanguageSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_LANGUAGE)
				.setMacro(Macro.SETTING, plugin.getConfig().getString("language"))
				.send();
	}


	private void displayLocaleSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_LOCALE)
				.setMacro(Macro.SETTING, localeProvider.getLocale().toLanguageTag())
				.send();
	}


	private void displayTimezoneSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_TIMEZONE)
				.setMacro(Macro.SETTING, plugin.getConfig().getString("timezone", ZoneId.systemDefault().toString()))
				.send();
	}


	private void displaySoundEffectsSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_SOUND_EFFECTS)
				.setMacro(Macro.SETTING, plugin.getConfig().getBoolean("sound-effects"))
				.send();
	}

	private void displayDestroyOnDropSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_DESTROY_ON_DROP)
				.setMacro(Macro.SETTING, plugin.getConfig().getString("destroy-on-drop"))
				.send();
	}


	private void displayPreventStorageSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_PREVENT_STORAGE)
				.setMacro(Macro.SETTING, plugin.getConfig().getBoolean("prevent-storage"))
				.send();
	}

	private void displayTargetDelaySetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_TARGET_DELAY)
				.setMacro(Macro.SETTING, plugin.getConfig().getString("target-delay") + " ticks")
				.send();
	}

	private void displayEnabledWorldsSetting(final CommandSender sender)
	{
		plugin.messageBuilder.compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS)
				.setMacro(Macro.SETTING, plugin.worldManager.getEnabledWorldNames().toString())
				.send();
	}

}

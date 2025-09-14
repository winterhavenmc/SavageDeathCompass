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

package com.winterhavenmc.deathcompass.core.commands;

import com.winterhavenmc.deathcompass.core.PluginController;
import com.winterhavenmc.deathcompass.core.util.Macro;
import com.winterhavenmc.deathcompass.core.util.MessageId;
import com.winterhavenmc.deathcompass.core.util.SoundId;
import com.winterhavenmc.library.messagebuilder.resources.configuration.LocaleProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.util.List;


/**
 * Displays configuration settings of the plugin
 */
final class StatusSubcommand extends AbstractSubcommand
{
	private final PluginController.ContextContainer ctx;
	private final LocaleProvider localeProvider;


	/**
	 * Class constructor
	 */
	StatusSubcommand(final PluginController.ContextContainer ctx)
	{
		this.ctx = ctx;
		this.name = "status";
		this.usageString = "/deathcompass status";
		this.description = MessageId.COMMAND_HELP_STATUS;
		this.permissionNode = "deathcompass.status";
		this.localeProvider = LocaleProvider.create(ctx.plugin());
	}


	@Override
	public void onCommand(final CommandSender sender, final List<String> args)
	{
		if (!sender.hasPermission(permissionNode))
		{
			ctx.messageBuilder().compose(sender, MessageId.COMMAND_FAIL_STATUS_PERMISSION).send();
			ctx.soundConfig().playSound(sender, SoundId.COMMAND_FAIL);
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
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_HEADER)
				.setMacro(Macro.PLUGIN, ctx.plugin().getDescription().getName())
				.send();
	}

	private void displayFooter(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_FOOTER)
				.setMacro(Macro.PLUGIN, ctx.plugin().getDescription().getName())
				.setMacro(Macro.URL, "https://github.com/winterhavenmc/SavageDeathCompass/")
				.send();
	}

	private void displayPluginVersion(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_PLUGIN_VERSION)
				.setMacro(Macro.SETTING, ctx.plugin().getDescription().getVersion())
				.send();
	}

	private void displayDebugSetting(final CommandSender sender)
	{
		if (ctx.plugin().getConfig().getBoolean("debug"))
		{
			sender.sendMessage(ChatColor.GREEN + "Debug: "
					+ ChatColor.RED + ctx.plugin().getConfig().getString("debug"));
		}
	}

	private void displayLanguageSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LANGUAGE)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("language"))
				.send();
	}


	private void displayLocaleSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_LOCALE)
				.setMacro(Macro.SETTING, localeProvider.getLocale().toLanguageTag())
				.send();
	}


	private void displayTimezoneSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TIMEZONE)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("timezone", ZoneId.systemDefault().toString()))
				.send();
	}


	private void displaySoundEffectsSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_SOUND_EFFECTS)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("sound-effects"))
				.send();
	}

	private void displayDestroyOnDropSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_DESTROY_ON_DROP)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("destroy-on-drop"))
				.send();
	}


	private void displayPreventStorageSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_PREVENT_STORAGE)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getBoolean("prevent-storage"))
				.send();
	}

	private void displayTargetDelaySetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_TARGET_DELAY)
				.setMacro(Macro.SETTING, ctx.plugin().getConfig().getString("target-delay") + " ticks")
				.send();
	}

	private void displayEnabledWorldsSetting(final CommandSender sender)
	{
		ctx.messageBuilder().compose(sender, MessageId.COMMAND_STATUS_ENABLED_WORLDS)
				.setMacro(Macro.SETTING, ctx.worldManager().getEnabledWorldNames().toString())
				.send();
	}

}

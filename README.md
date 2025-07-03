[![Codacy Badge](https://app.codacy.com/project/badge/Grade/203cd1e762604fdbaecff754b8e48570)](https://app.codacy.com/gh/winterhavenmc/SavageDeathCompass/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Spigot Version](https://badgen.net/static/spigot-api/1.21.7?color=yellow)](https://spigotmc.org)
&nbsp;[![License](https://badgen.net/static/license/GPLv3)](https://www.gnu.org/licenses/gpl-3.0)

### Description:

This plugin was created as an alternative to the /back on death command. With DeathCompass enabled, a player is given a compass when they respawn after death. This compass points to their last death location in that world, so they can find their dropped loot. It also works well alongside various death chest and graveyard plugins.

### Features:

* Customizable item name, supports color codes
* Customizable item lore with color codes
* Customizable messages with variable substitution
* Option to disable messages individually
* Localization files for messages, item name and lore. default is English, with Spanish and German examples included.
* Permissions for use and admin commands
* Configurable for which worlds the plugin is enabled
* stores last death locations for each world so they are persistent when changing worlds, logging out/in, and server restarts
* Uses SQLite for persistent storage
* Configurable option to destroy compass on drop, to keep them from cluttering your world. Also destroys compass on death drop if configured
* Configurable delay for setting compass target for better compatibility with other plugins that react to player respawn events
* Destroys compass on any interaction with SavageDeathChests owned by player
* Configurable option to prevent placing death compass in chests/containers
* Customizable sound effects
* A perfect compliment to SavageDeathChest and SavageGraveyards

### Commands:

Command | Description
------- | -----------
`/deathcompass status` | Displays version info and configured settings.
`/deathcompass reload` | Reloads configuration file.
`/deathcompass help [command]` | Displays short help and usage message.

### Permissions:

Permission | Description | Default
---------- | ----------- | -------
`deathcompass.use` | Give player a DeathCompass when respawning after death | true
`deathcompass.status` | Allow viewing plugin status | op
`deathcompass.reload` | Allow reloading of configuration file	| op
`deathcompass.admin` | All admin commands | op

### Installation:

Place the jar file in your server plugins folder and restart the server.
Open the config.yml file with your favorite text editor, and add any worlds in which you would like the plugin to be active to the enabled-worlds section, and make any other configuration changes you desire.
Issue the /deathcompass reload command; restarting the server is not necessary for configuration changes to take effect
Remember to grant any players or groups that should receive death compasses the deathcompass.use permission node, and any administrators of the plugin the deathcompass.admin permission node.

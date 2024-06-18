# SModeration
### An easy-to-use minecraft plugin for moderating your server.

SModeration can be used to mute, ban, and kick players that break the rules while providing a nice interface to your moderators.
It keeps track of all punishments to ensure that you get a complete overview of every rule violation on your server.
You can also modify player inventories and ender chests.

If you have any feature requests, please [open an issue](https://github.com/Shiewk/SModeration/issues).

## The SMod menu
![SMod Menu Interface](https://github.com/Shiewk/SModeration/assets/152653291/d89da0f5-61de-44cf-b59e-feea08831959)

SModeration provides a nice user interface that can be used instead of chat commands.

![SMod Menu Sort](https://github.com/Shiewk/SModeration/assets/152653291/23e3862d-0915-47bd-9c47-6d8d10f8ab69)

It has helpful functions like filtering and sorting options.

## Commands

### /smod
The /smod command just opens the SMod menu. It takes no arguments.
### /mute
The /mute command is used to mute players.

It requires 2 arguments:
- Player name
- Duration

If you want to, you can also add a **reason**.

For example, if you want to mute a player for breaking server rules, just use **/mute playername 1h 30min Breaking server rules.**

The player will be muted for 1 hour and 30 minutes with the reason "Breaking server rules.".

Muted players can still join the server, but can't use the chat.
### /ban
The /ban command works the same as the /mute command, with one important difference:

Banned players **can't even join** the server until the ban expires.
### /kick
The /kick command is a bit different. It does not require a duration because kicks are instant. Instead, you can use the command like this: **/kick playername reason**
### /modlogs
The /modlogs command can be used when the /smod menu is unavailable. It displays information about a player in chat instead of a menu.

Example: **/modlogs playername** shows you a message in chat that tells you whether the player is muted or banned.
### /unmute & /unban
The /unmute and /unban commands only take one argument, the player name.

The specified player will then be unmuted or unbanned.
### /invsee
The /invsee command can be used to view the inventory of another player.

It takes one argument: the player name.
The player has to be online.
### /enderchestsee
The /enderchestsee command can, similarly to /invsee, be used to view the ender chest of another player.

It takes one argument: the player name.
The player has to be online.

## Permissions
This plugin uses Bukkit permissions for commands and other actions.
- **smod.mute**: Allows the player to mute other players.
- **smod.ban**: Allows the player to ban and kick other players.
- **smod.kick**: Allows the player to kick other players.
- **smod.menu**: Allows the player to use the SModeration menu.
- **smod.notifications**: Allows the player to be notified when a punishment is issued.
- **smod.unmute**: Allows the player to unmute other players.
- **smod.unban**: Allows the player to unban other players.
- **smod.logs**: Allows the player to view mod logs.
- **smod.invsee**: Allows the player to view other players inventories.
- **smod.invsee.modify**: Allows the player to view and modify other players inventories.
- **smod.invsee.preventmodify**: When giving this permission to a player, prevents their inventory from being modified.
- **smod.enderchestsee**: Allows the player to view other players ender chests.
- **smod.enderchestsee.modify**: Allows the player to view and modify other players ender chests.

All of these permissions are granted by default if the player is a server operator.
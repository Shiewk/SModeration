# SModeration commands

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

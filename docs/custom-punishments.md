# Creating custom punishments

SModeration (in versions 2.0 or later) offers the ability to create custom punishment types.
These custom types can either be timed (they have a duration and expire) or untimed (they happen exactly once).

Here is an example:

```yaml
custom-punishments:
  enabled: true
  # This will register the type 'kill'
  kill:
    timed: false
    # The name which will be used for the punishment in-game
    name: Kill
    # Commands that will be executed when the punishment is issued
    effects:
      # Placeholders will be replaced
      # See 'Command Placeholders' section below
      - /kill $uuid
    # Registers commands that can be used to apply the punishment
    commands:
      - /kill
      - /smodkill
  giant:
    timed: true
    name: Giant
    # Timed punishments can have multiple types of commands:
    commands:
      # Commands to apply punishment
      apply:
        - /giant
        - /bigmode
      # Commands to cancel punishment
      cancel:
        - /ungiant
        - /smallmode
    # Several effects can be registered for timed punishments
    # See 'Effects' below for others
    effects:
      apply:
        - "attribute $uuid minecraft:scale base set 3"
      expire:
        - "attribute $uuid minecraft:scale base reset"
```

# Command Placeholders

Placeholders in commands will be replaced by some values that may be important for what you are trying to do.

The following placeholders are available:

| Placeholder                              | Description                    | Example                                |
|------------------------------------------|--------------------------------|----------------------------------------|
| `$type`                                  | The punishment type ID         | `giant`                                |
| `$uuid`                                  | The affected player's UUID     | `069a79f4-44e9-4726-a5be-fca90e38aaf5` |
| `$name`                                  | The affected player's username | `Notch`                                |
| `$moduuid`                               | The moderator's UUID           | `853c80ef-3c37-49fd-aa49-938b674adae6` |
| `$modname`                               | The moderator's name           | `jeb_`                                 |
| `$reason`                                | The reason for the punishment  | `Griefing`                             |
| `$duration` (only for timed punishments) | The duration in milliseconds   | `86400000`                             |

# Effects

Custom punishments defined in the configuration can execute commands when certain events occur.

## For untimed punishments

Untimed punishments' effects are executed only once, when the punishment is issued.
These are defined as a simple list of commands under the `effects` key.

Example:
```yaml
kill:
  timed: false
  name: Kill
  effects:
    - /kill $uuid
```

## For timed punishments

Timed punishments can have different effects for different events.
Here, instead of a simple list, the effects section is organized by event type,
with each type containing a list of commands to be executed.

| Event     | Triggers                                                                                           |
|-----------|----------------------------------------------------------------------------------------------------|
| `apply`   | When the punishment is first applied to the player.                                                |
| `expire`  | When the punishment duration ends (only triggers if the player is online; may be delayed by 3-5s). |
| `join`    | When the affected player joins the server while the punishment is active.                          |
| `death`   | When the affected player dies while the punishment is active.                                      |
| `respawn` | When the affected player respawns while the punishment is active.                                  |

Example:

```yaml
giant:
  timed: true
  name: Giant
  effects:
    apply:
      - "attribute $uuid minecraft:scale base set 3"
    expire:
      - "attribute $uuid minecraft:scale base reset"
```
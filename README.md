# VoidCrates

A feature-rich, highly configurable crate mod for Fabric servers running Minecraft 1.21.11.

> **Based on [SkiesCrates](https://github.com/PokeSkies/SkiesCrates) by Stampede, ported and rebranded under MPL-2.0.**

---

## Features

- Physical crate blocks placed anywhere in the world
- Key-based opening system with physical key items
- Configurable left-click preview and right-click open interactions
- Weighted reward system with fine-grained probability control
- Multiple opening animations (inventory spinner, world animation)
- Particle effects around crate blocks (circle, spiral, beam, pulse)
- Hologram support via HoloDisplays
- Economy integration (BEconomy, CobbleDollars, Impactor, Pebbles Economy)
- Cobblemon Pokémon rewards
- PlaceholderAPI and MiniPlaceholders support
- MongoDB, MySQL, SQLite, and H2 storage backends
- Full MiniMessage formatting throughout
- Per-crate sound effects

---

## Requirements

| Dependency | Version | Required |
|---|---|---|
| Minecraft | 1.21.11 | ✅ |
| Fabric Loader | ≥ 0.18.0 | ✅ |
| Fabric API | 0.141.3+1.21.11 | ✅ |
| Fabric Language Kotlin | ≥ 1.13.0 | ✅ |
| HoloDisplays | ≥ 0.5.0+1.21.11 | ⬜ Optional |

---

## Installation

1. Download the latest `VoidCrates-*.jar` from [Releases](https://github.com/yourusername/VoidCrates/releases)
2. Place it in your server's `mods/` folder
3. Install the required dependencies listed above
4. Start the server — config files generate automatically at `config/voidcrates/`

---

## Config Structure

```
config/voidcrates/
├── config.json        ← Storage backend, interaction settings, migrations
├── lang.json          ← All player-facing messages (MiniMessage formatted)
├── crates/            ← One .json file per crate type
├── keys/              ← One .json file per key type
└── previews/          ← One .json file per preview layout
```

---

## Commands

The base command is `/voidcrates`. Aliases: `/vc`, `/crates`, `/crate`

### Admin Commands

| Command | Permission | Description |
|---|---|---|
| `/vc set <crate-id>` | `voidcrates.command.set` | Register the block you're looking at as a crate |
| `/vc remove` | `voidcrates.command.remove` | Unregister the crate block you're looking at |
| `/vc give <crate-id> <player> [amount]` | `voidcrates.command.give` | Give a player a physical crate item |
| `/vc open <crate-id> <player>` | `voidcrates.command.open` | Force-open a crate for a player (no key needed) |
| `/vc reload` | `voidcrates.command.reload` | Reload all configs without restarting |

### Key Commands

| Command | Permission | Description |
|---|---|---|
| `/vc key give <key-id> <player> [amount]` | `voidcrates.command.key.give` | Give a player a key item |
| `/vc key remove <key-id> <player> [amount]` | `voidcrates.command.key.remove` | Remove keys from a player's inventory |
| `/vc key list <player>` | `voidcrates.command.key.list` | Open the key viewer GUI for a player |

All commands require **OP level 2** by default. Install [LuckPerms](https://luckperms.net) to use the permission nodes instead.

---

## Setting Up a Crate

### Step 1 — Create a Key

Create `config/voidcrates/keys/vote_key.json`:

```json
{
  "id": "vote_key",
  "display": {
    "item": "minecraft:tripwire_hook",
    "name": "<gold><bold>Vote Key",
    "lore": [
      "<gray>Right-click a Vote Crate to open it!"
    ]
  }
}
```

### Step 2 — Create the Crate

Create `config/voidcrates/crates/vote_crate.json`:

```json
{
  "id": "vote_crate",
  "name": "<gold>Vote Crate",
  "key": "vote_key",
  "interactions": {
    "open": "RIGHT_CLICK",
    "preview": "LEFT_CLICK"
  },
  "opening": {
    "type": "ANIMATED_INVENTORY"
  },
  "rewards": [
    {
      "id": "cash_500",
      "name": "<yellow>$500",
      "weight": 60,
      "broadcast": false,
      "display": {
        "item": "minecraft:gold_ingot",
        "name": "<yellow>$500 Cash"
      },
      "actions": [
        {
          "type": "COMMAND_CONSOLE",
          "command": "eco give %player% 500"
        },
        {
          "type": "MESSAGE_PLAYER",
          "message": "<green>You won <yellow>$500<green> from the Vote Crate!"
        },
        {
          "type": "PLAY_SOUND",
          "sound": "minecraft:entity.player.levelup",
          "volume": 1.0,
          "pitch": 1.0
        }
      ]
    },
    {
      "id": "diamonds",
      "name": "<aqua>10 Diamonds",
      "weight": 25,
      "broadcast": false,
      "display": {
        "item": "minecraft:diamond",
        "name": "<aqua>10 Diamonds"
      },
      "actions": [
        {
          "type": "COMMAND_CONSOLE",
          "command": "give %player% minecraft:diamond 10"
        }
      ]
    },
    {
      "id": "vip_rank",
      "name": "<light_purple><bold>VIP Rank",
      "weight": 5,
      "broadcast": true,
      "display": {
        "item": "minecraft:nether_star",
        "name": "<light_purple><bold>VIP Rank"
      },
      "actions": [
        {
          "type": "COMMAND_CONSOLE",
          "command": "lp user %player% parent set vip"
        },
        {
          "type": "MESSAGE_BROADCAST",
          "message": "<gold>%player% <yellow>just won <light_purple><bold>VIP<yellow> from the Vote Crate!"
        }
      ]
    }
  ]
}
```

### Step 3 — Place and Register In-Game

1. Place any block where you want the crate
2. Look directly at it and run `/vc set vote_crate`
3. Give yourself a key: `/vc key give vote_key YourName 1`
4. Right-click the block to open, left-click to preview

---

## Reward Weights

Weight is relative. A reward with weight `60` is 12× more likely than one with weight `5`.

**Chance = reward weight ÷ sum of all weights**

For the example above (60 + 25 + 5 = 90 total):
- $500 Cash → 66.7% chance
- 10 Diamonds → 27.8% chance
- VIP Rank → 5.5% chance

---

## Reward Action Types

| Type | Description |
|---|---|
| `COMMAND_CONSOLE` | Run a command as the server. Use `%player%` for the player's name |
| `COMMAND_PLAYER` | Run a command as the player |
| `MESSAGE_PLAYER` | Send a MiniMessage-formatted message to the player |
| `MESSAGE_BROADCAST` | Broadcast a MiniMessage message to the entire server |
| `PLAY_SOUND` | Play a sound to the player |
| `GIVE_KEY` | Give the player a VoidCrates key directly |
| `GIVE_ITEM` | Give the player an item directly |
| `ECONOMY_GIVE` | Give the player money (requires an economy mod) |

---

## Holograms

Requires [HoloDisplays](https://modrinth.com/mod/holodisplays) ≥ 0.5.0+1.21.11.

Add a `hologram` block to your crate config:

```json
"hologram": {
  "lines": [
    "<gold><bold>VOTE CRATE",
    "<gray>Right-click to open",
    "<yellow>Requires a Vote Key"
  ],
  "offset": { "x": 0.0, "y": 1.5, "z": 0.0 }
}
```

---

## Particle Effects

```json
"particles": [
  {
    "type": "CIRCLE",
    "particle": "minecraft:flame",
    "actions": ["IDLE"]
  }
]
```

Available types: `CIRCLE`, `SPIRAL`, `BEAM`, `PULSE`

---

## Placeholders

Requires [PlaceholderAPI](https://modrinth.com/mod/placeholder-api) or [MiniPlaceholders](https://modrinth.com/mod/miniplaceholders).

| Placeholder | Description |
|---|---|
| `%voidcrates:keys <key-id>%` | Number of keys a player has |

---

## Economy Support

VoidCrates supports the following economy mods natively. Install any one and use the `ECONOMY_GIVE` action type.

- [BEconomy](https://modrinth.com/mod/beconomy)
- [CobbleDollars](https://modrinth.com/mod/cobbledollars)
- [Impactor](https://github.com/NickImpact/Impactor)
- [Pebbles Economy](https://modrinth.com/mod/pebbles-economy)

If you use a different economy plugin, use `COMMAND_CONSOLE` with your economy plugin's give command instead.

---

## Storage Backends

Configure in `config/voidcrates/config.json`:

| Type | Notes |
|---|---|
| `H2` | Default. No setup needed, single-file database |
| `SQLITE` | Lightweight, good for small servers |
| `MYSQL` | Recommended for larger or networked servers |
| `MONGODB` | For servers already running MongoDB |

---

## Building from Source

Requires Java 21.

```bash
git clone https://github.com/yourusername/VoidCrates
cd VoidCrates
./gradlew build
```

Output jar: `build/libs/VoidCrates-*.jar` (use the one without `-dev` or `-sources`)

---

## License

Mozilla Public License 2.0 — see [LICENSE](LICENSE).

This is a fork of [SkiesCrates](https://github.com/PokeSkies/SkiesCrates) by Stampede, ported to Minecraft 1.21.11 and rebranded as VoidCrates. All original source files remain under MPL-2.0.

---
title: "How to Make a Battlepass"
sidebar_position: 1
---

A **battlepass** is the star of the show: one config file that defines how much **XP** each tier needs, how many **tiers** exist, and which **rewards** players earn on the Free and Premium tracks. This page takes you from an empty file to a working pass you can open in game.

## Quick start

1. Open the `/plugins/EcoBattlepass/battlepasses/` folder and copy `_example.yml` to a new file, e.g. `seasonal.yml`. The file name is the pass ID.
2. Set the `name` and the `battlepass:` block: `xp-formula`, `max-tier`, `command`, `premium-permission`, and the `battlepass-start` / `battlepass-end` dates.
3. Under `tiers:`, add a tier number and list the **reward** IDs it grants, each marked `free` or `premium`. See [How to make a reward](how-to-make-a-reward) for the reward files themselves.
4. Run `/ecobattlepass reload`.
5. Run your pass command (e.g. `/seasonal`) and confirm the GUI opens with your tiers and rewards.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real battlepass. You can also organise battlepasses into subfolders inside `battlepasses/`, and they'll still load.
:::

## Naming and IDs

The file name without `.yml` is the battlepass ID. You use this ID in your category configs, in effect filters, and as the command name.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the battlepass will not load.
:::

## The structure of a battlepass

| Part | What it controls |
| --- | --- |
| **Settings** | The XP formula, tier count, command, premium permission, and start/end dates |
| **Tiers** | Which rewards land on each tier, and whether they're free or premium |
| **Display overrides** | Optional per-tier button appearance that beats the `config.yml` defaults |

```yaml
# === Settings: how the pass behaves ===
name: "&6Example Battlepass" # Display name shown in the GUIs
battlepass:
  xp-formula: "1.5 * %level% + 5" # XP needed per tier; %level% scales it per tier
  max-tier: 100 # Highest tier players can reach
  command: "battlepass" # Command that opens this pass's GUI
  premium-permission: "example.pass.premium" # Permission that grants the Premium track
  battlepass-start: 2025-01-01 00:00 # Start date, server time; format YYYY-MM-DD HH:MM
  battlepass-end: 2025-05-01 00:00 # End date, server time; format YYYY-MM-DD HH:MM

# === Tiers: what each tier awards ===
tiers:
  - tier: 1 # Skip a tier entirely to give it no reward
    rewards:
      - id: coins_5000 # Reward ID; the file name in /rewards/
        tier: free # "free" anyone can claim, "premium" needs the premium permission
      - id: coins_10000
        tier: premium
```

### Settings

The `battlepass:` block controls how the pass runs.

```yaml
battlepass:
  xp-formula: "1.5 * %level% + 5" # XP for the next tier; %level% is the current tier
  max-tier: 100 # Highest reachable tier
  command: "battlepass" # Command that opens the GUI
  premium-permission: "example.pass.premium" # Permission for the Premium track
  battlepass-start: 2025-01-01 00:00 # Start date, server time
  battlepass-end: 2025-05-01 00:00 # End date, server time
```

:::info Dates use server time
`battlepass-start` and `battlepass-end` are read in the server's time zone, formatted `YYYY-MM-DD HH:MM`.
:::

### Tiers

Each entry under `tiers:` is a tier number and the rewards it grants.

```yaml
tiers:
  - tier: 1 # Tiers you don't list simply have no reward
    rewards:
      - id: diamond_block # Reward ID, defined in /rewards/
        tier: free # "free" anyone, "premium" needs the premium permission
      - id: money_1000
        tier: premium
```

### Display overrides

Each tier can optionally override the button appearance from `config.yml` for specific states. These beat the defaults, which is handy for highlighting milestone tiers.

```yaml
tiers:
  - tier: 1
    rewards:
      - id: diamond_block
        tier: free
    display:
      # Generic override: used in combined layout, and as the fallback in split layout
      # States: unlocked, locked, in-progress, claimed, unlocked-free, premium-required, hidden
      unlocked:
        item: diamond_block
        name: "&6&lSPECIAL TIER"
        lore:
          - "&7Special rewards await!"
          - "%free-rewards%"
          - "%premium-rewards%"

      # Split-layout overrides: only used when layout: split in config.yml
      free-track:
        unlocked:
          item: emerald_block
          name: "&a&lFREE TIER - SPECIAL"
      premium-track:
        premium-required:
          item: gold_block
          name: "&6&lUPGRADE NEEDED"
```

Override priority, highest to lowest: track-specific override, then generic override, then track-specific `config.yml` default, then `config.yml` default.

## Internal placeholders

| Placeholder | Value |
| --- | --- |
| `%level%` | The battlepass tier/level. Useful for XP scaling in `xp-formula`. |

:::tip Troubleshooting
- **Pass command does nothing?** The `command` value is wrong or the player lacks `ecobattlepass.command.<pass_id>`. Check the command name and permission.
- **Premium rewards won't claim?** The player is missing the `premium-permission` you set. Grant it, or set the reward's `tier` to `free`.
- **Config won't load after editing?** The file name (the ID) has invalid characters. Use lowercase letters, numbers, and underscores only.
- **Tiers look wrong in the GUI?** A per-tier `display` override is taking priority over `config.yml`. Remove the override to fall back to the defaults.
:::

<hr/>

## Where to go next

- **Rewards:** [How to make a reward](how-to-make-a-reward) defines what each tier grants.
- **Quests:** [Configuring a category](how-to-make-a-battlequest/configuring-a-category) adds the quest system that feeds XP into the pass.
- **GUI appearance:** [Plugin config](plugin-config) is the full annotated `config.yml`.
- **Placeholders:** [Internal placeholders](internalplaceholders) lists everything usable in lore and names.
- **Defaults:** the shipped configs live [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/battlepasses).
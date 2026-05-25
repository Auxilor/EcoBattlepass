---
title: "How to make a BattlePass"
sidebar_position: 1
---

The battlepass configs are the star of the show. This is where you define how much XP is needed per-tier, how many tiers are in the battlepass, and what rewards the player receives.

EcoBattlepass gives you the freedom to create multiple battlepasses, each with its own Quests, Tasks, Tiers, Rewards and commands. This allows you to create a different battlepass for an event, whilst running simultaneously with your main, seasonal pass.

## How to add battlepasses

Each battlepass is its own config file, placed in the `/battlepasses/` folder, and you can add or remove them as you please. There's an example config called `_example.yml` to help you out!

The ID of the Battlepass is the file name. This is what you would use in your category configs and effects.\
ID's must be lowercase letters, numbers, and underscores only.

## Example BattlePass Config

```yaml
name: "&6Example Battlepass"  
  
battlepass:  
  xp-formula: 1.5 * %level% + 5
  max-tier: 100
  command: 'battlepass'
  premium-permission: "example.pass.premium"  
  battlepass-start: 2025-01-01 00:00
  battlepass-end: 2025-05-01 00:00

tiers:  
  - tier: 1 
    rewards:  
      - id: coins_5000
        tier: free
      - id: coins_10000
        tier: premium
      - id: golden_apple_3  
        tier: premium
```

## Understanding the Sections

#### The BattlePass Configuration

```yml
name: "&6Example Battlepass" # The name of the battlepass, to display in GUIs.
battlepass:
  # The formula to calculate the XP needed to reach the next tier.
  # You can use %level% here to create xp scaling.
  xp-formula: "1.5 * %level% + 5"
   
  # The maximum tier of the BattlePass.
  max-tier: 100
   
   # The command used to open the BattlePass GUI.
  command: "battlepass"

  # The permission required for the premium pass
  premium-permission: "example.pass.premium"

  # The date and time the BattlePass starts. Format: YYYY-MM-DD HH:MM
  # This uses the server time.
  battlepass-start: 2025-01-01 00:00
   
  # The date the battle pass ends. Format: YYYY-MM-DD HH:MM
  # This uses the server time.
  battlepass-end: 2025-05-01 00:00
```

#### The Reward Tiers

```yaml
tiers:
  # The tier number for the reward(s).
  # Don't include the tier if you don't want a reward.
  - tier: 1
    rewards:
      # The ID of the reward, see more here:
      - id: diamond_block
      # The tier the reward is in.
      # "Premium" means only Premium players, "Free" allows anyone to claim
        tier: free
        
      # List all the rewards in the same format.
      - id: money_1000
        tier: premium
```

#### Per-Tier Display Overrides

Each tier can optionally override the button appearance configured in `config.yml` for specific states.
These take priority over the defaults.

```yaml
tiers:
  - tier: 1
    rewards:
      - id: diamond_block
        tier: free
    display:
      # Generic override — applies in combined layout mode and as fallback in split mode.
      # Available states: unlocked, locked, in-progress, claimed, unlocked-free, premium-required, hidden
      unlocked:
        item: diamond_block
        name: "&6&lSPECIAL TIER"
        lore:
          - "&7Special rewards await!"
          - "%free-rewards%"
          - "%premium-rewards%"

      claimed:
        item: emerald_block
        name: "&a&lSPECIAL TIER - CLAIMED"

      # Split layout overrides — only used when layout: split in config.yml
      free-track:
        unlocked:
          item: emerald_block
          name: "&a&lFREE TIER - SPECIAL"
          lore:
            - "%free-rewards%"
        claimed:
          item: emerald
          name: "&a&lCLAIMED FREE TIER"

      premium-track:
        unlocked:
          item: diamond_block
          name: "&b&lPREMIUM TIER - SPECIAL"
          lore:
            - "%premium-rewards%"
        premium-required:
          item: gold_block
          name: "&6&lPREMIUM TIER - UPGRADE NEEDED"
        claimed:
          item: diamond
          name: "&a&lCLAIMED PREMIUM TIER"
```

Override priority (highest to lowest): track-specific override → generic override → track-specific config.yml default → config.yml default.

## Internal Placeholders

| Placeholder | Value                                             |
| ----------- | ------------------------------------------------- |
| `%level%`   | The battlepass tier/level. Useful for XP scaling. |

<hr/>

## Default Configs
The default configs can be found [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/battlepasses).
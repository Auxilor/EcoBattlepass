---
title: "How to make a Reward"
sidebar_position: 2
---

Rewards are what the players earn for tiering up the battlepass. Without rewards, what would be the point? Thanks to libreforge, rewards don't have to just be some XP, some currency, an item. They can be Potion Effects, stat multipliers, and much more. Rewards are really easy to setup, and they are totally reusable.

## How to add rewards

Each reward is its own config file, placed in the `/rewards/` folder, and you can add or remove them as you please. There's an example config called `_example.yml` to help you out!

The ID of the Reward is the file name. This is what you use in your battlepass configs, and effect filters.\
ID's must be lowercase letters, numbers, and underscores only.

## Example Reward Config

```yaml
display:
  name: "&6x&b1 &fDiamond Block"
  reward-lore:
    - "&7This is a diamond block"
    - "&7It is very valuable"

effects:
  - id: give_item
    args:
      item: DIAMOND_BLOCK 1
```

## Understanding the Sections

#### The Display Options

```yaml
# This is what is shown in the BattlePass GUI on the tier.
display:
  name: "&6x&b1 &fDiamond Block"
  reward-lore:
    - "&7This is a diamond block"
    - "&7It is very valuable"
```

If you do not want to have any lore, you can simply blank it out.

```yaml
display:
  name: "&6x&b1 &fDiamond Block"
  reward-lore: [ ]
```

#### The Effects Section
:::danger Effects Section

The effects section is the core functionality of the reward. You can configure effects, conditions, filters, and mutators in this section to run when the BattlePass reward is claimed.

Check out [Configuring an Effect](https://plugins.auxilor.io/effects/configuring-an-effect) to understand how to configure this section correctly.

For more advanced users or setups, you can configure chains in this section to string together different effects under one trigger. Check out [Configuring an Effect Chain](https://plugins.auxilor.io/effects/configuring-a-chain) for more info.

:::
```yaml
# Effects are part of libreforge, you can read more below.
effects:
  - id: give_item
    args:
      item: DIAMOND_BLOCK 1
```

**Other Examples:**

```yml
effects:
  - id: add_holder
    args:
      effects: 
        - id: movement_speed_multiplier
          args:
            multiplier: 1.25
      conditions: []
      duration: 36000
```

This example grants the player a 25% Movement Speed Bonus for 30 Minutes.

```yaml
effects:
  - id: run_command
    args:
      command: '/envoy flare default %player% 2'
```

This example runs a command from an external plugin, in this case, giving the player 2 Envoy Flares from AxEnvoy.

```yaml
effects:
  - id: give_skill_xp
    args:
      amount: 100
      skill: mining
```

This example gives 100 Skill XP for the EcoSkills Mining skill.

<hr/>

## Default Configs
The default configs can be found [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/rewards).
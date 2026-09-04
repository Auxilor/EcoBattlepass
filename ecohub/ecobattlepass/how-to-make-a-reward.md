---
title: "How to Make a Reward"
sidebar_position: 2
---

A **reward** is what a player earns for tiering up the battlepass. Each reward is a reusable config file with a **display** (what shows on the tier button) and **effects** (what actually happens when it's claimed). Thanks to libreforge, a reward can be an item, currency, a potion effect, a stat multiplier, or anything else you can express as an effect. This page builds one from scratch.

## Quick start

1. Open the `/plugins/EcoBattlepass/rewards/` folder and copy `_example.yml` to a new file, e.g. `diamond_block.yml`. The file name is the reward ID.
2. Set the `display` `name` and `reward-lore` that show on the tier button.
3. Add the `effects` that fire when the reward is claimed, using the [Item Lookup System](https://hub.auxilor.io/wiki/eco/the-item-lookup-system-the-item-lookup-system) for any item arguments.
4. Reference the reward ID from a tier in your [battlepass config](how-to-make-a-battlepass).
5. Run `/ecobattlepass reload`, open the pass, claim the tier, and confirm the player receives the reward.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real reward. You can also organise rewards into subfolders inside `rewards/`, and they'll still load.
:::

## Naming and IDs

The file name without `.yml` is the reward ID. You use this ID in your battlepass configs and effect filters.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the reward will not load.
:::

## The structure of a reward

| Part | What it controls |
| --- | --- |
| **Display** | The name and lore shown on the tier button in the GUI |
| **Effects** | What happens when the reward is claimed |

```yaml
# === Display: what the player sees on the tier ===
display:
  name: "&6x&b1 &fDiamond Block" # Reward name in the GUI
  reward-lore: # Lines under the name; use [] for no lore
    - "&7This is a diamond block"
    - "&7It is very valuable"

# === Effects: what the player gets on claim ===
effects:
  - id: give_item # libreforge effect
    args:
      item: DIAMOND_BLOCK 1 # Item Lookup System syntax
```

### Display

The `display` block is the reward's name and lore on the tier button.

```yaml
display:
  name: "&6x&b1 &fDiamond Block" # Shown on the tier
  reward-lore: # Use [] to show no lore at all
    - "&7This is a diamond block"
    - "&7It is very valuable"
```

### Effects

The `effects` block is the core of the reward: it runs when the player claims the tier.

```yaml
effects:
  - id: give_item
    args:
      item: DIAMOND_BLOCK 1 # Item Lookup System syntax
```

Gives the player a diamond block when they claim the tier.

:::danger Effects are their own system
Effects, conditions, filters, and mutators are a shared libreforge system, documented in full elsewhere.

- [Configuring an Effect](https://hub.auxilor.io/wiki/libreforge/configuring-an-effect) covers single effects, conditions, and filters.
- [Configuring an Effect Chain](https://hub.auxilor.io/wiki/libreforge/configuring-a-chain) covers stringing multiple effects together under one trigger.
:::

:::tip Troubleshooting
- **Reward shows in the GUI but nothing happens on claim?** The `effects` block is missing or has a bad `id`. Check the effect against the libreforge docs.
- **Item won't give?** The `item:` argument is malformed. Verify the syntax with the Item Lookup System.
- **Config won't load after editing?** The file name (the ID) has invalid characters. Use lowercase letters, numbers, and underscores only.
:::

<hr/>

## Where to go next

- **Use the reward:** [How to make a battlepass](how-to-make-a-battlepass) is where you attach reward IDs to tiers.
- **Effects deep-dive:** [Configuring an Effect](https://hub.auxilor.io/wiki/libreforge/configuring-an-effect) is the full libreforge reference.
- **Item syntax:** the [Item Lookup System](https://hub.auxilor.io/wiki/eco/the-item-lookup-system-the-item-lookup-system) covers item arguments.
- **Defaults:** the shipped rewards live [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/rewards).
---
title: "PlaceholderAPI"
sidebar_position: 6
---

| Placeholder                                        | Description                                     | Options                     |
|----------------------------------------------------|-------------------------------------------------|-----------------------------|
| `%ecobattlepass_category_<id>_start_date%`         | The start date of the quest category.           |                             |
| `%ecobattlepass_category_<id>_end_date%`           | The end date of the quest category.             |                             |
| `%ecobattlepass_category_<id>_start_timer%`        | The time until the category starts.             |                             |
| `%ecobattlepass_category_<id>_end_timer%`          | The time until the category ends.               |                             |
| `%ecobattlepass_category_<id>_reset_timer%`        | The time until the category resets.             |                             |
| `%ecobattlepass_claimable_<battlepass>%`           | The amount of claimable tiers/rewards.          |                             |
| `%ecobattlepass_tier_<battlepass>%`                | The player's current Battlepass tier.           | add `_numeral` for numerals |
| `%ecobattlepass_xp_<battlepass>%`                  | The player's current tier xp.                   | add `_formatted` for commas |
| `%ecobattlepass_xp_required_<battlepass>%`         | The amount of xp needed for the tier.           | add `_formatted` for commas |
| `%ecobattlepass_<battlepass>_percentage_progress%` | The % progress through the battlepass tier.     |                             |
| `%ecobattlepass_<battlepass>_max_tiers%`           | The max tiers of the battlepass.                |                             |
| `%ecobattlepass_<battlepass>_pass_type%`           | The type of pass the player has (Free/Premium). |                             |
| `%ecobattlepass_tier_free_reward_<battlepass>_<tier>_<line>%`         | A specific line of the free rewards for a tier (0-indexed). Returns empty if out of bounds.    | |
| `%ecobattlepass_tier_premium_reward_<battlepass>_<tier>_<line>%`      | A specific line of the premium rewards for a tier (0-indexed). Returns empty if out of bounds. | |
| `%ecobattlepass_tier_claimed_free_reward_<battlepass>_<tier>_<line>%` | A specific line of the claimed free rewards for a tier (0-indexed).                            | |
| `%ecobattlepass_tier_claimed_premium_reward_<battlepass>_<tier>_<line>%` | A specific line of the claimed premium rewards for a tier (0-indexed).                      | |

### Tier Reward Line Placeholders Example

Given a reward defined as:
```yaml
display:
  name: "Iron Sword"       # Line 0
  reward-lore:
    - "&7Sharpness II"     # Line 1
    # Requesting line 2 returns an empty string (out of bounds)

effects:
  - id: give_item
    args:
      item: iron_sword 1 sharpness:2
```

For this reward in Tier 7 (free track) of a battlepass with ID `battlepass`:

```
%ecobattlepass_tier_free_reward_battlepass_7_0% → Iron Sword      (Line 0 - Name)
%ecobattlepass_tier_free_reward_battlepass_7_1% → &7Sharpness II  (Line 1 - Lore)
```

---
title: "Internal Placeholders"
sidebar_position: 7
---

EcoBattlepass offers a lot of internal placeholders that you can use in your lore and display names for BattlePasses, Categories, and Quests.

Rather than cluttering the config.yml, this is a dedicated page for all the internal placeholders for config.yml.

## BattlePass & Category GUI

| Placeholder             | Description                                                                | Options                         |
|-------------------------|----------------------------------------------------------------------------|---------------------------------|
| `%pass%`                | The battlepass name                                                        | add `_id` for the battlepass ID |
| `%pass_type%`           | The type of pass the player has (Free/Premium)                             |                                 |
| `%start_date%`          | The date the battlepass starts                                             |                                 |
| `%end_date%`            | The date the battlepass ends                                               |                                 |
| `%tier%`                | The battlepass tier/level                                                  | add `_numeral` for numerals     |
| `%tier_x%`              | The battlepass tier +/- x levels. E.g. `%tier_-5%` is current tier minus 5 | add `_numeral` for numerals     |
| `%claimable_tiers%`     | The amount of unclaimed tiers.                                             |                                 |
| `%max_tiers%`           | The max amount of tiers in the pass                                        |                                 |
| `%current_bp_xp%`       | The current tier XP                                                        | add `_formatted` for commas     |
| `%required_bp_xp%`      | The XP required for the tier                                               | add `_formatted` for commas     |
| `%current_task_xp%`     | The current task XP                                                        | add `_formatted` for commas     |
| `%required_task_xp%`    | The XP required for the task                                               | add `_formatted` for commas     |
| `%percentage_progress%` | The percentage of the tier XP progress                                     |                                 |

## BattlePass Tiers GUI

All the placeholders from BattlePass GUI work here. Additionally:

| Placeholder                | Description                                                | Options                                           |
|----------------------------|------------------------------------------------------------|---------------------------------------------------|
| `%free-rewards%`           | The free rewards for the tier                              | add `claimed-` prefix for claimed free rewards    |
| `%premium-rewards%`        | The premium rewards for the tier                           | add `claimed-` prefix for claimed premium rewards |
| `%page%`                   | The current page number (usable in the tiers GUI title)    |                                                   |
| `%max_page%`               | The total number of pages (usable in the tiers GUI title)  |                                                   |

## Quest GUI

| Placeholder       | Description                                | Options                         |
|-------------------|--------------------------------------------|---------------------------------|
| `%category_name%` | Name of the category                       | add `_id` for the category ID   |
| `%pass%`          | Name of the battlepass                     | add `_id` for the battlepass ID |
| `%completed%`     | Number of quests completed in the category |                                 |
| `%total%`         | Total number of quests in the category     |                                 |
| `%time%`          | Time until the category start/reset/end    |                                 |

## Tasks Lore

| Placeholder          | Description                      | Options                     |
|----------------------|----------------------------------|-----------------------------|
| `%task%`             | The name of the task             | add `_id` for the task ID   |
| `%current_task_xp%`  | Player's current XP in the task  | add `_formatted` for commas |
| `%required_task_xp%` | XP required to complete the task | add `_formatted` for commas |

## Other Internal Placeholders

| Placeholder  | Description         | Options                                  |
|--------------|---------------------|------------------------------------------|
| `%level%`    | The battlepass tier | Useful for math inside battlepass config |
| `%page%`     | The page number     | Available in Category and Quest GUIs     |
| `%category%` | The category name   | Available for Quest GUI title            |


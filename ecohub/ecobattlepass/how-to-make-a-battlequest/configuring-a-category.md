---
title: "Configuring a Category"
sidebar_position: 1
---

A **category** groups quests together in the Quests GUI and controls when they're available. Each category is one config file that sets its **display**, its **schedule** (start, duration, reset), and the **quests** it contains. This page builds one from scratch.

## Quick start

1. Open the `/plugins/EcoBattlepass/categories/` folder and copy `_example.yml` to a new file, e.g. `daily.yml`. The file name is the category ID.
2. Set the `name`, `item`, and `lore` shown in the Categories GUI.
3. Set `battlepass`, `priority`, `start-date`, `duration`, and `reset-time` to control where and when it appears.
4. Under `quests:`, list the [quest](configuring-a-quest) IDs in the order you want them shown.
5. Run `/ecobattlepass reload`, then `/ecobattlepass quests <pass_id>` and confirm the category appears with its quests.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real category. You can also organise categories into subfolders inside `categories/`, and they'll still load.
:::

## Naming and IDs

The file name without `.yml` is the category ID. Categories accept both Free and Premium quests.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the category will not load.
:::

## The structure of a category

| Part | What it controls |
| --- | --- |
| **Display** | The name, item, and lore shown in the Categories GUI |
| **Schedule** | Which pass it belongs to, its sort priority, and its start/duration/reset timing |
| **Quests** | The quest IDs in the category, in display order |

```yaml
# === Display: what shows in the Categories GUI ===
name: "&8»&e Daily Challenges" # Category name
item: nether_star 1 # Icon item
lore:
  - "&a%completed%/%total% Complete"
  - "&7%time%"

# === Schedule: where and when it appears ===
battlepass: battlepass # ID of the pass this category belongs to
priority: 0 # Sort order; 0 is highest and shows first
start-date: 2025-03-23 00:00 # When it starts; format YYYY-MM-DD HH:MM
duration: 100000 # Minutes it stays open; -1 for no end
reset-time: 1440 # Minutes between resets; 1440 = daily, -1 for no reset

# === Quests: the quests in this category ===
quests:
  - id: free_daily_quest_1
  - id: free_daily_quest_2
```

### Display

The display fields set the category's icon in the Categories GUI.

```yaml
name: "&8»&e Daily Challenges" # Category name
item: nether_star 1 # Icon item
lore:
  - "&a%completed%/%total% Complete"
  - "&7%time%" # Start/reset/end timer for the category
```

### Schedule

These fields tie the category to a pass and control its timing.

```yaml
battlepass: battlepass # ID of the pass this category belongs to
priority: 0 # Sort order; 0 is highest priority and shows first
start-date: 2025-03-23 00:00 # When it starts; format YYYY-MM-DD HH:MM
duration: 100000 # Minutes the category stays open; -1 for no end
reset-time: 1440 # Minutes between resets; 1440 = daily, -1 for no reset
timer-format: # Wording for %time%, by category state
  start: "Starts in %time%"
  end: "Ends in %time%"
  reset: "Resets in %time%"
  none: "One-Time quest (does not end or reset)"
```

### Quests

The `quests:` list is the quest IDs in the category. List order is GUI order.

```yaml
quests:
  - id: free_daily_quest_1
  - id: free_daily_quest_2
  - id: free_daily_quest_3
```

## Internal placeholders

| Placeholder | Value |
| --- | --- |
| `%completed%` | The number of completed quests in the category. |
| `%total%` | The total number of quests in the category. |
| `%time%` | The time until the category's start, reset, or end. |

:::tip Troubleshooting
- **Category doesn't appear?** The `battlepass` ID is wrong, or `start-date` is in the future. Check the ID matches a real pass and the date has passed.
- **Quests missing from the category?** A quest ID under `quests:` doesn't match a file in `/quests/`. Check the IDs.
- **Config won't load after editing?** The file name (the ID) has invalid characters. Use lowercase letters, numbers, and underscores only.
:::

<hr/>

## Where to go next

- **Add quests:** [Configuring a quest](configuring-a-quest) defines the quests you list here.
- **Add tasks:** [Configuring a task](configuring-a-task) defines what quests track.
- **Placeholders:** [Internal placeholders](../internalplaceholders) lists everything usable in lore and names.
- **Defaults:** the shipped categories live [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/categories).
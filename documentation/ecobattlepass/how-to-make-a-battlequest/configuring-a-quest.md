---
title: "Configuring a Quest"
sidebar_position: 2
---

A **quest** is a goal a player works toward inside a category. Each quest is one config file with a **display**, its **reward** (battlepass XP and which track it counts for), and the **tasks** that track progress. This page builds one from scratch.

## Quick start

1. Open the `/plugins/EcoBattlepass/quests/` folder and copy `_example.yml` to a new file, e.g. `chickens.yml`. The file name is the quest ID.
2. Set the `display` `item`, `display-name`, and `description`, using the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system) for the icon.
3. Set `battlepass-points` and `battlepass-tier` for the XP reward and track.
4. Under `tasks:`, list the [task](configuring-a-task) IDs and the `xp` each needs, then set `task-amount`.
5. Add the quest ID to a [category](configuring-a-category), run `/ecobattlepass reload`, open the Quests GUI, and confirm it appears.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real quest. You can also organise quests into subfolders inside `quests/`, and they'll still load.
:::

## Naming and IDs

The file name without `.yml` is the quest ID. You use this ID in your categories.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the quest will not load.
:::

## The structure of a quest

| Part | What it controls |
| --- | --- |
| **Display** | The icon, name, and description shown in the Quests GUI |
| **Reward** | The battlepass XP awarded and which track it counts for |
| **Tasks** | The tasks that track progress, and how many are required |

```yaml
# === Display: what shows in the Quests GUI ===
display:
  item: player_head texture:"..." # Icon; Item Lookup System syntax
  display-name: Chickens # Quest name
  description:
    - "Kill animals to earn points"

# === Reward: battlepass XP and track ===
battlepass-points: 100 # Battlepass XP awarded on completion
battlepass-tier: premium # "premium" or "free"; free quests progress for everyone

# === Tasks: what tracks progress ===
tasks:
  - id: chickens # Task ID, from /tasks/
    xp: 100 # Task XP required
task-amount: 1 # How many tasks from the list must be completed
```

### Display

The `display` block is the quest's icon, name, and description.

```yaml
display:
  item: player_head texture:"..." # Icon; see the Item Lookup System
  display-name: Chickens # Quest name
  description:
    - "Kill animals to earn points"
```

### Reward

These fields set the battlepass XP and which track the quest counts toward.

```yaml
battlepass-points: 100 # Battlepass XP awarded on completion
battlepass-tier: premium # "premium" or "free"; with "free", premium users still progress
```

### Tasks

The `tasks:` list is the task IDs the quest tracks. `task-amount` lets you require only some of them.

```yaml
tasks:
  - id: chickens # Task ID, from /tasks/
    xp: 100 # Task XP required to finish this task
task-amount: 1 # Tasks required from the list, e.g. 2 of 3 options
```

:::tip Troubleshooting
- **Quest doesn't appear in a category?** The quest ID isn't listed under that category's `quests:`. Add it.
- **Quest never completes?** A task ID under `tasks:` doesn't match a file in `/tasks/`, or `task-amount` is higher than the number of tasks. Check both.
- **Config won't load after editing?** The file name (the ID) has invalid characters. Use lowercase letters, numbers, and underscores only.
:::

<hr/>

## Where to go next

- **Add tasks:** [Configuring a task](configuring-a-task) defines what each task tracks.
- **Group quests:** [Configuring a category](configuring-a-category) is where you list this quest.
- **Item syntax:** the [Item Lookup System](https://plugins.auxilor.io/the-item-lookup-system) covers the display icon.
- **Defaults:** the shipped quests live [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/quests).
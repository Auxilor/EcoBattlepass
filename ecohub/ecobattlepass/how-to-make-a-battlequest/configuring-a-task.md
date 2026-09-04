---
title: "Configuring a Task"
sidebar_position: 3
---

A **task** is the trackable action behind a quest, e.g. "kill 100 chickens". Each task is one config file with a **display** and one or more **XP gain methods** that turn in-game triggers into progress. This page builds one from scratch.

## Quick start

1. Open the `/plugins/EcoBattlepass/tasks/` folder and copy `_example.yml` to a new file, e.g. `chickens.yml`. The file name is the task ID.
2. Set the `display` `display-name` and `lore` shown in the quest.
3. Under `xp-gain-methods:`, set a `trigger`, a `value` or `multiplier`, and optional `filters`. See the triggers list.
4. Reference the task ID from a [quest](configuring-a-quest), run `/ecobattlepass reload`, and confirm the action raises the task's progress in game.

:::tip
`_example.yml` is included as a reference and is **never loaded**, so copy or rename it to make a real task. You can also organise tasks into subfolders inside `tasks/`, and they'll still load.
:::

## Naming and IDs

The file name without `.yml` is the task ID. You use this ID in your quests.

:::warning ID rules
IDs may only contain lowercase letters, numbers, and underscores (a-z, 0-9, _). No spaces, capitals, or hyphens, or the task will not load.
:::

## The structure of a task

| Part | What it controls |
| --- | --- |
| **Display** | The name and lore shown for the task in the quest |
| **XP gain methods** | The triggers that turn in-game actions into task progress |

```yaml
# === Display: what shows in the quest ===
display:
  display-name: Chickens # Task name
  lore:
    - "&7Kill chickens"
    - "&7%current_task_xp%/%required_task_xp%"

# === XP gain methods: how progress is earned ===
xp-gain-methods:
  - trigger: kill # The libreforge trigger to listen for
    value: 1 # Flat progress per trigger; use "multiplier" to scale the trigger's value
    filters: # Optional; restrict which triggers count
      entities:
        - chicken
```

### Display

The `display` block is the task's name and lore inside the quest.

```yaml
display:
  display-name: Chickens # Task name
  lore:
    - "&7Kill chickens"
    - "&7%current_task_xp%/%required_task_xp%"
```

### XP gain methods

Each method takes a trigger, a count, optional conditions, args, and filters. Use `value` for a flat count per trigger, or `multiplier` to scale the value the trigger produces.

```yaml
xp-gain-methods:
  - trigger: kill # See the triggers list for all options
    value: 1 # Use "multiplier" instead to scale the trigger's value
    filters: # Optional; here, only chickens count
      entities:
        - chicken
```

:::info Value versus multiplier
`value` adds a fixed amount each time the trigger fires. `multiplier` instead takes the number the trigger produces (e.g. blocks mined) and scales it. Use one or the other.
:::

## Internal placeholders

| Placeholder | Value | Options |
| --- | --- | --- |
| `%current_task_xp%` | The current task XP. | add `_formatted` for commas |
| `%required_task_xp%` | The required task XP. | add `_formatted` for commas |

:::tip Troubleshooting
- **Task never progresses?** The `trigger` is wrong, or a `filter` excludes your action. Check the trigger against the triggers list and loosen the filter.
- **Task missing from a quest?** The task ID isn't listed under that quest's `tasks:`. Add it.
- **Config won't load after editing?** The file name (the ID) has invalid characters. Use lowercase letters, numbers, and underscores only.
:::

<hr/>

## Where to go next

- **Use the task:** [Configuring a quest](configuring-a-quest) is where you attach task IDs.
- **Triggers:** the triggers list covers every `trigger` you can use.
- **Defaults:** the shipped tasks live [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/tasks).
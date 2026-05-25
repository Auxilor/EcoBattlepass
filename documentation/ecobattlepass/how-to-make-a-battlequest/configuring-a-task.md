---
title: Configuring a Task
sidebar_position: 3
---

## How to add tasks

Each task is its own config file, placed in the `/tasks/` folder, and you can add or remove them as you please. There's an example config called `_example.yml` to help you out!

The ID of the Task is the file name. This is what you use in your Quests.\
ID's must be lowercase letters, numbers, and underscores only.

## Example Task Config

```yaml
display:
  display-name: Chickens
  lore:
    - "&7Kill chickens"
    - "&7%current_task_xp%/%required_task_xp%"

xp-gain-methods:
  - trigger: kill
    value: 1
    filters:
      entities:
        - chicken
```

### Understanding the Sections

#### The Display Section

```yaml
display:
  # The display name and lore of the task, this shows in the Quest.
  display-name: Chickens
  lore:
    - "&7Kill chickens"
    - "&7%current_task_xp%/%required_task_xp%"
```

#### The XP Gain Methods

```yaml
# An XP gain method takes a trigger, a multiplier, conditions, args and filters.
# The 'multiplier' takes the value produced by the trigger and multiplies it
# Alternatively, you can use 'value' to count a specific number and not a multiplier
xp-gain-methods:
  - trigger: kill
    value: 1 # You can also use "multiplier" here.
    filters: # Optional
      entities:
        - chicken
```

Read [here](https://plugins.auxilor.io/effects/all-triggers) for the triggers.

#### Internal Placeholders

| Placeholder          | Value                 | Options                     |
| -------------------- | --------------------- | --------------------------- |
| `%current_task_xp%`  | The current task xp.  | add `_formatted` for commas |
| `%required_task_xp%` | The required task xp. | add `_formatted` for commas |

<hr/>

## Default Configs
The default configs can be found [here](https://github.com/Auxilor/EcoBattlepass/tree/master/eco-core/core-plugin/src/main/resources/tasks).
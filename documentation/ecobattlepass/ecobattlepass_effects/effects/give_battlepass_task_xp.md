# `give_battlepass_task_xp`
:::infoRequires:
EcoBattlepass
:::

:::dangerTriggered Effect
This effect requires a [Trigger](https://plugins.auxilor.io/effects/all-triggers) to activate.
:::

Gives experience points for a task in a quest, excluding multipliers.

## Example Config

```yaml
- id: give_battlepass_task_xp
  args:
    amount: 100 # The amount of xp to give
    task: chickens # The task ID
    quest: daily_quest_1 # The quest ID
    battlepass: example # The battlepass ID
  ...other config (eg triggers, filters, mutators, etc)
```

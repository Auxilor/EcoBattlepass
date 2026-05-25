# `battlepass_task_xp_multiplier`
:::infoRequires:
EcoBattlepass
:::

:::dangerPermanent Effect
This effect is permanent and does not require a trigger.
:::

Multiplies Battlepass Task XP gain

## Example Config

```yaml
- id: battlepass_task_xp_multiplier
  args:
    multiplier: 2 # The multiplier for battlepass task xp gain
    battlepasses: # The list of battlepasses to multiply XP for
      - example
```


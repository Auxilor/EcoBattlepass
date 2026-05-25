# `battlepass_xp_multiplier`
:::infoRequires:
EcoBattlepass
:::

:::dangerPermanent Effect
This effect is permanent and does not require a trigger.
:::

Multiplies Battlepass XP gain

## Example Config

```yaml
- id: battlepass_xp_multiplier
  args:
    multiplier: 2 # The multiplier for battlepass xp gain
    battlepasses: # The list of battlepasses to multiply XP for
     - example
```

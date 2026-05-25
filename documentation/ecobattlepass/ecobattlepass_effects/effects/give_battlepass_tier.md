# `give_battlepass_tier`
:::infoRequires:
EcoBattlepass
:::

:::dangerTriggered Effect
This effect requires a [Trigger](https://plugins.auxilor.io/effects/all-triggers) to activate.
:::

Give battlepass tiers to the player

## Example Config

```yaml
- id: give_battlepass_tier
  args:
    amount: 2 # The amount of tiers to give
    battlepass: example # The battlepass ID
  ...other config (eg triggers, filters, mutators, etc)
```

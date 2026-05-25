# `give_battlepass_xp`
:::infoRequires:
EcoBattlepass
:::

:::dangerTriggered Effect
This effect requires a [Trigger](https://plugins.auxilor.io/effects/all-triggers) to activate.
:::

Gives battlepass experience points

## Example Config

```yaml
- id: give_battlepass_xp
  args:
    amount: 100 # The amount of xp to give
    battlepass: example # The battlepass ID
  ...other config (eg triggers, filters, mutators, etc)
```

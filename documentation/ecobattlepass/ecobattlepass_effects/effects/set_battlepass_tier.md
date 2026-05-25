# `set_battlepass_tier`
:::infoRequires:
EcoBattlepass
:::

:::dangerTriggered Effect
This effect requires a [Trigger](https://plugins.auxilor.io/effects/all-triggers) to activate.
:::

Set the player's battlepass tier

## Example Config

```yaml
- id: set_battlepass_tier
  args:
    tier: 10 # The tier to set
    battlepass: example # The battlepass ID
  ...other config (eg triggers, filters, mutators, etc)
```

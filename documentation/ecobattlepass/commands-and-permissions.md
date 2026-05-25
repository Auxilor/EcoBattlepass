---
title: "Commands and Permissions"
sidebar_position: 5
---

| Command                                                                                                                                                        | Description                                                     | Permission                           |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|--------------------------------------|
| `/<battlepass_id>`                                                                                                                                             | Open the battlepass GUI                                         | `ecobattlepass.command.<pass_id>`    |
| `/<battlepass_id> claim <tier\|all> [free\|premium]`                                                                                                           | Claim rewards for a specific tier or all available tiers. Optionally restrict to `free` or `premium` rewards only | `ecobattlepass.command.<pass_id>` |
| `/ecobattlepass reload`                                                                                                                                        | Reloads the plugin                                              | `ecobattlepass.command.reload`       |
| `/ecobattlepass quests <pass_id>`                                                                                                                              | Opens the Quests GUI                                            | `ecobattlepass.command.quests`       |
| `/ecobattlepass tier <pass_id>`                                                                                                                                | Opens the Tiers GUI                                             | `ecobattlepass.command.tier`         |
| `/ecobattlepass setpremium <player> <pass_id> <true/false> [silent]`                                                                                           | Set the player's premium pass                                   | `ecobattlepass.command.setpremium`   |
| `/ecobattlepass complete_task <player/all> <pass_id> <category_id> <quest_id> <task_id>`                                                                       | Complete the player's battlepass task                           | `ecobattlepass.command.completetask` |
| `/ecobattlepass reset battlepass <player/all> <pass_id>` <br/> `/ecobattlepass reset task <player/all> <pass_id> <category_id> <quest_id> <task_id>`           | Reset the player's battlepass or task experience                | `ecobattlepass.command.reset`        |
| `/ecobattlepass give <player/all> <pass_id> <tier/xp> <amount>`   <br/> `/ecobattlepass give <player/all> task_xp <category_id> <quest_id> <task_id> <amount>` | Give the player battlepass tiers, experience or task experience | `ecobattlepass.command.give`         |


### Additional Permissions

| Permission                               | Description                                                                                                |
|------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `ecobattlepass.xpmultiplier.<%increase>` | Multiply battlepass XP gain. The math is `1 + (<%increase> / 100)`. Example: `200` = 3x XP, `50` = 1.5x XP |
| `ecobattlepass.xpmultiplier.50percent`   | Gives 50% more battlepass XP (1.5x multiplier)                                                             |
| `ecobattlepass.xpmultiplier.double`      | Gives double battlepass XP (2x multiplier)                                                                 |
| `ecobattlepass.xpmultiplier.triple`      | Gives triple battlepass XP (3x multiplier)                                                                 |
| `ecobattlepass.xpmultiplier.quadruple`   | Gives quadruple battlepass XP (4x multiplier)                                                              |

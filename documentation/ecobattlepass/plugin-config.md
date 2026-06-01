---
title: "Plugin Config"
sidebar_position: 8
---

The main config file is `config.yml`, found at `/plugins/EcoBattlepass/config.yml`. It controls the plugin-wide GUIs (the battlepass, tiers, categories, and quests menus), the quest icon, time and date formats, and sounds. After editing, run `/ecobattlepass reload` to apply your changes.

## Default config.yml

```yaml
# The amount of time (in milliseconds) that the GUI icons should be cached for
# Prevents spamming the GUI to cause lag
gui-cache-ttl: 1000

# GUI Configuration for the Battlepass GUI (/battlepass)
battlepass-gui:
  title: "EcoBattlepass"
  mask:
    # The way the mask works is by having a list of materials
    # And then a pattern to use those materials.

    # The pattern is the rows in the GUI
    # Each line must be 9 long, and the amount of rows should be the amount of rows in the GUI
    # A zero represents nothing
    # A 1 represents the first material
    # A 2 represents the second material
    # And so on, you can add up to 9.
    materials:
      - black_stained_glass_pane
    pattern:
      - "111111111"
      - "111010111"
      - "111111111"

  buttons:
    tiers:
      item: lime_stained_glass_pane
      name: "&aTiers"
      lore:
        - ""
        - "&aLeft click &7to see your"
        - "&7battlepass progression"
        - "&7and claim rewards"
        - ""
        - "&7Tier: &a%tier%"
        - "&7XP: &a%current_bp_xp%&8/&a%required_bp_xp%"
        - "&7Progress: &a%percentage_progress%%"
        - "&7Claimable Tiers: &a%claimable_tiers%"
      location:
        row: 2
        column: 4

    # List of available placeholders:
    # https://plugins.auxilor.io/ecobattlepass/internalplaceholders#battlepass-and-category-gui

    quests:
      item: orange_stained_glass_pane
      name: "&6Quests"
      lore:
        - ""
        - "&6Left click &7to see"
        - "&7all available quests"
        - "&7and your progression"
        - ""
      location:
        row: 2
        column: 6

    close:
      enabled: true
      item: barrier
      name: "&cClose"
      location:
        row: 3
        column: 5

    # Custom GUI slots; see here for a how-to: https://plugins.auxilor.io/all-plugins/custom-gui-slots
    custom-slots: []

# GUI Configuration for the Tiers GUI (/battlepass tiers)
tiers-gui:
  # Supports %page% and %max_page% placeholders
  title: "EcoBattlepass: Tiers (%page%/%max_page%)"

  # Layout mode:
  # "combined" - Original behavior, free and premium rewards shown on the same row.
  # "split"    - Free rewards on one row, premium rewards on another (uses free-pattern/premium-pattern below).
  layout: "combined"

  # How to display tiers that have no rewards:
  # "normal"              - Show with their real state (original behavior)
  # "hidden"              - All empty tiers are invisible
  # "hidden-behind-level" - Empty tiers at or below the player's level are hidden; others show normally
  # "behind-level"        - Empty tiers at or below the player's level show as claimed; others show normally
  # "all"                 - All empty tiers show as claimed regardless of level
  empty-tier-display-mode: "normal"

  # Whether to open the tiers GUI at the player's current tier page.
  # Set to false to always open at page 1.
  open-at-current-tier: true

  mask:
    # The way the mask works is by having a list of materials
    # And then a pattern to use those materials.

    # The pattern is the rows in the GUI
    # Each line must be 9 long, and the amount of rows should be the amount of rows in the GUI
    # A zero represents nothing
    # A 1 represents the first material
    # A 2 represents the second material
    # And so on, you can add up to 9.
    materials:
      - black_stained_glass_pane
    pattern:
      - "111111111"
      - "111111111"
      - "111111111"
      - "111111111"
      - "111111111"
      - "111111111"
    progression-pattern:
      # To set the order of the pattern,
      # Use 1-9 and then a-z: a goes after 9.
      # Used when layout is "combined" (default).
      - "109ab0jkl"
      - "2080c0i0m"
      - "3070d0h0n"
      - "4560efg0o"
      - "00000000p"
      - "00000000q"

  # Split layout configuration (only used when layout: split)
  split:
    free-pattern:
      - "000000000"
      - "123456789"
      - "000000000"
      - "000000000"
      - "000000000"
      - "000000000"
    premium-pattern:
      - "000000000"
      - "000000000"
      - "000000000"
      - "123456789"
      - "000000000"
      - "000000000"

  buttons:
    # The amount of the item as a function of the level
    item-amount: "%level%"
    # For example, increasing every 10 levels would be "ceil((%level% + 1) / 10)"
    # The value is always rounded down.

    # Maximum item stack size for tier items (1-99).
    # Values above 64 require Paper 1.20.5+.
    max-item-amount: 64

    # Page changers support active/inactive states for when there are/aren't more pages.
    # The "item" and "name" keys each accept "active" and "inactive" sub-keys.
    # Legacy format (single material + name) is still supported for backward compatibility.
    prev-page:
      item:
        active: orange_stained_glass_pane
        inactive: gray_stained_glass_pane
      name:
        active: "&aPrevious page"
        inactive: "&7No more pages"
      lore:
        active: []
        inactive: []
      location:
        row: 6
        column: 4

    next-page:
      item:
        active: orange_stained_glass_pane
        inactive: gray_stained_glass_pane
      name:
        active: "&aNext Page"
        inactive: "&7No more pages"
      lore:
        active: []
        inactive: []
      location:
        row: 6
        column: 6

    close:
      enabled: true
      item: barrier
      name: "&cClose"
      location:
        row: 6
        column: 5

    empty-rewards-format: "&8&oNo rewards for this tier!"
    free-rewards-format: "&8»&r &9%reward%"
    premium-rewards-format: "&8»&r &6%reward%"
    missing-premium-rewards-format: "&c&l✘ &6%reward%"
    claimed-free-rewards-format: "&a&l✔ &9%reward%"
    claimed-premium-rewards-format: "&a&l✔ &6%reward%"

    # List of available placeholders:
    # https://plugins.auxilor.io/ecobattlepass/internalplaceholders#battlepass-tiers-gui

    # The different states a tier can be in
    # When only the free rewards are claimable.
    unlocked-free:
      item: lime_stained_glass_pane
      name: "&aTier %tier_numeral%"
      lore:
        - "&7Rewards:"
        - "&7Free Tier:"
        - "%free-rewards%"
        - "&7Premium Tier:"
        - "%premium-rewards%"
        - ""
        - "&aCLAIM"

    # When both the free and premium rewards are claimable.
    unlocked:
      item: lime_stained_glass_pane
      name: "&aTier %tier_numeral%"
      lore:
        - "&7Rewards:"
        - "&7Free Tier:"
        - "%free-rewards%"
        - "&7Premium Tier:"
        - "%premium-rewards%"
        - ""
        - "&aCLAIM"

    # When the free rewards have been claimed, but the premium rewards are not claimable due to missing premium.
    premium-required:
      item: orange_stained_glass_pane
      name: "&aTier %tier_numeral%"
      lore:
        - "&7Rewards:"
        - "&7Free Tier:"
        - "%claimed-free-rewards%"
        - "&7Premium Tier:"
        - "%premium-rewards%"
        - ""
        - "&6PURCHASE THE BATTLEPASS TO CLAIM PREMIUM REWARDS"

    # When the tier is not claimable yet, but is the next tier to be claimed.
    in-progress:
      item: yellow_stained_glass_pane
      name: "&eTier %tier_numeral%"
      lore:
        - "&7Rewards:"
        - "&7Free Tier:"
        - "%free-rewards%"
        - "&7Premium Tier:"
        - "%premium-rewards%"
        - ""
        - "&fProgress:"
        - "&8» &e%percentage_progress%%"
        - "&8» &e%current_bp_xp%&8/&7%required_bp_xp% &fXP"

    # When the tier is not claimable yet, and is not the next tier to be claimed.
    locked:
      item: red_stained_glass_pane
      name: "&cTier %tier_numeral%"
      lore:
        - "&7Rewards:"
        - "&7Free Tier:"
        - "%free-rewards%"
        - "&7Premium Tier:"
        - "%premium-rewards%"
        - ""
        - "&cLOCKED"

    # When all rewards have been claimed.
    claimed:
      item: green_stained_glass_pane glint
      name: "&aTier %tier_numeral%"
      lore:
        - "&7Rewards:"
        - "&7Free Tier:"
        - "%claimed-free-rewards%"
        - "&7Premium Tier:"
        - "%claimed-premium-rewards%"
        - ""
        - "&aCLAIMED"

    # Split layout button configurations (only used when layout: split)
    # These define the appearance of free and premium track buttons independently.
    free-track:
      unlocked:
        item: lime_stained_glass_pane
        name: "&aFree Tier %tier_numeral%"
        lore:
          - "&7Free Rewards:"
          - "%free-rewards%"
          - ""
          - "&aCLAIM"
      locked:
        item: red_stained_glass_pane
        name: "&cFree Tier %tier_numeral%"
        lore:
          - "&7Free Rewards:"
          - "%free-rewards%"
          - ""
          - "&cLOCKED"
      in-progress:
        item: yellow_stained_glass_pane
        name: "&eFree Tier %tier_numeral%"
        lore:
          - "&7Free Rewards:"
          - "%free-rewards%"
          - ""
          - "&fProgress:"
          - "&8» &e%percentage_progress%%"
      claimed:
        item: green_stained_glass_pane glint
        name: "&aFree Tier %tier_numeral%"
        lore:
          - "&7Free Rewards:"
          - "%claimed-free-rewards%"
          - ""
          - "&aCLAIMED"

    premium-track:
      unlocked:
        item: cyan_stained_glass_pane
        name: "&bPremium Tier %tier_numeral%"
        lore:
          - "&7Premium Rewards:"
          - "%premium-rewards%"
          - ""
          - "&aCLAIM"
      premium-required:
        item: orange_stained_glass_pane
        name: "&6Premium Tier %tier_numeral%"
        lore:
          - "&7Premium Rewards:"
          - "%premium-rewards%"
          - ""
          - "&6PURCHASE THE BATTLEPASS TO CLAIM"
      locked:
        item: red_stained_glass_pane
        name: "&cPremium Tier %tier_numeral%"
        lore:
          - "&7Premium Rewards:"
          - "%premium-rewards%"
          - ""
          - "&cLOCKED"
      in-progress:
        item: yellow_stained_glass_pane
        name: "&ePremium Tier %tier_numeral%"
        lore:
          - "&7Premium Rewards:"
          - "%premium-rewards%"
          - ""
          - "&fProgress:"
          - "&8» &e%percentage_progress%%"
      claimed:
        item: green_stained_glass_pane glint
        name: "&aPremium Tier %tier_numeral%"
        lore:
          - "&7Premium Rewards:"
          - "%claimed-premium-rewards%"
          - ""
          - "&aCLAIMED"

    # Custom GUI slots; see here for a how-to: https://plugins.auxilor.io/all-plugins/custom-gui-slots
    custom-slots: []

# GUI Configuration for the Categories GUI (/battlepass quests)
categories-gui:
  title: "&8Categories: %page%"
  mask:
    # The way the mask works is by having a list of materials
    # And then a pattern to use those materials.

    # The pattern is the rows in the GUI
    # Each line must be 9 long, and the amount of rows should be the amount of rows in the GUI
    # A zero represents nothing
    # A 1 represents the first material
    # A 2 represents the second material
    # And so on, you can add up to 9.
    materials:
      - black_stained_glass_pane
    pattern:
      # For this GUI, "c" represents a slot for a category.
      # Categories are sorted in the order they are in the categories.yml file.
      - "111111111"
      - "1c1c1c1c1"
      - "1111c1111"
      - "111111111"
      - "111111111"

  buttons:
    next-page:
      item:
        active: orange_stained_glass_pane
        inactive: gray_stained_glass_pane
      name:
        active: "&aNext page"
        inactive: ""
      lore:
        active: []
        inactive: []
      row: 5
      column: 6
    prev-page:
      item:
        active: orange_stained_glass_pane
        inactive: gray_stained_glass_pane
      name:
        active: "&aPrevious page"
        inactive: ""
      lore:
        active: []
        inactive: []
      row: 5
      column: 4
    close:
      enabled: true
      item: barrier
      name: "&cClose"
      row: 5
      column: 5

    # List of available placeholders:
    # https://plugins.auxilor.io/ecobattlepass/internalplaceholders#battlepass-and-category-gui

    # Custom GUI slots; see here for a how-to: https://plugins.auxilor.io/all-plugins/custom-gui-slots
    custom-slots: []

# GUI Configuration for the Quests GUI
quests-gui:
  title: "&8%category%: %page%"
  mask:
    # The way the mask works is by having a list of materials
    # And then a pattern to use those materials.

    # The pattern is the rows in the GUI
    # Each line must be 9 long, and the amount of rows should be the amount of rows in the GUI
    # A zero represents nothing
    # A 1 represents the first material
    # A 2 represents the second material
    # And so on, you can add up to 9.
    materials:
      - black_stained_glass_pane name:" "
    pattern:
      # For this GUI, "q" represents a slot for a quest.
      # Quests are sorted in the order they are in the categories.yml file.
      - "111111111"
      - "1q1q1q1q1"
      - "11q1q1q11"
      - "111111111"
      - "111111111"

  buttons:
    next-page:
      item:
        active: orange_stained_glass_pane
        inactive: black_stained_glass_pane
      name:
        active: "&aNext page"
        inactive: " "
      lore:
        active: []
        inactive: []
      row: 5
      column: 6
    prev-page:
      item:
        active: orange_stained_glass_pane
        inactive: black_stained_glass_pane
      name:
        active: "&aPrevious page"
        inactive: " "
      lore:
        active: []
        inactive: []
      row: 5
      column: 4
    close:
      enabled: true
      item: barrier
      name: "&cClose"
      row: 5
      column: 5

    # List of available placeholders:
    # https://plugins.auxilor.io/ecobattlepass/internalplaceholders#quest-gui

    # Custom GUI slots; see here for a how-to: https://plugins.auxilor.io/all-plugins/custom-gui-slots
    custom-slots: []

# GUI Configuration for the Quest Icon
quests-icon:
  timer-format:
    start: "Starts in %time%"
    end: "Ends in %time%"
    reset: "Resets in %time%"
    none: "One-Time quest (does not end or reset)"
  tasks-format:
    - "&a%task%"
    - " %task_lore%"
  tasks-separator: []
  name: "&6%quest_name%" # The name of the quest
  lore:
    - "&7%quest_description%" # This shows the description of the quest
    - ""
    - "&a%quest_tasks%" # This shows the tasks and their progress
    - ""
    - "&7%quest_tier%" # Premium or Both
    - "&7%quest_timer%" # This shows either: When it unlocks, when it started, or the time until it resets.

time-format:
  split: " "
  days: "&6%value%&fd"
  hours: "&6%value%&fh"
  minutes: "&6%value%&fm"
  seconds: "&6%value%&fs"

# Date format patterns (case-sensitive):
# d = day, M = month, y = year, H = hour (0-23), m = minute, s = second
# Example: "dd/MM/yyyy HH:mm:ss" → 31/05/2025 14:07:30
date-format: "yyyy-MM-dd HH:mm"

sound:
  tier-up:
    enabled: true # Whether the sound is enabled
    sound: block.anvil.use # The sound to play
    volume: 1 # The volume of the sound
    pitch: 1 # The pitch of the sound
  reward-claim:
    enabled: true
    sound: block.anvil.use
    volume: 1
    pitch: 1
  reward-locked:
    enabled: true
    sound: entity.villager.no
    volume: 1
    pitch: 1
  premium-required:
    enabled: true
    sound: entity.villager.no
    volume: 1
    pitch: 1
  premium-unlocked:
    enabled: true
    sound: ui_toast_challenge_complete
    volume: 1
    pitch: 1
  quest-complete:
    enabled: true
    sound: block.anvil.use
    volume: 1
    pitch: 1
  gui-click-sound:
    enabled: true
    sound: ui_button_click
    volume: 1
    pitch: 1
```

<hr/>

## Where to go next

- **Placeholders:** [Internal placeholders](internalplaceholders) lists everything usable in the GUI lore and names above.
- **Build a pass:** [How to make a battlepass](how-to-make-a-battlepass) covers the per-pass configs these GUIs display.
- **Custom GUI slots:** the [custom GUI slots](https://plugins.auxilor.io/all-plugins/custom-gui-slots) guide covers the `custom-slots` option.


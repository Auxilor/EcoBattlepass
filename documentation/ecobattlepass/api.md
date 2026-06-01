---
title: "API"
sidebar_position: 9
---

This page is for developers who want to hook into EcoBattlepass from their own plugin. EcoBattlepass is open-source, so you can read the implementation and build against its API.

## Source code

The source code is on GitHub [here](https://github.com/Auxilor/EcoBattlepass).

## Adding the dependency

1. Add the Auxilor repository and the EcoBattlepass dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://repo.auxilor.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.willfp:EcoBattlepass:<version>")
}
```

2. Replace `<version>` with the latest release. The latest version available on the repo can be found [here](https://github.com/Auxilor/EcoBattlepass/tags).

<hr/>

## Where to go next

- **Shared APIs:** the [eco framework](https://github.com/Auxilor/eco) is where the shared eco APIs live.
- **Config side:** [How to make a battlepass](how-to-make-a-battlepass) covers the config that the API drives.
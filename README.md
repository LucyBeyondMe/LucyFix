# LucyFix — Source Code

**by LucyBeyondMe** | Fabric 1.20.1 | Version 1.0.1

LucyFix removes arbitrary friction in modern Minecraft, restoring gameplay loops and progression to natural, intuitive systems.

\---

## What's in 1.0.0

LucyFix 1.0.1: Descriptions corrected, functionality is the same.
The prior release of 1.0.0 had incorrect descriptions in the source code. This release overwrites that with identical functionality to 1.0.0.
XP orbs no longer spawn.
Enchanting now requires only 1-3 lapis, scaling with the power of enchants.
Combining books in anvils now requires only lapis, scaling with the amount and power of enchants.
Repairing items in anvils now requires only the base material.
Upgrading to netherite no longer requires a netherite upgrade.

\---

## How to build

### Requirements

* Java JDK 17
* Gradle wrapper files from: https://github.com/FabricMC/fabric-example-mod/tree/1.20.1
(copy `gradlew`, `gradlew.bat`, and the `gradle/` folder into this directory)

### Build

```
gradlew.bat build        (Windows)
./gradlew build          (Mac/Linux)
```

### Output

```
build/libs/lucyfix-1.0.0.jar
```

### Install

1. Install Fabric loader for 1.20.1: https://fabricmc.net/use/installer/
2. Download Fabric API for 1.20.1: https://modrinth.com/mod/fabric-api
3. Drop both JARs into your mods folder

\---

## Project structure

```
lucyfix/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/main/
│   ├── java/com/lucybeyondme/lucyfix/
│   │   ├── LucyFix.java                        (entrypoint)
│   │   └── mixin/
│   │       ├── ExperienceOrbMixin.java          (XP orb suppression)
│   │       ├── PlayerEntityMixin.java           (XP gain suppression)
│   │       ├── AnvilScreenHandlerMixin.java     (anvil rework)
│   │       ├── EnchantmentScreenHandlerMixin.java (enchanting table)
│   │       ├── SmithingTransformRecipeMixin.java  (netherite template)
│   │       ├── InGameHudMixin.java              (client: hide XP bar)
│   │       └── EnchantmentScreenMixin.java      (client: show lapis cost)
│   └── resources/
│       ├── fabric.mod.json
│       ├── lucyfix.mixins.json
│       └── data/minecraft/recipes/             (netherite smithing overrides ×9)
```

\---

## License

MIT

LucyFix by LucyBeyondMe. Not affiliated with Mojang or Microsoft.


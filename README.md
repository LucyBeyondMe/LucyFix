# LucyFix — Source Code

**by LucyBeyondMe** | Fabric 1.20.1 | Version 1.0.0

LucyFix removes arbitrary friction in modern Minecraft, restoring gameplay loops and progression to natural, intuitive systems.

\---

## What's in 1.0.0

* **XP removed** — XP orbs never spawn. The experience bar is gone from the HUD. Mending still exists as an enchantment but has no function.
* **Enchanting table rework** — No XP cost. Lapis is the sole cost, scaling by slot: 1 / 2 / 3 lapis. Vanilla handles lapis consumption.
* **Anvil rework** — No XP cost, no prior work penalty, no Too Expensive cap. Material repairs are free. Applying enchanted books costs lapis from inventory, scaling with total enchantment level (sum of levels × 3).
* **Netherite upgrade template removed** — Diamond gear + netherite ingot = netherite gear. No template required. Armor trims unaffected.

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


# Sugar and Spice

<a href='https://neoforged.net/'><img alt="neoforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg"></a>

Sugar and Spice is a 1.21.1 NeoForge modpack currently in development. This repo incorporates both the Sugar and Spice
modpack and its core mod, Snips and Snails.

## Building the Modpack

To build the modpack and download resource packs, run the following command to build the modpack to build/libs.

```sh
./gradlew mrpack # 
```

## Running the Modpack

```sh
./gradlew mod:runClient 
```

The following command publishes the modpack to Modrinth, assuming you have proper publishing permissions set up in
`.env`.

```sh
:publishModrinth
```

## Adding Mods

Mods can be added to the `mods` folder by creating a JSON file with the following syntax. This format accepts both Curse
and Modrinth maven links, but try to always use Modrinth versions so that Modrinth can approve the pack more easily.

```json
{
  "maven": "maven.modrinth:create-alloyed:3.0.4+1.21.1-neoforge"
}
```

If you need to modify the mod, add a compile-only dependency to `mod/build.gradle`

```groovy
compileOnly "maven.modrinth:create:$create_version"
```

## License

[![Asset license (Unlicensed)](https://img.shields.io/badge/assets%20license-All%20Rights%20Reserved-red.svg?style=flat-square)](https://en.wikipedia.org/wiki/All_rights_reserved)
[![Code license (MIT)](https://img.shields.io/badge/code%20license-MIT-green.svg?style=flat-square)](https://github.com/cassiancc/Raspberry-Core/blob/main/LICENSE.txt)

If you are thinking about using the code or assets from Sugar and Spice or Snips and Snails, please note the project's
licensing. **All assets of this project are unlicensed and all rights are reserved to them by their respective
authors.** The source code of the Snips and Snails mod for Minecraft 1.21.1 is available under the MIT license.

## Credits

Buildscript setup adapted from cassiancc's [Lemonade](https://github.com/cassiancc/Lemonade), which was itself inspired
by modmuss50's Holiday Server Pack. Code used under its [MIT License](https://github.com/modmuss50/holiday-server-pack).

Snips and Snails contains code from [Better Log4j Config](https://modrinth.com/mod/better-log4j-config), used under
its [Apache License 2.0](https://github.com/BigWingBeat/better_log4j_config/blob/fabric/LICENSE).

---

[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-plural_vector.svg)](https://discord.com/invite/JcGRdT6Pbx) [![github-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/github-plural_vector.svg)](https://github.com/evanbones/Sugar-And-Spice)

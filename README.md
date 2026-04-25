# Sugar and Spice

<a href='https://neoforged.net/'><img alt="neoforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg"></a>

Sugar and Spice is a 1.21.1 NeoForge modpack currently in development. This repo incorporates both the Sugar
and Spice modpack and its core mod, Snips and Snails, into a single unified workspace.

## Setting Up the Workspace (Prism Launcher)

To seamlessly bridge the coremod and the modpack, we use Prism Launcher as our primary mod manager. We do this by
creating a **symlink** that points Prism's instance folder directly to the Gradle run environment.

1. Create a new 1.21.1 NeoForge instance in Prism Launcher named "Sugar and Spice".
2. Right-click the instance and select **Folder** to open the instance directory.
3. **Delete** the default `.minecraft` folder inside the instance directory.
4. Create a symlink that replaces the `.minecraft` folder and points to the `mod/run` directory in this repository.

**Windows (Command Prompt):**

```cmd
mklink /J "C:\Path\To\Prism\instances\Sugar and Spice\minecraft" "C:\Path\To\Repo\Sugar-And-Spice\mod\run"
```

Now, when you download or update mods via Prism, they are saved directly into `mod/run/mods`. Your configurations are
saved directly to `mod/run/config`.

## Running the Game

You can launch the game using Prism Launcher for casual testing, or run the following Gradle command to automatically
compile your coremod changes and launch the game:

```sh
./gradlew mod:runClient 
```

Gradle shares the `mod/run` folder with Prism, meaning all your downloaded mods and configs will be perfectly synced.

## Building and Releasing the Modpack

When you are ready to build a release `.mrpack` file:

1. Open Prism Launcher, right-click the instance, and click **Export Instance**.
2. Select Modrinth `.mrpack` format and export the zip file anywhere.
3. Open the exported zip and copy **only** the `modrinth.index.json` file into the root of this repository.
4. Run the following command:

```sh
./gradlew mrpack 
```

This task reads your `modrinth.index.json`, grabs your live configs from `mod/run/config`, compiles the
`Snips and Snails` coremod, and packages it all into a ready-to-publish `.mrpack` in `build/libs`.

Publish to Modrinth using:

```sh
./gradlew publishMods
```

## Modifying the Coremod (Snips and Snails)

If you need to compile against a specific mod's API, add a compile-only dependency to `mod/build.gradle`:

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

Snips and Snails contains code from [Better Log4j Config](https://modrinth.com/mod/better-log4j-config), used under
its [Apache License 2.0](https://github.com/BigWingBeat/better_log4j_config/blob/fabric/LICENSE).

Map colors are from [Remapped](https://github.com/Apollounknowndev/remapped), used under
its [MIT license](https://github.com/Apollounknowndev/remapped/blob/main/LICENSE).

---

[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-plural_vector.svg)](https://discord.com/invite/JcGRdT6Pbx) [![github-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/github-plural_vector.svg)](https://github.com/evanbones/Sugar-And-Spice)

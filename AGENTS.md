# Lumi Development Guide

These instructions apply to the whole repository. Check the current code, build files, and workflows before relying on a version or behavior described here. Update the relevant section when a change makes this guide inaccurate.

## Project Context

- Lumi is a Minecraft Bedrock Edition server core and a hard fork of Nukkit-MOT. Java code lives under `cn.nukkit`. The Maven group is `com.koshakmine`.
- Use Java 21 and the Gradle wrapper. The single Gradle project uses Kotlin DSL. The toolchain, dependencies, and packaging are defined in `build.gradle.kts`.
- Match Bedrock behavior. Adapt ports to Lumi's APIs, threading, data formats, and supported protocols. Use `.agents/Nukkit-MOT-Master` as a read-only reference when available. Preserve attribution and keep ports scoped to the requested behavior.
- `README.md` describes the project. `VERSIONING.md` defines branch and release policy. Routine work targets `dev`. Reserve `stable` for urgent release fixes.

## Source Map

Java paths below are relative to `src/main/java/cn/nukkit/`.

| Area | Entry points |
| --- | --- |
| Startup and ticks | `Nukkit`, `Server`, `scheduler/` |
| Player state and packet access | `Player`, `PlayerHandle` |
| Gameplay and content | `block/`, `blockentity/`, `item/`, `entity/`, `dispenser/` |
| Inventories and recipes | `inventory/`, `inventory/transaction/`, `inventory/request/`, `recipe/` |
| Registration and compatibility | `registry/Registries`, the individual registries, `item/RuntimeItems`, `level/GlobalBlockPalette`, and `Server` initialization order |
| Networking | `network/Network`, `network/PacketPool`, `network/session/`, `network/protocol/`, `network/process/`, `utils/BinaryStream` |
| Worlds and storage | `level/`, `level/format/leveldb/`, `level/format/generic/serializer/NetworkChunkSerializer` |
| Plugins and commands | `plugin/`, `event/`, `api/`, `command/`, `permission/` |
| Configuration | `settings/`, `settings/initializer/`, `settings/converter/LegacyPropertiesConverter`, `settings/transformer/` |

- `src/main/resources/gamedata/` contains versioned palettes, item runtime mappings and components, entity and biome data, and tags. `internal/` holds storage, legacy, and downgrade mappings. `recipes/` holds recipe data.
- Localize user-facing messages in `src/main/resources/lang/eng/lang.ini` and `src/main/resources/lang/rus/lang.ini`. Use the existing Log4j2 or server logger and keep hot paths quiet.
- The JUnit Jupiter suite lives in `src/test/java/cn/nukkit/`. Check `build.gradle.kts` for available test dependencies.

## Build and Validation

Use the repository root for Gradle commands. On Unix, use `./gradlew` in place of `.\gradlew.bat`.

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test --tests 'cn.nukkit.level.LevelTest'
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat shadowJar
```

- Use `compileJava` for a compile check, a focused test for an affected class, and `build` for CI-equivalent validation when appropriate. Consider the requested scope and risk before running a full suite. Add tests when requested, required, or needed to cover a regression the existing suite misses. Keep temporary checks out of source control.
- CI uses Java 21 and `./gradlew build`. `shadowJar` produces `build/libs/Lumi-<version>.jar` with `cn.nukkit.Nukkit` as the entry point. The ordinary `jar` task is disabled and depends on `shadowJar`.
- `releaseVersion` in `build.gradle.kts` defines the version. Builds default to `-SNAPSHOT`. Set `DEVELOPMENT=false` for a release build. Publishing, tagging, and other release actions require a release request.
- Preserve the Log4j2 plugin-cache transformer and the Shadow relocations from `org.densy.eventbus.api` to `cn.nukkit.event.bus` and `org.densy.eventbus.core` to `cn.nukkit.event.bus.impl`. Source imports use `org.densy.eventbus.*`.
- Report dependency-resolution failures separately from code failures. Keep repository URLs and dependency versions scoped to the task.
- For a runtime check, start the packaged JAR from a separate temporary working directory with disposable worlds, keys, and free ports. `Nukkit` uses `user.dir` for server data. Stop the smoke-test server with `stop`. Startup and compilation provide different coverage from a real Bedrock client.
- For documentation-only changes, review commands, paths, and the diff. In handoffs, state the checks actually performed and any important gaps.

## Working Rules and Java Style

- Inspect `git status` and the relevant staged and unstaged diffs before editing. Preserve existing work and avoid unrelated formatting, dependency changes, and generated output.
- Trace changes through their normal entry points, registrations, and lifecycle callbacks. Use existing extension points and keep code focused.
- Match the surrounding Java style. Use four spaces, same-line opening braces, `UpperCamelCase` types, `lowerCamelCase` methods and fields, and `UPPER_SNAKE_CASE` constants. Aim for 120 columns and wrap long code where it improves readability.
- Put declaration annotations on separate lines. Use appropriate visibility and keep intentional package-private APIs scoped to their packages. Prefer ordinary imports to fully qualified names in executable code.
- Keep one operation per line. Skip empty private constructors that serve no purpose. Follow local Lombok conventions and consider generated accessors and constructors when reviewing API changes.
- Write new comments in English. When porting code, translate Chinese comments and remove the original. If both languages are present, keep the English version. Preserve attribution and license notices.
- Use `//` for single-line comments. Add Javadoc only for a public contract that needs explanation. Format it with separate opening and closing lines.
- Use UTF-8 and LF for new Java files. Follow `.gitattributes` and preserve existing line endings in focused edits.
- Finish with `git diff --check` and review the files changed. Keep local worlds, players, plugins, resource packs, logs, settings, auth caches, keys, and build artifacts out of changes.

## API, Events, and Lifecycle

- Plugin-facing API includes `Server`, `Player`, entities, blocks, items, inventories, events, registries, and classes outside `api/`. Check compatibility with existing callers and the target release. Preserve source and binary compatibility for routine patch changes where practical.
- For intentional breaking changes, document the affected versions and APIs, old and new behavior, compatibility bridges, required data or configuration changes, and how to validate the migration. Use the target release's migration notes.
- `Event.call()` dispatches an event and returns whether it remains uncancelled. Preserve event timing, listener-modifiable values, and cancellation. Revalidate targets after callbacks that may move entities, change inventory state, or close a window.
- Preserve plugin load phases and dependency order. Inspect `Server` initialization and relevant registry or packet cache rebuilds when changing startup or registration.
- Determine which thread owns the state. Use the primary server thread for ordinary gameplay mutation. Run background work in `AsyncTask.onRun()` and apply results in `onCompletion(Server)` after checking that the relevant world, player, or entity is still valid.
- Keep disk and network I/O off the tick and Netty event loops where the existing lifecycle allows it. Bound scans, allocations, queues, and work per tick. Account for `tickDiff`, entity closure, world changes, and chunk unload.

## Protocol and Transport

- Keep Lumi's integer protocol model. The supported stable range starts at 1.20.0 and reaches 1.26.60 in this branch. Use `ProtocolInfo.CURRENT_PROTOCOL`, `SUPPORTED_PROTOCOLS`, and `LoginProcessor` for exact accepted versions, including previews and configured bounds.
- Check packet ID and registration in `Network` and `PacketPool`, wire order, field widths, signedness, optional flags, and version gates. Check inbound dispatch in `DataPacketManager` and processor `isSupported()`. Validate client-supplied sizes, indexes, coordinates, and actions before mutating state.
- Reuse `BinaryStream`, `Binary`, and the existing NBT helpers for packet codecs. Preserve buffer reuse, per-protocol batching, downgrade mappings, cache groups, and transport buffer ownership. Keep a transport's own Netty buffers where it needs them.
- Both RakNet and NetherNet are present. Trace shared code through `NetworkPlayerSession` and the relevant interface and session. For NetherNet, check `network.nethernet`, `general.server-udp-ports`, signaling, WebRTC media, identity binding, compression negotiation, and shutdown. Verify native packaging with the shaded JAR when changing transport dependencies.
- For blocks, items, and entities, check the relevant registry, IDs and identifiers, runtime and legacy mappings, palettes, component data, creative entries, recipes, and persistence as applicable. Keep resource updates and their readers compatible. Use structured tools for binary palette and NBT changes.

## Inventory and Transactions

- `general.server-authoritative-inventory` selects the mode for the entire server and defaults to `false` (CAI). Treat it as a startup setting because `StartGamePacket` advertises the mode at login. Use `player.isInventoryServerAuthoritative()` in player-facing code. Keep gameplay checks consistent with this shared setting, including proxied connections.
- SAI inventory mutations use `ItemStackRequestHandler` from standalone requests or `PlayerAuthInputPacket`. With SAI active, the legacy `InventoryTransactionProcessor` handles gameplay interactions such as use, attack, and release, and sends validated hotbar drops through SAI drop handling. CAI uses the legacy inventory transaction path and a dedicated `BundleRequestHandler` for supported bundle requests.
- Stack IDs belong to the tracked stack. The `BinaryStream` writers encode a positive stack ID when the layout includes one. Preserve the source stack's ID through protocol downgrades and distinguish stack descriptors from item instances. Allocate fresh IDs for distinct stacks through the existing inventory and request paths.
- For SAI changes, inspect `NetworkMapping`, request action validation, response slots, and `ItemStackRequestContext` rollback and commit handling. Keep ingredient consumption, result delivery, and side effects consistent when an action fails or a plugin closes the window.
- `TradeInventory` accepts offers supplied by the caller through `TradeInventoryRecipe`. Its SAI and CAI transaction paths share offer validation. Keep this infrastructure independent of villager professions, progression, and built-in offers.

## Worlds, Settings, and Commands

- On-disk LevelDB serializers and client chunk serializers have separate contracts. Preserve NBT types, defaults, stored versions, and old-world reads. Use disposable fixtures for migration and save/load checks.
- Derive height and coordinate limits from world or dimension data. Account for negative coordinates, chunk and section edges, and unloading.
- Put configuration defaults in the appropriate Okaeri model and use `@CustomKey` and `@Comment` as needed. The root `settings.yml` contains local runtime state. Review conversion and transformers when changing a key or type. The initializer removes orphaned keys, so key changes need an explicit migration path.
- For a command, check `SimpleCommandMap`, `DefaultPermissions`, parameter overloads, and localization. Follow the neighboring command tree and `CommandLogger` patterns.

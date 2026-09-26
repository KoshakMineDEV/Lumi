# Versioning and Release Policy

Lumi uses two main branches for development and releases: `stable` and `dev`.

## Branches

### `stable`

The `stable` branch contains release-ready code. Every push to this branch is treated as a stable release.

Stable versions use a normal semantic version format:

```text
MAJOR.MINOR.PATCH
```

Examples:

```text
1.6.0
1.6.1
1.7.0
```

Patch releases are used for regular minor fixes and small updates:

```text
1.6.0 -> 1.6.1
```

Minor releases are used for larger project-wide updates:

```text
1.6.9 -> 1.7.0
```

### `dev`

The `dev` branch contains the latest development version of the project.

Versions built from `dev` always use the current project version with the `-SNAPSHOT` suffix:

```text
{current project version}-SNAPSHOT
```

Example:

```text
1.6.0-SNAPSHOT
```

This means that `dev` builds are not considered final releases and may contain experimental or unfinished changes.

## Maven Repositories

The project is published to two Maven repositories depending on the source branch.

### Releases repository

Stable versions from the `stable` branch are published to:

```text
https://repo.lumi.su/releases
```

Only final release versions without the `-SNAPSHOT` suffix should be published here.

Example:

```text
1.6.0
1.6.1
1.7.0
```

### Snapshots repository

Development versions from the `dev` branch are published to:

```text
https://repo.lumi.su/snapshots
```

This repository always contains the most recent development builds with the `-SNAPSHOT` suffix.

Example:

```text
1.6.0-SNAPSHOT
```

## GitHub Releases

Every push to the `stable` branch creates a new GitHub Release for the current stable version.

Every push to either `stable` or `dev` also updates the `Latest Build` release. This release is used to provide the most recent build artifacts from the active development or stable branch.

## Pull Requests

Pull requests with experimental changes, new features, or non-critical updates should target the `dev` branch.

Pull requests should target the `stable` branch only when they contain critical fixes that must be released as soon as possible.

# Installation

## Requirements

- Paper `26.1.2`
- Java `25`

## Build

```powershell
gradle build
```

The plugin jar is created in:

```text
build/libs/
```

## Install On Server

1. Stop the Paper server.
2. Put the plugin jar into the server `plugins/` folder.
3. Start the server once so `plugins/ConnectFour/config.yml` is generated.
4. Edit the generated config to match your board coordinates.
5. Run `/connectfour reload` or restart the server.

## Permissions

- `connectfour.play`: allows moves.
- `connectfour.status`: allows status checks.
- `connectfour.admin`: allows reset, reload, scan, and log commands.

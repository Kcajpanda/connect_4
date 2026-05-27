# Connect Four Paper Plugin

Configurable Connect Four for Paper `26.1.2`.

## Build

This project targets Java 25 and Paper API `26.1.2.build.+`.

```powershell
gradle build
```

The plugin jar will be created under `build/libs/`.

## Command Blocks

Set each command block to run the matching column command:

```text
/connectfour drop 1 @p
/connectfour drop 2 @p
...
/connectfour drop 7 @p
```

The optional `@p` lets the plugin record the nearest player as the mover. Player commands also work:

```text
/connectfour drop 4
```

Admin commands:

```text
/connectfour reset
/connectfour reload
/connectfour scan
/connectfour status
/connectfour log 10
```

# Connect Four Paper Plugin

Configurable Connect Four for Paper `26.1.2`.

## Build

This project targets Java 25 and Paper API `26.1.2.build.+`.

```powershell
gradle build
```

The plugin jar will be created under `build/libs/`.

## Command Blocks

For command blocks, use `place`. It takes a zero-based column index, so the
left-most column is `0` and the right-most column is `6`.

```text
/connectfour place 0 @p
/connectfour place 1 @p
...
/connectfour place 6 @p
```

Use `drop` for one-based, animated player-facing commands:

```text
/connectfour drop 1 @p
/connectfour drop 2 @p
...
/connectfour drop 7 @p
```

The `@p` selector lets the plugin record the nearest player as the mover.
Player commands also work:

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

## Documentation

GitHub wiki source pages live in `docs/wiki/`.

Javadocs can be built locally with:

```powershell
gradle javadoc
```

The GitHub Actions workflow in `.github/workflows/javadocs.yml` publishes the
generated Javadocs to GitHub Pages.

# Developer Docs

## Source Layout

```text
src/main/java/dev/jackb/connectfour/
```

Important classes:

- `ConnectFourPlugin`: plugin entry point.
- `ConnectFourCommand`: command parsing, permissions, selectors, and tab completion.
- `ConnectFourGame`: board state, move placement, animation, win checks, reset, and fireworks.
- `GameConfig`: validates and exposes `config.yml`.
- `Vec3i`: integer world-coordinate helper.

## Javadocs

Build Javadocs locally:

```powershell
gradle javadoc
```

Output:

```text
build/docs/javadoc/
```

The `.github/workflows/javadocs.yml` workflow builds Javadocs and publishes them
to GitHub Pages.

## Publishing The Wiki

Run the `.github/workflows/wiki.yml` workflow manually from GitHub Actions to
copy the Markdown files from `docs/wiki/` into the repository wiki.

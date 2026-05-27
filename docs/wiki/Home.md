# Connect Four Plugin

Connect Four is a configurable Paper plugin for running a physical Connect Four
board inside a Minecraft world.

## Quick Links

- [Installation](Installation.md)
- [Configuration](Configuration.md)
- [Commands](Commands.md)
- [Command Blocks](Command-Blocks.md)
- [Animation Choices](Animation-Choices.md)
- [Gameplay Flow](Gameplay-Flow.md)
- [Developer Docs](Developer-Docs.md)

## What It Does

- Tracks a Connect Four board in memory.
- Reads board blocks from a configurable `config.yml`.
- Supports command-block moves with zero-based indexes.
- Supports animated player-facing drops.
- Records a move log with player, column, row, and timestamp.
- Checks horizontal, vertical, and diagonal wins after each move.
- Launches fireworks from configured positions when someone wins.
- Clears and rescans the board with admin commands.

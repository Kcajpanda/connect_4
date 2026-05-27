# Gameplay Flow

1. A player triggers a command block.
2. The command block runs `/connectfour place <index> @p`.
3. The plugin resolves the player.
4. The plugin scans the board if `sync-world-before-move` is enabled.
5. The plugin finds the lowest open row in the selected column.
6. The plugin places the active player's configured block.
7. The move is added to the move log.
8. The plugin checks horizontal, vertical, and diagonal win conditions.
9. If a player wins, fireworks launch and a win message is broadcast.
10. If the board is full with no winner, a draw message is broadcast.

## Resetting

Run:

```text
/connectfour reset
```

This clears all playable slots, clears drop locations if configured, wipes the
move log, and starts a new game.

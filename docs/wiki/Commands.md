# Commands

The base command is `/connectfour`. Aliases are `/connect4` and `/c4`.

## Player Commands

```text
/connectfour drop <column> [player|selector]
```

Drops a token using one-based columns. Column `1` is the left-most column.
This command uses the configured drop animation.

```text
/connectfour place <index> <player|selector>
```

Places a token instantly using zero-based indexes. Index `0` is the left-most
column and index `6` is the right-most column on a standard board.

## Admin Commands

```text
/connectfour reset
```

Clears the board and resets turn state.

```text
/connectfour reload
```

Reloads `config.yml` and scans the board.

```text
/connectfour scan
```

Reads the configured board blocks into memory.

```text
/connectfour status
```

Shows whose turn it is and how many tokens are on the board.

```text
/connectfour log 10
```

Shows the last 10 recorded moves.

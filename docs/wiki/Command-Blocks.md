# Command Blocks

Use the `place` subcommand for command blocks.

```text
/connectfour place 0 @p
/connectfour place 1 @p
/connectfour place 2 @p
/connectfour place 3 @p
/connectfour place 4 @p
/connectfour place 5 @p
/connectfour place 6 @p
```

The index is zero-based from left to right:

| Index | Column |
| --- | --- |
| `0` | Left-most |
| `1` | Second |
| `2` | Third |
| `3` | Fourth |
| `4` | Fifth |
| `5` | Sixth |
| `6` | Right-most |

`@p` is resolved by Bukkit selectors. When run from a command block, the plugin
records the nearest selected player as the mover.

## Why Use Place?

`place` is instant and predictable. It avoids timing issues where multiple
command blocks are triggered while an animated token is still falling.

Use `drop` when you want a visible animation:

```text
/connectfour drop 1 @p
```

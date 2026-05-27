# Configuration

The plugin is driven by `plugins/ConnectFour/config.yml`.

## Game Settings

```yaml
game:
  world: world
  rows: 6
  columns: 7
  player-one-block: RED_CONCRETE
  player-two-block: YELLOW_CONCRETE
  empty-block: AIR
```

`player-one-block` and `player-two-block` are the token materials the plugin
uses to track ownership.

## Board Geometry

```yaml
board:
  origin: {x: 0, y: 64, z: 0}
  column-step: {x: 1, y: 0, z: 0}
  row-step: {x: 0, y: 1, z: 0}
```

`origin` is the bottom-left playable slot. `column-step` moves one slot to the
right. `row-step` moves one slot up.

## Board Range

```yaml
board:
  range:
    min: {x: 0, y: 64, z: 0}
    max: {x: 6, y: 69, z: 0}
```

Every calculated playable slot must be inside this range. The plugin validates
this on startup and reload.

## Drop Locations

`drop-locations` contains one entry per column, left to right.

```yaml
drop-locations:
  - {x: 0, y: 70, z: 0}
  - {x: 1, y: 70, z: 0}
```

These locations are used by animated `/connectfour drop` commands and are also
the mental map for zero-based `/connectfour place` command blocks.

## Fireworks

```yaml
fireworks:
  - {x: 3, y: 72, z: 0}
```

When someone wins, fireworks launch from each configured position.

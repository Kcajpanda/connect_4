# Animation Choices

The plugin supports two styles:

```text
/connectfour place <index> <player|selector>
```

`place` is instant. This is the recommended command-block command because it is
predictable, fast, and cannot collide with another move animation.

```text
/connectfour drop <column> [player|selector]
```

`drop` uses a plugin-controlled block animation. It moves the configured token
material down the column path and then locks the final board slot into memory.

## Should The Board Require Falling Blocks?

No. A real Minecraft `FallingBlock` entity can look nice, but it ties gameplay
correctness to gravity, collision, and the exact shape of the physical board.
That is fragile for redstone and command-block builds.

The safer rule is:

- Use `place` for command blocks and game correctness.
- Use `drop` when you want a visible animation.
- Add real `FallingBlock` entities later as a cosmetic mode if the board design
  needs that look.

# Capture The Flag Rules (Project Baseline)

This document summarizes the baseline Capture the Flag (CTF) rules used in the current project.

## 1) Starting Conditions (Mirror Match)

- Both sides start symmetrically.
- Initial units: 14 creeps.
- Composition: Heal 4 / Melee 4 / Ranger 4 / Worker 2 (fixed bodies).
- One home-base flag per side.
- One home-base tower per side (starts with no energy).
- Two additional neutral flags (each linked to a tower and a link).

## 2) Flag Semantics (Including Linked Objects)

- A flag is treated as the reference object that controls other objects.
- Use `GameObject.controlledBy` to determine which flag an object is tied to.
- Use `Flag.my` to determine ownership status.
- When a neutral flag is captured, ownership of the tower linked to that flag is also assumed to transfer.

## 3) Victory Conditions

- A creep captures an enemy flag by stepping on it.
- You win by reducing the enemy's flag count to 0.
- Tick limit: 2000.
- When the time limit is reached, the side holding more flags wins; if tied, the result is a draw.
- Simultaneous capture resolution (both sides capturing each other's last flag on the same tick) is considered insufficiently specified in the docs, so avoiding simultaneous situations is recommended strategically.

## 4) Tower/Energy Operations

- Towers perform range-based ranged attack/heal actions.
- Effectiveness changes based on distance.
- Each action consumes energy.
- Worker energy injection is a key factor that determines operational tempo.

## 5) BodyPart (Dropped Parts)

- A creep can gain body augmentation by stepping on a BodyPart object.
- Parts can attach at 0 hits and can be activated via healing.
- Treat this as a combat snowball factor.

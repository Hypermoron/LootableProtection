# StructureProtection
A simple Paper plugin for protecting spawners and loot chests.

## Permissions
* structureprotection.breaklootables: Allows a player to override all protections for lootable blocks/entities.
* structureprotection.breaklootables.confirm: Warns a player if they attempt to break lootable blocks/entities, but allows them to continue breaking them if desired.
* structureprotection.breakspawners: Allows a player to override all protections for lootable blocks/entities.
* structureprotection.breakspawners.confirm: Warns a player if they attempt to break lootable blocks/entities, but allows them to continue breaking them if desired.

## Configuration
```
lootables:
  # Players with the permissions structureprotection.breaklootables and structureprotection.breaklootables.confirm will be able to override this protection.
  protect-from-players: true
  protect-from-explosions: true
  protect-lootable-minecarts: true

  # This protection is enabled in all worlds with restocking chests by default. Use the whitelist/blacklist to to include/exclude certain worlds.
  worlds:
    limit-worlds: false
    whitelist:
      - world
      - world_nether
      - world_the_end
    blacklist-mode: false

spawners:
  # Players with the permissions structureprotection.breakspawners and structureprotection.breakspawners.confirm will be able to override this protection.
  protect-from-players: true
  protect-from-explosions: true

  # This protection is enabled for all spawners by default. Use the whitelist/blacklist to include/exclude certain entity types. For a list of entity types, see https://papermc.io/javadocs/paper/1.18/org/bukkit/entity/EntityType.html
  entity-types:
    limit-entity-types: true
    whitelist:
      - SILVERFISH
      - VEX
    blacklist-mode: true

  # This protection is enabled in all worlds by default. Use the whitelist/blacklist to to include/exclude certain worlds.
  worlds:
    limit-worlds: false
    whitelist:
      - world
      - world_nether
      - world_the_end
    blacklist-mode: false
```

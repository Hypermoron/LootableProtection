package com.scrappymc.structureprotection;

import com.destroystokyo.paper.loottable.LootableInventory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StructureProtection extends JavaPlugin implements Listener {

    Map<UUID, Long> lootableWarningTimes = new HashMap<>();
    Map<UUID, Long> spawnerWarningTimes = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getState() instanceof LootableInventory) {
            if (isProtectedLootable((LootableInventory) block.getState(), player, block.getWorld())) {
                event.setCancelled(true);
                if (player.hasPermission("structureprotection.breaklootables.confirm")) {
                    lootableWarningTimes.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
                    player.sendMessage("This is a protected treasure chest. Other players may want to loot it!\nIf you still want to break it, you have 30 seconds to do so.");
                } else {
                    player.sendMessage("This is a protected treasure chest. You do not have permission to break it!");
                }
            }
        }
        if (block.getState() instanceof CreatureSpawner) {
            if (isProtectedSpawner((CreatureSpawner) block.getState(), player)) {
                event.setCancelled(true);
                if (player.hasPermission("structureprotection.breakspawners.confirm")) {
                    spawnerWarningTimes.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
                    player.sendMessage("This is a protected spawner. Other players may want to use it!\nIf you still want to break it, you have 30 seconds to do so.");
                } else {
                    player.sendMessage("This is a protected spawner. You do not have permission to break it!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> isProtectedBlock(block, null));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> isProtectedBlock(block, null));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!getConfig().getBoolean("lootables.protect-lootable-minecarts")) {
            return;
        }

        Player player = null;
        if (event.getAttacker() instanceof Player) {
            player = (Player) event.getAttacker();
        }

        if (event.getVehicle() instanceof LootableInventory) {
            if (isProtectedLootable((LootableInventory) event.getVehicle(), player, event.getVehicle().getWorld())) {
                event.setCancelled(true);
            } else if (player != null) {
                if (player.hasPermission("structureprotection.breaklootables.confirm")) {
                    lootableWarningTimes.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
                    player.sendMessage("This is a protected treasure chest. Other players may want to loot it!\nIf you still want to break it, you have 30 seconds to do so.");
                } else {
                    player.sendMessage("This is a protected treasure chest. You do not have permission to break it!");
                }
            }
        }
    }

    private boolean isProtectedBlock(Block block, Player player) {
        if (block.getState() instanceof LootableInventory) {
            return isProtectedLootable((LootableInventory) block.getState(), player, block.getWorld());
        }
        if (block.getState() instanceof CreatureSpawner) {
            return isProtectedSpawner((CreatureSpawner) block.getState(), player);
        }
        return false;
    }

    private boolean isProtectedLootable (LootableInventory lootable, Player player, World world) {
        if (lootable.hasLootTable()) {
            if (player != null) {
                if (!getConfig().getBoolean("lootables.protect-from-players")
                        || player.hasPermission("structureprotection.breaklootables")) {
                    return false;
                } else if (player.hasPermission("structureprotection.breaklootables.confirm")
                        && lootableWarningTimes.getOrDefault(player.getUniqueId(), 0L) >= System.currentTimeMillis() / 1000 - 30) {
                    lootableWarningTimes.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
                    return false;
                }
            } else if (!getConfig().getBoolean("lootables.protect-from-explosions")) {
                return false;
            }

            if (getConfig().getBoolean("lootables.worlds.limit-worlds")) {
                if (getConfig().getStringList("lootables.worlds.whitelist").contains(world.getName())
                        == getConfig().getBoolean("lootables.worlds.blacklist-mode")) {
                    return false;
                }
            } else if (!lootable.isRefillEnabled()) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isProtectedSpawner (CreatureSpawner spawner, Player player) {
        if (player != null) {
            if (!getConfig().getBoolean("spawners.protect-from-players")
                    || player.hasPermission("structureprotection.breakspawners")) {
                return false;
            } else if (player.hasPermission("structureprotection.breakspawners.confirm")
                    && spawnerWarningTimes.getOrDefault(player.getUniqueId(), 0L) >= System.currentTimeMillis() / 1000 - 30) {
                spawnerWarningTimes.put(player.getUniqueId(), System.currentTimeMillis() / 1000);
                return false;
            }
        } else if (!getConfig().getBoolean("spawners.protect-from-explosions")) {
            return false;
        }

        if (getConfig().getBoolean("spawners.entity-types.limit-entity-types")
                && getConfig().getStringList("spawners.entity-types.whitelist").contains(spawner.getSpawnedType().toString())
                == getConfig().getBoolean("spawners.entity-types.blacklist-mode")) {
            return false;
        }

        if (getConfig().getBoolean("spawners.worlds.limit-worlds")
                && getConfig().getStringList("spawners.worlds.whitelist").contains(spawner.getWorld().getName())
                == getConfig().getBoolean("spawners.worlds.blacklist-mode")) {
            return false;
        }
        return true;
    }
}
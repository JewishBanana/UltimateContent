package com.github.jewishbanana.ultimatecontent.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Cow;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import me.gamercoder215.mobchip.abstraction.ChipUtilFactory;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

/** Test-server-only plugin. Never include its descriptor/classes in the UC release jar. */
public final class MobChipSmokePlugin extends JavaPlugin {
    private final List<CustomEntity<?>> spawned = new ArrayList<>();

    @Override public void onEnable() {
        // World loading completes after UC's STARTUP enable.
        Bukkit.getScheduler().runTaskLater(this, this::runChecks, 40);
    }

    private void runChecks() {
        try {
            String adapter = ChipUtilFactory.getChipUtil().getClass().getSimpleName();
            String expected = System.getProperty("smoke.adapter");
            if (expected == null || !adapter.equals(expected)) throw new AssertionError("Wrong adapter: " + adapter);
            World world = Bukkit.getWorlds().get(0);
            Location location = new Location(world, 0.5, 100, 0.5);
            for (int x = -16; x <= 48; x++) for (int z = -16; z <= 16; z++)
                world.getBlockAt(x, 99, z).setType(Material.STONE, false);
            world.setTime(18000);
            String[] names = {"uc:shadow_leech", "uc:yeti", "uc:baby_end_totem", "uc:plague_rat", "uc:plague_bat"};
            for (String name : names) {
                UIEntityManager type = UIEntityManager.getEntityType(name);
                if (type == null) throw new AssertionError("Missing entity " + name);
                CustomEntity<?> custom = UIEntityManager.spawnEntity(location, type.getEntityClass());
                if (custom == null || !(custom.getEntity() instanceof Mob mob) || !mob.isValid())
                    throw new AssertionError("Could not spawn " + name);
                if (BukkitBrain.getBrain(mob).getGoalAI().isEmpty()) throw new AssertionError("No goals: " + name);
                spawned.add(custom);
                getLogger().info("SMOKE spawned " + name);
                location.add(7, 0, 0);
            }
            // Specifically exercise the remapped slime hierarchy behind the reported crash.
            for (Class<? extends Mob> clazz : List.of(Slime.class, MagmaCube.class)) {
                Mob mob = world.spawn(location, clazz);
                BukkitBrain.getBrain(mob).getGoalAI().put(new PathfinderFloat(mob), 0);
                getLogger().info("SMOKE goals installed for " + clazz.getSimpleName());
                mob.remove();
            }
            // Force the same NATURAL CreatureSpawnEvent/UIFramework path as the report,
            // without waiting for a rare natural replacement roll.
            var registry = UIEntityManager.getRegistry();
            var rates = new java.util.HashMap<UIEntityManager, Double>();
            registry.values().forEach(type -> { rates.put(type, type.getSpawnRate()); type.setSpawnRate(0); });
            UIEntityManager leech = UIEntityManager.getEntityType("uc:shadow_leech");
            var conditions = leech.getSpawnConditions();
            Cow source = world.spawn(location, Cow.class);
            try {
                leech.setSpawnRate(1);
                leech.setSpawnConditions(event -> true);
                CreatureSpawnEvent event = new CreatureSpawnEvent(source, CreatureSpawnEvent.SpawnReason.NATURAL);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) throw new AssertionError("Natural replacement did not complete");
                getLogger().info("SMOKE natural replacement passed");
            } finally {
                rates.forEach(UIEntityManager::setSpawnRate);
                leech.setSpawnConditions(conditions);
                source.remove();
            }
            Bukkit.getScheduler().runTaskLater(this, () -> {
                // Keep the mobs alive for 10 seconds so constructor-scheduled AI/body tasks also run.
                getLogger().info("SMOKE PASS adapter=" + adapter + ", custom mobs=" + spawned.size());
                Bukkit.shutdown();
            }, 200);
        } catch (Throwable error) {
            getLogger().log(Level.SEVERE, "SMOKE FAIL", error);
            Bukkit.shutdown();
        }
    }
}

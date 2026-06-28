package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.random.RandomGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.events.CustomEntitySpawnEvent;
import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedCreeper;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedDevourer;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedEnderman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedHowler;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSkeleton;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSpirit;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedTribesman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedWorm;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedZombie;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.mojang.datafixers.util.Pair;

public class EntitySpawningHandler implements Listener {
	
	private final RandomGenerator random = RandomGenerator.of("SplittableRandom");
	private final List<Pair<Function<Location, BaseEntity<?>>, Double>> entityTypes = new ArrayList<>();

	public EntitySpawningHandler(UltimateContent plugin) {
		entityTypes.addAll(Arrays.asList(
				Pair.of(InfestedZombie.attemptSpawn, UIEntityManager.getEntityType(InfestedZombie.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedSkeleton.attemptSpawn, UIEntityManager.getEntityType(InfestedSkeleton.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedCreeper.attemptSpawn, UIEntityManager.getEntityType(InfestedCreeper.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedEnderman.attemptSpawn, UIEntityManager.getEntityType(InfestedEnderman.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedDevourer.attemptSpawn, UIEntityManager.getEntityType(InfestedDevourer.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedHowler.attemptSpawn, UIEntityManager.getEntityType(InfestedHowler.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedSpirit.attemptSpawn, UIEntityManager.getEntityType(InfestedSpirit.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedTribesman.attemptSpawn, UIEntityManager.getEntityType(InfestedTribesman.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedWorm.attemptSpawn, UIEntityManager.getEntityType(InfestedWorm.REGISTERED_KEY).getSpawnRate())
				));
		
		new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.getOnlinePlayers().forEach(player -> {
					Location loc = player.getLocation();
					if (loc.isWorldLoaded() && loc.getBlock().getBiome() == Biome.DEEP_DARK) {
						List<Pair<Function<Location, BaseEntity<?>>, Double>> list = new ArrayList<>(entityTypes);
						Collections.shuffle(list);
						new BukkitRunnable() {
							@Override
							public void run() {
								if (!DependencyUtils.canSpawnCustomMobs(loc))
									return;
								if (player.getNearbyEntities(40.0, 30.0, 40.0).stream().filter(e -> e instanceof Monster).count() < 20) {
									int spawnCount = 0;
									for (Pair<Function<Location, BaseEntity<?>>, Double> pair : list) {
										if (random.nextFloat() >= pair.getSecond())
											continue;
										BaseEntity<?> entity = pair.getFirst().apply(loc);
										if (entity != null) {
//											if (entity.getCastedEntity() instanceof LivingEntity alive)
//												alive.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10000, 0, true, false));
											if (++spawnCount == 3)
												break;
										}
									}
								}
							}
						}.runTask(plugin);
					}
				});
			}
		}.runTaskTimerAsynchronously(plugin, 0, 100);
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler(ignoreCancelled = true)
	public void onCustomEntitySpawn(CustomEntitySpawnEvent event) {
		if (event.getReason() != SpawnReason.NATURAL)
			return;
		if (!DependencyUtils.canSpawnCustomMobs(event.getLocation()))
			event.setCancelled(true);
	}
}

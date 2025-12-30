package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.random.RandomGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedCreeper;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedDevourer;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedEnderman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedHowler;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSkeleton;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSpirit;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedTribesman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedZombie;
import com.mojang.datafixers.util.Pair;

public class EntitySpawningHandler {
	
	private final RandomGenerator random = RandomGenerator.of("SplittableRandom");
	private final List<Pair<Function<Location, BaseEntity<?>>, Double>> entityTypes = new ArrayList<>();

	public EntitySpawningHandler(Main plugin) {
		entityTypes.addAll(Arrays.asList(
				Pair.of(InfestedZombie.attemptSpawn, UIEntityManager.getEntityType(InfestedZombie.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedSkeleton.attemptSpawn, UIEntityManager.getEntityType(InfestedSkeleton.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedCreeper.attemptSpawn, UIEntityManager.getEntityType(InfestedCreeper.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedEnderman.attemptSpawn, UIEntityManager.getEntityType(InfestedEnderman.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedDevourer.attemptSpawn, UIEntityManager.getEntityType(InfestedDevourer.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedHowler.attemptSpawn, UIEntityManager.getEntityType(InfestedHowler.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedSpirit.attemptSpawn, UIEntityManager.getEntityType(InfestedSpirit.REGISTERED_KEY).getSpawnRate()),
				Pair.of(InfestedTribesman.attemptSpawn, UIEntityManager.getEntityType(InfestedTribesman.REGISTERED_KEY).getSpawnRate())
				));
		
		new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.getOnlinePlayers().forEach(player -> {
					Location loc = player.getLocation();
					if (loc.isWorldLoaded() && loc.getBlock().getBiome() == Biome.DEEP_DARK) {
						Stack<Pair<Function<Location, BaseEntity<?>>, Double>> stack = new Stack<>();
						for (int i=0; i < 5; i++)
							stack.add(entityTypes.get(random.nextInt(entityTypes.size())));
						new BukkitRunnable() {
							@Override
							public void run() {
								if (player.getNearbyEntities(40.0, 30.0, 40.0).stream().filter(e -> e instanceof Mob).count() < 20)
									while (!stack.isEmpty()) {
										Pair<Function<Location, BaseEntity<?>>, Double> pair = stack.pop();
										if (random.nextDouble() >= pair.getSecond())
											continue;
										BaseEntity<?> entity = pair.getFirst().apply(loc);
										if (entity != null) {
//											if (entity.getCastedEntity() instanceof LivingEntity alive)
//												alive.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10000, 0, true, false));
											break;
										}
									}
							}
						}.runTask(plugin);
					}
				});
			}
		}.runTaskTimerAsynchronously(plugin, 0, 200);
	}
}

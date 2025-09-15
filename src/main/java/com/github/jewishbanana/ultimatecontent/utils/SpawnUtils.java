package com.github.jewishbanana.ultimatecontent.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpawnUtils {

	public static final double MIN_SPAWN_DISTANCE_FROM_PLAYERS = 24.0;
	public static final double MAX_SPAWN_DISTANCE_FROM_PLAYERS = 70.0;

	public static Location findSpawnLocation(Location area, int height, double minDistance, double maxDistance) {
		Location spawn = Utils.findRandomSpotInRadius(area, minDistance, maxDistance, height, 5);
		return canMonsterSpawn(spawn, minDistance) ? spawn : null;
	}
	public static Location findSpawnLocation(Location area, int height) {
		return findSpawnLocation(area, height, MIN_SPAWN_DISTANCE_FROM_PLAYERS, MAX_SPAWN_DISTANCE_FROM_PLAYERS);
	}
	public static Location findSpawnLocation(Location area) {
		return findSpawnLocation(area, 2, MIN_SPAWN_DISTANCE_FROM_PLAYERS, MAX_SPAWN_DISTANCE_FROM_PLAYERS);
	}
	public static boolean canMonsterSpawn(Location location, double minDistance) {
		return location != null
				&& !location.getBlock().isLiquid()
				&& location.getBlock().getLightFromBlocks() == 0
				&& location.getWorld().getNearbyEntities(location, minDistance, minDistance, minDistance, e -> e instanceof Player).size() == 0;
	}
}

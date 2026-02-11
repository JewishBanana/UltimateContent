package com.github.jewishbanana.ultimatecontent.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.ultimatecontent.Main;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class NavigationUtils {
	
	private Mob mob;
	private List<Location> locations = new ArrayList<>();
	private boolean isDone;
	private boolean isAdvancing;

	private NavigationUtils(Mob mob) {
		this.mob = mob;
	}
	public static NavigationUtils createNavigationPath(Mob mob) {
		return new NavigationUtils(mob);
	}
	public void advance() {
		if (locations.isEmpty())
			throw new NullPointerException("There is no location to advance towards!");
		if (isAdvancing)
			return;
		Location location = locations.get(0);
		EntityBrain brain = BukkitBrain.getBrain(mob);
		brain.getController().moveTo(location);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!mob.isValid() || !mob.getWorld().equals(location.getWorld())) {
					this.cancel();
					return;
				}
//				location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, .0001);
				if (mob.getLocation().distanceSquared(location) < 0.25 || !brain.getBody().isMoving()) {
					locations.remove(location);
					if (locations.isEmpty())
						isDone = true;
					this.cancel();
					return;
				}
				brain.getController().moveTo(location);
			}
		}.runTaskTimer(Main.getInstance(), 2, 1);
	}
	public NavigationUtils addLocation(Location location) {
		locations.add(location);
		return this;
	}
	public NavigationUtils addLocation(Block block) {
		locations.add(block.getLocation().add(.5, 0, .5));
		return this;
	}
	public boolean isDone() {
		return this.isDone;
	}
	public static Location findPositionInDirection(Location initial, Vector direction, float minDistance, float maxDistance, double height) {
	    final World world = initial.getWorld();
	    final float dirX = (float) direction.getX();
	    final float dirY = (float) direction.getY();
	    final float dirZ = (float) direction.getZ();
	    final float angledX = -dirX;
	    final float angledZ = dirZ;
	    float pathX = (float) (initial.getX() + dirX * minDistance);
	    float pathY = (float) (initial.getY() + dirY * minDistance);
	    float pathZ = (float) (initial.getZ() + dirZ * minDistance);
	    final int iterations = (int)(maxDistance - minDistance);
	    for (int i = 0; i < iterations; i++) {
	        for (int j = -1; j < 2; j++) {
	            final float searchX = pathX + angledX * j;
	            final float searchY = pathY;
	            final float searchZ = pathZ + angledZ * j;
	            final Location searchLoc = new Location(world, searchX, searchY, searchZ);
	            final Location temp = SpawnUtils.findSmartYSpawn(initial, searchLoc, height, 3);
	            if (temp != null)
	                return temp;
	        }
	        pathX += dirX;
	        pathY += dirY;
	        pathZ += dirZ;
	    }
	    return null;
	}
	public static Location findPositionInDirection(Location initial, Vector direction, float minDistance, float maxDistance) {
		return findPositionInDirection(initial, direction, minDistance, maxDistance, 1);
	}
	public static Location findPositionInDirection(Entity entity, Vector direction, float minDistance, float maxDistance) {
	    return findPositionInDirection(entity.getLocation(), direction, minDistance, maxDistance, entity.getHeight());
	}
}

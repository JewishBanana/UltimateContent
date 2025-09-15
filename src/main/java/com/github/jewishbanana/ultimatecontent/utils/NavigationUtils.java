package com.github.jewishbanana.ultimatecontent.utils;

import java.util.ArrayDeque;
import java.util.Queue;

import org.bukkit.Location;
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
	private Queue<Location> locations = new ArrayDeque<>();
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
		Location location = locations.peek();
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
	public static Location findPositionInDirection(Location initial, Vector direction, double minDistance, double maxDistance) {
		Location path = initial.clone().add(direction.clone().multiply(minDistance));
		Vector angled = new Vector(-direction.getX(), 0, direction.getZ());
		for (int i=0; i < maxDistance - minDistance; i++) {
			for (int j=-1; j < 2; j++) {
				Location temp = EntityUtils.findSmartYSpawn(initial, path.clone().add(angled.clone().multiply(j)), 1, 3);
				if (temp != null)
					return temp;
			}
			path.add(direction);
		}
		return null;
	}
	public static Location findPositionInDirection(Entity entity, Vector direction, double minDistance, double maxDistance) {
		Location initial = entity.getLocation();
		Location path = initial.clone().add(direction.clone().multiply(minDistance));
		Vector angled = new Vector(-direction.getX(), 0, direction.getZ());
		for (int i=0; i < maxDistance - minDistance; i++) {
			for (int j=-1; j < 2; j++) {
				Location temp = EntityUtils.findSmartYSpawn(initial, path.clone().add(angled.clone().multiply(j)), entity.getHeight(), 3);
				if (temp != null)
					return temp;
			}
			path.add(direction);
		}
		return null;
	}
}

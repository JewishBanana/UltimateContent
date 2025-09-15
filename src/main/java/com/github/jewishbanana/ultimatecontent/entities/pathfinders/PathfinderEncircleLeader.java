package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderEncircleLeader extends CustomPathfinder {
	
	private UUID leader;
	private Set<UUID> squad;
	private Entity target;
	private Location lastLeaderLoc;
	private Location goal;
	private double clearanceDistance;
	private double clearanceDistanceSquared;

	public PathfinderEncircleLeader(@NotNull Mob m, UUID leader, Set<UUID> squad, double clearanceDistance) {
		super(m);
		this.leader = leader;
		this.squad = squad;
		this.clearanceDistance = clearanceDistance;
		this.clearanceDistanceSquared = clearanceDistance * clearanceDistance;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT };
	}
	@Override
	public boolean canStart() {
		if (entity.isInsideVehicle() || (entity.getTarget() != null && !entity.getTarget().isDead()))
			return false;
		target = Bukkit.getEntity(leader);
		if (target == null || target.isDead())
			return false;
		Location leaderLoc = target.getLocation();
		if (entity.getWorld().equals(leaderLoc.getWorld()) && leaderLoc.distanceSquared(entity.getLocation()) < clearanceDistanceSquared)
			return true;
		if (lastLeaderLoc != null && target.getWorld().equals(lastLeaderLoc.getWorld()) && leaderLoc.distanceSquared(lastLeaderLoc) > 0.015)
			return true;
		for (Entity local : entity.getNearbyEntities(clearanceDistance, clearanceDistance, clearanceDistance))
			if (!local.equals(entity) && !local.isDead() && squad.contains(local.getUniqueId()) && !local.isInsideVehicle())
				return true;
		return false;
	}
	@Override
	public void start() {
		int realCount = 0;
		for (UUID uuid : squad) {
			Entity local = Bukkit.getEntity(uuid);
			if (local != null && !local.isInsideVehicle())
				realCount++;
		}
		double increment = (Math.PI * 2) / (double) (Math.max(realCount, 1));
		lastLeaderLoc = target.getLocation();
		Vector vec = Utils.getVectorTowards(lastLeaderLoc, entity.getLocation()).setY(0).normalize().multiply(clearanceDistance + 1.0);
		core:
			for (double i=0; i < Math.PI * 2; i += increment) {
				Location loc = lastLeaderLoc.clone().add(vec);
//				loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc.clone().add(0,2,0), 1, 0, 0, 0, 0.001);
				Location temp = EntityUtils.findSmartYSpawn(lastLeaderLoc, loc, entity.getHeight(), 3);
				if (temp == null) {
					vec.rotateAroundY(increment);
					continue;
				} else
					loc.setY(temp.getY());
				for (Entity local : temp.getWorld().getNearbyEntities(temp, clearanceDistance, clearanceDistance, clearanceDistance, local -> !local.isDead() && !local.equals(entity)))
					if (squad.contains(local.getUniqueId())) {
						vec.rotateAroundY(increment);
						continue core;
					}
//				loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0,2,0), 1, 0, 0, 0, 0.001);
				goal = loc;
				BukkitBrain.getBrain(entity).getController().moveTo(goal);
				return;
			}
	}
	@Override
	public void tick() {
	}
	@Override
	public boolean canContinueToUse() {
		if (target == null || target.isDead() || goal == null || entity.isInsideVehicle() || (entity.getTarget() != null && !entity.getTarget().isDead()))
			return false;
		Location targetLoc = target.getLocation();
		if (!target.getWorld().equals(lastLeaderLoc.getWorld()) || targetLoc.distanceSquared(lastLeaderLoc) > 0.015)
			return false;
		Location entityLoc = entity.getLocation();
		if (goal.getWorld().equals(entity.getWorld()) && goal.distanceSquared(entityLoc) > 0.1)
			return true;
		if (!targetLoc.getWorld().equals(entityLoc.getWorld()) || targetLoc.distanceSquared(entityLoc) < clearanceDistanceSquared)
			return false;
		for (Entity local : entity.getNearbyEntities(clearanceDistance, clearanceDistance, clearanceDistance))
			if (!local.equals(entity) && !local.isDead() && squad.contains(local.getUniqueId()) && !local.isInsideVehicle())
				return false;
		return true;
	}
}

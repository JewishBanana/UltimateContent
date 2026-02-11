package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderEncircleLeader extends CustomPathfinder {
	
	private final UUID leader;
	private final Set<UUID> squad;
	private final double clearanceDistance;
	private final double clearanceDistanceSquared;
	private final double leaderMovementThreshold = 0.015;
	private final double goalReachedThreshold = 0.1;
	private final EntityController controller;
	
	private Entity target;
	private Location lastLeaderLoc;
	private Location goal;

	public PathfinderEncircleLeader(@NotNull Mob m, UUID leader, Set<UUID> squad, double clearanceDistance) {
		super(m);
		this.leader = leader;
		this.squad = squad;
		this.clearanceDistance = clearanceDistance;
		this.clearanceDistanceSquared = clearanceDistance * clearanceDistance;
		this.controller = BukkitBrain.getBrain(entity).getController();
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT };
	}
	@Override
	public boolean canStart() {
		if (entity.isInsideVehicle())
			return false;
		Entity currentTarget = entity.getTarget();
		if (currentTarget != null && !currentTarget.isDead())
			return false;
		target = Bukkit.getEntity(leader);
		if (target == null || target.isDead())
			return false;
		Location leaderLoc = target.getLocation();
		Location entityLoc = entity.getLocation();
		World entityWorld = entityLoc.getWorld();
		if (entityWorld.equals(leaderLoc.getWorld()) && leaderLoc.distanceSquared(entityLoc) < clearanceDistanceSquared)
			return true;
		if (lastLeaderLoc != null && target.getWorld().equals(lastLeaderLoc.getWorld()) && leaderLoc.distanceSquared(lastLeaderLoc) > leaderMovementThreshold)
			return true;
		Collection<Entity> nearby = entity.getNearbyEntities(clearanceDistance, clearanceDistance, clearanceDistance);
		for (Entity local : nearby) {
			if (!local.equals(entity) && !local.isDead() && squad.contains(local.getUniqueId()) && !local.isInsideVehicle())
				return true;
		}
		return false;
	}
	@Override
	public void start() {
		int realCount = 0;
		for (UUID uuid : squad) {
			Entity local = Bukkit.getEntity(uuid);
			if (local != null && !local.isDead() && !local.isInsideVehicle())
				realCount++;
		}
		double increment = (Math.PI * 2) / Math.max(realCount, 1);
		lastLeaderLoc = target.getLocation();
		Vector vec = Utils.getVectorTowards(lastLeaderLoc, entity.getLocation()).setY(0).normalize().multiply(clearanceDistance + 1.0);
		double maxRotations = Math.PI * 2;
		core:
			for (double i=0; i < maxRotations; i += increment) {
				Location loc = lastLeaderLoc.clone().add(vec);
				Location temp = SpawnUtils.findSmartYSpawn(lastLeaderLoc, loc, entity.getHeight(), 3);
				if (temp == null) {
					vec.rotateAroundY(increment);
					continue;
				}
				loc.setY(temp.getY());
				Collection<Entity> nearbyEntities = temp.getWorld().getNearbyEntities(temp, clearanceDistance, clearanceDistance, clearanceDistance, local -> !local.isDead() && !local.equals(entity));
				for (Entity local : nearbyEntities) {
					if (squad.contains(local.getUniqueId())) {
						vec.rotateAroundY(increment);
						continue core;
					}
				}
				goal = loc;
				controller.moveTo(goal);
				return;
			}
	}
	@Override
	public void tick() {
	}
	@Override
	public boolean canContinueToUse() {
		if (target == null || target.isDead() || goal == null || entity.isInsideVehicle())
			return false;
		Entity currentTarget = entity.getTarget();
		if (currentTarget != null && !currentTarget.isDead())
			return false;
		Location targetLoc = target.getLocation();
		World targetWorld = targetLoc.getWorld();
		if (!targetWorld.equals(lastLeaderLoc.getWorld()) || targetLoc.distanceSquared(lastLeaderLoc) > leaderMovementThreshold)
			return false;
		Location entityLoc = entity.getLocation();
		World entityWorld = entityLoc.getWorld();
		if (goal.getWorld().equals(entityWorld) && goal.distanceSquared(entityLoc) > goalReachedThreshold)
			return true;
		if (!targetWorld.equals(entityWorld) || targetLoc.distanceSquared(entityLoc) < clearanceDistanceSquared)
			return false;
		Collection<Entity> nearby = entity.getNearbyEntities(clearanceDistance, clearanceDistance, clearanceDistance);
		for (Entity local : nearby) {
			if (!local.equals(entity) && !local.isDead() && squad.contains(local.getUniqueId()) && !local.isInsideVehicle())
				return false;
		}
		return true;
	}
}
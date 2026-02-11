package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderFollowEntity extends CustomPathfinder {
	
	private final UUID toFollow;
	private final double minDistanceSquared;
	private final double teleportDistanceSquared;
	private final EntityController controller;
	
	private Location goal;
	private Entity target;
	
	public PathfinderFollowEntity(@NotNull Mob m, UUID toFollow, double minDistance, double teleportDistance) {
		super(m);
		this.toFollow = toFollow;
		this.minDistanceSquared = minDistance * minDistance;
		this.teleportDistanceSquared = teleportDistance * teleportDistance;
		this.controller = BukkitBrain.getBrain(entity).getController();
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		target = Bukkit.getEntity(toFollow);
		if (target == null || target.isDead())
			return false;
		Entity currentTarget = entity.getTarget();
		if (currentTarget != null && !currentTarget.isDead())
			return false;
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation();
		if (!targetLoc.getWorld().equals(entityLoc.getWorld()) || targetLoc.distanceSquared(entityLoc) <= minDistanceSquared)
			return false;
		return true;
	}
	@Override
	public void start() {
		goal = target.getLocation();
		controller.lookAt(goal.clone().add(0, entity.getEyeHeight(), 0));
		controller.moveTo(goal);
	}
	@Override
	public void tick() {
		if (target == null || !target.isOnGround())
			return;
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation();
		if (!targetLoc.getWorld().equals(entityLoc.getWorld()))
			return;
		if (targetLoc.distanceSquared(entityLoc) > teleportDistanceSquared)
			entity.teleport(target);
	}
	@Override
	public boolean canContinueToUse() {
		if (target == null || target.isDead())
			return false;
		Entity currentTarget = entity.getTarget();
		if (currentTarget != null && !currentTarget.isDead())
			return false;
		Location targetLoc = target.getLocation();
		Location entityLoc = entity.getLocation();
		World entityWorld = entityLoc.getWorld();
		if (!targetLoc.getWorld().equals(entityWorld))
			return false;
		if (targetLoc.distanceSquared(goal) > minDistanceSquared)
			return false;
		if (entityLoc.distanceSquared(targetLoc) <= minDistanceSquared)
			return false;
		return true;
	}
}
package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Sittable;
import org.jetbrains.annotations.NotNull;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderFollowEntity extends CustomPathfinder {

	private final UUID toFollow;
	private final double minDistanceSquared;
	private double teleportDistanceSquared;
	private double maxTeleportDistanceSquared;
	private final EntityController controller;

	private Entity target;
	private int pathUpdateTimer = 0;

	public PathfinderFollowEntity(@NotNull Mob m, UUID toFollow, double minDistance, double teleportDistance, double maxTeleportDistance) {
		super(m);
		this.toFollow = toFollow;
		this.minDistanceSquared = minDistance * minDistance;
		if (teleportDistance > 0) {
			this.teleportDistanceSquared = teleportDistance * teleportDistance;
			this.maxTeleportDistanceSquared = maxTeleportDistance * maxTeleportDistance;
		}
		this.controller = BukkitBrain.getBrain(entity).getController();
	}
	public PathfinderFollowEntity(@NotNull Mob m, UUID toFollow, double minDistance, double teleportDistance) {
		this(m, toFollow, minDistance, teleportDistance, teleportDistance * 1.5);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		if (entity instanceof Sittable sittable && sittable.isSitting())
			return false;
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
		pathUpdateTimer = 0;
		Location targetLoc = target.getLocation();
		controller.lookAt(targetLoc.clone().add(0, entity.getEyeHeight(), 0));
		controller.moveTo(targetLoc);
	}
	@Override
	public void tick() {
		if (target == null || target.isDead())
			return;
		Location targetLoc = target.getLocation();
		controller.lookAt(targetLoc.clone().add(0, entity.getEyeHeight(), 0));
		// Recalculate path every 10 ticks (mirrors vanilla FollowOwnerGoal cadence).
		if (--pathUpdateTimer <= 0) {
			pathUpdateTimer = 10;
			controller.moveTo(targetLoc);
		}
		if (teleportDistanceSquared == 0 || !target.isOnGround())
			return;
		double teleportDistanceCheck = teleportDistanceSquared;
		LivingEntity entityTarget = entity.getTarget();
		if (entityTarget != null && !entityTarget.isDead())
			teleportDistanceCheck = maxTeleportDistanceSquared;
		Location entityLoc = entity.getLocation();
		if (!targetLoc.getWorld().equals(entityLoc.getWorld()))
			return;
		if (targetLoc.distanceSquared(entityLoc) > teleportDistanceCheck) {
			entity.teleport(target);
			entity.setTarget(null);
		}
	}
	@Override
	public boolean canContinueToUse() {
		if (entity instanceof Sittable sittable && sittable.isSitting())
			return false;
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
		// Stop following once close enough; canStart() will re-engage if the owner moves away again.
		if (entityLoc.distanceSquared(targetLoc) <= minDistanceSquared)
			return false;
		return true;
	}
}

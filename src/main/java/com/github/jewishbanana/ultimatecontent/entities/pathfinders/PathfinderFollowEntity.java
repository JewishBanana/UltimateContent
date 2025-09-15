package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderFollowEntity extends CustomPathfinder {
	
	private UUID toFollow;
	private double minDistance;
	private double teleportDistance;
	private Location goal;
	private Entity target;

	public PathfinderFollowEntity(@NotNull Mob m, UUID toFollow, double minDistance, double teleportDistance) {
		super(m);
		this.toFollow = toFollow;
		this.minDistance = minDistance * minDistance;
		this.teleportDistance = teleportDistance * teleportDistance;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		target = Bukkit.getEntity(toFollow);
		if (target == null || target.isDead() || (entity.getTarget() != null && !entity.getTarget().isDead()))
			return false;
		if (!target.getWorld().equals(entity.getWorld()) || target.getLocation().distanceSquared(this.entity.getLocation()) <= minDistance)
			return false;
		return true;
	}
	@Override
	public void start() {
		goal = target.getLocation();
		EntityController controller = BukkitBrain.getBrain(entity).getController();
		controller.lookAt(goal.clone().add(0, entity.getEyeHeight(), 0));
		controller.moveTo(goal);
	}
	@Override
	public void tick() {
		if (target == null || !target.isOnGround())
			return;
		if (!target.getWorld().equals(entity.getWorld()) || target.getLocation().distanceSquared(this.entity.getLocation()) <= teleportDistance)
			return;
		entity.teleport(target);
	}
	@Override
	public boolean canContinueToUse() {
		if (target == null || target.isDead() || (entity.getTarget() != null && !entity.getTarget().isDead()))
			return false;
		Location targetLoc = target.getLocation();
		if (!targetLoc.getWorld().equals(entity.getWorld()) || targetLoc.distanceSquared(goal) > minDistance || entity.getLocation().distanceSquared(targetLoc) <= minDistance)
			return false;
		return true;
	}
}

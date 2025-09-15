package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.NavigationUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderMaintainTargetDistance extends CustomPathfinder {
	
	private double closestDistance;
	private double farthestDistance;
	private Location goal;
	private NavigationUtils navi;

	public PathfinderMaintainTargetDistance(@NotNull Mob m, double closestDistance, double farthestDistance) {
		super(m);
		this.closestDistance = closestDistance * closestDistance;
		this.farthestDistance = farthestDistance * farthestDistance;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		LivingEntity target = entity.getTarget();
		if (target == null || !target.isValid())
			return false;
		double distance = entity.getLocation().distanceSquared(target.getLocation());
		if (distance > farthestDistance)
			return true;
		if (distance >= closestDistance)
			return false;
		goal = NavigationUtils.findPositionInDirection(entity, Utils.getVectorTowards(target.getLocation(), entity.getLocation()).setY(0).normalize(), 3.0, 7.0);
		if (goal == null)
			return false;
		navi = NavigationUtils.createNavigationPath(entity).addLocation(goal);
		return true;
	}
	@Override
	public void start() {
		if (navi != null)
			navi.advance();
	}
	@Override
	public void stop() {
		navi = null;
	}
	@Override
	public void tick() {
		if (navi == null && entity.getTarget() != null)
			BukkitBrain.getBrain(entity).getController().moveTo(entity.getTarget());
	}
	@Override
	public boolean canContinueToUse() {
		return (navi != null && !navi.isDone() && entity.getTarget() != null && !Utils.isLocationsWithinDistance(entity.getTarget().getLocation(), goal, closestDistance))
				|| (navi == null && entity.getTarget() != null && !Utils.isLocationsWithinDistance(entity.getLocation(), entity.getTarget().getLocation(), farthestDistance));
	}
}

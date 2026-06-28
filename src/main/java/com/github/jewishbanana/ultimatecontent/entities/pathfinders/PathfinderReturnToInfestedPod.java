package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

/**
 * Drives an infested mob back to its originating infested pod. It is added at top priority (with the mob's combat/target
 * AI cleared) when the pod begins to close, so the swarm streams home instead of stopping to fight; any mob that does not
 * make it back in time is killed by the pod when it seals.
 *
 * <p>The gather point ({@code pod}) is the reachable spot at the <b>bottom</b> of the pod (DeadlyDisasters lowers it to
 * the cavity floor), and the goal stops once the mob is within {@code reachRadius} blocks of it in 3D. Because the target
 * sits on the floor rather than up in the air, the mob walks to it without jumping/spinning, and floor-pod mobs have to
 * actually descend into the pod (not stop at the top of the staircase) before they count as home. If the mob later leaves
 * that radius the goal re-triggers and walks it back.</p>
 */
public class PathfinderReturnToInfestedPod extends CustomPathfinder {

	private final Location pod;
	private final double reachRadiusSq;
	private final EntityController controller;

	public PathfinderReturnToInfestedPod(@NotNull Mob m, @NotNull Location pod, double reachRadius) {
		super(m);
		this.pod = pod;
		double r = Math.max(1.0, reachRadius);
		this.reachRadiusSq = r * r;
		this.controller = BukkitBrain.getBrain(entity).getController();
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	private boolean outsidePod() {
		return pod.getWorld() != null && pod.getWorld().equals(entity.getWorld()) && entity.getLocation().distanceSquared(pod) > reachRadiusSq;
	}
	@Override
	public boolean canStart() {
		return outsidePod();
	}
	@Override
	public void start() {
		entity.setTarget(null);
		controller.moveTo(pod);
	}
	@Override
	public void tick() {
		entity.setTarget(null); // never stop to fight on the way home
		controller.moveTo(pod);
	}
	@Override
	public boolean canContinueToUse() {
		return outsidePod();
	}
}

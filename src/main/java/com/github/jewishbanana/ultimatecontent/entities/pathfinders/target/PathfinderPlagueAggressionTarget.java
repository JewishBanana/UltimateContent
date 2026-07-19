package com.github.jewishbanana.ultimatecontent.entities.pathfinders.target;

import java.util.Comparator;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;

public class PathfinderPlagueAggressionTarget extends CustomPathfinder {

	private static final int TARGET_REASSESS_TICKS = 20;
	private static final int TARGET_DISENGAGE_TICKS = 200;
	private static final int TARGET_REACQUIRE_DELAY_TICKS = 40;
	private static final double TARGET_DISENGAGE_DISTANCE_SQUARED = 36.0;

	private final double radius;
	private final double radiusSquared;
	private LivingEntity target;
	private int reacquireTargetTicks;
	private int targetTicks;
	private int reassessTicks;

	public PathfinderPlagueAggressionTarget(@NotNull Mob entity, boolean villagerMode, double radius) {
		super(entity);
		this.radius = radius;
		this.radiusSquared = radius * radius;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.TARGETING };
	}
	@Override
	public boolean canStart() {
		if (reacquireTargetTicks > 0) {
			reacquireTargetTicks--;
			return false;
		}
		LivingEntity current = entity.getTarget();
		if (isValidTarget(current))
			return false;
		target = findTarget();
		return target != null;
	}
	@Override
	public void start() {
		entity.setTarget(target);
		targetTicks = 0;
		reassessTicks = TARGET_REASSESS_TICKS;
	}
	@Override
	public void tick() {
		LivingEntity current = entity.getTarget();
		if (!isValidTarget(current)) {
			entity.setTarget(null);
			return;
		}
		targetTicks++;
		if (targetTicks > TARGET_DISENGAGE_TICKS
				&& (current.getLocation().distanceSquared(entity.getLocation()) > TARGET_DISENGAGE_DISTANCE_SQUARED
						|| !entity.hasLineOfSight(current))) {
			reacquireTargetTicks = TARGET_REACQUIRE_DELAY_TICKS;
			target = null;
			entity.setTarget(null);
			return;
		}
		if (--reassessTicks <= 0) {
			reassessTicks = TARGET_REASSESS_TICKS;
			LivingEntity nearest = findTarget();
			if (nearest != null && !nearest.equals(current)) {
				target = nearest;
				targetTicks = 0;
				entity.setTarget(nearest);
			}
		}
	}
	@Override
	public boolean canContinueToUse() {
		return target != null && target.equals(entity.getTarget()) && isValidTarget(target);
	}
	private LivingEntity findTarget() {
		return entity.getNearbyEntities(radius, radius * 0.6, radius).stream()
				.filter(e -> e instanceof LivingEntity)
				.map(e -> (LivingEntity) e)
				.filter(this::isValidTarget)
				.min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(entity.getLocation())))
				.orElse(null);
	}
	private boolean isValidTarget(LivingEntity test) {
		if (test == null || test.equals(entity) || test.isDead() || !test.isValid() || EntityUtils.isEntityImmunePlayer(test))
			return false;
		if (!test.getWorld().equals(entity.getWorld()))
			return false;
		if (test.getLocation().distanceSquared(entity.getLocation()) > radiusSquared)
			return false;
		return true;
	}
}

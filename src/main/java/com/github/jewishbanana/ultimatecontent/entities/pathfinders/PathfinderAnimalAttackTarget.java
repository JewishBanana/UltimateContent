package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderAnimalAttackTarget extends CustomPathfinder {
	
	private Location goal;
	private LivingEntity target;
	private final double attackDamage;
	private final float attackRangeSquared;
	private final int attackCooldownTicks;
	private final float leapDistanceSquared;
	
	private int cooldown;
	private boolean leap;
	
	public PathfinderAnimalAttackTarget(@NotNull Mob m, double attackDamage, float attackRange, int attackCooldownTicks) {
		super(m);
		this.attackDamage = attackDamage;
		this.attackRangeSquared = attackRange * attackRange;
		float leapDistance = attackRange + 0.5f;
		this.leapDistanceSquared = leapDistance * leapDistance;
		this.attackCooldownTicks = attackCooldownTicks;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		LivingEntity target = entity.getTarget();
		return target != null && target.isValid();
	}
	@Override
	public void start() {
		target = entity.getTarget();
		goal = target.getLocation();
		EntityController controller = BukkitBrain.getBrain(entity).getController();
		controller.lookAt(goal.clone().add(0, target.getEyeHeight(), 0));
		controller.moveTo(goal, 1.5);
	}
	@Override
	public void tick() {
		if (EntityUtils.isEntityImmunePlayer(target)) {
			entity.setTarget(null);
			return;
		}
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		if (!target.getWorld().equals(entity.getWorld()))
			return;
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation();
		double distSquared = targetLoc.distanceSquared(entityLoc);
		if (!leap && distSquared <= leapDistanceSquared) {
			leap = true;
			entity.setVelocity(Utils.getVectorTowards(entityLoc, targetLoc.add(0, target.getHeight() / 2.0, 0)).multiply(0.3));
		}
		if (distSquared <= attackRangeSquared) {
			target.damage(attackDamage, entity);
			cooldown = attackCooldownTicks;
			entity.swingMainHand();
		} else
			leap = false;
	}
	@Override
	public boolean canContinueToUse() {
		LivingEntity target = entity.getTarget();
		if (target == null || !target.equals(this.target) || !target.isValid())
			return false;
		Location targetLoc = target.getLocation();
		if (EntityUtils.isEntityImmunePlayer(target) || !Utils.isLocationsWithinDistance(targetLoc, goal, 0.0625f))
			return false;
		if (!Utils.isLocationsWithinDistance(entity.getLocation(), goal, 0.25f) && !Utils.isLocationsWithinDistance(targetLoc, entity.getLocation(), attackRangeSquared))
			return false;
		return true;
	}
}
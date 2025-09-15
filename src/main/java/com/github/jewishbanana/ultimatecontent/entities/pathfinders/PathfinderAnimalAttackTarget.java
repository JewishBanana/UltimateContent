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
	private double attackDamage;
	private double attackRange;
	private int attackCooldownTicks;
	
	private int cooldown;
	private boolean leap;
	private double leapDistance;

	public PathfinderAnimalAttackTarget(@NotNull Mob m, double attackDamage, double attackRange, int attackCooldownTicks) {
		super(m);
		this.attackDamage = attackDamage;
		this.attackRange = attackRange * attackRange;
		this.leapDistance = (attackRange + 0.5) * (attackRange + 0.5);
		this.attackCooldownTicks = attackCooldownTicks;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		return entity.getTarget() != null && entity.getTarget().isValid();
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
		double dist = target.getLocation().distanceSquared(entity.getLocation());
		if (!leap && dist <= leapDistance) {
			leap = true;
			entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), target.getLocation().add(0, target.getHeight() / 2.0, 0)).multiply(0.3));
		}
		if (dist <= attackRange) {
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
		if (!Utils.isLocationsWithinDistance(targetLoc, goal, 0.0625))
			return false;
		if (!Utils.isLocationsWithinDistance(entity.getLocation(), goal, 0.25) && !Utils.isLocationsWithinDistance(targetLoc, entity.getLocation(), attackRange))
			return false;
		return true;
	}
}

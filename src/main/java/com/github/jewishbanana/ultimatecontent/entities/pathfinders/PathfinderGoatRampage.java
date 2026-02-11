package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderGoatRampage extends CustomPathfinder {
	
	private final double attackDamage;
	private final float attackRangeSquared;
	private final int attackCooldownTicks;
	private final EntityController controller;
	private final PotionEffectType slownessEffect;
	private final float goalDistanceThreshold = 0.0625f;
	private final float entityGoalDistanceThreshold = 0.25f;
	private final double velocityThreshold = 0.02;
	
	private Location goal;
	private LivingEntity target;
	private int cooldown;
	
	public PathfinderGoatRampage(@NotNull Mob m, double attackDamage, float attackRange, int attackCooldownTicks) {
		super(m);
		this.attackDamage = attackDamage;
		this.attackRangeSquared = attackRange * attackRange;
		this.attackCooldownTicks = attackCooldownTicks;
		this.controller = BukkitBrain.getBrain(entity).getController();
		this.slownessEffect = VersionUtils.getSlowness();
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
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation();
		World entityWorld = entityLoc.getWorld();
		if (!targetLoc.getWorld().equals(entityWorld))
			return;
		if (targetLoc.distanceSquared(entityLoc) <= attackRangeSquared) {
			cooldown = 20;
			entity.swingMainHand();
			entity.addPotionEffect(new PotionEffect(slownessEffect, 12, 255, true, false));
			new BukkitRunnable() {
				@Override
				public void run() {
					cooldown = attackCooldownTicks;
					entity.removePotionEffect(slownessEffect);
					LivingEntity temp = entity.getTarget();
					if (temp == null || !temp.isValid())
						return;
					Location tempLoc = temp.getLocation();
					if (!tempLoc.getWorld().equals(entity.getWorld()))
						return;
					entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), tempLoc.add(0, temp.getHeight() / 2.0 + 0.15, 0)).multiply(2.0));
					Vector yOffset = new Vector(0, .3, 0);
					new BukkitRunnable() {
						private int ticks = 20;
						
						@Override
						public void run() {
							if (--ticks <= 0 || !entity.isValid() || entity.getVelocity().lengthSquared() < velocityThreshold) {
								this.cancel();
								return;
							}
							Vector entityVel = entity.getVelocity();
							Vector knockbackVel = entityVel.clone().add(yOffset).multiply(2.5);
							Collection<Entity> nearby = entity.getNearbyEntities(0.7, 0.7, 0.7);
							for (Entity e : nearby) {
								e.setVelocity(knockbackVel);
								if (e instanceof LivingEntity alive)
									alive.damage(attackDamage, entity);
							}
						}
					}.runTaskTimer(Main.getInstance(), 0, 1);
				}
			}.runTaskLater(Main.getInstance(), 12);
		}
	}
	@Override
	public boolean canContinueToUse() {
		LivingEntity target = entity.getTarget();
		if (target == null || !target.equals(this.target) || !target.isValid())
			return false;
		Location targetLoc = target.getLocation();
		if (!Utils.isLocationsWithinDistance(targetLoc, goal, goalDistanceThreshold))
			return false;
		if (!Utils.isLocationsWithinDistance(entity.getLocation(), goal, entityGoalDistanceThreshold) && !Utils.isLocationsWithinDistance(targetLoc, entity.getLocation(), attackRangeSquared))
			return false;
		return true;
	}
}
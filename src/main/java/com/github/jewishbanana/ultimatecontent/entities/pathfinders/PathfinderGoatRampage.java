package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.potion.PotionEffect;
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
	
	private Location goal;
	private LivingEntity target;
	private double attackDamage;
	private double attackRange;
	private int attackCooldownTicks;
	
	private int cooldown;

	public PathfinderGoatRampage(@NotNull Mob m, double attackDamage, double attackRange, int attackCooldownTicks) {
		super(m);
		this.attackDamage = attackDamage;
		this.attackRange = attackRange * attackRange;
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
		if (target.getLocation().distanceSquared(entity.getLocation()) <= attackRange) {
			cooldown = 20;
			entity.swingMainHand();
			entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 12, 255, true, false));
			new BukkitRunnable() {
				@Override
				public void run() {
					cooldown = attackCooldownTicks;
					entity.removePotionEffect(VersionUtils.getSlowness());
					LivingEntity temp = entity.getTarget();
					if (temp == null || !temp.isValid() || !temp.getWorld().equals(entity.getWorld()))
						return;
					entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), temp.getLocation().add(0, temp.getHeight() / 2.0 + 0.15, 0)).multiply(2.0));
					new BukkitRunnable() {
						private int ticks = 20;
						
						@Override
						public void run() {
							if (--ticks <= 0 || !entity.isValid() || entity.getVelocity().lengthSquared() < 0.02) {
								this.cancel();
								return;
							}
							for (Entity e : entity.getNearbyEntities(0.7, 0.7, 0.7)) {
								e.setVelocity(entity.getVelocity().add(new Vector(0, .3, 0)).multiply(2.5));
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
		if (!Utils.isLocationsWithinDistance(targetLoc, goal, 0.0625))
			return false;
		if (!Utils.isLocationsWithinDistance(entity.getLocation(), goal, 0.25) && !Utils.isLocationsWithinDistance(targetLoc, entity.getLocation(), attackRange))
			return false;
		return true;
	}
}

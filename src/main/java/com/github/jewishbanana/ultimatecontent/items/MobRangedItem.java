package com.github.jewishbanana.ultimatecontent.items;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderRangedEntityAttack;

import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

/**
 * Implemented by weapons that a mob should fire at range (bows, thrown weapons). The mob item driver calls
 * {@link #applyMobRangedGoal(Mob)} for the main-hand holder so it shoots instead of only meleeing.
 */
public interface MobRangedItem {

	String RANGED_GOAL_KEY = "uc-mob-ranged-goal";

	/**
	 * Gives the holding mob a ranged attack goal tailored to this weapon.
	 *
	 * @param mob The mob holding the weapon
	 */
	void applyMobRangedGoal(Mob mob);

	/**
	 * Adds a ranged attack goal to the mob a single time, replacing its melee/native-bow attack goals so it uses the supplied
	 * shoot action. No-op if a ranged goal was already applied to this mob.
	 *
	 * @param mob The mob to give the goal
	 * @param interval Ticks between shots
	 * @param minRange The distance it tries to keep from the target
	 * @param maxRange The max distance it will shoot from
	 * @param shoot Fires one projectile from the mob
	 */
	static void addRangedGoal(Mob mob, int interval, double minRange, double maxRange, boolean instantShoot, Consumer<Mob> shoot) {
		if (mob.hasMetadata(RANGED_GOAL_KEY))
			return;
		mob.setMetadata(RANGED_GOAL_KEY, new FixedMetadataValue(UltimateContent.getInstance(), true));
		EntityAI goals = BukkitBrain.getBrain(mob).getGoalAI();
		goals.removeIf(wp -> {
			String name = wp.getPathfinder().getName();
			return name.equals("PathfinderMeleeAttack") || name.equals("PathfinderRangedBowAttack") || name.equals("PathfinderRangedCrossbowAttack");
		});
		goals.put(new PathfinderRangedEntityAttack(mob, interval, minRange, maxRange, 1.0, 2.0, null, shoot, instantShoot), 3);
	}

	/**
	 * Computes a launch velocity toward the target matching how vanilla ranged mobs (skeleton/snow golem/drowned) aim: a
	 * gravity-compensated arc at the given speed.
	 *
	 * @param shooter The mob firing
	 * @param target The target to aim at
	 * @param speed The launch speed (vanilla uses ~1.6)
	 * @return The velocity vector to apply to the projectile
	 */
	static Vector vanillaShootVelocity(Mob shooter, LivingEntity target, double speed) {
		Location from = shooter.getEyeLocation();
		Location to = target.getLocation();
		double dx = to.getX() - from.getX();
		double dy = (to.getY() + target.getHeight() / 3.0) - from.getY();
		double dz = to.getZ() - from.getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		Vector velocity = new Vector(dx, dy + horizontal * 0.2, dz);
		if (velocity.lengthSquared() < 1.0E-6)
			return from.getDirection().multiply(speed);
		return velocity.normalize().multiply(speed);
	}
}

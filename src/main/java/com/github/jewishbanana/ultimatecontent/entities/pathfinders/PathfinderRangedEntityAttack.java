package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.NavigationUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

import me.gamercoder215.mobchip.EntityBody.InteractionHand;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderRangedEntityAttack extends CustomPathfinder {
	
	private static final RandomGenerator random = Utils.random();
	
	private int interval;
	private double minRange;
	private double maxRange;
	private double speedMod;
	private float strafeSpeed;
	private Predicate<Mob> conditions;
	private Consumer<Mob> shootProjectile;
	
	private int attackTime;
	private int usingTicks;
	private int seeTime;
	private int strafingTime;
	private boolean strafingBackwards;
	private boolean strafingClockwise;
	private Location fleeLocation;

	public PathfinderRangedEntityAttack(@NotNull Mob mob, int interval, double minRange, double maxRange, double speedMod, double strafeSpeedMulti, Predicate<Mob> conditions, Consumer<Mob> shootProjectile) {
		super(mob);
		this.interval = interval;
		this.minRange = minRange * minRange;
		this.maxRange = maxRange * maxRange;
		this.speedMod = speedMod;
		this.strafeSpeed = (float) (0.5 * strafeSpeedMulti);
		this.conditions = conditions == null ? entity -> entity.getTarget() != null && entity.hasLineOfSight(entity.getTarget()) : conditions;
		this.shootProjectile = shootProjectile;
	}
	public PathfinderRangedEntityAttack(@NotNull Mob mob, int interval, double minRange, double maxRange, double speedMod) {
		this(mob, interval, minRange, maxRange, speedMod, 1.0, null, null);
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING };
	}
	@Override
	public boolean canStart() {
		return conditions.test(entity);
	}
	@Override
	public void start() {
	}
	public void stop() {
		this.seeTime = 0;
		this.attackTime = -1;
		stopUsingItem(entity);
	}
	@Override
	public void tick() {
		LivingEntity target = entity.getTarget();
		if (target == null)
			return;
		EntityBrain brain = BukkitBrain.getBrain(entity);
		double distance = entity.getLocation().distanceSquared(target.getLocation());
		boolean canSee = entity.hasLineOfSight(target);
		if (canSee != this.seeTime > 0)
			this.seeTime = 0;
		if (canSee) {
			this.seeTime++;
		} else {
			this.seeTime--;
		}
		if (distance < minRange) {
			if (fleeLocation == null || fleeLocation.distanceSquared(target.getLocation()) < minRange)
				fleeLocation = NavigationUtils.findPositionInDirection(entity, Utils.getVectorTowards(target.getLocation(), entity.getLocation()), 3.0, 7.0);
			if (fleeLocation != null)
				brain.getController().moveTo(fleeLocation, speedMod);
			this.strafingTime = -1;
		} else if (distance > maxRange || this.seeTime < 20) {
			brain.getController().moveTo(target, speedMod);
			this.strafingTime = -1;
		} else {
			brain.getController().moveTo(entity.getLocation());
			this.strafingTime++;
		}
		if (this.strafingTime >= 20) {
			if (random.nextFloat() < 0.3D)
				this.strafingClockwise = !this.strafingClockwise;
			if (random.nextFloat() < 0.3D)
				this.strafingBackwards = !this.strafingBackwards;
			this.strafingTime = 0;
		}
		if (this.strafingTime > -1) {
			if (distance > (this.maxRange * 0.75F)) {
				this.strafingBackwards = false;
			} else if (distance < (this.maxRange * 0.25F)) {
				this.strafingBackwards = true;
			}
			brain.getController().strafe(this.strafingBackwards ? -strafeSpeed : strafeSpeed,
					this.strafingClockwise ? strafeSpeed : -strafeSpeed);
			Entity vehicle = entity.getVehicle();
			if (vehicle instanceof Mob mob)
				BukkitBrain.getBrain(mob).getController().lookAt(target);
			brain.getController().lookAt(target);
		} else {
			brain.getController().lookAt(target);
		}
		if (brain.getBody().isUsingItem()) {
			usingTicks++;
			if (!canSee && this.seeTime < -60) {
				stopUsingItem(entity);
			} else if (canSee && usingTicks >= 20) {
				stopUsingItem(entity);
				shootProjectile.accept(entity);
				this.attackTime = this.interval;
			}
		} else if (--this.attackTime <= 0 && this.seeTime >= -60) {
			brain.getBody().useItem(InteractionHand.MAIN_HAND);
		}
	}
	@Override
	public boolean canContinueToUse() {
		LivingEntity target = entity.getTarget();
		if (target == null || target.isDead() || seeTime < -60)
			return false;
		return true;
	}
	private void stopUsingItem(Mob mob) {
		ItemStack item = mob.getEquipment().getItemInMainHand();
		mob.getEquipment().setItemInMainHand(new ItemStack(Material.AIR), true);
		new BukkitRunnable() {
			@Override
			public void run() {
				mob.getEquipment().setItemInMainHand(item, true);
			}
		}.runTaskLater(Main.getInstance(), 2);
		usingTicks = 0;
	}
}

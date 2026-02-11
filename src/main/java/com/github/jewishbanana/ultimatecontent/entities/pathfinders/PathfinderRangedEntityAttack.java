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

import me.gamercoder215.mobchip.EntityBody;
import me.gamercoder215.mobchip.EntityBody.InteractionHand;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.CustomPathfinder;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class PathfinderRangedEntityAttack extends CustomPathfinder {
	
	private static final RandomGenerator random = Utils.getRandomGenerator();
	private static final ItemStack AIR_ITEM = new ItemStack(Material.AIR);
	private static final double STRAFE_THRESHOLD = 0.3;
	private static final int SEE_TIME_THRESHOLD = -60;
	private static final int USING_TICKS_THRESHOLD = 20;
	private static final int STRAFE_TIME_RESET = 20;
	
	private final int interval;
	private final double minRangeSquared;
	private final double maxRangeSquared;
	private final double speedMod;
	private final float strafeSpeed;
	private final Predicate<Mob> conditions;
	private final Consumer<Mob> shootProjectile;
	private final EntityBrain brain;
	private final EntityController controller;
	private final EntityBody body;
	private final double maxRangeInner;
	private final double maxRangeOuter;
	
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
		this.minRangeSquared = minRange * minRange;
		this.maxRangeSquared = maxRange * maxRange;
		this.maxRangeInner = this.maxRangeSquared * 0.25;
		this.maxRangeOuter = this.maxRangeSquared * 0.75;
		this.speedMod = speedMod;
		this.strafeSpeed = (float) (0.5 * strafeSpeedMulti);
		this.conditions = conditions == null ? entity -> entity.getTarget() != null && entity.hasLineOfSight(entity.getTarget()) : conditions;
		this.shootProjectile = shootProjectile;
		this.brain = BukkitBrain.getBrain(entity);
		this.controller = brain.getController();
		this.body = brain.getBody();
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
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation();
		double distance = entityLoc.distanceSquared(targetLoc);
		boolean canSee = entity.hasLineOfSight(target);
		if (canSee != this.seeTime > 0)
			this.seeTime = 0;
		if (canSee) {
			this.seeTime++;
		} else {
			this.seeTime--;
		}
		if (distance < minRangeSquared) {
			if (fleeLocation == null || fleeLocation.distanceSquared(targetLoc) < minRangeSquared)
				fleeLocation = NavigationUtils.findPositionInDirection(entity, Utils.getVectorTowards(targetLoc, entityLoc), 3f, 7f);
			if (fleeLocation != null)
				controller.moveTo(fleeLocation, speedMod);
			this.strafingTime = -1;
		} else if (distance > maxRangeSquared || this.seeTime < STRAFE_TIME_RESET) {
			controller.moveTo(target, speedMod);
			this.strafingTime = -1;
		} else {
			controller.moveTo(entityLoc);
			this.strafingTime++;
		}
		if (this.strafingTime >= STRAFE_TIME_RESET) {
			if (random.nextFloat() < STRAFE_THRESHOLD)
				this.strafingClockwise = !this.strafingClockwise;
			if (random.nextFloat() < STRAFE_THRESHOLD)
				this.strafingBackwards = !this.strafingBackwards;
			this.strafingTime = 0;
		}
		if (this.strafingTime > -1) {
			if (distance > maxRangeOuter) {
				this.strafingBackwards = false;
			} else if (distance < maxRangeInner) {
				this.strafingBackwards = true;
			}
			controller.strafe(this.strafingBackwards ? -strafeSpeed : strafeSpeed,
					this.strafingClockwise ? strafeSpeed : -strafeSpeed);
			Entity vehicle = entity.getVehicle();
			if (vehicle instanceof Mob mob)
				BukkitBrain.getBrain(mob).getController().lookAt(target);
			controller.lookAt(target);
		} else {
			controller.lookAt(target);
		}
		if (body.isUsingItem()) {
			usingTicks++;
			if (!canSee && this.seeTime < SEE_TIME_THRESHOLD) {
				stopUsingItem(entity);
			} else if (canSee && usingTicks >= USING_TICKS_THRESHOLD) {
				stopUsingItem(entity);
				shootProjectile.accept(entity);
				this.attackTime = this.interval;
			}
		} else if (--this.attackTime <= 0 && this.seeTime >= SEE_TIME_THRESHOLD) {
			body.useItem(InteractionHand.MAIN_HAND);
		}
	}
	@Override
	public boolean canContinueToUse() {
		LivingEntity target = entity.getTarget();
		if (target == null || target.isDead() || seeTime < SEE_TIME_THRESHOLD)
			return false;
		return true;
	}
	private void stopUsingItem(Mob mob) {
		ItemStack item = mob.getEquipment().getItemInMainHand();
		mob.getEquipment().setItemInMainHand(AIR_ITEM, true);
		new BukkitRunnable() {
			@Override
			public void run() {
				mob.getEquipment().setItemInMainHand(item, true);
			}
		}.runTaskLater(Main.getInstance(), 2);
		usingTicks = 0;
	}
}
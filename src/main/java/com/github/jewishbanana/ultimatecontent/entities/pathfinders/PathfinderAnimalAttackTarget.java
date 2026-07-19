package com.github.jewishbanana.ultimatecontent.entities.pathfinders;

import java.util.random.RandomGenerator;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Wolf;
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
	private final double moveSpeed;
	private final double leapStrength;
	private final boolean controlLook;
	private final RandomGenerator random = Utils.getRandomGenerator();
	
	private int cooldown;
	private boolean leap;
	
	public PathfinderAnimalAttackTarget(@NotNull Mob m, double attackDamage, float attackRange, int attackCooldownTicks) {
		this(m, attackDamage, attackRange, attackCooldownTicks, 1.5, 0.3, true);
	}
	public PathfinderAnimalAttackTarget(@NotNull Mob m, double attackDamage, float attackRange, int attackCooldownTicks, double moveSpeed, double leapStrength) {
		this(m, attackDamage, attackRange, attackCooldownTicks, moveSpeed, leapStrength, true);
	}
	public PathfinderAnimalAttackTarget(@NotNull Mob m, double attackDamage, float attackRange, int attackCooldownTicks, double moveSpeed, double leapStrength, boolean controlLook) {
		super(m);
		this.attackDamage = attackDamage;
		this.attackRangeSquared = attackRange * attackRange;
		float leapDistance = attackRange + 0.5f;
		this.leapDistanceSquared = leapDistance * leapDistance;
		this.attackCooldownTicks = attackCooldownTicks;
		this.moveSpeed = moveSpeed;
		this.leapStrength = leapStrength;
		this.controlLook = controlLook;
	}
	@Override
	public @NotNull PathfinderFlag[] getFlags() {
		return controlLook ? new PathfinderFlag[] { PathfinderFlag.MOVEMENT, PathfinderFlag.LOOKING } : new PathfinderFlag[] { PathfinderFlag.MOVEMENT };
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
		if (controlLook)
			controller.lookAt(goal.clone().add(0, target.getEyeHeight(), 0));
		controller.moveTo(goal, moveSpeed);
	}
	@Override
	public void tick() {
		if (EntityUtils.isEntityImmunePlayer(target)) {
			entity.setTarget(null);
			return;
		}
		if (controlLook)
			BukkitBrain.getBrain(entity).getController().lookAt(target.getEyeLocation());
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		if (!target.getWorld().equals(entity.getWorld()))
			return;
		Location entityLoc = entity.getLocation();
		Location targetLoc = target.getLocation();
		double distSquared = targetLoc.distanceSquared(entityLoc);
		if (leapStrength > 0 && !leap && distSquared <= leapDistanceSquared) {
			leap = true;
			entity.setVelocity(Utils.getVectorTowards(entityLoc, targetLoc.add(0, target.getHeight() / 2.0, 0)).multiply(leapStrength));
		}
		if (distSquared <= attackRangeSquared && entity.hasLineOfSight(target)) {
			target.damage(attackDamage, entity);
			playAttackSound();
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
	private void playAttackSound() {
		Sound sound;
		float volume = 0.8f;
		float pitch;
		if (entity instanceof Villager || entity instanceof WanderingTrader) {
			sound = Sound.ENTITY_PLAYER_ATTACK_STRONG;
			pitch = random.nextFloat(0.8f, 1.0f);
		} else if (entity instanceof Fox) {
			sound = Sound.ENTITY_FOX_BITE;
			pitch = random.nextFloat(0.85f, 1.05f);
		} else if (entity instanceof Panda) {
			sound = Sound.ENTITY_PANDA_BITE;
			pitch = random.nextFloat(0.75f, 0.95f);
		} else if (entity instanceof Wolf) {
			sound = Sound.ENTITY_WOLF_GROWL;
			pitch = random.nextFloat(0.8f, 1.0f);
		} else if (entity instanceof Llama) {
			sound = Sound.ENTITY_LLAMA_SPIT;
			pitch = random.nextFloat(0.75f, 0.95f);
		} else if (entity instanceof Goat) {
			sound = Sound.ENTITY_GOAT_RAM_IMPACT;
			pitch = random.nextFloat(0.85f, 1.05f);
		} else if (entity instanceof Bee) {
			sound = Sound.ENTITY_BEE_STING;
			pitch = random.nextFloat(0.9f, 1.15f);
		} else if (entity instanceof AbstractHorse) {
			sound = Sound.ENTITY_HORSE_ANGRY;
			pitch = random.nextFloat(0.75f, 0.95f);
		} else {
			sound = Sound.ENTITY_FOX_BITE;
			pitch = entity.getWidth() <= 0.7 ? random.nextFloat(1.2f, 1.5f)
					: entity.getHeight() >= 1.3 ? random.nextFloat(0.65f, 0.85f) : random.nextFloat(0.9f, 1.1f);
		}
		entity.getWorld().playSound(entity.getLocation(), sound, volume, pitch);
	}
}

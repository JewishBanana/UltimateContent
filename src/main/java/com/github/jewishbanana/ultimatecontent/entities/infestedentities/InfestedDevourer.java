package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.Variant;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderBreakBlocks;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.goal.PathfinderClimbPowderedSnow;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class InfestedDevourer extends ComplexEntity<Silverfish> {

	public static final String REGISTERED_KEY = "uc:infested_devourer";
	
	public static Queue<InfestedDevourer> infestedDevourers;
	static {
		infestedDevourers = new ArrayDeque<>();
	}
	
	private enum InfestedDevourerVariant implements Variant {
		
		ALPHA("alpha_infested_devourer") {
			@Override
	        public void initVariant() {
				EntityVariant variant = getEntityVariant();
				variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.INFESTED_ALPHA_DEVOURER.getHead());
				variant.movementSpeed = 0.5;
	        }
		};
		
		private InfestedDevourerVariant(String variantPathName) {
			registerVariant(CustomEntityType.INFESTED_DEVOURER, variantPathName);
		}
	}
	
	private InfestedDevourerVariant variant;
	private double speed;
	private boolean movingToBlock;
	private int wallCreepCooldown = 160;
	
	public Queue<Block> blocks = new ArrayDeque<>();
	
	public InfestedDevourer(Silverfish entity) {
		super(entity, CustomEntityType.INFESTED_DEVOURER);
		this.variant = getEntityVariant(InfestedDevourerVariant.class);
		this.speed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
		infestedDevourers.add(this);
		
		setInvisible(entity);
		entity.setSilent(true);
		entity.setInvulnerable(false);
		entity.setAI(true);
		entity.setGravity(true);
		
		createStands(entity.getLocation(), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(entityVariant.getLoadoutArmor(LoadoutEquipmentSlot.HEAD).getItem());
		}, new Vector(0, -.75, 0)));
		setHeadStand(0);
		
		scheduleTask(new BukkitRunnable() {
			private final EntityController controller = BukkitBrain.getBrain(entity).getController();
			private int damageTicks = 0;
			private Location targetBlock;
			private int inWallTicks = 0;
			private Vector incrementIntoWall;
			private LivingEntity target;
			private boolean leavingWall;
			private Location lastLoc = entity.getLocation();
			
			@Override
			public void run() {
				if (!entity.isValid() || leavingWall)
					return;
				Location entityLoc = entity.getLocation();
				if (inWallTicks > 0) {
					Block block = targetBlock.getBlock();
					if (block.isPassable()) {
						inWallTicks = 0;
						entity.setAI(true);
						entity.setInvulnerable(false);
						entity.setGravity(true);
						targetBlock = null;
						return;
					}
					if (inWallTicks > 50) {
						entity.teleport(entityLoc.add(incrementIntoWall));
						entityLoc.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entityLoc, 3, .2, .2, .2, .1, block.getType().createBlockData());
					}
					if (inWallTicks == 1) {
						Location targetLoc = target != null ? target.getEyeLocation().add(0, 1, 0) : blocks.isEmpty() ? null : blocks.peek().getLocation().add(.5, .5, .5);
						if (target != null && (!target.isValid() || !Utils.isLocationsWithinDistance(entityLoc, target.getLocation(), 625))) {
							incrementIntoWall.multiply(-1);
							leavingWall = true;
						} else if (targetLoc != null) {
							Block spot = Utils.rayTraceForBlock(targetLoc, 9, 1, temp -> temp.getType().isOccluding(), () -> Utils.getRandomizedVector(1.0, 0, 1.0).setY(random.nextDouble()/2-0.7));
							if (spot != null) {
								targetBlock = spot.getLocation().add(.5, .5, .5);
								entity.teleport(targetBlock);
								entityLoc.setDirection(targetLoc.toVector().subtract(entityLoc.toVector()));
								ArmorStand stand = headStand.getEntity(entityLoc);
								stand.setRotation(entityLoc.getYaw(), 0);
								stand.setHeadPose(new EulerAngle(Math.toRadians(entityLoc.getPitch()),0,0));
								incrementIntoWall = Utils.getVectorTowards(targetBlock, targetLoc).multiply(0.1);
								leavingWall = true;
							}
						}
						if (leavingWall)
							new BukkitRunnable() {
								private int moveTicks = 10;
								
								@Override
								public void run() {
									Block block = targetBlock.getBlock();
									if (block.isPassable()) {
										inWallTicks = 0;
										entity.setAI(true);
										entity.setInvulnerable(false);
										entity.setGravity(true);
										leavingWall = false;
										targetBlock = null;
										wallCreepCooldown = 100;
										this.cancel();
										return;
									}
									if (moveTicks-- > 0) {
										Location loc = entity.getLocation();
										entity.teleport(loc.add(incrementIntoWall));
										loc.getWorld().spawnParticle(VersionUtils.getBlockCrack(), loc, 3, .2, .2, .2, .1, block.getType().createBlockData());
									} else {
										inWallTicks = 0;
										entity.setAI(true);
										entity.setInvulnerable(false);
										entity.setGravity(true);
										entity.setTarget(target);
										leavingWall = false;
										targetBlock = null;
										wallCreepCooldown = 100;
										this.cancel();
									}
								}
							}.runTaskTimer(plugin, 0, 1);
					} else
						inWallTicks--;
					return;
				}
				LivingEntity currentTarget = entity.getTarget();
				Location goal = currentTarget == null ? controller.getTargetMoveLocation() : currentTarget.getLocation().add(0, currentTarget.getHeight() / 3.0 * 2.0, 0);
				if (entity.isOnGround()) {
					if (!blocks.isEmpty() && Utils.isLocationsWithinDistance(entityLoc, goal, 3))
						entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 10, 10, true, false));
					else {
						entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), goal).multiply(speed).setY(0.45));
						if (IS_VERSION_19_OR_ABOVE)
							playSound(entity.getLocation(), Sound.BLOCK_SCULK_STEP, 1, 1);
					}
				}
				entityLoc.setDirection((currentTarget == null ? goal.add(entityLoc.getDirection().setY(0).normalize().multiply(2.0)) : goal).toVector().subtract(entityLoc.toVector()));
				ArmorStand stand = headStand.getEntity(entityLoc);
				stand.setRotation(entityLoc.getYaw(), 0);
				stand.setHeadPose(new EulerAngle(Math.toRadians(entityLoc.getPitch()),0,0));
				if (damageTicks == 0) {
					if (isTargetInRange(entity, 0, 1.25)) {
						EntityUtils.damageEntity(currentTarget, entityVariant.damage, "infestedDevourer", entity, DamageCause.ENTITY_ATTACK);
						entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entityLoc.clone().add(entityLoc.getDirection().multiply(0.3)), 5, .2, .2, .2, .1, Material.REDSTONE_BLOCK.createBlockData());
						playSound(entity.getLocation(), Sound.ENTITY_FOX_BITE, 1, .5);
						damageTicks = 15;
						wallCreepCooldown = 100;
					}
				} else
					damageTicks--;
				if (targetBlock != null) {
					if (targetBlock.getBlock().isPassable() || (target == null && blocks.isEmpty())) {
						targetBlock = null;
						wallCreepCooldown = 100;
						return;
					}
					if (currentTarget == null)
						controller.moveTo(targetBlock);
					if (Utils.isLocationsWithinDistance(entityLoc, targetBlock, 1.56)) {
						entity.setAI(false);
						entity.setInvulnerable(true);
						entity.setGravity(false);
						entityLoc.setDirection(targetBlock.toVector().subtract(entityLoc.toVector()));
						stand.setRotation(entityLoc.getYaw(), 0);
						stand.setHeadPose(new EulerAngle(Math.toRadians(entityLoc.getPitch()),0,0));
						inWallTicks = 60;
						incrementIntoWall = Utils.getVectorTowards(entityLoc, targetBlock).multiply(entityLoc.distance(targetBlock) / 10.0);
						movingToBlock = false;
						return;
					}
				}
				if (!Utils.isLocationsWithinDistance(entityLoc, lastLoc, 2.25)) {
					lastLoc = entityLoc;
					wallCreepCooldown = Math.max(wallCreepCooldown, 30);
				}
				if (wallCreepCooldown == 0) {
					if (targetBlock != null) {
						targetBlock = null;
						movingToBlock = false;
					}
					if (isTargetInRange(entity, 9, 625, false) || (!blocks.isEmpty() && !Utils.isLocationsWithinDistance(entityLoc, blocks.peek().getLocation().add(.5, .5, .5), 2.25))) {
						Block b = Utils.rayTraceForBlock(entityLoc.add(0, .5, 0), 8, 5, block -> block.getType().isOccluding(), () -> Utils.getRandomizedVector(1.0, 0, 1.0).setY(random.nextDouble()/2-0.7));
						if (b != null) {
							target = currentTarget;
							entity.setTarget(null);
							targetBlock = b.getLocation().add(.5, .5, .5);
							controller.moveTo(targetBlock);
							movingToBlock = true;
							wallCreepCooldown = 100;
						}
					}
					wallCreepCooldown = 20;
				} else
					wallCreepCooldown--;
			}
		}.runTaskTimer(plugin, 0, 1));
	}
	public void setAIGoals(Silverfish entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getGoalAI();
		goals.clear();
		PathfinderBreakBlocks pathfinder = new PathfinderBreakBlocks(entity, blocks, 10.0, variant == InfestedDevourerVariant.ALPHA ? 30.0 : 6.0, 1.0, 1.8, (float) entityVariant.volume);
		pathfinder.endAction = block -> {
			entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), block.getLocation().add(.5, .5, .5)).multiply(0.5));
			playSound(entity.getLocation(), Sound.ENTITY_FOX_BITE, 1, .5);
			wallCreepCooldown = 50;
		};
		goals.put(pathfinder, 1);
		goals.put(new PathfinderFloat(entity), 2);
		goals.put(new PathfinderClimbPowderedSnow(entity), 2);
		goals.put(new PathfinderMeleeAttack(entity, 1.0, false), 3);
	}
	public void onTargetEntity(EntityTargetEvent event) {
		if (movingToBlock && event.getTarget() != null && event.getReason() != TargetReason.TARGET_ATTACKED_ENTITY)
			event.setCancelled(true);
	}
	public void onChangeBlock(EntityChangeBlockEvent event) {
		event.setCancelled(true);
	}
	public void unload() {
		super.unload();
		infestedDevourers.remove(this);
	}
	public void setAttributes(Silverfish entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25);
		entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.0);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedDevourer.REGISTERED_KEY, InfestedDevourer.class);
		Variant.initVariants(InfestedDevourerVariant.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findSpawnLocation(area, 1);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		return UIEntityManager.spawnEntity(spawn, InfestedDevourer.class);
	};
}

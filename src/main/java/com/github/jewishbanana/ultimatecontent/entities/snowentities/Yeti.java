package com.github.jewishbanana.ultimatecontent.entities.snowentities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.abilities.YetiRoar;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.PhysicsEngine;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBody;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.ai.goal.PathfinderMoveTowardsTarget;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomLook;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomStrollLand;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class Yeti extends ComplexEntity<IronGolem> {
	
	public static final String REGISTERED_KEY = "uc:yeti";
	private static final YetiRoar ability;
	private static final ItemStack SNOW_BLOCK_ITEM = new ItemStack(Material.SNOW_BLOCK);
	private static final ItemStack PACKED_ICE_ITEM = new ItemStack(Material.PACKED_ICE);
	private static final EulerAngle ARM_ANGLE_1 = new EulerAngle(0, 0.9, 0.4);
	private static final EulerAngle ARM_ANGLE_2 = new EulerAngle(0, 0.9, -0.4);
	private static final EulerAngle TAIL_ANGLE = new EulerAngle(0.4, 0, 0);
	static {
		ability = AbilityAttributes.createInitializedAbilityInstance(YetiRoar.class);
		ability.damage = 8.0;
		ability.range = 12.0;
		ability.freezeTicks = 200;
	}
	
	public Yeti(IronGolem entity) {
		super(entity, CustomEntityType.YETI, false);
		
		entity.setCanPickupItems(false);
		entity.setSilent(true);
		
		headStand = new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(CustomHead.YETI.getHead());
		});
		createStands(entity.getLocation(), headStand, new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
			stand.setHeadPose(ARM_ANGLE_1);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
			stand.setHeadPose(ARM_ANGLE_2);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(PACKED_ICE_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(PACKED_ICE_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(PACKED_ICE_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(PACKED_ICE_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(PACKED_ICE_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(PACKED_ICE_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
		}), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(SNOW_BLOCK_ITEM);
			stand.setHeadPose(TAIL_ANGLE);
		}));
		
		scheduleTask(new BukkitRunnable() {
			private EntityBody body = BukkitBrain.getBrain(entity).getBody();
			private Location step = entity.getLocation();
			private boolean foundTarget;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Location loc = entity.getLocation();
				Vector forward = loc.getDirection();
				headStand.getEntity(loc).setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0, 0));
				Location bodyLoc = loc.clone();
				bodyLoc.setYaw(body.getBodyRotation());
				double rotation = Math.toRadians(body.getBodyRotation());
				Vector right = new Vector(Math.sin(rotation), 0, -Math.cos(rotation)).rotateAroundY(Math.toRadians(90));
				Vector left = right.clone().multiply(-1);
				Vector bodyDir = bodyLoc.getDirection();
				Vector bodyDirReverse = bodyDir.clone().multiply(-1);
				Vector forwardX035 = forward.clone().multiply(0.35);
				Vector forwardX030 = forward.clone().multiply(0.3);
				Vector forwardX015 = forward.clone().multiply(0.15);
				Vector bodyDirX025 = bodyDir.clone().multiply(0.25);
				Vector bodyDirX015 = bodyDir.clone().multiply(0.15);
				Vector bodyDirX010 = bodyDir.clone().multiply(0.1);
				Vector bodyDirRevX025 = bodyDirReverse.clone().multiply(0.25);
				Vector bodyDirRevX015 = bodyDirReverse.clone().multiply(0.15);
				Vector bodyDirRevX010 = bodyDirReverse.clone().multiply(0.1);
				Vector leftX030 = left.clone().multiply(0.3);
				Vector leftX020 = left.clone().multiply(0.2);
				Vector leftX035 = left.clone().multiply(0.35);
				Vector leftX060 = left.clone().multiply(0.6);
				Vector rightX030 = right.clone().multiply(0.3);
				Vector rightX020 = right.clone().multiply(0.2);
				Vector rightX035 = right.clone().multiply(0.35);
				Vector rightX060 = right.clone().multiply(0.6);
				Vector perpRight = new Vector(forward.getZ(), 0, -forward.getX()).multiply(0.2);
				Vector perpLeft = new Vector(-forward.getZ(), 0, forward.getX()).multiply(0.2);
				for (int i=0; i < stands.length; i++) {
					Entity temp = getCreatureStandEntity(i, loc);
					switch (i) {
					case 0:
						temp.teleport(loc.clone().add(0, 0.7, 0).add(forwardX035));
						break;
					case 1:
						temp.teleport(loc.clone().add(0, 0.4, 0).add(forwardX030));
						break;
					case 2:
						temp.teleport(loc.clone().add(0, 0.5, 0).add(perpRight).add(forwardX015));
						break;
					case 3:
						temp.teleport(loc.clone().add(0, 0.5, 0).add(perpLeft).add(forwardX015));
						break;
					case 4:
						temp.teleport(bodyLoc.clone().add(0, 0.1, 0).add(leftX030).add(bodyDirX025));
						break;
					case 5:
						temp.teleport(bodyLoc.clone().add(0, 0.1, 0).add(rightX030).add(bodyDirX025));
						break;
					case 6:
						temp.teleport(bodyLoc.clone().add(0, -0.4, 0).add(leftX020).add(bodyDirX015));
						break;
					case 7:
						temp.teleport(bodyLoc.clone().add(0, -0.4, 0).add(rightX020).add(bodyDirX015));
						break;
					case 8:
						temp.teleport(bodyLoc.clone().add(0, -0.9, 0).add(leftX035).add(bodyDirX010));
						break;
					case 9:
						temp.teleport(bodyLoc.clone().add(0, -0.9, 0).add(rightX035).add(bodyDirX010));
						break;
					case 10:
						temp.teleport(bodyLoc.clone().add(0, -1.4, 0).add(leftX035));
						break;
					case 11:
						temp.teleport(bodyLoc.clone().add(0, -1.4, 0).add(rightX035));
						break;
					case 12:
						temp.teleport(bodyLoc.clone().add(0, 0.1, 0).add(leftX030).add(bodyDirRevX025));
						break;
					case 13:
						temp.teleport(bodyLoc.clone().add(0, 0.1, 0).add(rightX030).add(bodyDirRevX025));
						break;
					case 14:
						temp.teleport(bodyLoc.clone().add(0, -0.4, 0).add(leftX020).add(bodyDirRevX015));
						break;
					case 15:
						temp.teleport(bodyLoc.clone().add(0, -0.4, 0).add(rightX020).add(bodyDirRevX015));
						break;
					case 16:
						temp.teleport(bodyLoc.clone().add(0, 0.15, 0).add(leftX060));
						break;
					case 17:
						temp.teleport(bodyLoc.clone().add(0, 0.15, 0).add(rightX060));
						break;
					case 18:
						temp.teleport(loc.clone().add(0, 0.6, 0).add(bodyDirRevX010));
						break;
					}
				}
				if (!Utils.isLocationsWithinDistance(loc, step, 0.8f) && entity.isOnGround()) {
					step = loc;
					playSound(loc, Sound.BLOCK_SNOW_STEP, 2f, .5f);
				}
				LivingEntity target = entity.getTarget();
				if (!foundTarget) {
					if (target != null && target.isValid()) {
						foundTarget = true;
						playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 2f, .5f);
					}
				} else if (target == null || !target.isValid())
						foundTarget = false;
			}
		}.runTaskTimer(plugin, 0, 1));
		scheduleTask(new BukkitRunnable() {
			private int cooldown;
			private Location past;
			private int time;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (cooldown > 0) {
					cooldown--;
					return;
				}
				if (past != null) {
					LivingEntity target = entity.getTarget();
					if (target == null || !target.isValid()) {
						past = null;
						time = 0;
						return;
					}
					if (!Utils.isLocationsWithinDistance(entity.getLocation(), past, 0.1f)) {
						past = null;
						time = 0;
						return;
					}
					if (time++ >= 4) {
						time = 0;
						past = null;
						Location temp = entity.getLocation();
						entity.setVelocity(Utils.getVectorTowards(temp, target.getLocation()).setY(0.9));
						playSound(temp, Sound.ENTITY_PANDA_BITE, 2f, .5f);
						new BukkitRunnable() {
							private int ticks;
							
							@Override
							public void run() {
								if (ticks++ >= 20 || !target.isValid()) {
									this.cancel();
									return;
								}
								Location temp = entity.getLocation();
								entity.setVelocity(entity.getVelocity().add(Utils.getVectorTowards(temp, target.getLocation()).multiply(0.03)));
								if (entity.isOnGround()) {
									this.cancel();
									playSound(temp, Sound.BLOCK_GLASS_FALL, 2f, .5f);
									playSound(temp, Sound.ENTITY_PLAYER_HURT_FREEZE, 2f, .6f);
								}
							}
						}.runTaskTimer(plugin, 1, 1);
					}
					return;
				}
				LivingEntity target = entity.getTarget();
				if (target == null || !target.isValid())
					return;
				past = entity.getLocation();
			}
		}.runTaskTimer(plugin, 0, 5));
		scheduleTask(new BukkitRunnable() {
			private int cooldown;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (cooldown > 0) {
					cooldown--;
					return;
				}
				LivingEntity target = entity.getTarget();
				if (target == null || !target.isValid())
					return;
				if (Utils.isLocationsWithinDistance(entity.getLocation(), target.getLocation(), 49)) {
					cooldown = 15;
					entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 40, 5, true, false));
					ability.setVolume((float) entityVariant.volume);
					ability.activate(entity.getLocation(), entity, null);
				}
			}
		}.runTaskTimer(plugin, 0, 10));
		
		makeParticleTask(entity, 1, () -> {
			entity.getWorld().spawnParticle(Particle.SNOWFLAKE, entity.getLocation().add(0, 1.4, 0), 10, .5, .6, .5, 0.04);
		});
	}
	public void setAIGoals(IronGolem entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, new EntityType[0]), 1);
		goals.put(new PathfinderNearestAttackableTarget<Player>(entity, Player.class), 2);
		goals.put(new PathfinderNearestAttackableTarget<Animals>(entity, Animals.class), 3);
		
		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 0);
		goals.put(new PathfinderMeleeAttack(entity), 4);
		goals.put(new PathfinderMoveTowardsTarget(entity), 5);
		goals.put(new PathfinderRandomStrollLand(entity), 6);
		goals.put(new PathfinderLookAtEntity<LivingEntity>(entity, LivingEntity.class), 7);
		goals.put(new PathfinderRandomLook(entity), 8);
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(VersionUtils.getSnowShovel(), event.getEntity().getLocation().add(0, 1.5, 0), 40, .3, .4, .3, 0.01);
		if (VersionUtils.displaysAllowed && getSectionBoolean("deathRagdoll", false)) {
			Location loc = event.getEntity().getLocation();
			int lifeTicks = (int) (getSectionDouble("ragdollSeconds", 7.0) * 20.0);
			ArmorStand head = headStand.getEntity(loc);
			PhysicsEngine.dropBlockWithPhysics(head.getLocation().add(0, 1.5, 0), head.getEquipment().getHelmet(), 1f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			for (int i=1; i < stands.length; i++) {
				ArmorStand stand = (ArmorStand) getCreatureStandEntity(i, loc);
				PhysicsEngine.dropBlockWithPhysics(stand.getLocation().add(0, 1.5, 0), stand.getEquipment().getHelmet(), 0.6f, Utils.getRandomizedVector().multiply(0.2), 0.06, lifeTicks);
			}
		}
	}
	public void setAttributes(IronGolem entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(Yeti.REGISTERED_KEY, Yeti.class);
		type.setSpawnConditions(event -> {
			Location loc = event.getLocation();
			if (!BlockUtils.isBlockColdBiome(loc.getBlock()) || !SpawnUtils.canMonsterSpawn(loc))
				return false;
			if (!Utils.isAreaClear(loc, 1.8f, 2.5f))
				return false;
			return true;
		});
	}
}
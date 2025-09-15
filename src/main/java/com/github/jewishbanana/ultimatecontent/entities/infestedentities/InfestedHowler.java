package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderMaintainTargetDistance;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.SpawnUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomLook;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomStrollLand;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class InfestedHowler extends ComplexEntity<Zombie> {

	public static final String REGISTERED_KEY = "uc:infested_howler";
	
	private double spawnWardenChance;
	private boolean animation;
	private int shriek;
	private LivingEntity aliveWarden;

	public InfestedHowler(Zombie entity) {
		super(entity, CustomEntityType.INFESTED_HOWLER, false);
		
		setInvisible(entity);
		entity.setBaby();
		entity.setSilent(true);
		
		createStands(entity.getLocation(), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.getEquipment().setHelmet(CustomHead.INFESTED_HOWLER.getHead());
		}, new Vector(0, -1.4, 0)), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
		}, new Vector(0, -.6, 0)));
		setHeadStand(1);
		
		scheduleTask(new BukkitRunnable() {
			private double animationOffset;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Location loc = entity.getLocation();
				if (shriek > 0) {
					if (IS_VERSION_19_OR_ABOVE && shriek % 5 == 0)
						entity.getWorld().spawnParticle(Particle.SHRIEK, entity.getEyeLocation(), 1, 0, 0, 0, 0.1, 0);
					shriek--;
					if (shriek == 0) {
						LivingEntity target = entity.getTarget();
						if (target != null && target.isValid()) {
							for (Entity e : entity.getNearbyEntities(30, 30, 30))
								if (e instanceof Monster monster) {
									if (IS_VERSION_19_OR_ABOVE && monster.getType() == EntityType.WARDEN)
										((org.bukkit.entity.Warden) monster).increaseAnger(target, 100);
									if (monster.getTarget() == null)
										monster.setTarget(target);
								}
//							for (InfestedCaves cave : DeathMessages.infestedcaves)
//								if (cave.getLocation().getWorld().equals(entity.getWorld()) && cave.getLocation().distanceSquared(entity.getLocation()) <= cave.getSizeSquared()) {
//									cave.shriekEvent(entity.getTarget(), (rand.nextDouble()*100 < (double) entityType.grabCustomSetting("warden_notify_chance")) ? true : false);
//									cave.closeRoute(entity.getTarget(), 8, Utils.getVectorTowards(entity.getLocation(), entity.getTarget().getLocation().add(0,1,0)));
//									return;
//								}
							if (IS_VERSION_19_OR_ABOVE && (aliveWarden == null || !aliveWarden.isValid()) && random.nextDouble() < spawnWardenChance)
								spawnWarden(loc, target);
						}
					}
				}
				CreatureStand<?> stand = getCreatureStand(0);
				stand.getEntity(loc).teleport(loc.clone().add(stand.getOffset()));
				ArmorStand head = headStand.getEntity(loc);
				if (animation) {
					if (animationOffset < -0.1)
						animationOffset += 0.02;
				} else if (animationOffset > -0.6) {
					animationOffset -= 0.02;
					if (animationOffset <= -0.6)
						head.getEquipment().setHelmet(new ItemStack(Material.AIR));
				}
				head.teleport(loc.clone().add(0, animationOffset, 0));
				if (entity.getTarget() != null) {
					Location newLocation = loc.clone().setDirection(entity.getTarget().getEyeLocation().subtract(0, 1, 0).toVector().subtract(loc.toVector()));
					head.setRotation(newLocation.getYaw(), 0);
					head.setHeadPose(new EulerAngle(Math.toRadians(newLocation.getPitch()), 0, 0));
				} else
					head.setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0, 0));
			}
		}.runTaskTimer(plugin, 0, 1));
		scheduleTask(new BukkitRunnable() {
			private int cooldown = 4;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (cooldown > 0) {
					cooldown--;
					return;
				}
				if (isTargetInRange(entity, 0, 36)) {
					cooldown = 12;
					shriek = 40;
					entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 40, 10, true, false));
					Location loc = entity.getLocation();
					if (!animation) {
						animation = true;
						headStand.getEntity(loc).getEquipment().setHelmet(CustomHead.INFESTED_HOWLER_EYES.getHead());
					}
					if (IS_VERSION_19_OR_ABOVE)
						playSound(loc, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 10, .5);
				} else
					animation = false;
			}
		}.runTaskTimer(plugin, 0, 10));
		
		makeParticleTask(entity, VersionUtils.getBlockCrack(), new Vector(), 1, .2, .05, .2, .1, BaseEntity.infestedEntityParticles);
	}
	public void setAIGoals(Zombie entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, new EntityType[0]), 1);
		goals.put(new PathfinderNearestAttackableTarget<Player>(entity, Player.class, 10, true, false), 2);
		
		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 1);
		goals.put(new PathfinderMaintainTargetDistance(entity, 5.5, 5.9), 2);
		goals.put(new PathfinderRandomStrollLand(entity), 3);
		goals.put(new PathfinderLookAtEntity<Player>(entity, Player.class), 4);
		goals.put(new PathfinderLookAtEntity<LivingEntity>(entity, LivingEntity.class), 5);
		goals.put(new PathfinderRandomLook(entity), 6);
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
		this.spawnWardenChance = getSectionDouble("spawnWardenChance", 25.0) / 100.0;
	}
	private void spawnWarden(Location location, LivingEntity target) {
		for (int i=2; i < 30; i++) {
			Location spawn = EntityUtils.findSmartYSpawn(location, location.clone().add(random.nextInt(-i-1, i), 0, random.nextInt(-i-1, i)), 3.0, 5);
			if (spawn == null || !Utils.isAreaClear(Utils.getCenterOfBlock(spawn.getBlock()).add(0, 1, 0), 1.95) || spawn.getBlock().getRelative(BlockFace.DOWN).isPassable() || spawn.getBlock().getRelative(BlockFace.DOWN, 2).isPassable() || spawn.getBlock().getRelative(BlockFace.DOWN, 3).isPassable())
				continue;
			aliveWarden = (LivingEntity) location.getWorld().spawnEntity(spawn, EntityType.WARDEN);
			aliveWarden.setInvisible(true);
			Utils.mergeEntityData(aliveWarden, "{Brain:{memories:{\"minecraft:dig_cooldown\":{ttl:1200L,value:{}},\"minecraft:is_emerging\":{ttl:134L,value:{}}}}}");
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> aliveWarden.setInvisible(false), 10);
			((org.bukkit.entity.Warden) aliveWarden).setAnger(target, 60);
			break;
		}
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedHowler.REGISTERED_KEY, InfestedHowler.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
	public static final Function<Location, BaseEntity<?>> attemptSpawn = area -> {
		Location spawn = SpawnUtils.findSpawnLocation(area, 1);
		if (spawn == null || spawn.getBlock().getBiome() != Biome.DEEP_DARK)
			return null;
		return UIEntityManager.spawnEntity(spawn, InfestedHowler.class);
	};
}

package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.entities.Variant;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderAvoidBrightAreas;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderClimbPowderedSnow;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderMeleeAttack;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class ShadowLeech extends ComplexEntity<Silverfish> {
	
	public static final String REGISTERED_KEY = "uc:shadow_leech";
	
	private enum ShadowLeechVariant implements Variant {
		
		BLOODY("bloody_shadow_leech") {
			@Override
	        public void initVariant() {
				EntityVariant variant = getEntityVariant();
				variant.defaultLoadout.addEquipmentSlotDefaults(LoadoutEquipmentSlot.HEAD, CustomHead.BLOODY_LEECH.getHead());
				variant.movementSpeed = 0.3;
	        }
		};
		
		private ShadowLeechVariant(String variantPathName) {
			registerVariant(CustomEntityType.SHADOW_LEECH, variantPathName);
		}
	}
	
	private static byte maxLightLevel = 11;
	
	private ShadowLeechVariant variant;
	private LivingEntity attached;
	private Team team;

	public ShadowLeech(Silverfish entity) {
		super(entity, CustomEntityType.SHADOW_LEECH);
		this.variant = getEntityVariant(ShadowLeechVariant.class);
		
		setInvisible(entity);
		entity.setSilent(true);
		entity.setGravity(true);
		entity.setAI(true);
		
		createStands(entity.getLocation(), new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
			initStand(stand);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(entityType.getLoadoutArmor(LoadoutEquipmentSlot.HEAD).getItem());
		}, new Vector(0, -.75, 0)));
		setHeadStand(0);
		
		scheduleTask(new BukkitRunnable() {
			private Location step = entity.getLocation();
			private BlockData blockData = Material.REDSTONE_BLOCK.createBlockData();
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				int light = entity.getLocation().getBlock().getLightLevel();
				if (attached == null || attached.isDead()) {
					if (isTargetInRange(entity, 2.25, 9)) {
						entity.setVelocity(Utils.getVectorTowards(entity.getLocation(), entity.getTarget().getLocation().add(0, entity.getTarget().getHeight() / 2.0 + random.nextDouble(), 0)));
						entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ITEM_FRAME_BREAK, getSoundCategory(), 1f, .5f);
					}
					if (Utils.isLocationsWithinDistance(entity.getLocation(), step, 0.8) && entity.isOnGround()) {
						step = entity.getLocation();
						playSound(entity.getLocation(), Sound.ENTITY_ENDERMITE_STEP, .15, .8);
					}
					if (light >= maxLightLevel) {
						entity.getWorld().spawnParticle(Particle.CLOUD, entity.getLocation().add(0, .4, 0), 4, .15, .15, .15, 0.000001);
						playSound(entity.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, .5, 2);
						entity.damage(1.0);
					}
					return;
				}
				if (light >= maxLightLevel) {
					if (variant != ShadowLeechVariant.BLOODY) {
						entity.setVelocity(Utils.getVectorTowards(attached.getLocation().add(0, attached.getHeight() / 2.0, 0), entity.getLocation()).multiply(0.5));
						attached = null;
					}
					entity.getWorld().spawnParticle(Particle.CLOUD, entity.getLocation().add(0,.4,0), 4, .15, .15, .15, 0.000001);
					playSound(entity.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, .5, 2);
					entity.damage(1.0);
				}
				if (variant == ShadowLeechVariant.BLOODY)
					entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entity.getLocation(), 8, .1, .1, .1, 1, blockData);
				else
					entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entity.getLocation(), 3, .1, .1, .1, 1, blockData);
				EntityUtils.pureDamageEntity(attached, entityType.damage, "shadowLeech", entity, DamageCause.ENTITY_ATTACK);
				playSound(entity.getLocation(), Sound.ENTITY_ENDERMITE_DEATH, .5, .8);
			}
		}.runTaskTimer(plugin, 0, 10));
		
		if (variant == ShadowLeechVariant.BLOODY)
			makeParticleTask(entity, VersionUtils.getBlockCrack(), new Vector(), 1, .2, .2, .2, 1, Material.REDSTONE_BLOCK.createBlockData());
	}
	public void setAIGoals(Silverfish entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderAvoidBrightAreas(entity, (byte) 11, 5.0), 1);
		goals.put(new PathfinderFloat(entity), 2);
		goals.put(new PathfinderClimbPowderedSnow(entity), 2);
		goals.put(new PathfinderMeleeAttack(entity, 1.0, false), 3);
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		if (attached != null || !(event.getEntity() instanceof LivingEntity alive))
			return;
		Silverfish entity = getCastedEntity();
		Vector offsetOfAttach = entity.getLocation().toVector().setY(0).subtract(alive.getLocation().toVector().setY(0)).normalize().multiply((entity.getLocation().getY()-alive.getLocation().getY())*0.25).setY(Math.min(entity.getLocation().toVector().subtract(alive.getLocation().toVector()).getY(), alive.getHeight()));
		try {
			offsetOfAttach.checkFinite();
		} catch (IllegalArgumentException e) {
			return;
		}
		attached = alive;
		entity.setAI(false);
		entity.setGravity(false);
		EntityUtils.makeEntityFaceLocation(entity, attached.getLocation().add(0, attached.getHeight() / 3.0 * 2.0, 0));
		Vector facingDirection = entity.getLocation().getDirection();
		Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
		team = sb.registerNewTeam(("uc-sl-"+entity.getUniqueId()).substring(0, 16));
		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		team.addEntry(entity.getUniqueId().toString());
		new BukkitRunnable() {
			@Override
			public void run() {
				if (entity == null || entity.isDead() || attached == null || attached.isDead()) {
					this.cancel();
					attached = null;
					Silverfish entity = getCastedEntity();
					if (entity != null) {
						entity.setAI(true);
						entity.setGravity(true);
						entity.setTarget(null);
					}
					if (team != null) {
						team.unregister();
						team = null;
					}
					return;
				}
				entity.teleport(attached.getLocation().add(offsetOfAttach).setDirection(facingDirection));
				entity.setVelocity(attached.getVelocity());
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(VersionUtils.getNormalSmoke(), event.getEntity().getLocation().add(0,.25,0), 30, .15, .15, .15, .000001);
	}
	public void onChangeBlock(EntityChangeBlockEvent event) {
		event.setCancelled(true);
	}
	public void unload() {
		super.unload();
		if (team != null)
			team.unregister();
		team = null;
	}
	public void setAttributes(Silverfish entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
		entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.0);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(ShadowLeech.REGISTERED_KEY, ShadowLeech.class);
		Variant.initVariants(ShadowLeechVariant.class);
		
		type.setSpawnConditions(event -> {
			if (!(event.getEntity() instanceof Monster))
				return false;
			Location loc = event.getLocation();
			if (loc.getBlock().getLightLevel() >= maxLightLevel || !Utils.isEnvironment(loc.getWorld(), Environment.NORMAL, Environment.THE_END))
				return false;
			return true;
		});
	}
}

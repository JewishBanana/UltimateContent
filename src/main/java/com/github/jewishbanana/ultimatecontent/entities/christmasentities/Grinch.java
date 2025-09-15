package com.github.jewishbanana.ultimatecontent.entities.christmasentities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.items.christmas.CursedCandyCane;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.specialevents.ChristmasEvent;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class Grinch extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:grinch";
	
	private boolean cursedVariant;

	public Grinch(Zombie entity) {
		super(entity, CustomEntityType.GRINCH);
		
		EntitiesHandler.makeEntityNoSunlightCombust(entity);
		entity.setSilent(true);
		entity.setCanPickupItems(false);
		entity.setMetadata("uc-christmasmobs", Main.getFixedMetadata());
		
		if (random.nextDouble() * 100 < getSectionDouble("cursed_candy_cane_spawn", 0.0)) {
			entity.getEquipment().setItemInMainHand(UIItemType.getItem(CursedCandyCane.class), true);
			cursedVariant = true;
		}
		
		scheduleTask(new BukkitRunnable() {
			private int abilityCooldown;
			
			@Override
			public void run() {
				if (abilityCooldown > 0) {
					abilityCooldown--;
					return;
				}
				if (!entity.isValid())
					return;
				if (isTargetInRange(entity, 30, 350)) {
					abilityCooldown = cursedVariant ? 4 : 6;
					entity.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 15, 10, true, false));
					ItemStack weapon = entity.getEquipment().getItemInMainHand();
					entity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
					ArmorStand stand = entity.getWorld().spawn(entity.getLocation().add(0, 100, 0), ArmorStand.class, temp -> {
						ComplexEntity.initStand(temp);
						temp.getEquipment().setItemInMainHand(entity.getEquipment().getItemInMainHand());
						temp.setRightArmPose(new EulerAngle(0, 0, 0));
						EntitiesHandler.attachRemoveKey(temp);
					});
					Slime slime = entity.getWorld().spawn(entity.getLocation().add(0, 100, 0), Slime.class, temp -> {
						temp.setSize(0);
						temp.setSilent(true);
						temp.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
						temp.setHealth(100.0);
						temp.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.0);
						temp.setInvisible(true);
						temp.teleport(entity);
						temp.teleport(temp.getLocation().add(0,1,0));
						temp.setVelocity(Utils.getVectorTowards(entity.getLocation(), entity.getTarget().getLocation()).multiply(1.5).add(new Vector(0,entity.getLocation().distance(entity.getTarget().getLocation())/20,0)));
						EntitiesHandler.attachRemoveKey(temp);
					});
					entity.swingMainHand();
					new BukkitRunnable() {
						@Override
						public void run() {
							if (stand == null || stand.isDead() || slime == null || slime.isDead() || entity == null) {
								if (entity != null)
									entity.getEquipment().setItemInMainHand(weapon);
								this.cancel();
								return;
							}
							if (cursedVariant && entity.getTarget() != null && entity.getTarget().getWorld().equals(entity.getWorld()))
								slime.setVelocity(slime.getVelocity().add(Utils.getVectorTowards(slime.getLocation(), entity.getTarget().getLocation().add(0,entity.getTarget().getHeight()/2,0)).multiply(0.2)));
							stand.teleport(slime.getLocation().add(0,-1,0));
							Vector vec = slime.getVelocity();
							double pivot = Math.abs(vec.getX());
							if (Math.abs(vec.getZ()) > pivot)
								pivot = Math.abs(vec.getZ());
							double angle = Math.toDegrees(Math.atan2(Math.abs(vec.getY()), pivot));
							if (vec.getY() >= 0)
								stand.setRightArmPose(new EulerAngle(Math.toRadians(360-angle), 0, 0));
							else
								stand.setRightArmPose(new EulerAngle(Math.toRadians(360+angle), 0, 0));
							if (cursedVariant)
								stand.getWorld().spawnParticle(VersionUtils.getBlockCrack(), stand.getLocation().add(0,1,0), 2, .1, .1, .1, 1, Material.PURPLE_WOOL.createBlockData());
							else
								stand.getWorld().spawnParticle(VersionUtils.getBlockCrack(), stand.getLocation().add(0,1,0), 2, .1, .1, .1, 1, Material.RED_WOOL.createBlockData());
							stand.getWorld().spawnParticle(VersionUtils.getBlockCrack(), stand.getLocation().add(0,1,0), 2, .1, .1, .1, 1, Material.SNOW.createBlockData());
							if (slime.isOnGround()) {
								entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entity.getLocation().add(0,1.2,0), 30, .4, .6, .4, 1, Material.SNOW.createBlockData());
								entity.getWorld().spawnParticle(VersionUtils.getBlockDust(), entity.getLocation().add(0,1.2,0), 30, .4, .6, .4, 1, Material.SNOW.createBlockData());
								entity.teleport(slime);
								stand.remove();
								slime.remove();
								plugin.getServer().getScheduler().runTaskLater(plugin, () -> entity.getEquipment().setItemInMainHand(weapon), 3);
								this.cancel();
								return;
							}
							for (Entity e : slime.getNearbyEntities(.5, .5, .5))
								if (!e.equals(entity) && e instanceof LivingEntity alive && !EntityUtils.isEntityImmunePlayer(entity)) {
									EntityUtils.pureDamageEntity(alive, 7.0, "dd-candycane", entity, DamageCause.PROJECTILE);
									alive.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, true, false));
									if (cursedVariant)
										alive.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 2, true, false));
									entity.setHealth(Math.min(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), entity.getHealth()+12.0));
									entity.getWorld().spawnParticle(VersionUtils.getBlockCrack(), entity.getLocation().add(0,1.2,0), 30, .4, .6, .4, 1, Material.SNOW.createBlockData());
									entity.getWorld().spawnParticle(VersionUtils.getBlockDust(), entity.getLocation().add(0,1.2,0), 30, .4, .6, .4, 1, Material.SNOW.createBlockData());
									entity.teleport(slime);
									entity.getWorld().spawnParticle(Particle.COMPOSTER, entity.getLocation().add(0,1.2,0), 10, .3, .5, .3, .001);
									stand.remove();
									slime.remove();
									plugin.getServer().getScheduler().runTaskLater(plugin, () -> entity.getEquipment().setItemInMainHand(weapon), 3);
									this.cancel();
									return;
								}
						}
					}.runTaskTimer(plugin, 0, 1);
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		BlockData limeWool = Material.LIME_WOOL.createBlockData();
		BlockData redWool = Material.RED_WOOL.createBlockData();
		makeParticleTask(entity, 1, () -> {
			if (random.nextInt(4) != 0)
				return;
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0, entity.getHeight() / 2, 0), 2, .3, .5, .3, 1, limeWool);
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().add(0, entity.getHeight() / 2, 0), 2, .3, .5, .3, 1, redWool);
		});
	}
	public void onTargetLivingEntity(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() != null
				&& event.getTarget().hasMetadata("uc-christmasmobs")
				&& !(UIEntityManager.getEntity(event.getTarget()) instanceof TameableEntity tameable && tameable.getOwner() != null && !tameable.getOwner().equals(event.getEntity().getUniqueId())))
			event.setCancelled(true);
	}
	public void unload() {
		super.unload();
		EntitiesHandler.removeEntitiyNoSunlightCombust(getUniqueId());
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(Grinch.REGISTERED_KEY, Grinch.class);
		
		type.setSpawnConditions(ChristmasEvent.eventEntitySpawnCondition);
	}
}

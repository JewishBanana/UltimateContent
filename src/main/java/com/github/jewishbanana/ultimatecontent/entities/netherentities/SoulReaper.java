package com.github.jewishbanana.ultimatecontent.entities.netherentities;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class SoulReaper extends ComplexEntity<Skeleton> {
	
	public static final String REGISTERED_KEY = "uc:soul_reaper";
	private static final Set<DamageCause> immuneDamageTypes = Set.of(DamageCause.DROWNING, DamageCause.FALL, DamageCause.FIRE, DamageCause.FIRE_TICK, DamageCause.FREEZE, DamageCause.HOT_FLOOR, DamageCause.LAVA, DamageCause.SUFFOCATION, DamageCause.WITHER, DamageCause.POISON);

	public SoulReaper(Skeleton entity) {
		super(entity, CustomEntityType.SOUL_REAPER, false);
		
		entity.setSilent(true);
		entity.setAI(false);
		entity.setGravity(false);
		
		createStands(entity.getLocation(), new CreatureStand<Vex>(Vex.class, stand -> {
			setInvisible(stand);
			stand.setCollidable(false);
			stand.setInvulnerable(true);
			stand.setSilent(true);
			stand.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
			stand.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(entityType.damage);
			stand.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK).setBaseValue(entityType.knockback);
			
			/**
			 * Pointless attribute as Vex uses its own implementation of MoveControl which does not utilize any attributes for speed
			 * TODO:
			 * - Create implementation of VexMoveControl with a setter for the speedModifier variable
			 */
			stand.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(entityType.movementSpeed);
			
			stand.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
			stand.setCustomName(entityType.displayName);
			stand.setCustomNameVisible(entityType.nameVisible);
			setTargetGoals(stand);
			EntitiesHandler.attachRemoveKey(stand);
		}, new Vector(0, -.3, 0)));
		
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				Entity vex = getCreatureStandEntity(0);
				if (vex == null || !vex.isValid())
					return;
				entity.teleport(vex.getLocation().subtract(0, .3, 0));
				LivingEntity target = ((Mob) vex).getTarget();
				if (target != null && target.isValid())
					EntityUtils.makeEntityFaceLocation(entity, target.getLocation());
			}
		}.runTaskTimer(plugin, 0, 1));
		
		makeParticleTask(entity, 1, () -> {
			Location loc = entity.getLocation();
			loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, .2, .2, .2, 0.04);
			loc.getWorld().spawnParticle(Particle.SQUID_INK, loc.add(0, .5, 0), 8, .3, .4, .3, 0.015);
		});
	}
	public void setTargetGoals(Vex entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, new EntityType[0]), 1);
		goals.put(new PathfinderNearestAttackableTarget<Vex>(entity, Vex.class), 2);
	}
	public void onDamaged(EntityDamageEvent event) {
		if (immuneDamageTypes.contains(event.getCause())) {
			event.setCancelled(true);
			return;
		}
		super.onDamaged(event);
	}
	public void wasHit(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof LivingEntity target) || EntityUtils.isEntityImmunePlayer(target))
			return;
		Entity vex = getCreatureStandEntity(0);
		if (vex == null || !vex.isValid())
			return;
		((Mob) vex).setTarget(target);
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation().add(0, .5, 0), 15, .3, .5, .3, .03);
	}
	public SoundCategory getSoundCategory() {
		return SoundCategory.NEUTRAL;
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(SoulReaper.REGISTERED_KEY, SoulReaper.class);
		
		type.setSpawnConditions(event -> {
			if (event.getLocation().getBlock().getBiome() != Biome.SOUL_SAND_VALLEY)
				return false;
			return true;
		});
	}
}

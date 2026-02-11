package com.github.jewishbanana.ultimatecontent.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.TameableEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderAllyHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerHurtByEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerHurtEntity;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.target.PathfinderOwnerTargetedByEntity;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

public class PathfindersHandler implements Listener {

	private static Map<UUID, PathfinderHurtByEntity> pathfinderHurtByTarget = new HashMap<>();
	private static Map<UUID, List<PathfinderOwnerHurtByEntity>> pathfinderOwnerDamageSignal = new HashMap<>();
	private static Map<TameableEntity, PathfinderOwnerHurtByEntity> pathfinderOwnerDamageSignalMobs = new HashMap<>();
	private static Map<UUID, List<PathfinderOwnerHurtEntity>> pathfinderOwnerHurtEntity = new HashMap<>();
	private static Map<TameableEntity, PathfinderOwnerHurtEntity> pathfinderOwnerHurtEntityMobs = new HashMap<>();
	private static Map<UUID, TameableEntity> customTamedMobs = new HashMap<>();
	private static Map<TameableEntity, PathfinderAllyHurtByEntity> pathfinderAllyHurtByEntityMobs = new HashMap<>();
	private static Map<UUID, List<PathfinderOwnerTargetedByEntity>> pathfinderOwnerTargeted = new HashMap<>();
	private static Map<TameableEntity, PathfinderOwnerTargetedByEntity> pathfinderOwnerTargetedMobs = new HashMap<>();
	
	public PathfindersHandler(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		UUID uuid = event.getEntity().getUniqueId();
		LivingEntity damager = event.getDamager() instanceof LivingEntity living ? living : (event.getEntity() instanceof Projectile projectile ? (projectile.getShooter() instanceof LivingEntity shooter ? shooter : null) : null);
		if (damager == null)
			return;
		if (event.getEntity() instanceof LivingEntity damaged) {
			List<PathfinderOwnerHurtEntity> pathfinderOwnerHurt = pathfinderOwnerHurtEntity.get(damager.getUniqueId());
			if (pathfinderOwnerHurt != null)
				pathfinderOwnerHurt.forEach(e -> e.signal = damaged);
		}
		if (EntityUtils.isEntityImmunePlayer(damager))
			return;
		PathfinderHurtByEntity pathfinderHurtBy = pathfinderHurtByTarget.get(uuid);
		if (pathfinderHurtBy != null)
			pathfinderHurtBy.signal = damager;
		List<PathfinderOwnerHurtByEntity> pathfinderOwnerHurtBy = pathfinderOwnerDamageSignal.get(uuid);
		if (pathfinderOwnerHurtBy != null)
			pathfinderOwnerHurtBy.forEach(e -> e.signal = damager);
		TameableEntity tameable = customTamedMobs.get(uuid);
		if (tameable != null) {
			UUID owner = tameable.getOwner();
			pathfinderAllyHurtByEntityMobs.forEach((k, v) -> {
				if (k.getOwner().equals(owner))
					v.signal = damager;
			});
		}
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onTargetOwner(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() == null || !(event.getEntity() instanceof LivingEntity targeter))
			return;
		List<PathfinderOwnerTargetedByEntity> pathfinderOwnerTarget = pathfinderOwnerTargeted.get(event.getTarget().getUniqueId());
		if (pathfinderOwnerTarget != null)
			pathfinderOwnerTarget.forEach(e -> e.signal = targeter);
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onTarget(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() == null || !(event.getEntity() instanceof Tameable targeter))
			return;
		TameableEntity targeted = customTamedMobs.get(event.getTarget().getUniqueId());
		if (targeted != null && targeted.getOwner() != null && targeter.getOwner() != null && targeted.getOwner().equals(targeter.getOwner().getUniqueId()))
			event.setCancelled(true);
	}
	public static void addPathfinder(TameableEntity entity, PathfinderOwnerHurtByEntity pathfinder) {
		UUID owner = entity.getOwner();
		if (owner == null)
			return;
		List<PathfinderOwnerHurtByEntity> pathfinders = pathfinderOwnerDamageSignal.get(owner);
		if (pathfinders != null)
			pathfinders.add(pathfinder);
		else
			pathfinderOwnerDamageSignal.put(owner, new ArrayList<>(Arrays.asList(pathfinder)));
		pathfinderOwnerDamageSignalMobs.put(entity, pathfinder);
	}
	public static void addPathfinder(UUID entity, PathfinderHurtByEntity pathfinder) {
		pathfinderHurtByTarget.put(entity, pathfinder);
	}
	public static void addPathfinder(TameableEntity entity, PathfinderOwnerHurtEntity pathfinder) {
		UUID owner = entity.getOwner();
		if (owner == null)
			return;
		List<PathfinderOwnerHurtEntity> set = pathfinderOwnerHurtEntity.get(owner);
		if (set != null)
			set.add(pathfinder);
		else
			pathfinderOwnerHurtEntity.put(owner, new ArrayList<>(Arrays.asList(pathfinder)));
		pathfinderOwnerHurtEntityMobs.put(entity, pathfinder);
	}
	public static void addPathfinder(TameableEntity entity, PathfinderAllyHurtByEntity pathfinder) {
		pathfinderAllyHurtByEntityMobs.put(entity, pathfinder);
	}
	public static void addTamedMob(UUID uuid, TameableEntity entity) {
		customTamedMobs.put(uuid, entity);
	}
	public static void addPathfinder(TameableEntity entity, PathfinderOwnerTargetedByEntity pathfinder) {
		UUID owner = entity.getOwner();
		if (owner == null)
			return;
		List<PathfinderOwnerTargetedByEntity> set = pathfinderOwnerTargeted.get(owner);
		if (set != null)
			set.add(pathfinder);
		else
			pathfinderOwnerTargeted.put(owner, new ArrayList<>(Arrays.asList(pathfinder)));
		pathfinderOwnerTargetedMobs.put(entity, pathfinder);
	}
	public static void removePathfinders(BaseEntity<? extends Entity> entity) {
		if (entity instanceof TameableEntity tameable) {
			UUID owner = tameable.getOwner();
			PathfinderOwnerHurtByEntity pathfinderHurtOwner = pathfinderOwnerDamageSignalMobs.remove(tameable);
			if (pathfinderHurtOwner != null) {
				List<PathfinderOwnerHurtByEntity> set = pathfinderOwnerDamageSignal.get(owner);
				if (set != null) {
					set.remove(pathfinderHurtOwner);
					if (set.isEmpty())
						pathfinderOwnerDamageSignal.remove(owner);
				}
			}
			PathfinderOwnerHurtEntity pathfinderOwnerHurt = pathfinderOwnerHurtEntityMobs.remove(tameable);
			if (pathfinderOwnerHurt != null) {
				List<PathfinderOwnerHurtEntity> set = pathfinderOwnerHurtEntity.get(owner);
				if (set != null) {
					set.remove(pathfinderOwnerHurt);
					if (set.isEmpty())
						pathfinderOwnerHurtEntity.remove(owner);
				}
			}
			pathfinderAllyHurtByEntityMobs.remove(tameable);
			customTamedMobs.remove(entity.getUniqueId());
			PathfinderOwnerTargetedByEntity pathfinderOwnerTarget = pathfinderOwnerTargetedMobs.remove(tameable);
			if (pathfinderOwnerTarget != null) {
				List<PathfinderOwnerTargetedByEntity> set = pathfinderOwnerTargeted.get(owner);
				if (set != null) {
					set.remove(pathfinderOwnerTarget);
					if (set.isEmpty())
						pathfinderOwnerTargeted.remove(owner);
				}
			}
		}
		pathfinderHurtByTarget.remove(entity.getUniqueId());
	}
}

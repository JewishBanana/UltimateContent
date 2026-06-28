package com.github.jewishbanana.ultimatecontent.entities.endentities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class VoidGuardian extends BaseEntity<Zombie> {

	public static final String REGISTERED_KEY = "uc:void_guardian";

	// Tracks how many times each VoidGuardian has healed back from rage for each player.
	// Outer key: player UUID → inner map: guardian entity UUID → heal count.
	// Used to drive the Anger Management achievement (same guardian must heal 3 times).
	private static final ConcurrentHashMap<UUID, Map<UUID, Integer>> angerHealCounts = new ConcurrentHashMap<>();

	private boolean rageMode;
	private UUID lastToRage;
	private double maxHealth;

	public VoidGuardian(Zombie entity) {
		super(entity, CustomEntityType.VOID_GUARDIAN);
		
		entity.setCanPickupItems(false);
		entity.setSilent(true);
		
		maxHealth = entity.getAttribute(VersionUtils.getMaxHealthAttribute()).getValue();
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (entity.getHealth() < maxHealth) {
					entity.setHealth(Math.min(entity.getHealth() + 1.0, maxHealth));
					entity.getWorld().spawnParticle(Particle.COMPOSTER, entity.getLocation().add(0, .5, 0), 5, .3, .4, .3, .001);
				}
				if (rageMode) {
					entity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, entity.getLocation().add(0, .5, 0), 10, .3, .3, .3, .25);
					if (entity.getHealth() > maxHealth / 2.0) {
						rageMode = false;
						changeColor(50, 50, 50);
						if (entityVariant.loadout.armor[3] == null)
							entity.getEquipment().setHelmet(CustomHead.VOID_GUARD.getHead());
						entity.getAttribute(VersionUtils.getMovementSpeedAttribute()).setBaseValue(entityVariant.movementSpeed);
						if (lastToRage != null) {
							onHealFromRage(lastToRage, entity.getUniqueId());
							lastToRage = null;
						}
					}
				}
			}
		}.runTaskTimer(plugin, 0, 20));
		
		makeParticleTask(entity, 1, () -> {
			VersionUtils.spawnDragonBreathParticle(entity.getLocation().add(0, entity.getHeight() / 2.0, 0), 3, .25, .5, .25, .015, 1f);
		});
	}
	public void onDamaged(EntityDamageEvent event) {
		super.onDamaged(event);
		if (rageMode)
			return;
		Zombie entity = (Zombie) event.getEntity();
		if (entity.getHealth() <= maxHealth / 2.0) {
			rageMode = true;
			changeColor(76, 48, 255);
			if (entityVariant.loadout.armor[3] == null)
				entity.getEquipment().setHelmet(CustomHead.VOID_GUARD_RAGE.getHead());
			entity.getAttribute(VersionUtils.getMovementSpeedAttribute()).setBaseValue(entityVariant.movementSpeed * 2.0);
			// Record the enrager here so the achievement fires even if event ordering ran onDamaged before wasHit.
			if (event instanceof EntityDamageByEntityEvent byEntity && byEntity.getDamager() instanceof Player attacker)
				lastToRage = attacker.getUniqueId();
		}
	}
	public void wasHit(EntityDamageByEntityEvent event) {
		if (!rageMode && event.getDamager() instanceof Player)
			lastToRage = event.getDamager().getUniqueId();
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		event.setDamage(Math.max(18.0 - (((Zombie) event.getDamager()).getHealth() / 4.0), 4.0));
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		UUID guardianUUID = event.getEntity().getUniqueId();
		// Remove this guardian from all players' tracking maps so dead guardians don't count toward bestAliveCount.
		angerHealCounts.values().forEach(map -> map.remove(guardianUUID));
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation().add(0, event.getEntity().getHeight()/2.0, 0), 15, .3, .5, .3, .03);
		Player killer = event.getEntity().getKiller();
		if (killer != null)
			DependencyUtils.awardAchievementProgress(killer.getUniqueId(), "mobs.slayer.void_mobs", 1, -1);
	}
	/**
	 * Called when this guardian heals back out of rage mode. Tracks per-player heal counts and drives the
	 * Anger Management achievement: the same guardian must heal 3 times for the same player.
	 *
	 * Progress is only updated when the healing guardian is at or above the player's current best alive
	 * guardian count, so an accidental heal on a weaker guardian never resets a higher count in progress.
	 */
	private static void onHealFromRage(UUID playerUUID, UUID guardianUUID) {
		Map<UUID, Integer> playerCounts = angerHealCounts.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
		int newCount = playerCounts.merge(guardianUUID, 1, Integer::sum);
		if (newCount >= 3) {
			// The same guardian healed 3 times: award the Anger Management tier.
			// Award enough progress to saturate the tier goal regardless of current display progress.
			DependencyUtils.awardAchievementProgress(playerUUID, "master.series.void_master", 3, 2);
			angerHealCounts.remove(playerUUID);
			return;
		}
		// Find the highest heal count among all still-alive guardians for this player.
		int bestAliveCount = 0;
		for (Map.Entry<UUID, Integer> entry : playerCounts.entrySet()) {
			org.bukkit.entity.Entity e = Bukkit.getEntity(entry.getKey());
			if (e != null && e.isValid() && entry.getValue() > bestAliveCount)
				bestAliveCount = entry.getValue();
		}
		// Only set the display progress if this guardian is now at or above the best alive count.
		// This prevents an accidental heal on a low-count guardian from resetting a higher in-progress count.
		if (newCount >= bestAliveCount)
			DependencyUtils.setAchievementTierProgress(playerUUID, "master.series.void_master", 2, newCount);
	}
	private void changeColor(int red, int green, int blue) {
		Zombie entity = getCastedEntity();
		ItemStack[] armor = entity.getEquipment().getArmorContents();
		for (int i=0; i < 3; i++)
			if (entityVariant.loadout.armor[i] == null) {
				LeatherArmorMeta meta = (LeatherArmorMeta) armor[i].getItemMeta();
				meta.setColor(Color.fromRGB(red, green, blue));
				armor[i].setItemMeta(meta);
			}
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(VoidGuardian.REGISTERED_KEY, VoidGuardian.class);
		
		type.setSpawnConditions(event -> {
			if (!CustomEntityType.VOID_GUARDIAN.isWorldSpawnable(event.getLocation().getWorld()))
				return false;
			return true;
		});
	}
}

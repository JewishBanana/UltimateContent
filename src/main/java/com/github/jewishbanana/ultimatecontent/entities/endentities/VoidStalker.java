package com.github.jewishbanana.ultimatecontent.entities.endentities;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class VoidStalker extends BaseEntity<Phantom> {

	public static final String REGISTERED_KEY = "uc:void_stalker";

	/** Set just before spawnEntity() is called for a natural-spawn VoidStalker, cleared in the constructor.
	 *  This tells the constructor to elevate the actual entity rather than trying to teleport the cancelled original phantom. */
	private static boolean pendingElevation;

	public VoidStalker(Phantom entity) {
		super(entity, CustomEntityType.VOID_STALKER);

		boolean shouldElevate = pendingElevation;
		pendingElevation = false;

		setInvisible(entity);

		makeParticleTask(entity, Particle.SQUID_INK, 5, .4, .4, .4, 0.01);

		if (shouldElevate) {
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!entity.isValid())
					return;
				entity.teleport(entity.getLocation().add(0, 20, 0));
			});
		}
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof LivingEntity alive)
			alive.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true));
	}
	public void onCombust(EntityCombustEvent event) {
		event.setCancelled(true);
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation(), 10, .3, .3, .3, .03);
		Player killer = event.getEntity().getKiller();
		if (killer != null) {
			// Counts toward the general "Void Slayer" series and the "Predator Become Prey" mastery tier (slay void stalkers).
			DependencyUtils.awardAchievementProgress(killer.getUniqueId(), "mobs.slayer.void_mobs", 1, -1);
			DependencyUtils.awardAchievementProgress(killer.getUniqueId(), "master.series.void_master", 1, 1);
		}
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(VoidStalker.REGISTERED_KEY, VoidStalker.class);
		
		type.setSpawnConditions(event -> {
			if (!CustomEntityType.VOID_STALKER.isWorldSpawnable(event.getLocation().getWorld()))
				return false;
			// Only replace natural spawns that have a 3×3×3 clear volume 20 blocks above; skip the spawn
			// otherwise so the original entity spawns instead of a ground-level stalker.
			// The constructor reads the flag to teleport the actual entity (not the cancelled original).
			if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
				if (!Utils.isAreaClear(event.getLocation().add(0, 20, 0), 1.5f))
					return false;
				pendingElevation = true;
			}
			return true;
		});
	}
}

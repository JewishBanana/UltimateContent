package com.github.jewishbanana.ultimatecontent.entities.endentities;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World.Environment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class VoidStalker extends BaseEntity<Phantom> {
	
	public static final String REGISTERED_KEY = "uc:void_stalker";

	public VoidStalker(Phantom entity) {
		super(entity, CustomEntityType.VOID_STALKER);
		
		setInvisible(entity);
		
		makeParticleTask(entity, Particle.SQUID_INK, 5, .4, .4, .4, 0.01);
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
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(VoidStalker.REGISTERED_KEY, VoidStalker.class);
		
		type.setSpawnConditions(event -> {
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.THE_END))
				return false;
			return true;
		});
	}
}

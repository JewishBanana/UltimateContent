package com.github.jewishbanana.ultimatecontent.entities.endentities;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class VoidArcher extends BaseEntity<Skeleton> {
	
	public static final String REGISTERED_KEY = "uc:void_archer";

	public VoidArcher(Skeleton entity) {
		super(entity, CustomEntityType.VOID_ARCHER);
		
		entity.setCanPickupItems(false);
		entity.setSilent(true);
		
		makeParticleTask(entity, 1, Particle.PORTAL, new Vector(0,.75,0), 7, .1, .1, .1, .8);
	}
	public void onDamaged(EntityDamageEvent event) {
		Location loc = Utils.findRandomSpotInRadius(event.getEntity().getLocation(), 7, 12, 2, 10);
		if (loc != null) {
			Location entityLoc = event.getEntity().getLocation();
			playSound(entityLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
			entityLoc.getWorld().spawnParticle(Particle.PORTAL, entityLoc.add(0, event.getEntity().getHeight() / 2.0, 0), 15, .2, .5, .2, 0.1);
			event.getEntity().teleport(loc);
			playSound(event.getEntity().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
			entityLoc.getWorld().spawnParticle(Particle.PORTAL, event.getEntity().getLocation().add(0, event.getEntity().getHeight() / 2.0, 0), 15, .2, .5, .2, 0.1);
		}
	}
	public void onDeath(EntityDeathEvent event) {
		super.onDeath(event);
		event.getEntity().getWorld().spawnParticle(Particle.SOUL, event.getEntity().getLocation().add(0,event.getEntity().getHeight()/2.0,0), 15, .3, .3, .3, .03);
	}
	public void setAttributes(Skeleton entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(VoidArcher.REGISTERED_KEY, VoidArcher.class);
		
		type.setSpawnConditions(event -> {
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.THE_END))
				return false;
			return true;
		});
	}
}

package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.ExplodingEntity;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;

public class PrimedCreeper extends BaseEntity<Creeper> implements ExplodingEntity {
	
	public static final String REGISTERED_KEY = "uc:primed_creeper";
	
	private double damageMultiplier;
	private UUID uuid;

	public PrimedCreeper(Creeper entity) {
		super(entity, CustomEntityType.PRIMED_CREEPER);
		
		entity.setMaxFuseTicks(1);
		this.uuid = entity.getUniqueId();
		EntitiesHandler.explodingEntities.put(uuid, this);
		
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (isTargetInRange(entity, 0, 16, false))
					entity.explode();
			}
		}.runTaskTimer(plugin, 0, 10));
	}
	public void unload() {
		super.unload();
		EntitiesHandler.explodingEntities.remove(uuid);
	}
	public void setAttributes(Creeper entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
		this.damageMultiplier = getSectionDouble("damageMultiplier", 1.0);
	}
	public double getExplosionDamageMultiplier() {
		return damageMultiplier;
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(PrimedCreeper.REGISTERED_KEY, PrimedCreeper.class);
		
		type.setSpawnConditions(event -> {
			return event.getEntityType() == EntityType.CREEPER;
		});
	}
}

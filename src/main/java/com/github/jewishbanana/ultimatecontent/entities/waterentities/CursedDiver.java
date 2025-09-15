package com.github.jewishbanana.ultimatecontent.entities.waterentities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.EntityType;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;

public class CursedDiver extends BaseEntity<Drowned> {
	
	public static final String REGISTERED_KEY = "uc:cursed_diver";

	public CursedDiver(Drowned entity) {
		super(entity, CustomEntityType.CURSED_DIVER);
		
		BlockData blockData = Material.OBSIDIAN.createBlockData();
		makeParticleTask(entity, 1, () -> {
			entity.getWorld().spawnParticle(Particle.FALLING_WATER, entity.getLocation().add(0,1,0), 4, .4, .7, .4, 0.0001);
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, entity.getLocation().clone().add(0,1.4,0), 3, .2, .4, .2, 1, blockData);
		});
	}
	public void setAttributes(Drowned entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(CursedDiver.REGISTERED_KEY, CursedDiver.class);
		
		type.setSpawnConditions(event -> {
			if (event.getEntityType() != EntityType.DROWNED)
				return false;
			return true;
		});
	}
}

package com.github.jewishbanana.ultimatecontent.entities.waterentities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.EntityType;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CursedDiver extends BaseEntity<Drowned> {
	
	public static final String REGISTERED_KEY = "uc:cursed_diver";

	public CursedDiver(Drowned entity) {
		super(entity, CustomEntityType.CURSED_DIVER);
		
		BlockData blockData = Material.OBSIDIAN.createBlockData();
		makeParticleTask(entity, 1, () -> {
			Location loc = entity.getLocation();
			entity.getWorld().spawnParticle(Particle.FALLING_WATER, loc.getX(), loc.getY() + 1.0, loc.getZ(), 4, .4, .7, .4, 0.0001);
			entity.getWorld().spawnParticle(Particle.FALLING_DUST, loc.getX(), loc.getY() + 1.4, loc.getZ(), 3, .2, .4, .2, 1, blockData);
		});
	}
	public void setAttributes(Drowned entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(40);
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

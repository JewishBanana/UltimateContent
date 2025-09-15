package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class SkeletonKnight extends BaseEntity<Skeleton> {
	
	public static final String REGISTERED_KEY = "uc:skeleton_knight";

	public SkeletonKnight(Skeleton entity) {
		super(entity, CustomEntityType.SKELETON_KNIGHT);
		
		entity.setCanPickupItems(true);
	}
	public void setAttributes(Skeleton entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
	}
	public void spawnHorse() {
		Entity entity = getEntity();
		if (entity == null)
			return;
		entity.getWorld().spawn(entity.getLocation(), SkeletonHorse.class, temp -> {
			temp.setTamed(true);
			temp.addPassenger(entity);
			temp.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.375);
			EntitiesHandler.attachRemoveKey(temp);
		});
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(SkeletonKnight.REGISTERED_KEY, SkeletonKnight.class);
		type.setRandomizeData(true);
		
		type.setSpawnConditions(event -> {
			if (!(event.getEntity() instanceof Monster))
				return false;
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.NORMAL))
				return false;
			if (!Utils.isAreaClear(loc.clone().add(0, 1.75, 0), 1.5))
				return false;
			return true;
		});
	}
}

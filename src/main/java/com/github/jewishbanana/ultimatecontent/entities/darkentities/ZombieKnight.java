package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class ZombieKnight extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:zombie_knight";

	public ZombieKnight(Zombie entity) {
		super(entity, CustomEntityType.ZOMBIE_KNIGHT);
		
		entity.setCanPickupItems(true);
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(30);
	}
	public void spawnHorse() {
		Entity entity = getEntity();
		if (entity == null)
			return;
		entity.getWorld().spawn(entity.getLocation(), ZombieHorse.class, temp -> {
			temp.setTamed(true);
			temp.addPassenger(entity);
			temp.getAttribute(VersionUtils.getMovementSpeedAttribute()).setBaseValue(0.425);
			EntitiesHandler.attachRemoveKey(temp);
		});
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(ZombieKnight.REGISTERED_KEY, ZombieKnight.class);
		type.setRandomizeData(true);
		
		type.setSpawnConditions(event -> {
			if (!(event.getEntity() instanceof Monster))
				return false;
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.NORMAL))
				return false;
			if (!Utils.isAreaClear(loc, 1.8f, 2.5f))
				return false;
			return true;
		});
	}
}

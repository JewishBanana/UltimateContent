package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.Utils.AreaClearing;

public class ZombieKnight extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:zombie_knight";

	public ZombieKnight(Zombie entity) {
		super(entity, CustomEntityType.ZOMBIE_KNIGHT);
		
		entity.setCanPickupItems(true);
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
	}
	public void spawnHorse() {
		Entity entity = getEntity();
		if (entity == null)
			return;
		entity.getWorld().spawn(entity.getLocation(), ZombieHorse.class, temp -> {
			temp.setTamed(true);
			temp.addPassenger(entity);
			temp.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.425);
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
			if (!Utils.isAreaClear(loc.getBlock().getRelative(BlockFace.UP), AreaClearing.PLUS_SIGN_3D_FROM_CENTER))
				return false;
			return true;
		});
	}
}

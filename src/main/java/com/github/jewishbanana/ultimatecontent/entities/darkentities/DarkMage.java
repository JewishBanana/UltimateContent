package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class DarkMage extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:dark_mage";

	public DarkMage(Zombie entity) {
		super(entity, CustomEntityType.DARK_MAGE);
		
		EntitiesHandler.makeEntityNoSunlightCombust(entity);
		entity.setCanPickupItems(false);
		
		final int color = random.nextInt(20, 60);
		EntityUtils.modifyLoadoutArmorColor(this, color, color, color, LoadoutEquipmentSlot.FEET, LoadoutEquipmentSlot.LEGS, LoadoutEquipmentSlot.CHEST);
	}
	public void unload() {
		super.unload();
		EntitiesHandler.removeEntitiyNoSunlightCombust(getUniqueId());
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(30);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(DarkMage.REGISTERED_KEY, DarkMage.class);
		
		type.setSpawnConditions(event -> {
			if (!CustomEntityType.DARK_MAGE.isWorldSpawnable(event.getLocation().getWorld()))
				return false;
			return event.getEntityType() == EntityType.ZOMBIE;
		});
	}
}

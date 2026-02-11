package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class DarkMage extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:dark_mage";

	public DarkMage(Zombie entity) {
		super(entity, CustomEntityType.DARK_MAGE);
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(VersionUtils.getFollowRangeAttribute()).setBaseValue(30);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(DarkMage.REGISTERED_KEY, DarkMage.class);
		
		type.setSpawnConditions(event -> {
			return event.getEntityType() == EntityType.ZOMBIE;
		});
	}
}

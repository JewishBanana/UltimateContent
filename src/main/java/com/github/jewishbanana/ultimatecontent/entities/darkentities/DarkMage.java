package com.github.jewishbanana.ultimatecontent.entities.darkentities;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;

public class DarkMage extends BaseEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:dark_mage";

	public DarkMage(Zombie entity) {
		super(entity, CustomEntityType.DARK_MAGE);
	}
	public void setAttributes(Zombie entity) {
		super.setAttributes(entity);
		entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(30);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(DarkMage.REGISTERED_KEY, DarkMage.class);
		
		type.setSpawnConditions(event -> {
			return event.getEntityType() == EntityType.CREEPER;
		});
	}
}

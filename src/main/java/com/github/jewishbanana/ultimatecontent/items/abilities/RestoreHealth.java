package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.Map;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;

public class RestoreHealth extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:restore_health";
	
	private double healAmount;
	private double particleMultiplier;

	public void activate(Entity entity, GenericItem base) {
		entity.getWorld().spawnParticle(Particle.COMPOSTER, entity.getLocation().add(0,1.1,0), (int) (particleMultiplier * 20.0), .3, .4, .3, 0.001);
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, volume * 1, 0.5f);
		if (entity instanceof LivingEntity)
			((LivingEntity) entity).setHealth(Math.min(((LivingEntity) entity).getHealth()+healAmount, ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
	}
	public void initFields() {
		this.healAmount = getDoubleField("heal_amount", 16.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, RestoreHealth.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("heal_amount", healAmount);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		healAmount = (double) map.get("heal_amount");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
}
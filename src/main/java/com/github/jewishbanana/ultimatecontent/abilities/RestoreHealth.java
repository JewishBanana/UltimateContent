package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class RestoreHealth extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:restore_health";
	
	private double healAmount;
	private double particleMultiplier;

	public RestoreHealth(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		Location entityLoc = entity.getLocation().add(0, entity.getHeight() / 1.63, 0);
		entity.getWorld().spawnParticle(Particle.COMPOSTER, entityLoc.getX(), entityLoc.getY(), entityLoc.getZ(), (int) Math.ceil(20.0 * particleMultiplier), entity.getWidth() / 2.0, entity.getHeight() / 4.5, entity.getWidth() / 2.0, 0.001);
		playSound(entityLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, .5f);
		if (entity instanceof LivingEntity alive)
			alive.setHealth(Math.min(alive.getHealth() + healAmount, alive.getAttribute(VersionUtils.getMaxHealthAttribute()).getValue()));
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, RestoreHealth.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		healAmount = registerSerializedDoubleField("healAmount", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
}
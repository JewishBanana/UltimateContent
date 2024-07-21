package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.Map;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;

public class JumpBoost extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:jump_boost";
	
	private double jumpHeight;
	private double particleMultiplier;
	private boolean slowFall;
	private boolean noFall;
	
	private Target target = Target.ACTIVATOR;

	public void activate(Entity entity, GenericItem base) {
		entity.getWorld().spawnParticle(Particle.END_ROD, entity.getLocation(), (int) (particleMultiplier * 10.0), 1.5, .5, 1.5, 0.1);
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, volume * 1, 0.5f);
		entity.setVelocity(entity.getVelocity().add(new Vector(0,jumpHeight/10.0,0)));
		if (slowFall && entity instanceof LivingEntity)
			((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 1, true, false));
		new RepeatingTask(3, 1) {
			@Override
			public void run() {
				if (entity == null || entity.isDead() || entity.isOnGround())
					cancel();
				if (particleMultiplier > 0)
					entity.getWorld().spawnParticle(Particle.CLOUD, entity.getLocation(), (int) (particleMultiplier * 3.0), 2, 2, 2, 0.1);
				if (noFall)
					entity.setFallDistance(0);
			}
		};
	}
	public void initFields() {
		this.jumpHeight = getDoubleField("jump_height", 10.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.slowFall = getBooleanField("slow_fall", false);
		this.noFall = getBooleanField("no_fall_damage", true);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, JumpBoost.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("jump_height", jumpHeight);
		map.put("particleMultiplier", particleMultiplier);
		map.put("slow_fall", slowFall);
		map.put("no_fall_damage", noFall);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		jumpHeight = (double) map.get("jump_height");
		particleMultiplier = (double) map.get("particleMultiplier");
		slowFall = (boolean) map.get("slow_fall");
		noFall = (boolean) map.get("no_fall_damage");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

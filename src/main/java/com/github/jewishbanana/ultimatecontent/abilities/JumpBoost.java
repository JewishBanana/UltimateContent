package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;

public class JumpBoost extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:jump_boost";
	
	private double jumpHeight;
	private double particleMultiplier;
	private boolean slowFall;
	private boolean noFall;
	
	private Target target = Target.ACTIVATOR;

	public JumpBoost(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		entity.getWorld().spawnParticle(Particle.END_ROD, entity.getLocation(), (int) (particleMultiplier * 10.0), 1.5, .5, 1.5, 0.1);
		playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, .5);
		entity.setVelocity(entity.getVelocity().add(new Vector(0,jumpHeight/10.0,0)));
		if (slowFall && entity instanceof LivingEntity alive)
			alive.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 1, true, false));
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid() || entity.isOnGround()) {
					this.cancel();
					return;
				}
				if (particleMultiplier > 0)
					entity.getWorld().spawnParticle(Particle.CLOUD, entity.getLocation(), (int) (particleMultiplier * 3.0), 2, 2, 2, 0.1);
				if (noFall)
					entity.setFallDistance(0);
			}
		}.runTaskTimer(plugin, 3, 1);
	}
	public void initFields() {
		this.jumpHeight = getDoubleField("jumpHeight", 10.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.slowFall = getBooleanField("slowFall", false);
		this.noFall = getBooleanField("noFall", true);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, JumpBoost.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("jumpHeight", jumpHeight);
		map.put("particleMultiplier", particleMultiplier);
		map.put("slowFall", slowFall);
		map.put("noFall", noFall);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		jumpHeight = (double) map.get("jumpHeight");
		particleMultiplier = (double) map.get("particleMultiplier");
		slowFall = (boolean) map.get("slowFall");
		noFall = (boolean) map.get("noFall");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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
	private boolean slowFall;
	private boolean noFall;
	private double particleMultiplier;
	
	private Target target = Target.ACTIVATOR;

	public JumpBoost(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		Location entityLoc = entity.getLocation();
		World world = entityLoc.getWorld();
		if (particleMultiplier > 0)
			world.spawnParticle(Particle.END_ROD, entityLoc, (int) Math.ceil(particleMultiplier * 10.0), 1.5, .5, 1.5, 0.1);
		playSound(entityLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, .5f);
		entity.setVelocity(entity.getVelocity().add(new Vector(0, jumpHeight / 10.0, 0)));
		if (slowFall && entity instanceof LivingEntity alive)
			alive.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 1, true, false));
		new BukkitRunnable() {
			final int particleCount = (int) Math.ceil(3.0 * particleMultiplier);
			
			@Override
			public void run() {
				if (entity == null || !entity.isValid() || entity.isOnGround()) {
					this.cancel();
					return;
				}
				if (particleCount > 0)
					world.spawnParticle(Particle.CLOUD, entity.getLocation(), particleCount, 2, 2, 2, 0.1);
				if (noFall)
					entity.setFallDistance(0);
			}
		}.runTaskTimer(plugin, 3, 1);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, JumpBoost.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		jumpHeight = registerSerializedDoubleField("jumpHeight", map);
		slowFall = registerSerializedBooleanField("slowFall", map);
		noFall = registerSerializedBooleanField("noFall", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

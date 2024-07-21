package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.Map;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;

public class Blinding extends AbilityAttributes implements Listener {
	
	public static String REGISTERED_KEY = "ui:blinding";
	
	private double time;
	private double particleMultiplier;
	
	private Mob mob;
	
	private Target target = Target.ATTACKER;
	
	public void activate(Entity entity, GenericItem base) {
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, volume * 1, 0.5f);
		entity.getWorld().spawnParticle(Particle.SQUID_INK, entity.getLocation().add(0,entity.getHeight()/2.0,0), (int) (particleMultiplier * 8.0), .25, .5, .25, 0.0001);
		final int length = (int) (time * 20.0);
		if (entity instanceof LivingEntity)
			((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, length, 0));
		if (entity instanceof Mob) {
			int[] timer = {0};
			Mob mob = (Mob) entity;
			mob.setTarget(null);
			Blinding instance = new Blinding();
			instance.mob = mob;
			plugin.getServer().getPluginManager().registerEvents(instance, plugin);
			new RepeatingTask(0, 1) {
				@Override
				public void run() {
					if (mob == null || mob.isDead() || timer[0] >= length) {
						cancel();
						HandlerList.unregisterAll(instance);
						return;
					}
					mob.setTarget(null);
					timer[0]++;
				}
			};
		}
	}
	@EventHandler
	public void onTarget(EntityTargetLivingEntityEvent e) {
		if (e.getEntity() == null || !e.getEntity().equals(mob))
			return;
		e.setCancelled(true);
	}
	public void initFields() {
		this.time = getDoubleField("time", 10.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, Blinding.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("time", time);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		time = (double) map.get("time");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

package com.github.jewishbanana.ultimatecontent.abilities;

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
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;

public class Blinding extends AbilityAttributes implements Listener {
	
	public static final String REGISTERED_KEY = "uc:blinding";
	
	private double time;
	private double particleMultiplier;
	
	private Mob mob;
	
	private Target target = Target.ATTACKER;
	
	public Blinding(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		if (!canEntityBeHarmed(entity))
			return;
		playSound(entity.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1, .5);
		entity.getWorld().spawnParticle(Particle.SQUID_INK, entity.getLocation().add(0,entity.getHeight()/2.0,0), (int) (particleMultiplier * 8.0), .25, .5, .25, 0.0001);
		final int length = (int) (time * 20.0);
		if (entity instanceof LivingEntity alive)
			alive.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, length, 0));
		if (entity instanceof Mob mob) {
			mob.setTarget(null);
			Blinding instance = UIAbilityType.createAbilityInstance(this.getClass());
			instance.mob = mob;
			plugin.getServer().getPluginManager().registerEvents(instance, plugin);
			new BukkitRunnable() {
				private int tick;
				
				@Override
				public void run() {
					if (mob == null || mob.isDead() || tick >= length) {
						this.cancel();
						HandlerList.unregisterAll(instance);
						return;
					}
					mob.setTarget(null);
					tick++;
				}
			}.runTaskTimer(plugin, 0, 1);
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
		UIAbilityType.registerAbility(REGISTERED_KEY, Blinding.class);
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

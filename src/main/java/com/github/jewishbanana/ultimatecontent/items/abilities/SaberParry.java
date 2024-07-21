package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Particle;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.ActivatedSlot;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class SaberParry extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:saber_parry";
	
	public static Map<UUID, UUID> parryMap = new HashMap<>();
	public static Set<UUID> projectileParry = new HashSet<>();
	
	private double knockbackMultiplier;
	private double particleMultiplier;
	
	private ActivatedSlot activatingSlot = ActivatedSlot.MAIN_HAND;

	public void wasHit(EntityDamageByEntityEvent event, GenericItem base) {
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;
		UUID hit = parryMap.remove(event.getDamager().getUniqueId());
		if (hit != null && event.getEntity().getUniqueId().equals(hit) && shouldActivate()) {
			event.setCancelled(true);
			Vector vec = Utils.getUnormalizedVectorTowards(event.getEntity().getLocation(), event.getDamager().getLocation()).multiply(0.5);
			event.getDamager().setVelocity(event.getDamager().getVelocity().add(vec.clone().normalize().multiply(0.2).setY(0.1).multiply(knockbackMultiplier)));
			event.getEntity().getWorld().spawnParticle(Particle.END_ROD, event.getEntity().getLocation().add(0,1.3,0).add(vec), (int) (particleMultiplier * 5.0), 0, 0, 0, 0.1);
		}
	}
	public void hitByProjectile(ProjectileHitEvent event, GenericItem base) {
		if (projectileParry.remove(event.getHitEntity().getUniqueId()) && shouldActivate()) {
			event.setCancelled(true);
			event.getEntity().getWorld().spawnParticle(Particle.END_ROD, event.getEntity().getLocation(), (int) (particleMultiplier * 3.0), 0, 0, 0, 0.1);
			event.getEntity().remove();
		}
	}
	public void initFields() {
		this.chance = getDoubleField("chance", 60.0);
		this.knockbackMultiplier = getDoubleField("knockbackMultiplier", 1.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, SaberParry.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("knockbackMultiplier", knockbackMultiplier);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		knockbackMultiplier = (double) map.get("knockbackMultiplier");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
	public ActivatedSlot getActivatingSlot() {
		return activatingSlot;
	}
	public void setActivatingSlot(ActivatedSlot activatingSlot) {
		this.activatingSlot = activatingSlot;
	}
}

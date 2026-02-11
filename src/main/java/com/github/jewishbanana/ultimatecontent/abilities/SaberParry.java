package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.ActivatedSlot;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class SaberParry extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:saber_parry";
	
	public static final Map<UUID, UUID> parryMap = new HashMap<>();
	public static final Set<UUID> projectileParry = new HashSet<>();
	
	private double knockbackMultiplier;
	private double particleMultiplier;
	
	private ActivatedSlot activatingSlot = ActivatedSlot.MAIN_HAND;

	public SaberParry(UIAbilityType type) {
		super(type);
	}
	public void wasHit(EntityDamageByEntityEvent event, GenericItem base) {
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;
		Entity damager = event.getDamager();
		Entity entity = event.getEntity();
		UUID hit = parryMap.remove(damager.getUniqueId());
		if (hit != null && entity.getUniqueId().equals(hit) && shouldActivate()) {
			event.setCancelled(true);
			Location loc = entity.getLocation();
			Vector vec = Utils.getNoneNormalizedVectorTowards(loc, damager.getLocation()).multiply(0.5);
			damager.setVelocity(damager.getVelocity().add(vec.clone().normalize().multiply(0.2).setY(0.1).multiply(knockbackMultiplier)));
			if (particleMultiplier > 0)
				loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, entity.getHeight() / 1.385, 0).add(vec), (int) Math.ceil(5.0 * particleMultiplier), 0, 0, 0, 0.1);
		}
	}
	public void hitByProjectile(ProjectileHitEvent event, GenericItem base) {
		if (projectileParry.remove(event.getHitEntity().getUniqueId()) && shouldActivate()) {
			event.setCancelled(true);
			Entity entity = event.getEntity();
			if (particleMultiplier > 0)
				entity.getWorld().spawnParticle(Particle.END_ROD, entity.getLocation(), (int) Math.ceil(3.0 * particleMultiplier), 0, 0, 0, 0.1);
			entity.remove();
		}
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, SaberParry.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		knockbackMultiplier = registerSerializedDoubleField("knockbackMultiplier", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
	public ActivatedSlot getActivatingSlot() {
		return activatingSlot;
	}
	public void setActivatingSlot(ActivatedSlot activatingSlot) {
		this.activatingSlot = activatingSlot;
	}
}

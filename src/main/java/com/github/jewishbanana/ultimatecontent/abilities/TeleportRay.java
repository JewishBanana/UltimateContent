package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class TeleportRay extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:teleport_ray";
	
	private double range;
	private double particleMultiplier;
	
	private Target target = Target.ACTIVATOR;

	public TeleportRay(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		Location loc = entity instanceof LivingEntity living ? living.getEyeLocation() : entity.getLocation();
		Vector vec = loc.getDirection().multiply(1.5);
		World world = loc.getWorld();
		List<Location> pLocs = new ArrayList<>();
		final int particleCount = (int) Math.ceil(5.0 * particleMultiplier);
		for (int i=0; i < range; i++) {
			loc.add(vec);
			pLocs.add(loc.clone());
			for (Entity e : world.getNearbyEntities(loc, 1.5, 1.5, 1.5, e -> e instanceof LivingEntity && !e.equals(entity) && canEntityBeHarmed(e))) {
				Location to = entity.getLocation();
				entity.teleport(e);
				e.teleport(to);
				playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, .8f);
				playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, .8f);
				vec.normalize();
				DustOptions options = new DustOptions(Color.fromARGB(50, 0, 0, 255), 0.5f);
				for (Location temp : pLocs) {
					Vector tempVec = new Vector(vec.getZ(), vec.getY(), -vec.getX()).multiply(0.5);
					for (int j=0; j < 6; j++) {
						if (particleCount > 0)
							world.spawnParticle(Particle.END_ROD, temp.getX() + tempVec.getX(), temp.getY() + tempVec.getY(), temp.getZ() + tempVec.getZ(), 1, 0, 0, 0, 0.001);
						tempVec.add(vec.clone().multiply(1.5 / 6.0));
						tempVec.rotateAroundAxis(vec, Math.toRadians(60.0));
					}
					world.spawnParticle(VersionUtils.getRedstoneDust(), temp.clone().add(vec.clone().multiply(0.75)), particleCount, 1.5, 1.5, 1.5, 0.001, options);
				}
				return;
			}
		}
		vec.normalize();
		DustOptions options = new DustOptions(Color.fromARGB(50, 255, 0, 0), 0.5f);
		for (Location temp : pLocs) {
			Vector tempVec = new Vector(vec.getZ(), vec.getY(), -vec.getX()).multiply(0.5);
			for (int j=0; j < 6; j++) {
				if (particleCount > 0)
					world.spawnParticle(Particle.END_ROD, temp.getX() + tempVec.getX(), temp.getY() + tempVec.getY(), temp.getZ() + tempVec.getZ(), 1, 0, 0, 0, 0.001);
				tempVec.add(vec.clone().multiply(1.5/6.0));
				tempVec.rotateAroundAxis(vec, Math.toRadians(60.0));
			}
			world.spawnParticle(VersionUtils.getRedstoneDust(), temp.clone().add(vec.clone().multiply(0.75)), particleCount, 1.5, 1.5, 1.5, 0.001, options);
		}
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, TeleportRay.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		range = registerSerializedDoubleField("range", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

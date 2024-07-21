package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class TeleportRay extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:teleport_ray";
	
	private double range;
	private double particleMultiplier;
	
	private Target target = Target.ACTIVATOR;

	public void activate(Entity entity, GenericItem base) {
		Location loc = entity instanceof LivingEntity ? ((LivingEntity) entity).getEyeLocation() : entity.getLocation();
		Vector vec = loc.getDirection().multiply(1.5);
		World world = loc.getWorld();
		Queue<Location> pLocs = new ArrayDeque<>();
		for (int i=0; i < range; i++) {
			loc.add(vec);
			pLocs.add(loc.clone());
			for (Entity e : world.getNearbyEntities(loc, 1.5, 1.5, 1.5, e -> e instanceof LivingEntity && !e.equals(entity) && !Utils.isEntityImmunePlayer(e))) {
				Location to = entity.getLocation();
				entity.teleport(e);
				e.teleport(to);
				world.playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, volume * 1, 0.8f);
				world.playSound(e.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, volume * 1, 0.8f);
				vec.normalize();
				DustOptions options = new DustOptions(Color.fromARGB(50, 0, 0, 255), 0.5f);
				for (Location temp : pLocs) {
					Vector tempVec = new Vector(vec.getZ(), vec.getY(), -vec.getX()).multiply(0.5);
					for (int j=0; j < 6; j++) {
						if (particleMultiplier > 0)
							world.spawnParticle(Particle.END_ROD, temp.clone().add(tempVec), 1, 0, 0, 0, 0.001);
						tempVec.add(vec.clone().multiply(1.5/6.0));
						tempVec.rotateAroundAxis(vec, Math.toRadians(60.0));
					}
					world.spawnParticle(VersionUtils.getRedstoneDust(), temp.clone().add(vec.clone().multiply(0.75)), (int) (particleMultiplier * 5.0), 1.5, 1.5, 1.5, 0.001, options);
				}
			}
		}
		vec.normalize();
		DustOptions options = new DustOptions(Color.fromARGB(50, 255, 0, 0), 0.5f);
		for (Location temp : pLocs) {
			Vector tempVec = new Vector(vec.getZ(), vec.getY(), -vec.getX()).multiply(0.5);
			for (int j=0; j < 6; j++) {
				if (particleMultiplier > 0)
					world.spawnParticle(Particle.END_ROD, temp.clone().add(tempVec), 1, 0, 0, 0, 0.001);
				tempVec.add(vec.clone().multiply(1.5/6.0));
				tempVec.rotateAroundAxis(vec, Math.toRadians(60.0));
			}
			world.spawnParticle(VersionUtils.getRedstoneDust(), temp.clone().add(vec.clone().multiply(0.75)), (int) (particleMultiplier * 5.0), 1.5, 1.5, 1.5, 0.001, options);
		}
	}
	public void initFields() {
		this.range = getDoubleField("range", 2.5);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, TeleportRay.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

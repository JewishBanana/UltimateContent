package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class PropulsionBlast extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:propulsion_blast";
	
	private double radius;
	private double particleMultiplier;
	private boolean destroyProjectile;
	
	private Target target = Target.PROJECTILE;

	public PropulsionBlast(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		activate(entity.getLocation().add(0,entity.getHeight()/2,0), base);
		if (destroyProjectile && entity instanceof Projectile)
			entity.remove();
	}
	public void activate(Location loc, GenericItem base) {
		Vector rotation = new Vector(1, 0, 0);
		World world = loc.getWorld();
		for (int i=0; i < 36; i++) {
			Vector angle = rotation.clone();
			for (int j=0; j < 36; j++) {
				Location dir = loc.clone().add(angle);
				Vector force = Utils.getVectorTowards(loc, dir).multiply(radius/25.0);
				if (random.nextDouble() < particleMultiplier)
					world.spawnParticle(Particle.CLOUD, dir, 0, force.getX(), force.getY(), force.getZ());
				angle.rotateAroundZ(Math.toRadians(10.0));
			}
			rotation.rotateAroundY(Math.toRadians(10.0));
		}
		new BukkitRunnable() {
			private double distance;
			
			@Override
			public void run() {
				distance += radius / 20.0;
				for (Entity e : world.getNearbyEntities(loc, distance, distance, distance)) {
					double dist = e.getLocation().distance(loc);
					if (dist > distance || !canEntityBeHarmed(e))
						continue;
					if (e instanceof LivingEntity) {
						if (!EntityUtils.rayTraceEntityConeForSolid(e, loc))
							e.setVelocity(Utils.getVectorTowards(loc, e.getLocation().add(0,e.getHeight()/2.0,0)).multiply((0.075*radius)*(1.0-(dist/radius))));
					} else if (!Utils.rayTraceForSolid(loc, e.getLocation()))
						e.setVelocity(Utils.getVectorTowards(loc, e.getLocation().add(0,e.getHeight()/2.0,0)).multiply((0.075*radius)*(1.0-(dist/radius))));
				}
				if (distance >= radius)
					this.cancel();
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	public void initFields() {
		this.radius = getDoubleField("radius", 10.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.destroyProjectile = getBooleanField("destroyProjectile", true);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, PropulsionBlast.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("radius", radius);
		map.put("particleMultiplier", particleMultiplier);
		map.put("destroyProjectile", destroyProjectile);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		radius = (double) map.get("radius");
		particleMultiplier = (double) map.get("particleMultiplier");
		destroyProjectile = (boolean) map.get("destroyProjectile");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

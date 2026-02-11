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
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class PropulsionBlast extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:propulsion_blast";
	
	private double radius;
	private boolean destroyProjectile;
	private double particleMultiplier;
	
	private Target target = Target.PROJECTILE;

	public PropulsionBlast(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		activate(entity.getLocation().add(0, entity.getHeight() / 2, 0), base);
		if (destroyProjectile && entity instanceof Projectile)
			entity.remove();
	}
	public void activate(Location loc, GenericItem base) {
	    World world = loc.getWorld();
	    final double radiusDiv25 = radius / 25.0;
	    final double angleIncrement = Math.toRadians(10.0);
	    Vector rotation = new Vector(1, 0, 0);
	    for (int i = 0; i < 36; i++) {
	        Vector angle = rotation.clone();
	        for (int j = 0; j < 36; j++) {
	            if (random.nextFloat() < particleMultiplier) {
	                Location dir = loc.clone().add(angle);
	                Vector force = angle.clone().multiply(radiusDiv25);
	                world.spawnParticle(Particle.CLOUD, dir, 0, force.getX(), force.getY(), force.getZ());
	            }
	            angle.rotateAroundZ(angleIncrement);
	        }
	        rotation.rotateAroundY(angleIncrement);
	    }
	    new BukkitRunnable() {
	        private double distance;
	        private final double increment = radius / 20.0;
	        private final double velocityBase = 0.075 * radius;

	        @Override
	        public void run() {
	            distance += increment;
	            if (distance >= radius) {
	                this.cancel();
	                return;
	            }
	            for (Entity e : world.getNearbyEntities(loc, distance, distance, distance)) {
	                if (!canEntityBeHarmed(e))
	                	continue;
	                double dist = e.getLocation().distanceSquared(loc);
	                double distanceSquared = distance * distance;
	                if (dist > distanceSquared)
	                	continue;
	                dist = Math.sqrt(dist);
	                Location targetLoc = e.getLocation().add(0, e.getHeight() / 2.0, 0);
	                if (e instanceof LivingEntity ? !EntityUtils.rayTraceEntityConeForSolid(e, loc) : !BlockUtils.rayTraceForSolid(loc, e.getLocation().add(0, e.getHeight() / 2.0, 0))) {
	                    Vector velocity = Utils.getVectorTowards(loc, targetLoc).multiply(velocityBase * (1.0 - dist / radius));
	                    e.setVelocity(velocity);
	                }
	            }
	        }
	    }.runTaskTimer(plugin, 0, 1);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, PropulsionBlast.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		radius = registerSerializedDoubleField("radius", map);
		destroyProjectile = registerSerializedBooleanField("destroyProjectile", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

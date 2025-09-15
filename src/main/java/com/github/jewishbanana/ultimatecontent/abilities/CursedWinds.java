package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CursedWinds extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:cursed_winds";
	
	private double damage = 4.0;
	private double range = 10.0;
	private double particleMultiplier = 1.0;
	private int fireTicks = 80;
	
	private Target target = Target.ACTIVATOR;
	
	public CursedWinds(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		Location loc = entity instanceof LivingEntity alive ? alive.getEyeLocation() : entity.getLocation();
		activate(loc, entity, Set.of(entity.getUniqueId()), base);
	}
	public void activate(Location loc, GenericItem base) {
		activate(loc, null, new HashSet<UUID>(), base);
	}
	public void activate(Location loc, Entity activator, Set<UUID> immuneEntities, GenericItem base) {
		Vector motion = loc.getDirection().multiply(3.0);
		playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1, .6);
		World tempW = loc.getWorld();
		BlockData bd = Material.SAND.createBlockData();
		final int particleCount = (int) Math.ceil(10.0 * particleMultiplier);
		Set<UUID> hitEntities = new HashSet<>();
		new BukkitRunnable() {
			private double distance;
			
			@Override
			public void run() {
				distance += 3.0;
				if (distance > range) {
					this.cancel();
					distance = range - 1.5;
				}
				loc.add(motion);
				tempW.spawnParticle(Particle.FLAME, loc, particleCount, 1, 1, 1, .05);
				tempW.spawnParticle(VersionUtils.getBlockDust(), loc, particleCount, 1, 1, 1, .1, bd);
				for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5))
					if (!hitEntities.contains(e.getUniqueId()) && !immuneEntities.contains(e.getUniqueId()) && e instanceof LivingEntity alive && canEntityBeHarmed(e, activator)) {
						if (EntityUtils.damageEntity(alive, damage, "deaths.cursedWinds", activator, DamageCause.MAGIC)) {
							e.setFireTicks(fireTicks);
							e.setVelocity(motion.clone().multiply(0.5));
						}
						hitEntities.add(e.getUniqueId());
					}
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 4.0);
		this.range = getDoubleField("range", 10.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.fireTicks = getIntegerField("fireTicks", 80);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, CursedWinds.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("damage", damage);
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		map.put("fireTicks", fireTicks);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = (double) map.get("damage");
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
		fireTicks = (int) map.get("fireTicks");
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

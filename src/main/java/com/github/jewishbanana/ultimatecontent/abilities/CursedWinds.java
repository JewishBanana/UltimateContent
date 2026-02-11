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
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CursedWinds extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:cursed_winds";
	
	public double damage;
	public double range;
	public int fireTicks;
	public double sizeMultiplier;
	private double particleMultiplier;
	
	private Target target = Target.ACTIVATOR;
	
	public CursedWinds(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		Location loc = entity instanceof LivingEntity alive ? alive.getEyeLocation() : entity.getLocation();
		activate(loc, entity, Set.of(entity.getUniqueId()), base);
	}
	public void activate(Location loc, GenericItem base) {
		activate(loc, null, Set.of(), base);
	}
	public void activate(Location loc, Entity activator, Set<UUID> immuneEntities, GenericItem base) {
		Vector motion = loc.getDirection().multiply(3.0);
		playSound(loc, Sound.ITEM_FIRECHARGE_USE, 1f, .6f);
		World tempW = loc.getWorld();
		BlockData bd = Material.SAND.createBlockData();
		Set<UUID> hitEntities = new HashSet<>();
		new BukkitRunnable() {
			private double distance;
			private final double size = 1.5 * sizeMultiplier;
			private final double particleRange = Math.max(size - 0.5, 0);
			final int particleCount = (int) Math.ceil(6.67 * particleRange * particleMultiplier);
			private World world = loc.getWorld();
			private Vector force = motion.clone().multiply(0.5);
			
			@Override
			public void run() {
				distance += 3.0;
				if (distance > range) {
					this.cancel();
					distance = range - 1.5;
				}
				loc.add(motion);
				if (BlockUtils.isBlockType(loc.getBlock(), Material.WATER)) {
					this.cancel();
					return;
				}
				if (particleCount > 0) {
					tempW.spawnParticle(Particle.FLAME, loc, particleCount, particleRange, particleRange, particleRange, .05);
					tempW.spawnParticle(VersionUtils.getBlockDust(), loc, particleCount, particleRange, particleRange, particleRange, .1, bd);
				}
				for (Entity e : world.getNearbyEntities(loc, size, size, size))
					if (!hitEntities.contains(e.getUniqueId()) 
							&& !immuneEntities.contains(e.getUniqueId()) 
							&& e instanceof LivingEntity alive 
							&& !BlockUtils.isBlockType(e.getLocation().getBlock(), Material.WATER)
							&& canEntityBeHarmed(e, activator)) {
						if (EntityUtils.damageEntity(alive, damage, "deaths.cursedWinds", DamageCause.MAGIC, activator)) {
							e.setFireTicks(fireTicks);
							e.setVelocity(force);
						}
						hitEntities.add(e.getUniqueId());
					}
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, CursedWinds.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = registerSerializedDoubleField("damage", map);
		range = registerSerializedDoubleField("range", map);
		fireTicks = registerSerializedIntegerField("fireTicks", map);
		sizeMultiplier = registerSerializedDoubleField("sizeMultiplier", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
	public Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
}

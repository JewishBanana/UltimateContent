package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.listeners.RegionHandler;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;

public class TidalWave extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:tidal_wave";
	
	private double range;
	private double damage;
	private double particleMultiplier;
	
	private Target target = Target.PROJECTILE;
	
	public TidalWave(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		activate(entity.getLocation(), entity, base);
	}
	public void activate(Location loc, GenericItem base) {
		activate(loc, null, base);
	}
	public void activate(Location loc, Entity activator, GenericItem base) {
		World world = loc.getWorld();
		Vector direction = loc.getDirection();
		Vector angle = new Vector(direction.getZ(), 0, -direction.getX());
		Location spot = loc.clone().add(direction.clone().multiply(2)).add(angle.clone().multiply(-2));
		Block[] water = new Block[15];
		Queue<Block> allWaters = new ArrayDeque<>();
		Queue<Block> puddles = new ArrayDeque<>();
		int waterParticles = (int) (30.0 * particleMultiplier);
		int bubbleParticles = (int) (10.0 * particleMultiplier);
		new BukkitRunnable() {
			private int tick = (int) range;
			
			@Override
			public void run() {
				for (Block b : water)
					if (b != null && b.getType() == Material.WATER) {
						world.spawnParticle(Particle.FALLING_WATER, b.getLocation().add(.5,.5,.5), waterParticles, .5, .5, .5, 0.0001);
						if (random.nextInt(4) == 0 && b.getRelative(BlockFace.DOWN).getType().isSolid()) {
							puddles.add(b);
							allWaters.remove(b);
							Levelled data = ((Levelled) b.getBlockData());
							data.setLevel(7);
							b.setBlockData(data);
							RegionHandler.fluidChangeBlocks.add(b);
						} else
							b.setType(Material.AIR);
					}
				if (tick <= 0) {
					for (Block b : allWaters)
						if (b != null)
							b.setType(Material.AIR);
					if (tick <= -10) {
						this.cancel();
						new BukkitRunnable() {
							@Override
							public void run() {
								if (puddles.isEmpty())
									this.cancel();
								Block b = puddles.poll();
								if (b != null && b.getType() == Material.WATER)
									b.setType(Material.AIR);
								RegionHandler.fluidChangeBlocks.add(b);
							}
						}.runTaskTimer(plugin, 60, 1);
					}
					tick--;
					return;
				}
				tick--;
				for (int cycle=0; cycle < 3; cycle++) {
					Location line = spot.clone().add(direction.clone().multiply(cycle)).add(0,cycle,0);
					for (int i=0; i < 5; i++) {
						Block b = line.clone().add(angle.clone().multiply(i)).getBlock();
						if (!b.isPassable() || !canBlockBeDamaged(b))
							continue;
						allWaters.add(b);
						world.spawnParticle(Particle.BUBBLE_POP, b.getLocation().add(.5,.5,.5), bubbleParticles, .5, .5, .5, 0.0001);
						b.setType(Material.WATER);
						water[i+(cycle*5)] = b;
						for (Entity e : world.getNearbyEntities(b.getLocation(), 0.5, 0.5, 0.5)) {
							if (e.equals(activator) || !canEntityBeHarmed(e, activator))
								continue;
							if (e instanceof LivingEntity alive)
								if (!EntityUtils.damageEntity(alive, damage, activator != null ? "deaths.tidalWaveSource" : "deaths.tidalWave", activator, DamageCause.DROWNING))
									continue;
							e.setVelocity(direction);
						}
					}
				}
				spot.add(direction);
				playSound(loc, Sound.WEATHER_RAIN, 1, .75);
			}
		}.runTaskTimer(plugin, 0, 2);
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 4.0);
		this.range = getDoubleField("range", 15.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, TidalWave.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("damage", damage);
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = (double) map.get("damage");
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

package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
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
		List<Block> allWaters = new ArrayList<>();
		Queue<Block> puddles = new ArrayDeque<>();
		final int waterParticles = (int) Math.ceil(30.0 * particleMultiplier);
		final int bubbleParticles = (int) Math.ceil(10.0 * particleMultiplier);
		new BukkitRunnable() {
			private int tick = (int) Math.ceil(range);
			
			@Override
			public void run() {
				for (Block b : water)
					if (b != null && b.getType() == Material.WATER) {
						if (waterParticles > 0)
							world.spawnParticle(Particle.FALLING_WATER, BlockUtils.getCenterOfBlock(b), waterParticles, .5, .5, .5, 0.0001);
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
					Location line = spot.clone().add(direction.clone().multiply(cycle)).add(0, cycle, 0);
					for (int i=0; i < 5; i++) {
						Block b = line.clone().add(angle.clone().multiply(i)).getBlock();
						if (!b.isPassable() || !canBlockBeDamaged(b))
							continue;
						allWaters.add(b);
						Location center = BlockUtils.getCenterOfBlock(b);
						if (bubbleParticles > 0)
							world.spawnParticle(Particle.BUBBLE_POP, center, bubbleParticles, .5, .5, .5, 0.0001);
						b.setType(Material.WATER);
						water[i+(cycle*5)] = b;
						for (Entity e : world.getNearbyEntities(center, 0.5, 0.5, 0.5)) {
							if (e.equals(activator) || !canEntityBeHarmed(e, activator))
								continue;
							if (e instanceof LivingEntity alive)
								if (!EntityUtils.damageEntity(alive, damage, activator != null ? "deaths.tidalWaveSource" : "deaths.tidalWave", DamageCause.DROWNING, activator))
									continue;
							e.setVelocity(direction);
						}
					}
				}
				spot.add(direction);
				playSound(loc, Sound.WEATHER_RAIN, 1f, .75f);
			}
		}.runTaskTimer(plugin, 0, 2);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, TidalWave.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = registerSerializedDoubleField("damage", map);
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

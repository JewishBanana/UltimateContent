package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.listeners.RegionHandler;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class TidalWave extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:tidal_wave";
	
	private double range;
	private double damage;
	private double particleMultiplier;
	
	private Target target = Target.PROJECTILE;
	
	private Vector direction;
	private Entity shooter;
	
	public void activate(Entity entity, GenericItem base) {
		shooter = entity;
		if (entity != null)
			direction = entity.getLocation().getDirection();
		activate(entity.getLocation(), base);
	}
	public void activate(Location loc, GenericItem base) {
		World world = loc.getWorld();
		int[] timer = {(int) range};
		if (direction == null)
			direction = Utils.getRandomizedVector(1, 0, 1);
		Vector angle = new Vector(direction.getZ(), 0, -direction.getX());
		Location spot = loc.clone().add(direction.clone().multiply(2)).add(angle.clone().multiply(-2));
		Block[] water = new Block[15];
		Queue<Block> allWaters = new ArrayDeque<>();
		Queue<Block> puddles = new ArrayDeque<>();
		int waterParticles = (int) (30.0 * particleMultiplier);
		int bubbleParticles = (int) (10.0 * particleMultiplier);
		new RepeatingTask(0, 2) {
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
				if (timer[0] <= 0) {
					for (Block b : allWaters)
						if (b != null)
							b.setType(Material.AIR);
					if (timer[0] <= -10) {
						cancel();
						new RepeatingTask(60, 1) {
							@Override
							public void run() {
								if (puddles.isEmpty())
									cancel();
								Block b = puddles.poll();
								if (b != null && b.getType() == Material.WATER)
									b.setType(Material.AIR);
								RegionHandler.fluidChangeBlocks.add(b);
							}
						};
					}
					timer[0]--;
					return;
				}
				timer[0]--;
				for (int cycle=0; cycle < 3; cycle++) {
					Location line = spot.clone().add(direction.clone().multiply(cycle)).add(0,cycle,0);
					for (int i=0; i < 5; i++) {
						Block b = line.clone().add(angle.clone().multiply(i)).getBlock();
						if (b.getType() != Material.AIR || DependencyUtils.isLocationProtected(b.getLocation()))
							continue;
						allWaters.add(b);
						world.spawnParticle(Particle.BUBBLE_POP, b.getLocation().add(.5,.5,.5), bubbleParticles, .5, .5, .5, 0.0001);
						b.setType(Material.WATER);
						water[i+(cycle*5)] = b;
						for (Entity e : world.getNearbyEntities(b.getLocation(), 0.5, 0.5, 0.5)) {
							if (e.equals(shooter) || DependencyUtils.isEntityProtected(e))
								continue;
							e.setVelocity(direction);
							if (e instanceof LivingEntity && !(e instanceof Player && Utils.isPlayerImmune((Player) e)))
								if (shooter != null)
									Utils.damageEntity((LivingEntity) e, damage, "deaths.tidalWaveSource", false, shooter, DamageCause.DROWNING);
								else
									Utils.damageEntity((LivingEntity) e, damage, "deaths.tidalWave", false, DamageCause.DROWNING);
						}
					}
				}
				spot.add(direction);
				world.playSound(spot, Sound.WEATHER_RAIN, SoundCategory.HOSTILE, 1f * volume, 0.75f);
			}
		};
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 4.0);
		this.range = getDoubleField("range", 15.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, TidalWave.class);
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

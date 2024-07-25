package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class YetiRoar extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:yeti_roar";
	
	private double damage;
	private double range;
	private double particleMultiplier;
	private int freezeTicks;
	
	private Entity activator;

	public void activate(Entity entity, GenericItem base) {
		activator = entity;
		activate(entity.getLocation().add(0,entity.getHeight()/2.0,0), base);
	}
	public void activate(Location loc, GenericItem base) {
		World world = loc.getWorld();
		BlockVector block = new BlockVector(loc.getX(), loc.getY(), loc.getZ());
		BlockData bd = Material.PACKED_ICE.createBlockData();
		world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.HOSTILE, 1f * volume, 0.6f);
		world.playSound(loc, Sound.ENTITY_RAVAGER_ROAR, SoundCategory.HOSTILE, 2f * volume, .5f);
		int[] time = {0};
		int snowParticles = (int) (20.0 * particleMultiplier);
		int blockCrackParticles = (int) (3.0 * particleMultiplier);
		new RepeatingTask(0, 5) {
		@Override
		public void run() {
			time[0]++;
				for (int x = -time[0]; x < time[0]; x++)
					for (int z = -time[0]; z < time[0]; z++) {
						Vector position = block.clone().add(new Vector(x, 0, z));
						Block b = world.getBlockAt(position.toLocation(world));
						if (block.distance(position) >= (time[0] - 1) && block.distance(position) <= time[0]) {
							if (b.isPassable()) {
								for (int i = 0; i < 3; i++) {
									b = b.getRelative(BlockFace.DOWN);
									if (!b.isPassable())
										break;
								}
								if (b.isPassable())
									continue;
								else
									b = b.getRelative(BlockFace.UP);
							} else {
								for (int i = 0; i < 3; i++) {
									b = b.getRelative(BlockFace.UP);
									if (b.isPassable())
										break;
								}
								if (!b.isPassable())
									continue;
							}
							world.spawnParticle(VersionUtils.getSnowShovel(), b.getLocation().clone().add(0.5, 0.5, 0.5), snowParticles, .3, .5, .3, 0.001);
							world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().clone().add(0.5, 0.5, 0.5), blockCrackParticles, .3, .5, .3, 0.001, bd);
							if (b.getType() == Material.FIRE) {
								b.setType(Material.AIR);
								world.playSound(b.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
								world.spawnParticle(VersionUtils.getLargeSmoke(), b.getLocation().clone().add(0.5, 0.2, 0.5), 5, .3, .5, .3, 0.001, bd);
							}
							world.playSound(b.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.HOSTILE, 0.5f * volume, 0.5f);
							for (Entity e : world.getNearbyEntities(b.getLocation().clone().add(0.5, 0.5, 0.5), 0.5, 1, 0.5)) {
								if (e.equals(activator) || DependencyUtils.isEntityProtected(e))
									continue;
								e.setVelocity(Utils.getVectorTowards(loc, e.getLocation()).multiply(0.5).setY(0.8));
								if (e instanceof LivingEntity && !(e instanceof Player && Utils.isPlayerImmune((Player) e))) {
									((LivingEntity) e).addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 100, 7, true, false));
									e.setFreezeTicks(freezeTicks);
									Utils.damageEntity((LivingEntity) e, damage, "deaths.yetiRoar", false, DamageCause.FREEZE);
								}
							}
						}
					}
				if (time[0] >= range)
					cancel();
			}
		};
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 1.0);
		this.range = getDoubleField("range", 4.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.freezeTicks = getIntegerField("freezeTicks", 100);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, YetiRoar.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("damage", damage);
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		map.put("freezeTicks", freezeTicks);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = (double) map.get("damage");
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
		freezeTicks = (int) map.get("freezeTicks");
	}
}

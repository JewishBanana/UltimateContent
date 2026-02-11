package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.github.jewishbanana.deadlydisasters.utils.BlockUtils;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class YetiRoar extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:yeti_roar";
	
	public double damage;
	public double range;
	public int freezeTicks;
	private double particleMultiplier;

	public YetiRoar(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		activate(entity.getLocation().add(0, entity.getHeight() / 2.0, 0), entity, base);
	}
	public void activate(Location loc, GenericItem base) {
		activate(loc, null, base);
	}
	public void activate(Location loc, Entity activator, GenericItem base) {
		World world = loc.getWorld();
		BlockVector block = new BlockVector(loc.getX(), loc.getY(), loc.getZ());
		BlockData bd = Material.PACKED_ICE.createBlockData();
		playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, .6f);
		playSound(loc, Sound.ENTITY_RAVAGER_ROAR, 2f, .5f);
		final int snowParticles = (int) Math.ceil(20.0 * particleMultiplier);
		final int blockCrackParticles = (int) Math.ceil(3.0 * particleMultiplier);
		new BukkitRunnable() {
			private int tick;

			@Override
			public void run() {
				tick++;
				for (int x = -tick; x < tick; x++)
					for (int z = -tick; z < tick; z++) {
						Vector position = block.clone().add(new Vector(x, 0, z));
						Block b = world.getBlockAt(position.toLocation(world));
						if (block.distance(position) >= (tick - 1) && block.distance(position) <= tick) {
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
							Location center = BlockUtils.getCenterOfBlock(b);
							if (snowParticles > 0)
								world.spawnParticle(VersionUtils.getSnowShovel(), center, snowParticles, .3, .5, .3, 0.001);
							if (blockCrackParticles > 0)
								world.spawnParticle(VersionUtils.getBlockCrack(), center, blockCrackParticles, .3, .5, .3, 0.001, bd);
							if (Tag.FIRE.isTagged(b.getType()) && canBlockBeDamaged(b)) {
								b.setType(Material.AIR);
								playSound(center, Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
								world.spawnParticle(VersionUtils.getLargeSmoke(), center.getX(), center.getY() - 0.3, center.getZ(), 5, .3, .5, .3, 0.001);
							}
							playSound(center, Sound.ENTITY_PLAYER_HURT_FREEZE, .5f, .5f);
							for (Entity e : world.getNearbyEntities(center, 0.5, 1, 0.5)) {
								if (e.equals(activator) || !canEntityBeHarmed(e, activator))
									continue;
								if (e instanceof LivingEntity alive) {
									if (!EntityUtils.damageEntity(alive, damage, "deaths.yetiRoar", DamageCause.FREEZE, activator, false, false, Sound.ENTITY_PLAYER_HURT_FREEZE))
										continue;
									alive.addPotionEffect(new PotionEffect(VersionUtils.getSlowness(), 100, 7, true, false));
								}
								e.setFreezeTicks(freezeTicks);
								e.setVelocity(Utils.getVectorTowards(loc, e.getLocation()).multiply(0.5).setY(0.8));
							}
						}
					}
				if (tick >= range)
					this.cancel();
			}
		}.runTaskTimer(plugin, 0, 5);
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, YetiRoar.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = registerSerializedDoubleField("damage", map);
		range = registerSerializedDoubleField("range", map);
		freezeTicks = registerSerializedIntegerField("freezeTicks", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
}

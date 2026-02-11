package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class SpawnPlatform extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:spawn_platform";
	private static final List<SpawnPlatform> list = new ArrayList<>();
	
	private double range;
	private double particleMultiplier;
	
	private Map<Block, BlockState> blocks;

	public SpawnPlatform(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		entity.setFallDistance(0);
		activate(entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().add(.5, .5, .5), base);
	}
	public void activate(Location loc, GenericItem base) {
		SpawnPlatform instance = UIAbilityType.createAbilityInstance(this.getClass());
		instance.blocks = new HashMap<>();
		World world = loc.getWorld();
		BlockData blackData = Material.BLACK_STAINED_GLASS.createBlockData();
		BlockVector block = new BlockVector(loc.getX(), loc.getY(), loc.getZ());
		final int particleCount = (int) Math.ceil(3.0 * particleMultiplier);
		for (int x=(int) -range; x <= Math.ceil(range); x++)
			for (int z=(int) -range; z <= Math.ceil(range); z++) {
				Vector position = block.clone().add(new Vector(x, 0, z));
				if (block.distance(position) > range)
					continue;
				Block b = world.getBlockAt(position.toLocation(world));
				if (b.isPassable() && canBlockBeDamaged(b)) {
					instance.blocks.put(b, b.getState());
					b.setType(Material.BLACK_STAINED_GLASS);
					if (particleCount > 0)
						world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5, 0, .5), (int) particleCount, .5, .1, .5, 1, blackData);
				}
			}
		if (instance.blocks.isEmpty())
			return;
		playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, 1.75f);
		list.add(instance);
		if (instance.blocks.containsKey(loc.getBlock()))
			loc.getBlock().setType(Material.SEA_LANTERN);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, 1.25f);
			instance.blocks.keySet().forEach(k -> {
				if (k.getType() == Material.BLACK_STAINED_GLASS)
					k.setType(Material.ORANGE_STAINED_GLASS);
				if (particleCount > 0)
					world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5, 0, .5), particleCount, .5, .1, .5, 1, blackData);
			});
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, .75f);
				BlockData orangeData = Material.ORANGE_STAINED_GLASS.createBlockData();
				instance.blocks.keySet().forEach(k -> {
					if (k.getType() == Material.ORANGE_STAINED_GLASS)
						k.setType(Material.RED_STAINED_GLASS);
					if (particleCount > 0)
						world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5, 0, .5), particleCount, .5, .1, .5, 1, orangeData);
				});
				plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
					playSound(loc, Sound.BLOCK_GLASS_BREAK, .5f, .5f);
					BlockData redData = Material.RED_STAINED_GLASS.createBlockData();
					instance.blocks.forEach((k, v) -> {
						if (k.getType() == Material.RED_STAINED_GLASS || k.getType() == Material.SEA_LANTERN)
							v.update(true);
						if (particleCount > 0)
							world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5, 0, .5), particleCount, .5, .1, .5, 1, redData);
						list.remove(instance);
					});
				}, 100);
			}, 100);
		}, 200);
	}
	private void sweep() {
		blocks.forEach((k, v) -> {
			Material type = k.getType();
			if (type == Material.BLACK_STAINED_GLASS || type == Material.ORANGE_STAINED_GLASS || type == Material.RED_STAINED_GLASS || type == Material.SEA_LANTERN)
				v.update(true);
		});
	}
	public void clean() {
		list.forEach(e -> e.sweep());
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, SpawnPlatform.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		range = registerSerializedDoubleField("range", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
	}
}

package com.github.jewishbanana.ultimatecontent.items.abilities;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class SpawnPlatform extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:spawn_platform";
	private static Queue<SpawnPlatform> list = new ArrayDeque<>();
	
	private double range;
	private double particleMultiplier;
	
	private Map<Block, BlockState> blocks = new HashMap<>();

	public void activate(Entity entity, GenericItem base) {
		entity.setFallDistance(0);
		activate(entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().add(.5,.5,.5), base);
	}
	public void activate(Location loc, GenericItem base) {
		SpawnPlatform instance = new SpawnPlatform();
		World world = loc.getWorld();
		BlockData blackData = Material.BLACK_STAINED_GLASS.createBlockData();
		BlockVector block = new BlockVector(loc.getX(), loc.getY(), loc.getZ());
		for (int x=(int) -range; x <= Math.ceil(range); x++)
			for (int z=(int) -range; z <= Math.ceil(range); z++) {
				Vector position = block.clone().add(new Vector(x, 0, z));
				if (block.distance(position) > range)
					continue;
				Block b = world.getBlockAt(position.toLocation(world));
				if (b.isPassable()) {
					instance.blocks.put(b, b.getState());
					b.setType(Material.BLACK_STAINED_GLASS);
					world.spawnParticle(VersionUtils.getBlockCrack(), b.getLocation().add(.5,0,.5), (int) (particleMultiplier * 3.0), .5, .1, .5, 1, blackData);
				}
			}
		if (instance.blocks.isEmpty())
			return;
		world.playSound(loc, Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, volume * 0.5f, 1.75f);
		list.add(instance);
		if (instance.blocks.containsKey(loc.getBlock()))
			loc.getBlock().setType(Material.SEA_LANTERN);
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			world.playSound(loc, Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, volume * 0.5f, 1.25f);
			instance.blocks.keySet().forEach(k -> {
				if (k.getType() == Material.BLACK_STAINED_GLASS)
					k.setType(Material.ORANGE_STAINED_GLASS);
				world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5,0,.5), (int) (particleMultiplier * 3.0), .5, .1, .5, 1, blackData);
			});
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				world.playSound(loc, Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, volume * 0.5f, 0.75f);
				BlockData orangeData = Material.ORANGE_STAINED_GLASS.createBlockData();
				instance.blocks.keySet().forEach(k -> {
					if (k.getType() == Material.ORANGE_STAINED_GLASS)
						k.setType(Material.RED_STAINED_GLASS);
					world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5,0,.5), (int) (particleMultiplier * 3.0), .5, .1, .5, 1, orangeData);
				});
				plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
					world.playSound(loc, Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, volume * 0.5f, 0.5f);
					BlockData redData = Material.RED_STAINED_GLASS.createBlockData();
					instance.blocks.forEach((k, v) -> {
						if (k.getType() == Material.RED_STAINED_GLASS || k.getType() == Material.SEA_LANTERN)
							v.update(true);
						world.spawnParticle(VersionUtils.getBlockCrack(), k.getLocation().add(.5,0,.5), (int) (particleMultiplier * 3.0), .5, .1, .5, 1, redData);
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
	public void initFields() {
		this.range = getDoubleField("range", 2.5);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, SpawnPlatform.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
	}
}

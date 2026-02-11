package com.github.jewishbanana.ultimatecontent.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BlockUtils {
	
	private static final RandomGenerator random;
	static {
		random = Utils.getRandomGenerator();
	}

	public static Tag<Material> getMaterialTagByName(String name) {
	    try {
	        Field field = Tag.class.getField(name);
	        Object value = field.get(null);
	        if (value instanceof Tag<?>) {
	            @SuppressWarnings("unchecked")
	            Tag<Material> tag = (Tag<Material>) value;
	            return tag;
	        }
	    } catch (NoSuchFieldException | IllegalAccessException e) {}
	    return null;
	}
	public static boolean rayTraceForSolid(Location initial, Location target) {
	    final float dx = (float)(target.getX() - initial.getX());
	    final float dy = (float)(target.getY() - initial.getY());
	    final float dz = (float)(target.getZ() - initial.getZ());
	    final float lengthSquared = dx * dx + dy * dy + dz * dz;
	    if (lengthSquared == 0.0f || !Float.isFinite(lengthSquared))
	        return !initial.getBlock().isPassable();
	    final float invLength = Utils.fastInverseSqrt(lengthSquared);
	    final float dirX = dx * invLength;
	    final float dirY = dy * invLength;
	    final float dirZ = dz * invLength;
	    if (!Float.isFinite(dirX) || !Float.isFinite(dirY) || !Float.isFinite(dirZ))
	        return !initial.getBlock().isPassable();
	    final World world = initial.getWorld();
	    final int distance = (int)Math.sqrt(lengthSquared);
	    if (!initial.getBlock().isPassable())
	        return true;
	    float x = (float)initial.getX();
	    float y = (float)initial.getY();
	    float z = (float)initial.getZ();
	    for (int i = 1; i < distance; i++) {
	        x += dirX;
	        y += dirY;
	        z += dirZ;
	        if (!world.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z)).isPassable())
	            return true;
	    }
	    return false;
	}
	public static Block rayCastForBlock(Location location, int minRange, int maxRange, int maxAttempts, Set<Material> materialWhitelist, Set<Block> blockWhitelist) {
	    final World world = location.getWorld();
	    final float startX = (float)location.getX();
	    final float startY = (float)location.getY();
	    final float startZ = (float)location.getZ();
	    for (int i = 0; i < maxAttempts; i++) {
	        final float dx = random.nextFloat(-1.0f, 1.0f);
	        final float dy = random.nextFloat(-1.0f, 1.0f);
	        final float dz = random.nextFloat(-1.0f, 1.0f);
	        final float lengthSquared = dx * dx + dy * dy + dz * dz;
	        if (lengthSquared == 0.0f)
	            continue;
	        final float invLength = Utils.fastInverseSqrt(lengthSquared);
	        final float dirX = dx * invLength;
	        final float dirY = dy * invLength;
	        final float dirZ = dz * invLength;
	        float x = startX;
	        float y = startY;
	        float z = startZ;
	        for (int c = 0; c < maxRange; c++) {
	            x += dirX;
	            y += dirY;
	            z += dirZ;
	            final Block b = world.getBlockAt((int)Math.floor(x), (int)Math.floor(y), (int)Math.floor(z));
	            if (!b.isPassable()) {
	                if (c < minRange)
	                    break;
	                if (blockWhitelist != null && !blockWhitelist.contains(b))
	                    break;
	                if (materialWhitelist != null && !materialWhitelist.contains(b.getType()))
	                    break;
	                return b;
	            }
	        }
	    }
	    return null;
	}
	public static Block rayCastForBlock(Location location, int minRange, int maxRange, int maxAttempts, Set<Material> materialWhitelist) {
	    return rayCastForBlock(location, minRange, maxRange, maxAttempts, materialWhitelist, null);
	}
	public static Block rayTraceForBlock(Location location, Vector direction, double maxDistance, Predicate<Block> conditions) {
	    final float dx = (float) direction.getX();
	    final float dy = (float) direction.getY();
	    final float dz = (float) direction.getZ();
	    final float lengthSquared = dx * dx + dy * dy + dz * dz;
	    if (lengthSquared == 0.0f)
	        return null;
	    final float invLength = Utils.fastInverseSqrt(lengthSquared);
	    final float dirX = dx * invLength * 0.8f;
	    final float dirY = dy * invLength * 0.8f;
	    final float dirZ = dz * invLength * 0.8f;
	    final World world = location.getWorld();
	    final int steps = (int) (maxDistance / 0.8);
	    float x = (float) location.getX() + dirX;
	    float y = (float) location.getY() + dirY;
	    float z = (float) location.getZ() + dirZ;
	    for (int i = 0; i < steps; i++) {
	        final Block temp = world.getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
	        if (temp != null && conditions.test(temp))
	            return temp;
	        x += dirX;
	        y += dirY;
	        z += dirZ;
	    }
	    return null;
	}
	public static Block rayTraceForBlock(Location location, Vector direction, double maxDistance) {
		return rayTraceForBlock(location, direction, maxDistance, temp -> !temp.isPassable());
	}
	public static Block rayTraceForBlock(Location location, Location target, double maxDistance) {
		return rayTraceForBlock(location, Utils.getVectorTowards(location, target), maxDistance, temp -> !temp.isPassable());
	}
	public static Block getHighestExposedBlock(Block block, int maxDistance) {
	    if (block == null)
	        return null;
	    final World world = block.getWorld();
	    int x = block.getX();
	    int y = block.getY();
	    int z = block.getZ();
	    if (block.isPassable()) {
	        for (int i = 0; i < maxDistance; i++) {
	            y--;
	            if (!world.getBlockAt(x, y, z).isPassable())
	                return world.getBlockAt(x, y, z);
	        }
	    } else {
	        for (int i = 0; i < maxDistance; i++) {
	            y++;
	            if (world.getBlockAt(x, y, z).isPassable())
	                return world.getBlockAt(x, y - 1, z);
	        }
	    }
	    return null;
	}
	public static Location getCenterOfBlock(Block block) {
		return block.getLocation().add(.5, .5, .5);
	}
	public static List<Block> getBlocksInCircleRadius(Location location, float radius) {
	    final List<Block> list = new ArrayList<>((int) (Math.PI * radius * radius) + 1);
	    final float radiusSquared = radius * radius;
	    final World world = location.getWorld();
	    final float centerX = (float) location.getX();
	    final float centerY = (float) location.getY();
	    final float centerZ = (float) location.getZ();
	    final int minX = (int) Math.floor(centerX - radius);
	    final int maxX = (int) Math.floor(centerX + radius);
	    final int minZ = (int) Math.floor(centerZ - radius);
	    final int maxZ = (int) Math.floor(centerZ + radius);
	    final int blockY = (int) Math.floor(centerY);
	    for (int x = minX; x <= maxX; x++) {
	        final float dx = x - centerX;
	        final float dxSquared = dx * dx;
	        for (int z = minZ; z <= maxZ; z++) {
	            final float dz = z - centerZ;
	            final float distanceSquared = dxSquared + dz * dz;
	            if (distanceSquared <= radiusSquared)
	                list.add(world.getBlockAt(x, blockY, z));
	        }
	    }
	    return list;
	}
	public static List<Block> getBlocksInCircleCircumference(Location location, float radius) {
	    final List<Block> list = new ArrayList<>((int) (2 * Math.PI * radius) + 1);
	    final float outerRadius = radius * radius;
	    final float innerRadius = (radius - 1) * (radius - 1);
	    final World world = location.getWorld();
	    final float centerX = (float) location.getX();
	    final float centerY = (float) location.getY();
	    final float centerZ = (float) location.getZ();
	    final int minX = (int) Math.floor(centerX - radius);
	    final int maxX = (int) Math.floor(centerX + radius);
	    final int minZ = (int) Math.floor(centerZ - radius);
	    final int maxZ = (int) Math.floor(centerZ + radius);
	    final int blockY = (int) Math.floor(centerY);
	    for (int x = minX; x <= maxX; x++) {
	        final float dx = x - centerX;
	        final float dxSquared = dx * dx;
	        for (int z = minZ; z <= maxZ; z++) {
	            final float dz = z - centerZ;
	            final float distanceSquared = dxSquared + dz * dz;
	            if (distanceSquared <= outerRadius && distanceSquared > innerRadius)
	                list.add(world.getBlockAt(x, blockY, z));
	        }
	    }
	    return list;
	}
	public static List<Block> getBlocksInSphereRadius(Location location, float radius) {
	    final List<Block> list = new ArrayList<>((int) (4.188790 * radius * radius * radius) + 1);
	    final float radiusSquared = radius * radius;
	    final World world = location.getWorld();
	    final float centerX = (float) location.getX();
	    final float centerY = (float) location.getY();
	    final float centerZ = (float) location.getZ();
	    final int minX = (int) Math.floor(centerX - radius);
	    final int maxX = (int) Math.floor(centerX + radius);
	    final int minY = (int) Math.floor(centerY - radius);
	    final int maxY = (int) Math.floor(centerY + radius);
	    final int minZ = (int) Math.floor(centerZ - radius);
	    final int maxZ = (int) Math.floor(centerZ + radius);
	    for (int x = minX; x <= maxX; x++) {
	        final float dx = x - centerX;
	        final float dxSquared = dx * dx;
	        for (int y = minY; y <= maxY; y++) {
	            final float dy = y - centerY;
	            final float dySquared = dy * dy;
	            final float dxdySquared = dxSquared + dySquared;
	            for (int z = minZ; z <= maxZ; z++) {
	                final float dz = z - centerZ;
	                final float distanceSquared = dxdySquared + dz * dz;
	                if (distanceSquared <= radiusSquared)
	                    list.add(world.getBlockAt(x, y, z));
	            }
	        }
	    }
	    return list;
	}
	public static List<Block> getBlocksInCylinderRadius(Location location, float radius, float height) {
		List<Block> list = new ArrayList<>();
	    double radiusSquared = radius * radius;
	    int blockX = location.getBlockX();
	    int blockY = location.getBlockY();
	    int blockZ = location.getBlockZ();
	    World world = location.getWorld();
	    int radiusCeil = (int) Math.ceil(radius);
	    for (int y = 0; y <= height; y++)
	        for (int x = -radiusCeil; x <= radiusCeil; x++)
	            for (int z = -radiusCeil; z <= radiusCeil; z++) {
	                double distSquared = x * x + z * z;
	                if (distSquared <= radiusSquared)
	                    list.add(world.getBlockAt(blockX + x, blockY + y, blockZ + z));
	            }
	    return list;
	}
	public static boolean isBlockColdBiome(Block block) {
		double humidity = block.getWorld().getHumidity(block.getX(), block.getY(), block.getZ());
		return humidity >= 0.4 && humidity <= 0.8 && block.getWorld().getTemperature(block.getX(), block.getY(), block.getZ()) < 0.15;
	}
	public static boolean isBlockDesertBiome(Block block) {
		return block.getWorld().getTemperature(block.getX(), block.getY(), block.getZ()) >= 1.8 && block.getWorld().getHumidity(block.getX(), block.getY(), block.getZ()) < 0.1;
	}
	public static double getDamageOnBlock(Block block, ItemStack item) {
		double toolDamage = 1.0;
		double harvestDamage = 100.0;
		if (item != null) {
			switch (item.getType()) {
			case WOODEN_AXE:
			case WOODEN_PICKAXE:
			case WOODEN_SHOVEL:
				toolDamage = 2.0;
				break;
			case STONE_AXE:
			case STONE_PICKAXE:
			case STONE_SHOVEL:
				toolDamage = 4.0;
				break;
			case IRON_AXE:
			case IRON_PICKAXE:
			case IRON_SHOVEL:
				toolDamage = 6.0;
				break;
			case DIAMOND_AXE:
			case DIAMOND_PICKAXE:
			case DIAMOND_SHOVEL:
				toolDamage = 8.0;
				break;
			case GOLDEN_AXE:
			case GOLDEN_PICKAXE:
			case GOLDEN_SHOVEL:
				toolDamage = 12.0;
				break;
			case NETHERITE_AXE:
			case NETHERITE_PICKAXE:
			case NETHERITE_SHOVEL:
				toolDamage = 9.0;
				break;
			default:
				break;
			}
			switch (item.getType()) {
			case WOODEN_PICKAXE:
			case STONE_PICKAXE:
			case IRON_PICKAXE:
			case GOLDEN_PICKAXE:
			case DIAMOND_PICKAXE:
			case NETHERITE_PICKAXE:
				if (Tag.MINEABLE_PICKAXE.isTagged(block.getType()))
					harvestDamage = 30.0;
				break;
			case WOODEN_AXE:
			case STONE_AXE:
			case IRON_AXE:
			case GOLDEN_AXE:
			case DIAMOND_AXE:
			case NETHERITE_AXE:
				if (Tag.MINEABLE_AXE.isTagged(block.getType()))
					harvestDamage = 30.0;
				break;
			case WOODEN_SHOVEL:
			case STONE_SHOVEL:
			case IRON_SHOVEL:
			case GOLDEN_SHOVEL:
			case DIAMOND_SHOVEL:
			case NETHERITE_SHOVEL:
				if (Tag.MINEABLE_SHOVEL.isTagged(block.getType()))
					harvestDamage = 30.0;
				break;
			default:
				break;
			}
			if (item.containsEnchantment(VersionUtils.getEfficiency()))
				toolDamage += Math.pow(item.getEnchantmentLevel(VersionUtils.getEfficiency()), 2.0) + 1;
		}
		return toolDamage / block.getType().getHardness() / harvestDamage;
	}
	public static boolean isBlockType(Block block, Material type) {
		return block != null && block.getType() == type;
	}
}

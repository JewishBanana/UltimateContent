package com.github.jewishbanana.ultimatecontent.utils;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.github.jewishbanana.ultimatecontent.Main;

public class Utils {
	
	private static final JavaPlugin plugin;
	private static final RandomGenerator random;
	public static int descriptionLine;
	public static final String prefix;
	private static final Pattern hexPattern;
	private static boolean usingSpigot;
	private static final Map<DyeColor, ChatColor> dyeChatMap;
	static
	{
		hexPattern = Pattern.compile("\\(hex:#[a-fA-F0-9]{6}\\)");
		plugin = Main.getInstance();
		random = RandomGenerator.of("SplittableRandom");
		prefix = convertString("&a[UltimateContent]: ");
		
		dyeChatMap = new HashMap<>();
		dyeChatMap.put(DyeColor.BLACK, ChatColor.BLACK);
		dyeChatMap.put(DyeColor.BLUE, ChatColor.DARK_BLUE);
		dyeChatMap.put(DyeColor.BROWN, ChatColor.GOLD);
		dyeChatMap.put(DyeColor.CYAN, ChatColor.AQUA);
		dyeChatMap.put(DyeColor.GRAY, ChatColor.DARK_GRAY);
		dyeChatMap.put(DyeColor.GREEN, ChatColor.DARK_GREEN);
		dyeChatMap.put(DyeColor.LIGHT_BLUE, ChatColor.BLUE);
		dyeChatMap.put(DyeColor.LIGHT_GRAY, ChatColor.GRAY);
		dyeChatMap.put(DyeColor.LIME, ChatColor.GREEN);
		dyeChatMap.put(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE);
		dyeChatMap.put(DyeColor.ORANGE, ChatColor.GOLD);
		dyeChatMap.put(DyeColor.PINK, ChatColor.LIGHT_PURPLE);
		dyeChatMap.put(DyeColor.PURPLE, ChatColor.DARK_PURPLE);
		dyeChatMap.put(DyeColor.RED, ChatColor.DARK_RED);
		dyeChatMap.put(DyeColor.WHITE, ChatColor.WHITE);
		dyeChatMap.put(DyeColor.YELLOW, ChatColor.YELLOW);
		
		try {
	        Class.forName("org.bukkit.entity.Player$Spigot");
	        usingSpigot = true;
	    } catch (Throwable tr) {
	    	usingSpigot = false;
	    }
	}
	
	public static String convertString(String text) {
		if (text == null)
			return null;
		String s = text;
		Matcher match = hexPattern.matcher(s);
		if (usingSpigot) {
		    while (match.find()) {
		        String color = s.substring(match.start(), match.end());
		        s = s.replace(color, net.md_5.bungee.api.ChatColor.of(color.substring(5, color.length()-1))+"");
		        match = hexPattern.matcher(s);
		    }
		    return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', s);
		}
	    while (match.find()) {
	        String color = s.substring(match.start(), match.end());
	        Color col = Color.decode(color);
	        s = s.replace(color, dyeChatMap.getOrDefault(DyeColor.getByColor(org.bukkit.Color.fromRGB(col.getRed(), col.getGreen(), col.getBlue())), ChatColor.WHITE)+"");
	        match = hexPattern.matcher(s);
	    }
	    return ChatColor.translateAlternateColorCodes('&', s);
	}
	public static String getNumerical(int num) {
		switch (num) {
		default:
		case 1: return "I";
		case 2: return "II";
		case 3: return "III";
		case 4: return "IV";
		case 5: return "V";
		case 6: return "VI";
		case 7: return "VII";
		case 8: return "VIII";
		case 9: return "IX";
		case 10: return "X";
		}
	}
	@SuppressWarnings("deprecation")
	public static double getEnchantDamage(ItemStack item, LivingEntity toDamage) {
		ItemMeta meta = item.getItemMeta();
		double damage = 0.0;
		if (meta.hasEnchant(VersionUtils.getSharpness()))
			damage += 0.5 * (meta.getEnchantLevel(VersionUtils.getSharpness()) - 1) + 1.0;
		if (toDamage != null && meta.hasEnchant(VersionUtils.getSmite()) && toDamage.getCategory() == EntityCategory.UNDEAD)
			damage += 2.5 * meta.getEnchantLevel(VersionUtils.getSmite());
		if (toDamage != null && meta.hasEnchant(VersionUtils.getAthropods()) && toDamage.getCategory() == EntityCategory.ARTHROPOD)
			damage += 2.5 * meta.getEnchantLevel(VersionUtils.getAthropods());
		if (toDamage != null && meta.hasEnchant(Enchantment.IMPALING) && toDamage.getCategory() == EntityCategory.WATER)
			damage += 2.5 * meta.getEnchantLevel(Enchantment.IMPALING);
		return damage;
	}
	public static boolean rayTraceForSolid(Location initial, Location target) {
		Vector vec = getVectorTowards(initial, target);
		try {
			vec.checkFinite();
		} catch (IllegalArgumentException err) {
			return false;
		}
		int distance = (int) initial.distance(target);
		if (!initial.getBlock().isPassable())
			return true;
		Location temp = initial.clone();
		for (int i=1; i < distance; i++)
			if (!temp.add(vec).getBlock().isPassable())
				return true;
		return false;
	}
	public static Vector getVectorTowards(Location initial, Location towards) {
		return new Vector(towards.getX() - initial.getX(), towards.getY() - initial.getY(), towards.getZ() - initial.getZ()).normalize();
	}
	public static Vector getUnormalizedVectorTowards(Location initial, Location towards) {
		return new Vector(towards.getX() - initial.getX(), towards.getY() - initial.getY(), towards.getZ() - initial.getZ());
	}
	public static void runConsoleCommand(String command, World world) {
		Entity entity = world.spawn(new Location(world, 0, 0, 0), CommandMinecart.class);
		World tempWorld = Bukkit.getWorld("world");
		boolean gameRule = tempWorld.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK);
		tempWorld.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
		plugin.getServer().dispatchCommand(entity, command);
		tempWorld.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, gameRule);
		entity.remove();
	}
	public static void mergeEntityData(Entity entity, String data) {
		Location entityLoc = entity.getLocation();
		runConsoleCommand("data merge entity @e[x="+entityLoc.getX()+",y="+entityLoc.getY()+",z="+entityLoc.getZ()+",distance=..0.1,limit=1] "+data, entity.getWorld());
	}
	public static BlockFace getBlockFace(Player player) {
	    List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
	    if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding())
	    	return null;
	    Block targetBlock = lastTwoTargetBlocks.get(1);
	    Block adjacentBlock = lastTwoTargetBlocks.get(0);
	    return targetBlock.getFace(adjacentBlock);
	}
	public static Block rayCastForBlock(Location location, int minRange, int maxRange, int maxAttempts, Set<Material> materialWhitelist) {
		for (int i=0; i < maxAttempts; i++) {
			Location tempLoc = location.clone();
			Vector tempVec = new Vector(random.nextDouble(-1, 1), random.nextDouble(-1, 1), random.nextDouble(-1, 1)).normalize();
			for (int c=0; c < maxRange; c++) {
				tempLoc.add(tempVec);
				Block b = tempLoc.getBlock();
				if (!b.isPassable()) {
					if (c < minRange || (materialWhitelist != null && !materialWhitelist.contains(b.getType())))
						break;
					return b;
				}
			}
		}
		return null;
	}
	public static Block rayCastForBlock(Location location, int minRange, int maxRange, int maxAttempts, Set<Material> materialWhitelist, Set<Block> blockWhitelist) {
		for (int i=0; i < maxAttempts; i++) {
			Location tempLoc = location.clone();
			Vector tempVec = new Vector(random.nextDouble(-1, 1), random.nextDouble(-1, 1), random.nextDouble(-1, 1)).normalize();
			for (int c=0; c < maxRange; c++) {
				tempLoc.add(tempVec);
				Block b = tempLoc.getBlock();
				if (!b.isPassable()) {
					if (c < minRange || !blockWhitelist.contains(b) || (materialWhitelist != null && !materialWhitelist.contains(b.getType())))
						break;
					return b;
				}
			}
		}
		return null;
	}
	public static void damageItem(ItemStack toDamage, int damage) {
		ItemMeta meta = toDamage.getItemMeta();
		if (!(meta instanceof Damageable damageable))
			return;
		damageable.setDamage(damageable.getDamage() + damage);
		if (damageable.getDamage() >= toDamage.getType().getMaxDurability()) {
			toDamage.setAmount(0);
			return;
		}
		toDamage.setItemMeta(meta);
	}
	public static void repairItem(ItemStack toRepair, int health) {
		ItemMeta meta = toRepair.getItemMeta();
		if (!(meta instanceof Damageable damageable))
			return;
		damageable.setDamage(Math.max(damageable.getDamage() - health, 0));
		toRepair.setItemMeta(meta);
	}
	public static boolean isEnvironment(World world, Environment environment) {
		return world.getEnvironment() == environment || world.getEnvironment() == Environment.CUSTOM;
	}
	public static boolean isEnvironment(World world, Environment... environments) {
		Environment environment = world.getEnvironment();
		if (environment == Environment.CUSTOM)
			return true;
		for (Environment temp : environments)
			if (temp == environment)
				return true;
		return false;
	}
	public static Vector randomVector() {
		return new Vector(random.nextDouble(-1, 1), random.nextDouble(-1, 1), random.nextDouble(-1, 1));
	}
	public static RandomGenerator random() {
		return random;
	}
	public static List<ItemStack> createIngredients(Collection<Material> materials) {
		List<ItemStack> list = new ArrayList<>();
		for (Material material : materials)
			list.add(new ItemStack(material));
		return list;
	}
	public static Vector getRandomizedVector(double xWeight, double yWeight, double zWeight) {
		return new Vector(xWeight == 0 ? 0.0 : random.nextDouble(-xWeight, xWeight), yWeight == 0 ? 0.0 : random.nextDouble(-yWeight, yWeight), zWeight == 0 ? 0.0 : random.nextDouble(-zWeight, zWeight)).normalize();
	}
	public static Vector getRandomizedVector() {
		return new Vector(random.nextDouble(-1, 1), random.nextDouble(-1, 1), random.nextDouble(-1, 1)).normalize();
	}
	public static void sendErrorMessage() {
		Main.consoleSender.sendMessage(Utils.convertString("&c[UltimateContent]: An error has occurred above this message. Please report the full error to the discord https://discord.gg/MhXFj72VeN"));
	}
	public static double calculateAnimationValue(
            int currentFrame, 
            int totalFrames, 
            double acceleration, 
            double deceleration, 
            double maxValue
    ) {
        if (totalFrames <= 0 || currentFrame < 0 || currentFrame >= totalFrames) {
            throw new IllegalArgumentException("Invalid frame or totalFrames input.");
        }

        // Compute midpoint of animation (peak value)
        int midpoint = totalFrames / 2;

        double value;
        if (currentFrame < midpoint) {
            // Acceleration phase
            value = acceleration * currentFrame * currentFrame;
        } else {
            // Deceleration phase
            int frameFromEnd = totalFrames - currentFrame - 1;
            value = deceleration * frameFromEnd * frameFromEnd;
        }

        // Ensure value does not exceed maxValue
        return Math.min(value, maxValue);
    }
	public static Location findRandomSpotInRadius(Location initial, double minDist, double maxDist, int height, int attempts) {
		double squaredMin = minDist * minDist;
		double squaredMax = maxDist * maxDist;
		for (int i=0; i < attempts; i++) {
			double distance = random.nextDouble(minDist, maxDist);
			Location temp = EntityUtils.findSmartYSpawn(initial, initial.clone().add(getRandomizedVector().multiply(distance)), height, (int) Math.floor(Math.sqrt(squaredMax - (distance * distance))));
			if (temp != null && temp.distanceSquared(initial) >= squaredMin)
				return temp;
		}
		return null;
	}
	public static Location findRandomSpotInRadius(Location initial, double minDist, double maxDist, int height, int attempts, Predicate<Location> conditions) {
		double squaredMin = minDist * minDist;
		double squaredMax = maxDist * maxDist;
		for (int i=0; i < attempts; i++) {
			double distance = random.nextDouble(minDist, maxDist);
			Location temp = EntityUtils.findSmartYSpawn(initial, initial.clone().add(getRandomizedVector().multiply(distance)), height, (int) Math.floor(Math.sqrt(squaredMax - (distance * distance))));
			if (temp != null && temp.distanceSquared(initial) >= squaredMin && conditions.test(temp.clone()))
				return temp;
		}
		return null;
	}
	public static Location findRandomSpotInCircle(Location initial, double minDist, double maxDist) {
		return initial.clone().add(getRandomizedVector(1.0, 0.0, 1.0).multiply(random.nextDouble(minDist, maxDist)));
	}
	public static Location findRandomSpotInCircle(Location initial, double minDist, double maxDist, int attempts, Predicate<Location> conditions) {
		for (int i=0; i < attempts; i++) {
			double distance = random.nextDouble(minDist, maxDist);
			Location temp = initial.clone().add(getRandomizedVector(1.0, 0.0, 1.0).multiply(distance));
			if (conditions.test(temp.clone()))
				return temp;
		}
		return null;
	}
	public static Block rayTraceForBlock(Location location, int maxDistance, int attempts, Predicate<Block> condition, Supplier<Vector> createVector) {
		for (int i=0; i < attempts; i++) {
			Vector vec = createVector.get();
			Location temp = location.clone().add(vec);
			for (int j=1; j < maxDistance; j++) {
				Block block = temp.getBlock();
//				block.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, temp, 1, 0, 0, 0, .001);
				if (condition.test(block))
					return block;
				if (!block.isPassable())
					break;
				temp.add(vec);
			}
		}
		return null;
	}
	public static Block rayTraceForBlock(Location location, int maxDistance, int attempts, Predicate<Block> condition) {
		return rayTraceForBlock(location, maxDistance, attempts, condition, () -> getRandomizedVector());
	}
	public static Block getHighestExposedBlock(Block block, int maxDistance) {
		if (block == null)
			return null;
		Block b = block;
		if (b.isPassable())
			for (int i=0; i < maxDistance; i++) {
				b = b.getRelative(BlockFace.DOWN);
				if (!b.isPassable())
					return b;
			}
		else
			for (int i=0; i < maxDistance; i++) {
				b = b.getRelative(BlockFace.UP);
				if (b.isPassable())
					return b.getRelative(BlockFace.DOWN);
			}
		return null;
	}
	public static Queue<Block> getBlocksInCircleRadius(Location location, double radius) {
		Queue<Block> queue = new ArrayDeque<>();
		double radiusSquared = radius * radius;
		Vector block = new Vector(location.getX(), location.getY(), location.getZ());
		World world = location.getWorld();
		for (double x = -radius; x <= radius; x++)
			for (double z = -radius; z <= radius; z++) {
				Vector position = block.clone().add(new Vector(x, 0, z));
				if (block.distanceSquared(position) <= radiusSquared)
					queue.add(position.toLocation(world).getBlock());
			}
		return queue;
	}
	public static Queue<Block> getBlocksInCircleCircumference(Location location, double radius) {
		Queue<Block> queue = new ArrayDeque<>();
		double outerRadius = radius * radius;
		double innerRadius = (radius - 1) * (radius - 1);
		Vector block = new Vector(location.getX(), location.getY(), location.getZ());
		World world = location.getWorld();
		for (double x = -radius; x <= radius; x++)
			for (double z = -radius; z <= radius; z++) {
				Vector position = block.clone().add(new Vector(x, 0, z));
				double distance = block.distanceSquared(position);
				if (distance <= outerRadius && distance > innerRadius)
					queue.add(position.toLocation(world).getBlock());
			}
		return queue;
	}
	public static Queue<Block> getBlocksInSphereRadius(Location location, double radius) {
		Queue<Block> queue = new ArrayDeque<>();
		double radiusSquared = radius * radius;
		for (double x = -radius; x <= radius; x++)
			for (double y = -radius; y <= radius; y++)
				for (double z = -radius; z <= radius; z++) {
					Location position = location.clone().add(x, y, z);
					if (position.distanceSquared(location) <= radiusSquared) {
						queue.add(position.getBlock());
//						position.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, getCenterOfBlock(position.getBlock()), 1, 0, 0, 0, .00001);
					}
				}
		return queue;
	}
	public static int clamp(int value, int min, int max) {
		return value < min ? min : value > max ? max : value;
	}
	public static double clamp(double value, double min, double max) {
		return value < min ? min : value > max ? max : value;
	}
	public static float clamp(float value, float min, float max) {
		return value < min ? min : value > max ? max : value;
	}
	public static boolean isLocationsWithinDistance(Location loc1, Location loc2, double distanceSquared) {
		return loc1 != null && loc2 != null && loc1.getWorld().equals(loc2.getWorld()) && loc1.distanceSquared(loc2) <= distanceSquared;
	}
	public static enum AreaClearing {
		
		CUBE_3X3_FROM_CENTER(block -> {
			for (int x = block.getX() - 1; x <= block.getX() + 1; x++)
				for (int y = block.getY() - 1; y <= block.getY() + 1; y++)
					for (int z = block.getZ() - 1; z <= block.getZ() + 1; z++)
						if (!block.getWorld().getBlockAt(x, y, z).isPassable())
							return false;
			return true;
		}),
		CUBE_3X3_FROM_CENTER_BOTTOM(block -> {
			for (int x = block.getX() - 1; x <= block.getX() + 1; x++)
				for (int y = block.getY(); y <= block.getY() + 2; y++)
					for (int z = block.getZ() - 1; z <= block.getZ() + 1; z++)
						if (!block.getWorld().getBlockAt(x, y, z).isPassable())
							return false;
			return true;
		}),
		CUBE_3X3_FROM_CENTER_TOP(block -> {
			for (int x = block.getX() - 1; x <= block.getX() + 1; x++)
				for (int y = block.getY() - 2; y <= block.getY(); y++)
					for (int z = block.getZ() - 1; z <= block.getZ() + 1; z++)
						if (!block.getWorld().getBlockAt(x, y, z).isPassable())
							return false;
			return true;
		}),
		PLUS_SIGN_3D_FROM_CENTER(block -> {
			for (BlockFace face : Set.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST))
				if (!block.getRelative(face).isPassable())
					return false;
			return true;
		});
		
		private Function<Block, Boolean> function;
		
		private AreaClearing(Function<Block, Boolean> function) {
			this.function = function;
		}
	}
	public static boolean isAreaClear(Block block, AreaClearing clearing) {
		return clearing.function.apply(block);
	}
	public static boolean isAreaClear(Location location, double radius) {
		for (Block b : getBlocksInSphereRadius(location, radius))
			if (!b.isPassable())
				return false;
		return true;
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
	public static Vector getParabolicVelocity(Location from, Location to, double heightGain, double gravity) {
	    double dx = to.getX() - from.getX();
	    double dz = to.getZ() - from.getZ();
	    double horizontalDist = Math.sqrt(dx * dx + dz * dz);

	    double endGain = to.getY() - from.getY();
	    double maxGain = Math.max(heightGain, endGain + heightGain);

	    double velocityY = Math.sqrt(2 * gravity * maxGain);
	    double timeUp = velocityY / gravity;
	    double timeDown = Math.sqrt(2 * (maxGain - endGain) / gravity);
	    double totalTime = timeUp + timeDown;

	    double velocityXZ = horizontalDist / totalTime;

	    Vector velocity = new Vector(dx, 0, dz).normalize().multiply(velocityXZ);
	    velocity.setY(velocityY);

	    return velocity;
	}
	public static Location getCenterOfBlock(Block block) {
		return block.getLocation().add(.5, .5, .5);
	}
	public static <T> boolean isNotNullAndCondition(T object, Predicate<T> condition) {
		return object != null && condition.test(object);
	}
}

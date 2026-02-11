package com.github.jewishbanana.ultimatecontent.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public static Vector getVectorTowards(Location initial, Location towards) {
	    final float dx = (float)(towards.getX() - initial.getX());
	    final float dy = (float)(towards.getY() - initial.getY());
	    final float dz = (float)(towards.getZ() - initial.getZ());
	    final float lengthSquared = dx * dx + dy * dy + dz * dz;
	    if (lengthSquared == 0.0f)
	        return new Vector(0, 0, 0);
	    final float invLength = fastInverseSqrt(lengthSquared);
	    return new Vector(dx * invLength, dy * invLength, dz * invLength);
	}
	public static Vector getNoneNormalizedVectorTowards(Location initial, Location towards) {
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
	public static List<ItemStack> createIngredients(Collection<Material> materials) {
		List<ItemStack> list = new ArrayList<>();
		for (Material material : materials)
			list.add(new ItemStack(material));
		return list;
	}
	static float fastInverseSqrt(float x) {
	    final float halfX = 0.5f * x;
	    int i = Float.floatToRawIntBits(x);
	    i = 0x5f3759df - (i >> 1);
	    float y = Float.intBitsToFloat(i);
	    y = y * (1.5f - halfX * y * y);
	    return y;
	}
	public static Vector getRandomizedVector(float xWeight, float yWeight, float zWeight) {
	    final float x = xWeight == 0 ? 0.0f : random.nextFloat(-xWeight, xWeight);
	    final float y = yWeight == 0 ? 0.0f : random.nextFloat(-yWeight, yWeight);
	    final float z = zWeight == 0 ? 0.0f : random.nextFloat(-zWeight, zWeight);
	    final float lengthSquared = x * x + y * y + z * z;
	    if (lengthSquared == 0.0f)
	        return new Vector(0, 0, 0);
	    final float invLength = fastInverseSqrt(lengthSquared);
	    return new Vector(x * invLength, y * invLength, z * invLength);
	}
	public static Vector getRandomizedVector() {
	    final float x = random.nextFloat(-1.0f, 1.0f);
	    final float y = random.nextFloat(-1.0f, 1.0f);
	    final float z = random.nextFloat(-1.0f, 1.0f);
	    final float lengthSquared = x * x + y * y + z * z;
	    if (lengthSquared == 0.0f)
	        return new Vector(0, 0, 0);
	    final float invLength = fastInverseSqrt(lengthSquared);
	    return new Vector(x * invLength, y * invLength, z * invLength);
	}
	public static double calculateAnimationValue(int currentFrame, int totalFrames, double acceleration, double deceleration, double maxValue) {
		if (totalFrames <= 0 || currentFrame < 0 || currentFrame >= totalFrames)
			throw new IllegalArgumentException("Invalid frame or totalFrames input.");
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
	public static Location findRandomSpotInRadius(Location initial, float minDist, float maxDist, int height, int attempts, Supplier<Vector> vector, Predicate<Location> conditions) {
		final double squaredMin = minDist * minDist;
		final World world = initial.getWorld();
		final double initialX = initial.getX();
		final double initialY = initial.getY();
	    final double initialZ = initial.getZ();
	    final int verticalRange = (int) maxDist;
	    for (int i = 0; i < attempts; i++) {
	        final float distance = random.nextFloat(minDist, maxDist);
	        final Vector dir = vector.get();
	        final double offsetX = dir.getX() * distance;
	        final double offsetZ = dir.getZ() * distance;
	        final Location searchLoc = new Location(world, initialX + offsetX, initialY, initialZ + offsetZ);
	        final Location temp = SpawnUtils.findSmartYSpawn(initial, searchLoc, height, verticalRange);
	        if (temp != null) {
	            final double dx = temp.getX() - initialX;
	            final double dz = temp.getZ() - initialZ;
	            final double distSquared = dx * dx + dz * dz;
	            if (distSquared >= squaredMin && conditions.test(temp))
	                return temp;
	        }
	    }
	    return null;
	}
	public static Location findRandomSpotInRadius(Location initial, float minDist, float maxDist, int height, int attempts, Supplier<Vector> vector) {
		return findRandomSpotInRadius(initial, minDist, maxDist, height, attempts, vector, test -> true);
	}
	public static Location findRandomSpotInRadius(Location initial, float minDist, float maxDist, int height, int attempts) {
		return findRandomSpotInRadius(initial, minDist, maxDist, height, attempts, () -> getRandomizedVector());
	}
	public static Location findRandomSpotInCircle(Location initial, float minDist, float maxDist, int attempts, Predicate<Location> conditions) {
	    final World world = initial.getWorld();
	    final double initialX = initial.getX();
	    final double initialY = initial.getY();
	    final double initialZ = initial.getZ();
	    for (int i = 0; i < attempts; i++) {
	        final float distance = random.nextFloat(minDist, maxDist);
	        final Vector dir = getRandomizedVector(1f, 0f, 1f);
	        final double x = initialX + dir.getX() * distance;
	        final double y = initialY + dir.getY() * distance;
	        final double z = initialZ + dir.getZ() * distance;
	        final Location temp = new Location(world, x, y, z);
	        if (conditions.test(temp))
	            return temp;
	    }
	    return null;
	}
	public static Location findRandomSpotInCircle(Location initial, float minDist, float maxDist) {
		return initial.clone().add(getRandomizedVector(1f, 0f, 1f).multiply(random.nextFloat(minDist, maxDist)));
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
	public static boolean isLocationsWithinDistance(Location loc1, Location loc2, float distanceSquared) {
		return loc1 != null && loc2 != null && loc1.getWorld().equals(loc2.getWorld()) && loc1.distanceSquared(loc2) <= distanceSquared;
	}
	public static enum AreaClearing {
		
		CUBE_3X3_FROM_CENTER(block -> {
			final World world = block.getWorld();
			for (int x = block.getX() - 1; x <= block.getX() + 1; x++)
				for (int y = block.getY() - 1; y <= block.getY() + 1; y++)
					for (int z = block.getZ() - 1; z <= block.getZ() + 1; z++)
						if (!world.getBlockAt(x, y, z).isPassable())
							return false;
			return true;
		}),
		CUBE_3X3_FROM_CENTER_BOTTOM(block -> {
			final World world = block.getWorld();
			for (int x = block.getX() - 1; x <= block.getX() + 1; x++)
				for (int y = block.getY(); y <= block.getY() + 2; y++)
					for (int z = block.getZ() - 1; z <= block.getZ() + 1; z++)
						if (!world.getBlockAt(x, y, z).isPassable())
							return false;
			return true;
		}),
		CUBE_3X3_FROM_CENTER_TOP(block -> {
			final World world = block.getWorld();
			for (int x = block.getX() - 1; x <= block.getX() + 1; x++)
				for (int y = block.getY() - 2; y <= block.getY(); y++)
					for (int z = block.getZ() - 1; z <= block.getZ() + 1; z++)
						if (!world.getBlockAt(x, y, z).isPassable())
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
	public static boolean isAreaClear(Location location, float radius) {
		for (Block b : BlockUtils.getBlocksInSphereRadius(location, radius))
			if (!b.isPassable())
				return false;
		return true;
	}
	public static boolean isAreaClear(Location location, float radius, float height) {
		for (Block b : BlockUtils.getBlocksInCylinderRadius(location, radius, height))
			if (!b.isPassable())
				return false;
		return true;
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
	public static <T> boolean isNotNullAndCondition(T object, Predicate<T> condition) {
		return object != null && condition.test(object);
	}
	public static void sendExceptionLog(Exception error) {
		error.printStackTrace();
		Main.consoleSender.sendMessage(Utils.convertString("&c[UltimateContent]: An error has occurred above this message. Please report the full error to the discord https://discord.gg/MhXFj72VeN"));
	}
	public static void sendConsoleMessage(String message) {
		Main.consoleSender.sendMessage(Utils.prefix + convertString(message));
	}
	public static RandomGenerator getRandomGenerator() {
		return random;
	}
}

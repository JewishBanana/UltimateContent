package com.github.jewishbanana.ultimatecontent.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.github.jewishbanana.ultimatecontent.Main;

public class Utils {
	
	private static Main plugin;
	private static Random rand;
	public static int descriptionLine;
	public static String prefix;
	private static Pattern hexPattern;
	private static boolean usingSpigot;
	private static Map<DyeColor, ChatColor> dyeChatMap;
	private static FixedMetadataValue fallingBlockData;
	static
	{
		hexPattern = Pattern.compile("\\(hex:#[a-fA-F0-9]{6}\\)");
		plugin = Main.getInstance();
		rand = new Random();
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
		
		fallingBlockData = new FixedMetadataValue(plugin, "protected");
		
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
	public static List<String> chopLore(List<String> lore) {
		List<String> tempLore = new ArrayList<>();
		if (lore != null)
			for (String line : lore) {
				line = Utils.convertString(line);
				int offset = 0;
				for (int i=0; i < line.length(); i++)
					if (line.charAt(i) == ChatColor.COLOR_CHAR)
						offset += 2;
				int max_length = descriptionLine + offset;
				if (line.length()-1 > max_length) {
					int c = 0;
					for (int i=max_length; i > 0; i--) {
						if (i == 0) {
							tempLore.add(Utils.convertString(ChatColor.getLastColors(line.substring(0, c))+line.substring(c)));
							break;
						}
						if (line.charAt(i) == ' ') {
							tempLore.add(Utils.convertString(ChatColor.getLastColors(line.substring(0, c+1))+line.substring(c, i)));
							c += i-c+1;
							if (i+max_length >= line.length()) {
								tempLore.add(Utils.convertString(ChatColor.getLastColors(line.substring(0, c))+line.substring(c, line.length())));
								break;
							}
							i = c+max_length;
						}
					}
				} else
					tempLore.add(line);
			}
		return tempLore;
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
	public static ArmorStand lockArmorStand(ArmorStand stand, boolean setInvisible, boolean setGravity, boolean setMarker) {
		if (VersionUtils.getMCVersion() >= 1.17) {
			stand.setInvisible(setInvisible);
			stand.addEquipmentLock(EquipmentSlot.CHEST, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.FEET, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.HAND, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.HEAD, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.LEGS, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
			stand.addEquipmentLock(EquipmentSlot.OFF_HAND, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		} else
			stand.setVisible(!setInvisible);
		stand.setGravity(setGravity);
		stand.setArms(true);
		stand.setMarker(setMarker);
		return stand;
	}
	public static Location findSmartYSpawn(Location pivot, Location spawn, int height, int maxDistance) {
		Block b = spawn.getBlock();
		Location loc1 = null, loc2 = null;
		down:
			for (int i = spawn.getBlockY(); i > spawn.getBlockY()-maxDistance; i--) {
				b = b.getRelative(BlockFace.DOWN);
				if (!b.isPassable() && b.getRelative(BlockFace.UP).isPassable() && !b.getRelative(BlockFace.UP).isLiquid()) {
					for (int c = 2; c <= height-1; c++)
						if (!b.getRelative(BlockFace.UP, c).isPassable())
							continue down;
					loc1 = b.getRelative(BlockFace.UP).getLocation().add(0.5,0.01,0.5);
					break down;
				}
			}
		b = spawn.getBlock();
		up:
			for (int i = spawn.getBlockY(); i < spawn.getBlockY()+maxDistance; i++) {
				b = b.getRelative(BlockFace.UP);
				if (b.isPassable() && !b.getRelative(BlockFace.DOWN).isPassable() && !b.isLiquid()) {
					for (int c = 1; c < height; c++)
						if (!b.getRelative(BlockFace.UP, c).isPassable())
							continue up;
					loc2 = b.getLocation().add(0.5,0.01,0.5);
					break up;
				}
			}
		if (loc1 != null && loc2 == null)
			return loc1;
		else if (loc1 == null && loc2 != null)
			return loc2;
		else if (loc1 == null && loc2 == null)
			return null;
		if (Math.abs(pivot.getY()-loc2.getY()) < Math.abs(pivot.getY()-loc1.getY()))
			return loc1;
		else
			return loc2;
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
	public static boolean rayTraceEntityConeForSolid(Entity entity, Location initial) {
		double height = entity.getHeight(), width = entity.getWidth();
		Location target = entity.getLocation().add(0,height/2.0,0);
		if (rayTraceForSolid(initial, target))
			return true;
		if (rayTraceForSolid(initial, target.add(0,height/2.0,0)))
			return true;
		if (rayTraceForSolid(initial, target.clone().subtract(0,height/2.0,0)))
			return true;
		Vector angle = Utils.getVectorTowards(initial, target);
		try {
			angle.checkFinite();
		} catch (IllegalArgumentException err) {
			return false;
		}
		if (rayTraceForSolid(initial, target.clone().add(new Vector(angle.getZ(), 0, -angle.getX()).normalize().multiply(width/2.0))))
			return true;
		if (rayTraceForSolid(initial, target.clone().add(new Vector(-angle.getZ(), 0, angle.getX()).normalize().multiply(width/2.0))))
			return true;
		return false;
	}
	public static Vector getVectorTowards(Location initial, Location towards) {
		return new Vector(towards.getX() - initial.getX(), towards.getY() - initial.getY(), towards.getZ() - initial.getZ()).normalize();
	}
	public static Vector getUnormalizedVectorTowards(Location initial, Location towards) {
		return new Vector(towards.getX() - initial.getX(), towards.getY() - initial.getY(), towards.getZ() - initial.getZ());
	}
	public static void makeEntityFaceLocation(Entity entity, Location to) {
		Vector dirBetweenLocations = to.toVector().subtract(entity.getLocation().toVector());
		entity.teleport(entity.getLocation().setDirection(dirBetweenLocations));
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
	@SuppressWarnings("removal")
	public static <T extends EntityDamageEvent> boolean pureDamageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, Entity source, T event, DamageCause cause) {
		if (entity.isDead())
			return false;
		if (event != null) {
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				return false;
			entity.setLastDamageCause(event);
		} else
			entity.setLastDamageCause(new EntityDamageEvent(entity, cause, damage));
		if (entity.getHealth()-damage <= 0) {
			if (!ignoreTotem) {
				entity.setHealth(0.00001);
				if (meta != null && entity.getEquipment().getItemInMainHand().getType() != Material.TOTEM_OF_UNDYING && entity.getEquipment().getItemInOffHand().getType() != Material.TOTEM_OF_UNDYING)
					entity.setMetadata(meta, plugin.fixedData);
				entity.damage(1);
				return true;
			}
			if (meta != null)
				entity.setMetadata(meta, plugin.fixedData);
			entity.setHealth(0);
			playDamageEffect(entity);
			return true;
		}
		entity.setHealth(Math.max(entity.getHealth()-damage, 0));
		playDamageEffect(entity);
		return true;
	}
	@SuppressWarnings("removal")
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, Entity source, DamageCause cause) {
		if (source == null)
			return pureDamageEntity(entity, damage, meta, ignoreTotem, source, new EntityDamageEvent(entity, cause, damage), null);
		return pureDamageEntity(entity, damage, meta, ignoreTotem, source, new EntityDamageByEntityEvent(source, entity, cause, damage), null);
	}
	@SuppressWarnings("removal")
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, DamageCause cause) {
		return pureDamageEntity(entity, damage, meta, ignoreTotem, null, new EntityDamageEvent(entity, cause, damage), null);
	}
	public static void damageArmor(LivingEntity entity, double damage) {
		int dmg = Math.max((int) (damage + 4 / 4), 1);
		for (ItemStack armor : entity.getEquipment().getArmorContents()) {
			if (armor == null || armor.getItemMeta() == null)
				continue;
			ItemMeta meta = armor.getItemMeta();
			if (((Damageable) meta).getDamage() >= armor.getType().getMaxDurability()) armor.setAmount(0);
			else ((Damageable) meta).setDamage(((Damageable) meta).getDamage()+dmg);
			armor.setItemMeta(meta);
		}
	}
	public static <T extends EntityDamageEvent> boolean damageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, Entity source, T event) {
		if (event != null) {
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled())
				return false;
		}
		double armor = entity.getAttribute(Attribute.GENERIC_ARMOR).getValue();
		double toughness = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
		double actualDamage = damage * (1 - Math.min(20, Math.max(armor / 5, armor - damage / (2 + toughness / 4))) / 25);
		Utils.pureDamageEntity(entity, actualDamage, meta, ignoreTotem, source, null, event.getCause());
		Utils.damageArmor(entity, actualDamage);
		return true;
	}
	@SuppressWarnings("removal")
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, Entity source, DamageCause cause) {
		if (source != null)
			return damageEntity(entity, damage, meta, ignoreTotem, source, new EntityDamageByEntityEvent(source, entity, cause, damage));
		return damageEntity(entity, damage, meta, ignoreTotem, source, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("removal")
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, DamageCause cause) {
		return damageEntity(entity, damage, meta, ignoreTotem, null, new EntityDamageEvent(entity, cause, damage));
	}
//	public static EntityDamageByEntityEvent versionSafeDamageEvent(Entity source, Entity damagee, DamageCause cause, double damage, Location damageLocation, String damageType) {
//		if (usingDeprecatedDamageEvent)
//			return new EntityDamageByEntityEvent(source, damagee, cause, DamageSource.builder(DamageType.), damage);
//	}
	public static Block rayCastForBlock(Location location, int minRange, int maxRange, int maxAttempts, Set<Material> materialWhitelist) {
		for (int i=0; i < maxAttempts; i++) {
			Location tempLoc = location.clone();
			Vector tempVec = new Vector((rand.nextDouble()*2)-1, (rand.nextDouble()*2)-1, (rand.nextDouble()*2)-1).normalize();
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
			Vector tempVec = new Vector((rand.nextDouble()*2)-1, (rand.nextDouble()*2)-1, (rand.nextDouble()*2)-1).normalize();
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
		((Damageable) meta).setDamage(((Damageable) meta).getDamage()+damage);
		if (((Damageable) meta).getDamage() >= toDamage.getType().getMaxDurability())
			toDamage.setAmount(0);
		else
			toDamage.setItemMeta(meta);
	}
	public static void repairItem(ItemStack toRepair, int health) {
		ItemMeta meta = toRepair.getItemMeta();
		((Damageable) meta).setDamage(Math.max(((Damageable) meta).getDamage()-health, 0));
		toRepair.setItemMeta(meta);
	}
	public static boolean isEnvironment(World world, Environment environment) {
		return world.getEnvironment() == environment || world.getEnvironment() == Environment.CUSTOM;
	}
	public static Vector randomVector() {
		return new Vector(rand.nextDouble()*2-1.0, rand.nextDouble()*2-1.0, rand.nextDouble()*2-1.0);
	}
	public static Random random() {
		return rand;
	}
	public static EquipmentSlot getEquipmentSlot(EntityEquipment inventory, ItemStack item) {
		for (EquipmentSlot slot : EquipmentSlot.values())
			if (inventory.getItem(slot).equals(item))
				return slot;
		return null;
	}
	public static boolean isPlayerImmune(Player player) {
		GameMode mode = player.getGameMode();
		return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
	}
	public static boolean isEntityImmunePlayer(Entity entity) {
		if (!(entity instanceof Player))
			return false;
		GameMode mode = ((Player) entity).getGameMode();
		return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
	}
	public static List<ItemStack> createIngredients(Collection<Material> materials) {
		List<ItemStack> list = new ArrayList<>();
		for (Material material : materials)
			list.add(new ItemStack(material));
		return list;
	}
	public static void markFallingBlock(FallingBlock block) {
		block.setMetadata("ui-fb", fallingBlockData);
	}
	@SuppressWarnings("deprecation")
	public static void playDamageEffect(LivingEntity entity) {
		if (VersionUtils.usingNewDamageEvent)
			entity.playHurtAnimation(0);
		else
			entity.playEffect(EntityEffect.HURT);
	}
	public static Vector getRandomizedVector(double xWeight, double yWeight, double zWeight) {
		return new Vector(rand.nextDouble()*xWeight-(xWeight/2.0), rand.nextDouble()*yWeight-(yWeight/2.0), rand.nextDouble()*zWeight-(zWeight/2.0)).normalize();
	}
	public static Vector getRandomizedVector() {
		return new Vector(rand.nextDouble()*2.0-1.0, rand.nextDouble()*2.0-1.0, rand.nextDouble()*2.0-1.0).normalize();
	}
}

package com.github.jewishbanana.ultimatecontent.utils;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.LoadoutEquipmentSlot;

public class EntityUtils {
	
	private static final JavaPlugin plugin;
	private static Set<Material> leatherArmor;
	private static final FixedMetadataValue fallingBlockData;
	private static final FixedMetadataValue damageData;
	static {
		plugin = Main.getInstance();
		leatherArmor = Set.of(Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET);
		fallingBlockData = new FixedMetadataValue(plugin, "protected");
		damageData = Main.getFixedMetadata();
	}
	
	@SuppressWarnings("removal")
	private static <T extends EntityDamageEvent> boolean pureDamageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, T event) {
		if (entity == null || entity.isDead())
			return false;
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return false;
		entity.setLastDamageCause(event);
		if (entity.getHealth()-damage <= 0) {
			if (!ignoreTotem && (entity.getEquipment().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING || entity.getEquipment().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING)) {
				if (event instanceof EntityDamageByEntityEvent damageEntityEvent)
					entity.damage(1, damageEntityEvent.getDamager());
				else
					entity.damage(1);
				return true;
			}
			if (meta != null)
				entity.setMetadata(meta, damageData);
			entity.setHealth(0);
			playDamageEffect(entity);
			if (meta != null)
				entity.removeMetadata(meta, plugin);
			return true;
		}
		entity.setHealth(entity.getHealth()-damage);
		playDamageEffect(entity);
		if (event instanceof EntityDamageByEntityEvent damageEntityEvent && entity instanceof Mob mob && damageEntityEvent.getDamager() instanceof LivingEntity livingDamager)
			mob.setTarget(livingDamager);
		return true;
	}
	@SuppressWarnings("removal")
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, Entity source, @NotNull DamageCause cause, boolean ignoreTotem) {
		if (source != null)
			return pureDamageEntity(entity, damage, meta, ignoreTotem, new EntityDamageByEntityEvent(source, entity, cause, damage));
		return pureDamageEntity(entity, damage, meta, ignoreTotem, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("removal")
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, Entity source, @NotNull DamageCause cause) {
		if (source != null)
			return pureDamageEntity(entity, damage, meta, false, new EntityDamageByEntityEvent(source, entity, cause, damage));
		return pureDamageEntity(entity, damage, meta, false, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("removal")
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, boolean ignoreTotem) {
		return pureDamageEntity(entity, damage, meta, ignoreTotem, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("removal")
	public static boolean pureDamageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause) {
		return pureDamageEntity(entity, damage, meta, false, new EntityDamageEvent(entity, cause, damage));
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
	private static <T extends EntityDamageEvent> boolean damageEntity(LivingEntity entity, double damage, String meta, boolean ignoreTotem, T event) {
		double armor = entity.getAttribute(Attribute.GENERIC_ARMOR).getValue();
		double toughness = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
		double actualDamage = damage * (1 - Math.min(20, Math.max(armor / 5, armor - damage / (2 + toughness / 4))) / 25);
		if (pureDamageEntity(entity, actualDamage, meta, ignoreTotem, event)) {
			damageArmor(entity, actualDamage);
			return true;
		}
		return false;
	}
	@SuppressWarnings("removal")
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, Entity source, @NotNull DamageCause cause, boolean ignoreTotem) {
		if (source != null)
			return damageEntity(entity, damage, meta, ignoreTotem, new EntityDamageByEntityEvent(source, entity, cause, damage));
		return damageEntity(entity, damage, meta, ignoreTotem, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("removal")
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, Entity source, @NotNull DamageCause cause) {
		if (source != null)
			return damageEntity(entity, damage, meta, false, new EntityDamageByEntityEvent(source, entity, cause, damage));
		return damageEntity(entity, damage, meta, false, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("removal")
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause, boolean ignoreTotem) {
		return damageEntity(entity, damage, meta, ignoreTotem, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("removal")
	public static boolean damageEntity(LivingEntity entity, double damage, String meta, @NotNull DamageCause cause) {
		return damageEntity(entity, damage, meta, false, new EntityDamageEvent(entity, cause, damage));
	}
	@SuppressWarnings("deprecation")
	public static void playDamageEffect(LivingEntity entity) {
		if (VersionUtils.usingNewDamageEvent)
			entity.playHurtAnimation(0);
		else
			entity.playEffect(EntityEffect.HURT);
	}
	public static Location findSmartYSpawn(Location pivot, Location spawn, double height, int maxDistance) {
		if (pivot == null || spawn == null)
			return null;
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
	public static void markFallingBlock(FallingBlock block) {
		block.setMetadata("uc-fb", fallingBlockData);
	}
	public static void lockArmorStand(ArmorStand stand) {
		stand.addEquipmentLock(EquipmentSlot.CHEST, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.FEET, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HAND, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.HEAD, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.LEGS, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
		stand.addEquipmentLock(EquipmentSlot.OFF_HAND, org.bukkit.entity.ArmorStand.LockType.ADDING_OR_CHANGING);
	}
	public static boolean rayTraceEntityConeForSolid(Entity entity, Location initial) {
		double height = entity.getHeight(), width = entity.getWidth();
		Location target = entity.getLocation().add(0,height/2.0,0);
		if (Utils.rayTraceForSolid(initial, target))
			return true;
		if (Utils.rayTraceForSolid(initial, target.add(0,height/2.0,0)))
			return true;
		if (Utils.rayTraceForSolid(initial, target.clone().subtract(0,height/2.0,0)))
			return true;
		Vector angle = Utils.getVectorTowards(initial, target);
		try {
			angle.checkFinite();
		} catch (IllegalArgumentException err) {
			return false;
		}
		if (Utils.rayTraceForSolid(initial, target.clone().add(new Vector(angle.getZ(), 0, -angle.getX()).normalize().multiply(width/2.0))))
			return true;
		if (Utils.rayTraceForSolid(initial, target.clone().add(new Vector(-angle.getZ(), 0, angle.getX()).normalize().multiply(width/2.0))))
			return true;
		return false;
	}
	public static void makeEntityFaceLocation(Entity entity, Location to) {
		Vector dirBetweenLocations = to.toVector().subtract(entity.getLocation().toVector());
		entity.teleport(entity.getLocation().setDirection(dirBetweenLocations));
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
		if (!(entity instanceof Player player))
			return false;
		GameMode mode = player.getGameMode();
		return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
	}
	public static void modifyLoadoutArmorColor(BaseEntity<? extends LivingEntity> base, int red, int green, int blue, LoadoutEquipmentSlot... slots) {
		LivingEntity entity = base.getCastedEntity();
		ItemStack[] armor = entity.getEquipment().getArmorContents();
		for (LoadoutEquipmentSlot slot : slots) {
			ItemStack item = armor[slot.slotIndex];
			if (item == null || !item.hasItemMeta() || !leatherArmor.contains(item.getType()) || base.getEntityType().loadout.armor[slot.slotIndex] != null)
				continue;
			LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
			meta.setColor(Color.fromRGB(red, green, blue));
			item.setItemMeta(meta);
		}
	}
	public static boolean isEntityUnderHealth(LivingEntity entity, double value) {
		if (entity == null)
			return false;
		return entity.getHealth() < entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * value;
	}
}

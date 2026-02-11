package com.github.jewishbanana.ultimatecontent.items;

import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.ItemCategory.DefaultCategory;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class BaseItem extends GenericItem {

	protected static final JavaPlugin plugin;
	protected static final RandomGenerator random;
	protected static final Enchantment powerEnchant;
	protected static final Enchantment protectionEnchant;
	
	public static final String PARTICLE_LORE_IDENTIFIER = "particles";
	
	private static final Map<Class<?>, String> configMap;
	
	static {
		plugin = Main.getInstance();
		random = RandomGenerator.of("SplittableRandom");
		powerEnchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("power"));
		protectionEnchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("protection"));
		
		configMap = new HashMap<>();
	}
	
	private boolean onCooldown;
	
	public BaseItem(ItemStack item) {
		super(item);
	}
	public void activatedAbility(Ability ability, Event event, Entity activator, Entity target) {}
	
	public int getIntegerField(String field) {
		try {
			return plugin.getConfig().getInt(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dinteger &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return 0;
		}
	}
	public double getDoubleField(String field) {
		try {
			return plugin.getConfig().getDouble(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &ddouble &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return 0.0;
		}
	}
	public boolean getBooleanField(String field) {
		try {
			return plugin.getConfig().getBoolean(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dboolean &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return false;
		}
	}
	public String getStringField(String field) {
		try {
			return plugin.getConfig().getString(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dstring &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return null;
		}
	}
	public boolean isOnCooldown() {
		return onCooldown;
	}
	public void setCooldown(int ticks) {
		this.onCooldown = true;
		plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.onCooldown = false, ticks);
	}
	public boolean shouldConsumeItem() {
		return false;
	}
	public String getConfigPath() {
		return configMap.get(this.getClass());
	}
	public static void setConfigPath(Class<?> clazz, String configPath) {
		configMap.put(clazz, configPath);
	}
	public String getConfigItemSection() {
		return "misc";
	}
	public Rarity getRarity() {
		return Rarity.NONE;
	}
	public boolean isDeprecated() {
		return false;
	}
	protected enum CustomItemCategories {
		
		EASTER_ITEMS("uc:easter_items", "&aEaster Items"),
		CHRISTMAS_ITEMS("uc:christmas_items", "&cChristmas Items");
		
		private ItemCategory category;
		
		private CustomItemCategories(String identifier, String displayName) {
			this.category = new ItemCategory(identifier, Utils.convertString(displayName));
		}
		public ItemCategory getItemCategory() {
			return category;
		}
	}
	public static void createCustomCategories() {
		ItemCategory.addItemCategoryAfter(CustomItemCategories.EASTER_ITEMS.getItemCategory(), DefaultCategory.MISCELLANEOUS);
		ItemCategory.addItemCategoryAfter(CustomItemCategories.CHRISTMAS_ITEMS.getItemCategory(), CustomItemCategories.EASTER_ITEMS.getItemCategory());
	}
}

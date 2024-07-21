package com.github.jewishbanana.ultimatecontent.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class BaseItem extends GenericItem {

	protected static Main plugin;
	protected static Random random;
	protected static Enchantment powerEnchant;
	protected static Enchantment protectionEnchant;
	static {
		plugin = Main.getInstance();
		random = new Random();
		powerEnchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("power"));
		protectionEnchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("protection"));
	}
	
	private static Map<Class<?>, String> configMap = new HashMap<>();
	
	private boolean onCooldown;
	
	public BaseItem(ItemStack item) {
		super(item);
	}
	public int getIntegerField(String field, int defaultValue) {
		if (!plugin.getConfig().contains(getConfigPath()+'.'+field))
			return defaultValue;
		try {
			return plugin.getConfig().getInt(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dinteger &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return defaultValue;
		}
	}
	public double getDoubleField(String field, double defaultValue) {
		if (!plugin.getConfig().contains(getConfigPath()+'.'+field))
			return defaultValue;
		try {
			return plugin.getConfig().getDouble(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &ddouble &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return defaultValue;
		}
	}
	public boolean getBooleanField(String field, boolean defaultValue) {
		if (!plugin.getConfig().contains(getConfigPath()+'.'+field))
			return defaultValue;
		try {
			return plugin.getConfig().getBoolean(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dboolean &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return defaultValue;
		}
	}
	public String getStringField(String field, String defaultValue) {
		if (!plugin.getConfig().contains(getConfigPath()+'.'+field))
			return defaultValue;
		try {
			return plugin.getConfig().getString(getConfigPath()+'.'+field);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dstring &evalue from config path '"+getConfigPath()+'.'+field+"' please fix this value!"));
			return defaultValue;
		}
	}
	
	public void activatedAbility(Ability ability, Event event, Entity activator, Entity target) {}
	
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
}

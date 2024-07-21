package com.github.jewishbanana.ultimatecontent.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.ultimatecontent.Main;

public class Weapon extends BaseItem {
	
	protected static NamespacedKey lightsaberParticleKey;
	static {
		lightsaberParticleKey = new NamespacedKey(Main.getInstance(), "lpk");
	}
	
	public Weapon(ItemStack item) {
		super(item);
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.WEAPONS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "weapons";
	}
}

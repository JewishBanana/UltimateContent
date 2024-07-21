package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class TritonsFang extends Weapon {
	
	public static String REGISTERED_KEY = "ui:tritons_fang";

	public TritonsFang(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.TRIDENT).assembleLore().setCustomModelData(100014).build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, TritonsFang.class);
	}
}

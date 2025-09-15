package com.github.jewishbanana.ultimatecontent.items.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class TritonsFang extends Weapon {
	
	public static final String REGISTERED_KEY = "uc:tritons_fang";

	public TritonsFang(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.TRIDENT).assembleLore().setCustomModelData(100014).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, TritonsFang.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}

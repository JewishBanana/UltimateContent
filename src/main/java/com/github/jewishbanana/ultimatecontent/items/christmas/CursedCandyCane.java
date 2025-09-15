package com.github.jewishbanana.ultimatecontent.items.christmas;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.items.Weapon;

public class CursedCandyCane extends Weapon {
	
	public static final String REGISTERED_KEY = "uc:cursed_candy_cane";
	
	public CursedCandyCane(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.DIAMOND_SWORD).assembleLore().setCustomModelData(100020).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, CursedCandyCane.class);
	}
	public ItemCategory getItemCategory() {
		return CustomItemCategories.CHRISTMAS_ITEMS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "christmas_items";
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}

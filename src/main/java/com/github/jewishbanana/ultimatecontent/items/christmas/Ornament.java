package com.github.jewishbanana.ultimatecontent.items.christmas;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;

public class Ornament extends CraftingMaterial {
	
	public static final String REGISTERED_KEY = "uc:ornament";
	
	public Ornament(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.GHAST_TEAR).assembleLore().setCustomModelData(100018).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, Ornament.class);
	}
	public ItemCategory getItemCategory() {
		return CustomItemCategories.CHRISTMAS_ITEMS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "christmas_items";
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}

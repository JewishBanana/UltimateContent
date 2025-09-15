package com.github.jewishbanana.ultimatecontent.items.easter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;

public class GreenEgg extends BaseItem {
	
	public static final String REGISTERED_KEY = "uc:green_egg";

	public GreenEgg(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.TURTLE_EGG).assembleLore().setCustomModelData(100023).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, GreenEgg.class);
	}
	public ItemCategory getItemCategory() {
		return CustomItemCategories.EASTER_ITEMS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "easter_items";
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}

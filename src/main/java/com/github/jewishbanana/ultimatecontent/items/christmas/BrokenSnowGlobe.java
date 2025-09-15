package com.github.jewishbanana.ultimatecontent.items.christmas;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;

public class BrokenSnowGlobe extends CraftingMaterial {

	public static final String REGISTERED_KEY = "uc:broken_snow_globe";
	
	public BrokenSnowGlobe(ItemStack item) {
		super(item);
	}
	public boolean placeBlock(BlockPlaceEvent event) {
		event.setCancelled(true);
		return false;
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), CustomHead.BROKEN_SNOW_GLOBE.getHead()).assembleLore().build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, BrokenSnowGlobe.class);
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

package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;

public class BoosterPack extends BaseItem {
	
	public static String REGISTERED_KEY = "ui:booster_pack";
	
	public BoosterPack(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.FEATHER).assembleLore().build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, BoosterPack.class);
	}
	public boolean shouldConsumeItem() {
		return true;
	}
}

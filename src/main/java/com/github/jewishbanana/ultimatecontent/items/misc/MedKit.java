package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class MedKit extends BaseItem {
	
	public static String REGISTERED_KEY = "ui:med_kit";
	
	public MedKit(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.SUGAR).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, MedKit.class);
	}
	public boolean shouldConsumeItem() {
		return true;
	}
}


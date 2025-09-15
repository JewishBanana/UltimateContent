package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class PropulsionCannon extends BaseItem {
	
	public static final String REGISTERED_KEY = "uc:propulsion_cannon";
	
	public PropulsionCannon(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.SNOWBALL).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, PropulsionCannon.class);
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}
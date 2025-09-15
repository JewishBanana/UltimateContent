package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class MatterSwap extends BaseItem {
	
	public static final String REGISTERED_KEY = "uc:matter_swap";
	
	public MatterSwap(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.GREEN_DYE).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, MatterSwap.class);
	}
	public boolean shouldConsumeItem() {
		return true;
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}


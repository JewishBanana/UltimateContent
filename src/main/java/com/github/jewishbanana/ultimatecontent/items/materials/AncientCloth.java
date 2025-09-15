package com.github.jewishbanana.ultimatecontent.items.materials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class AncientCloth extends CraftingMaterial {
	
	public static final String REGISTERED_KEY = "uc:ancient_cloth";

	public AncientCloth(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.PAPER).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().setCustomModelData(100008).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, AncientCloth.class);
	}
	public Rarity getRarity() {
		return Rarity.COMMON;
	}
}

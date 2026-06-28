package com.github.jewishbanana.ultimatecontent.items.materials;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

/**
 * A purely cosmetic crafting material ported from the legacy DeadlyDisasters "Blood Ingot". It currently has no
 * functionality of its own — it only carries a custom texture (via custom model data) and serves as an achievement
 * reward. Recipes/uses may be added later.
 */
public class BloodIngot extends CraftingMaterial {

	public static final String REGISTERED_KEY = "uc:blood_ingot";

	public BloodIngot(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.BRICK).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().setCustomModelData(100016).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, BloodIngot.class);
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}

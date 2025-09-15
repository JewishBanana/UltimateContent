package com.github.jewishbanana.ultimatecontent.items.easter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;

public class GoldenEgg extends CraftingMaterial {
	
	public static final String REGISTERED_KEY = "uc:golden_egg";

	public GoldenEgg(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.TURTLE_EGG).assembleLore().setCustomModelData(100028).build();
	}
	public static void register() {
		UIItemType type = UIItemType.registerItem(REGISTERED_KEY, GoldenEgg.class);
		
		type.registerRecipe(createAnvilIncrementEnchantRecipe(type, BunnyHop.REGISTERED_KEY, "golden_egg_anvil", new RecipeChoice.MaterialChoice(BunnyHop.applicableTypes),
				base -> 10));
	}
	public ItemCategory getItemCategory() {
		return CustomItemCategories.EASTER_ITEMS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "easter_items";
	}
	public Rarity getRarity() {
		return Rarity.LEGENDARY;
	}
}

package com.github.jewishbanana.ultimatecontent.items.easter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.items.CraftingMaterial;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class GoldenEgg extends CraftingMaterial {
	
	public static final String REGISTERED_KEY = "uc:golden_egg";

	public GoldenEgg(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.TURTLE_EGG).assembleLore().setCustomModelData(100028).build();
	}
	@SuppressWarnings("deprecation")
	public static void register() {
		UIItemType type = UIItemType.registerItem(REGISTERED_KEY, GoldenEgg.class);
		
		final boolean isCopperBootsAllowed = VersionUtils.isMCVersionOrAbove("1.21.9");
		type.registerRecipe(createAnvilIncrementEnchantRecipe(type, BunnyHop.REGISTERED_KEY, "golden_egg_anvil", new RecipeChoice.MaterialChoice(BunnyHop.applicableTypes),
				base -> 10,
				result -> {
					if (result == null)
						return result;
					int cmd = 0;
					switch (result.getType()) {
					case LEATHER_BOOTS -> cmd = 110028;
					case IRON_BOOTS -> cmd = 100029;
					case CHAINMAIL_BOOTS -> cmd = 100030;
					case GOLDEN_BOOTS -> cmd = 100031;
					case DIAMOND_BOOTS -> cmd = 100032;
					case NETHERITE_BOOTS -> cmd = 100033;
					default -> {}
					}
					if (isCopperBootsAllowed && result.getType() == Material.COPPER_BOOTS)
						cmd = 110029;
					if (cmd == 0)
						return result;
					ItemMeta meta = result.getItemMeta();
					meta.setCustomModelData(cmd);
					result.setItemMeta(meta);
					return result;
				}));
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

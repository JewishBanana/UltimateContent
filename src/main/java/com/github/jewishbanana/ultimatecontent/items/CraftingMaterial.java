package com.github.jewishbanana.ultimatecontent.items;

import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe.AnvilChoice;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe.AnvilResult;

public class CraftingMaterial extends BaseItem {

	public CraftingMaterial(ItemStack item) {
		super(item);
	}
	protected static AnvilRecipe createAnvilIncrementEnchantRecipe(UIItemType itemType, String enchant, String key, RecipeChoice ingredients, Function<GenericItem, Integer> costDecider) {
		UIEnchantment type = UIEnchantment.getEnchant(enchant);
		if (type == null)
			return null;
		AnvilChoice anvilChoice = new AnvilChoice(ingredients, new RecipeChoice.ExactChoice(itemType.getItem()));
		return new AnvilRecipe(new NamespacedKey(plugin, key), anvilChoice, inventory -> {
			ItemStack item = inventory.getFirstSlot().clone();
			int level = type.getEnchantLevel(item);
			if (level >= type.getMaxLevel() || item.getAmount() != inventory.getSecondSlot().getAmount() || !type.canBeEnchanted(item))
				return new AnvilResult(new ItemStack(Material.AIR));
//			ItemMeta meta = item.getItemMeta();
//			ItemListener.attachRecipeMetaFix(meta);
//			item.setItemMeta(meta);
			GenericItem base = GenericItem.createItemBase(item);
			if (type.addEnchant(base, level+1, true, true)) {
				base.getType().getBuilder().assembleLore(item, item.getItemMeta(), base.getType(), base);
				return new AnvilResult(item, costDecider.apply(base));
			}
			return new AnvilResult(new ItemStack(Material.AIR));
		});
	}
	public ItemCategory getItemCategory() {
		return ItemCategory.DefaultCategory.CRAFTING_INGREDIENTS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "materials";
	}
}

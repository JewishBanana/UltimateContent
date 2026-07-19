package com.github.jewishbanana.ultimatecontent.items.books;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe.AnvilChoice;
import com.github.jewishbanana.uiframework.utils.AnvilRecipe.AnvilResult;
import com.github.jewishbanana.ultimatecontent.UltimateContent;
import com.github.jewishbanana.ultimatecontent.enchants.AncientCurse;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.EnchantBook;

public class AncientCurseBook extends EnchantBook {
	
	public static final String REGISTERED_KEY = "uc:ancient_curse_book";

	public AncientCurseBook(ItemStack item) {
		super(item);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.ENCHANTED_BOOK).build();
	}
	public static void register() {
		UIItemType type = UIItemType.registerItem(REGISTERED_KEY, AncientCurseBook.class);
		UIEnchantment enchant = UIEnchantment.getEnchant(AncientCurse.REGISTERED_KEY);
		type.addEnchant(enchant, 1);
		GenericItem book = type.createNewInstance(type.getItem());
		book.refreshItemLore();
		AnvilChoice choice = new AnvilChoice(new RecipeChoice.MaterialChoice(AncientCurse.applicableTypes),
				new RecipeChoice.ExactChoice(book.getItem()));
		type.registerUsedRecipe(new AnvilRecipe(new NamespacedKey(UltimateContent.getInstance(), "ancient_curse_anvil_preview"), choice, inventory -> {
			ItemStack result = inventory.getFirstSlot().clone();
			GenericItem base = GenericItem.createItemBaseNoID(result);
			if (!enchant.addEnchant(base, 1, true, false))
				return new AnvilResult(new ItemStack(Material.AIR));
			base.refreshItemLore();
			return new AnvilResult(base.getItem(), enchant.getAnvilCost(result, inventory.getSecondSlot(), 1));
		}));
	}
	public String getDisplayName() {
		return displayHex+"Ancient Curse Book";
	}
}

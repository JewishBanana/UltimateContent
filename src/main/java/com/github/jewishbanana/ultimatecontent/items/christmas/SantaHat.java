package com.github.jewishbanana.ultimatecontent.items.christmas;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.Armor;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;

public class SantaHat extends Armor {
	
	public static final String REGISTERED_KEY = "uc:santa_hat";
	
	public int elfSummonCount;
	public int elfRespawnTimer;

	public SantaHat(ItemStack item) {
		super(item);
		this.elfSummonCount = getIntegerField("elf_summon_count", 3);
		this.elfRespawnTimer = (int) (getDoubleField("elf_respawn_timer", 60.0) * 20.0);
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.DIAMOND_HELMET).assembleLore().setCustomModelData(100021).build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, SantaHat.class);
	}
	public ItemCategory getItemCategory() {
		return CustomItemCategories.CHRISTMAS_ITEMS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "christmas_items";
	}
	public Rarity getRarity() {
		return Rarity.LEGENDARY;
	}
}

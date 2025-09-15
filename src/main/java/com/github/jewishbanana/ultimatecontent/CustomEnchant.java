package com.github.jewishbanana.ultimatecontent;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.mojang.datafixers.util.Pair;

public class CustomEnchant extends UIEnchantment {
	
	protected static final JavaPlugin plugin;
	static {
		plugin = Main.getInstance();
	}
	
	public enum EnchantRarity {
	    COMMON(1),
	    UNCOMMON(2),
	    RARE(3),
	    VERY_RARE(4),
	    EPIC(5),
	    LEGENDARY(6),
	    MYTHIC(7);
		
		private int cost;
	    
	    private EnchantRarity(int cost) {
	    	this.cost = cost;
	    }
	    public int getRarityCost() {
	    	return cost;
	    }
	}
	
	public Map<Integer, Queue<Pair<Ability, Queue<Ability.Action>>>> abilities = new HashMap<>();
	
	public void loadEnchant(GenericItem base) {
		if (base.getItem().getType() == Material.ENCHANTED_BOOK)
			return;
		int level = this.getEnchantLevel(base.getItem());
		if (abilities.containsKey(level))
			for (Pair<Ability, Queue<Ability.Action>> pair : abilities.get(level))
				base.addUniqueAbility(pair.getFirst(), pair.getSecond(), false);
	}
	public void unloadEnchant(GenericItem base) {
		if (base.getItem().getType() == Material.ENCHANTED_BOOK)
			return;
		int level = this.getEnchantLevel(base.getItem());
		if (abilities.containsKey(level))
			for (Pair<Ability, Queue<Ability.Action>> pair : abilities.get(level))
				base.removeUniqueAbility(pair.getFirst());
	}
	public EnchantRarity getRarity() {
		return EnchantRarity.UNCOMMON;
	}
	public int getAnvilCost(ItemStack item, ItemStack material, int level) {
		return level * getRarity().getRarityCost() * (material.getType() == Material.ENCHANTED_BOOK ? 1 : 2);
	}
}

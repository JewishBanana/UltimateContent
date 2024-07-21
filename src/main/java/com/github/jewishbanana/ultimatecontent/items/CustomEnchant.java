package com.github.jewishbanana.ultimatecontent.items;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Material;

import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.Main;
import com.mojang.datafixers.util.Pair;

public class CustomEnchant extends UIEnchantment {
	
	protected static Main plugin;
	static {
		plugin = Main.getInstance();
	}
	
	public Map<Integer, Queue<Pair<Ability, Queue<Ability.Action>>>> abilities = new HashMap<>();
	
	public CustomEnchant(String registeredName, int id) {
		super(registeredName, id);
	}
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
}

package com.github.jewishbanana.ultimatecontent.items.enchants;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.items.CustomEnchant;

public class AncientCurse extends CustomEnchant {
	
	public static String REGISTERED_KEY = "ui:ancient_curse";
	public static List<Material> applicableTypes = Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.ENCHANTED_BOOK);
	
	public AncientCurse(String registeredName, int id) {
		super(registeredName, id);
		this.setMaxLevel(3);
	}
	public static void register() {
		UIEnchantment enchant = UIEnchantment.registerEnchant(REGISTERED_KEY, AncientCurse.class);
		
		enchant.setApplicableTypes(applicableTypes);
	}
}

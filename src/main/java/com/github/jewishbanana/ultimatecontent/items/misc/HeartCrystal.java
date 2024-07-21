package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class HeartCrystal extends BaseItem {
	
	public static String REGISTERED_KEY = "ui:heart_crystal";
	
	public HeartCrystal(ItemStack item) {
		super(item);
	}
	public boolean interacted(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player p = event.getPlayer();
			if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() >= 40.0)
				return true;
			double amount = Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()+2.0, 40.0);
			p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(amount);
			p.setHealth(Math.min(p.getHealth()+2.0, p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
			Utils.playDamageEffect(p);
			p.playSound(p, Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 0.5f);
			if (item.getAmount() == 1)
				GenericItem.removeBaseItem(item);
			item.setAmount(item.getAmount()-1);
		}
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return ItemBuilder.create(getType(), Material.REDSTONE).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().build();
	}
	public static void register() {
		ItemType.registerItem(REGISTERED_KEY, HeartCrystal.class);
	}
}

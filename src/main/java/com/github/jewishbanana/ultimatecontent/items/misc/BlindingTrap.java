package com.github.jewishbanana.ultimatecontent.items.misc;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class BlindingTrap extends BaseItem {
	
	public static final String REGISTERED_KEY = "uc:blinding_trap";
	
	public BlindingTrap(ItemStack item) {
		super(item);
	}
	public void activatedAbility(Ability ability, Event event, Entity activator, Entity target) {
		if (target instanceof Player)
			target.sendMessage(Utils.convertString("&6"+(activator instanceof Player ? ((Player) activator).getDisplayName() : activator.getType().name())+" &7has used a "+this.getType().getBuilder().getItem().getItemMeta().getDisplayName()+" &7on you!"));
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), Material.INK_SAC).setHiddenEnchanted(VersionUtils.getUnbreaking()).assembleLore().build();
	}
	public static void register() {
		UIItemType.registerItem(REGISTERED_KEY, BlindingTrap.class);
	}
	public boolean shouldConsumeItem() {
		return true;
	}
	public Rarity getRarity() {
		return Rarity.RARE;
	}
}

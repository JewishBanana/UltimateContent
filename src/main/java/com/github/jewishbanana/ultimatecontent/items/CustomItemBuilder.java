package com.github.jewishbanana.ultimatecontent.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.jewishbanana.uiframework.UIFramework;
import com.github.jewishbanana.uiframework.items.Ability;
import com.github.jewishbanana.uiframework.items.Ability.Action;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemType;
import com.github.jewishbanana.uiframework.utils.UIFDataUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CustomItemBuilder extends ItemBuilder {
	
	public static CustomItemBuilder create(ItemType type, ItemStack item) {
		CustomItemBuilder builder = new CustomItemBuilder();
		builder.type = type;
		builder.item = item;
		builder.meta = builder.item.getItemMeta();
		if (type != null)
			builder.registerName(type.getDisplayName()).build();
		return builder;
	}
	@SuppressWarnings("deprecation")
	public ItemStack assembleLore(ItemStack tempItem, ItemMeta tempMeta, ItemType id, GenericItem base) {
		if (!id.doesUseLoreFormat())
			return tempItem;
		List<String> lore = new ArrayList<>(Arrays.asList(((BaseItem) base).getRarity().getLabel(), " "));
		boolean firstSpace = true;
		if (!id.getLore().isEmpty()) {
			firstSpace = true;
			for (String s : id.getLore())
				lore.add(s);
		}
		if (base != null && !base.getFields().isEmpty() && base.getFields().values().stream().anyMatch(e -> e.getLore() != null)) {
			if (firstSpace)
				lore.add(" ");
			else
				firstSpace = true;
			base.getFields().values().stream().filter(e -> e.getLore() != null).forEach(e -> lore.add(e.getLore()));
		}
		if (!id.getAbilities().isEmpty() || (base != null && !base.getUniqueAbilities().isEmpty())) {
			if (firstSpace)
				lore.add(" ");
			else
				firstSpace = true;
			Map<Ability, String> abilities = new LinkedHashMap<>();
			if (base != null && !base.getUniqueAbilities().isEmpty())
				base.getUniqueAbilities().forEach((k, v) -> {
					String actions = "";
					boolean passive = false;
					for (Action action : v)
						if (action == Ability.Action.UNBOUND)
							continue;
						else if (action.isPassive()) {
							if (!passive) {
								actions += action.getLangName()+' ';
								passive = true;
							}
						} else
							actions += action.getLangName()+' ';
					if (abilities.containsKey(k))
						abilities.replace(k, abilities.get(k)+actions);
					else
						abilities.put(k, actions);
				});
			if (!id.getAbilities().isEmpty())
				id.getAbilities().forEach((k, v) -> {
					String actions = "";
					boolean passive = false;
					for (Action action : v)
						if (action == Ability.Action.UNBOUND)
							continue;
						else if (action.isPassive()) {
							if (!passive) {
								actions += action.getLangName()+' ';
								passive = true;
							}
						} else
							actions += action.getLangName()+' ';
					if (abilities.containsKey(k))
						abilities.replace(k, abilities.get(k)+actions);
					else
						abilities.put(k, actions);
				});
			for (Entry<Ability, String> entry : abilities.entrySet())
				lore.add(Utils.convertString(entry.getValue()+entry.getKey().getDisplayName()+' '+entry.getKey().getDescription()));
		}
		boolean spacing = false;
		if (!tempMeta.getEnchants().isEmpty()) {
			tempMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			if (base == null ? !(enchanted && tempMeta.getEnchants().size() == 1) : (tempMeta.getEnchants().size() != (enchanted ? (base.getHiddenEnchant() != null ? 2 : 1) : base.getHiddenEnchant() != null ? 1 : 0))) {
				if (firstSpace)
					lore.add(" ");
				else
					firstSpace = true;
				spacing = true;
				tempMeta.getEnchants().forEach((k, v) -> {
					if (!(enchanted && k == enchantment) && !(base != null && k == base.getHiddenEnchant()))
						lore.add(Utils.convertString("&7"+com.github.jewishbanana.uiframework.utils.VersionUtils.getFormattedEnchantName(k)+(k.getMaxLevel() == 1 ? "" : ' '+Utils.getNumerical(v))));
				});
			}
		}
		if (base != null && !base.getEnchants().isEmpty()) {
			if (!spacing && firstSpace)
				lore.add(" ");
			else
				firstSpace = true;
			base.getEnchants().forEach(e -> {
				int level = e.getEnchantLevel(tempItem);
				lore.add(Utils.convertString(("&7"+e.getDisplayName().replace("%l%", ""+level).replace("%nl%", Utils.getNumerical(level)))));
			});
		}
		boolean attributeSpacing = false;
		if (id.getDamage() != 0.0 || id.getAttackSpeed() != 0.0) {
			if (firstSpace) {
				lore.add(" ");
				attributeSpacing = true;
			} else
				firstSpace = true;
			tempMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			lore.add(Utils.convertString(UIFramework.getLangString("attributes.main_hand_lore")));
			if (id.getDamage() == 0.0)
				id.setDamage(1.0);
			double damage = id.getDamage();
			if (tempMeta.hasEnchant(VersionUtils.getSharpness()))
				damage += 0.5 * (tempMeta.getEnchantLevel(VersionUtils.getSharpness()) - 1) + 1.0;
			lore.add(Utils.convertString(UIFramework.getLangString("attributes.attack_damage").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(damage))));
			if (id.getAttackSpeed() == 0.0)
				id.setAttackSpeed(1.0);
			lore.add(Utils.convertString(UIFramework.getLangString("attributes.attack_speed").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(id.getAttackSpeed()))));
			if (tempMeta.hasAttributeModifiers() && tempMeta.getAttributeModifiers().containsKey(Attribute.GENERIC_ATTACK_SPEED))
				tempMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
			tempMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.fromString("545ff361-b6e6-4531-9c4c-398ef5589a8a"), "generic.attackSpeed", id.getAttackSpeed()-4.01, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		}
		if (id.getProjectileDamage() != 0.0) {
			if (firstSpace && !attributeSpacing) {
				lore.add(" ");
				attributeSpacing = true;
			} else
				firstSpace = true;
			tempMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			lore.add(Utils.convertString(UIFramework.getLangString("attributes.thrown_projectile")));
			lore.add(Utils.convertString(UIFramework.getLangString("attributes.projectile_damage").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(id.getProjectileDamage()))));
		}
		if (id.getProjectileDamageMultiplier() != 1.0) {
			if (firstSpace && !attributeSpacing) {
				lore.add(" ");
				attributeSpacing = true;
			} else
				firstSpace = true;
			tempMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			lore.add(Utils.convertString(UIFramework.getLangString("attributes.shot_projectiles")));
			lore.add(Utils.convertString(UIFramework.getLangString("attributes.shot_multiplier").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(id.getProjectileDamageMultiplier()))));
		}
		if (!tempMeta.hasDisplayName() && id.getDisplayName() != null)
			tempMeta.setDisplayName(id.getDisplayName());
		// Debug Item ID's
//		if (base != null) lore.addAll(Arrays.asList(" ", UIFUtils.convertString("&8ID: &a["+base.getUniqueId()+"]")));
		tempMeta.setLore(Utils.chopLore(lore));
		tempItem.setItemMeta(tempMeta);
		return tempItem;
	}
}

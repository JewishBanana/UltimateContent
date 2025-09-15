package com.github.jewishbanana.ultimatecontent.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;
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
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.uiframework.utils.UIFDataUtils;
import com.github.jewishbanana.uiframework.utils.UIFUtils;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class CustomItemBuilder extends ItemBuilder {
	
	private static final UUID attributesUUID;
	private static String deprecatedString;
	static {
		attributesUUID = UUID.fromString("545ff361-b6e6-4531-9c4c-398ef5589a8a");
	}
	
	public static CustomItemBuilder create(UIItemType type, ItemStack item) {
		CustomItemBuilder builder = new CustomItemBuilder();
		builder.type = type;
		builder.item = item;
		builder.meta = builder.item.getItemMeta();
		if (type != null)
			builder.registerName(type.getDisplayName()).build();
		return builder;
	}
	public static CustomItemBuilder create(UIItemType type, Material material) {
		return create(type, new ItemStack(material));
	}
	@SuppressWarnings("deprecation")
	public ItemStack assembleLore(ItemStack tempItem, ItemMeta tempMeta, UIItemType id, GenericItem givenBase) {
		if (givenBase == null)
			return assembleLore(tempItem, tempMeta, id);
		if (!id.doesUseLoreFormat())
			return tempItem;
		BaseItem base = (BaseItem) givenBase;
		List<String> lore = new ArrayList<>();
		boolean firstSpace = false;
		if (base != null && base.getRarity() != Rarity.NONE) {
			lore.addAll(Arrays.asList(base.getRarity().getLabel()));
//			firstSpace = true;
		}
		if (!id.getLore().isEmpty()) {
			if (firstSpace)
				lore.add(" ");
			else
				firstSpace = true;
			for (String s : id.getLore())
				lore.add(s);
		}
		if (!base.getFields().isEmpty() && base.getFields().values().stream().anyMatch(e -> e.getLore() != null)) {
			if (firstSpace)
				lore.add(" ");
			else
				firstSpace = true;
			base.getFields().values().stream().filter(e -> e.getLore() != null).forEach(e -> lore.add(e.getLore()));
		}
		if (!id.getAbilities().isEmpty() || !base.getUniqueAbilities().isEmpty()) {
			if (firstSpace)
				lore.add(" ");
			else
				firstSpace = true;
			Map<Ability, String> abilities = new LinkedHashMap<>();
			if (!base.getUniqueAbilities().isEmpty())
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
				lore.add(UIFUtils.convertString(entry.getValue()+entry.getKey().getDisplayName()+' '+entry.getKey().getDescription()));
		}
		boolean spacing = false;
		if (!tempMeta.getEnchants().isEmpty()) {
			tempMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			if (tempMeta.getEnchants().size() != (enchanted ? (base.getHiddenEnchant() != null ? 2 : 1) : base.getHiddenEnchant() != null ? 1 : 0)) {
				if (firstSpace)
					lore.add(" ");
				else
					firstSpace = true;
				spacing = true;
				tempMeta.getEnchants().forEach((k, v) -> {
					if (!(enchanted && k == enchantment) && k != base.getHiddenEnchant())
						lore.add(UIFUtils.convertString("&7"+com.github.jewishbanana.uiframework.utils.VersionUtils.getFormattedEnchantName(k)+(k.getMaxLevel() == 1 ? "" : ' '+UIFUtils.getNumerical(v))));
				});
			}
		}
		Map<UIEnchantment, Integer> customEnchants = base.getEnchants();
		if (!customEnchants.isEmpty()) {
			if (!spacing && firstSpace)
				lore.add(" ");
			else
				firstSpace = true;
			customEnchants.forEach((enchant, level) -> {
				lore.add(UIFUtils.convertString(("&7"+enchant.getDisplayName().replace("%l%", ""+level).replace("%nl%", UIFUtils.getNumerical(level)))));
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
			lore.add(UIFUtils.convertString(UIFramework.getLangString("attributes.main_hand_lore")));
			if (id.getDamage() == 0.0)
				id.setDamage(1.0);
			double damage = id.getDamage();
			if (tempMeta.hasEnchant(VersionUtils.getSharpness()))
				damage += 0.5 * (tempMeta.getEnchantLevel(VersionUtils.getSharpness()) - 1) + 1.0;
			lore.add(UIFUtils.convertString(UIFramework.getLangString("attributes.attack_damage").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(damage))));
			if (id.getAttackSpeed() == 0.0)
				id.setAttackSpeed(1.0);
			lore.add(UIFUtils.convertString(UIFramework.getLangString("attributes.attack_speed").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(id.getAttackSpeed()))));
			if (tempMeta.hasAttributeModifiers() && tempMeta.getAttributeModifiers().containsKey(Attribute.GENERIC_ATTACK_SPEED))
				tempMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
			tempMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(attributesUUID, "generic.attackSpeed", id.getAttackSpeed()-4.01, Operation.ADD_NUMBER, EquipmentSlot.HAND));
		}
		if (id.getProjectileDamage() != 0.0) {
			if (firstSpace && !attributeSpacing) {
				lore.add(" ");
				attributeSpacing = true;
			} else
				firstSpace = true;
			tempMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			lore.add(UIFUtils.convertString(UIFramework.getLangString("attributes.thrown_projectile")));
			lore.add(UIFUtils.convertString(UIFramework.getLangString("attributes.projectile_damage").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(id.getProjectileDamage()))));
		}
		if (id.getProjectileDamageMultiplier() != 1.0) {
			if (firstSpace && !attributeSpacing) {
				lore.add(" ");
				attributeSpacing = true;
			} else
				firstSpace = true;
			tempMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			lore.add(UIFUtils.convertString(UIFramework.getLangString("attributes.shot_projectiles")));
			lore.add(UIFUtils.convertString(UIFramework.getLangString("attributes.shot_multiplier").replaceAll("%value%", UIFDataUtils.getDecimalFormatted(id.getProjectileDamageMultiplier()))));
		}
		if (!tempMeta.hasDisplayName() && id.getDisplayName() != null)
			tempMeta.setDisplayName(id.getDisplayName());
		// Debug Item ID's
		if (base != null) lore.addAll(Arrays.asList(" ", UIFUtils.convertString("&8ID: &a["+base.getUniqueId()+"]")));
		if (base.isDeprecated())
			lore.addAll(Arrays.asList(" ", deprecatedString));
		tempMeta.setLore(UIFUtils.chopLore(lore));
		tempItem.setItemMeta(tempMeta);
		return tempItem;
	}
	public static void reload(Main plugin) {
		deprecatedString = Utils.convertString(DataUtils.getConfigString("language.misc.deprecatedItem", "(hex:#e0bd5c)(This item is subject to changes/removal and should typically not be given to others!)"));
	}
}

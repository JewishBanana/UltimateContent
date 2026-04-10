package com.github.jewishbanana.ultimatecontent.enchants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.StoredField;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.CustomEnchant;
import com.github.jewishbanana.ultimatecontent.items.BaseItem;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class BunnyHop extends CustomEnchant {
	
	public static final String REGISTERED_KEY = "uc:bunny_hop";
	public static final List<Material> applicableTypes = new ArrayList<>(Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS));
	public static final NamespacedKey bunnyHopParticleKey = new NamespacedKey(plugin, "ui-bhpk");
	static {
		if (VersionUtils.isMCVersionOrAbove("1.21.9"))
			applicableTypes.add(Material.COPPER_BOOTS);
	}
	
	private StoredField<Byte> particleField;
	
	public BunnyHop() {
		this.setMaxLevel(3);
	}
	public void inventoryClick(InventoryClickEvent event, GenericItem base) {
		if (!base.hasSpecialLore(BaseItem.PARTICLE_LORE_IDENTIFIER)) {
			base.setSpecialLore(BaseItem.PARTICLE_LORE_IDENTIFIER, Utils.convertString(DataUtils.getConfigString("language.misc.all") + DataUtils.getConfigString("language.items.particleToggle")));
			particleField.setValue((byte) 0);
			base.refreshItemLore();
			return;
		}
		if (EntityUtils.isPlayerImmune((Player) event.getWhoClicked()))
			event.getWhoClicked().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.particleToggleError")));
		if (event.getClick() == ClickType.RIGHT) {
			byte value = particleField.getValue();
			if (value++ == 2)
				value = 0;
			base.setSpecialLore(BaseItem.PARTICLE_LORE_IDENTIFIER, Utils.convertString((value == 0 ? DataUtils.getConfigString("language.misc.all") : (value == 1 ? DataUtils.getConfigString("language.misc.others") : DataUtils.getConfigString("language.misc.none")))
					+ DataUtils.getConfigString("language.items.particleToggle")));
			particleField.setValue(value);
			base.refreshItemLore();
		}
	}
	public void loadEnchant(GenericItem base) {
		ItemStack item = base.getItem();
		if (item != null && item.hasItemMeta()) {
			byte value = item.getItemMeta().getPersistentDataContainer().getOrDefault(bunnyHopParticleKey, PersistentDataType.BYTE, (byte) 0);
			particleField = base.registerField(BaseItem.PARTICLE_LORE_IDENTIFIER, value);
		}
		super.loadEnchant(base);
	}
	public void unloadEnchant(GenericItem base) {
		super.unloadEnchant(base);
		ItemStack item = base.getItem();
		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			meta.getPersistentDataContainer().set(bunnyHopParticleKey, PersistentDataType.BYTE, particleField.getValue());
			item.setItemMeta(meta);
		}
	}
	public EnchantRarity getRarity() {
		return EnchantRarity.LEGENDARY;
	}
	public static void register() {
		UIEnchantment enchant = UIEnchantment.registerEnchant(REGISTERED_KEY, BunnyHop.class);
		
		enchant.setApplicableTypes(applicableTypes);
	}
}

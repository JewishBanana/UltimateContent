package com.github.jewishbanana.ultimatecontent.enchants;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemField;
import com.github.jewishbanana.uiframework.items.UIEnchantment;
import com.github.jewishbanana.ultimatecontent.CustomEnchant;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class BunnyHop extends CustomEnchant {
	
	public static final String REGISTERED_KEY = "uc:bunny_hop";
	public static final List<Material> applicableTypes = Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS);
	
	public static NamespacedKey bunnyHopParticleKey = new NamespacedKey(plugin, "ui-bhpk");
	
	public BunnyHop() {
		this.setMaxLevel(3);
	}
	@SuppressWarnings("unchecked")
	public void inventoryClick(InventoryClickEvent event, GenericItem base) {
		if (EntityUtils.isPlayerImmune((Player) event.getWhoClicked()))
			event.getWhoClicked().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.particleToggleError")));
		ItemField<Byte> field = (ItemField<Byte>) base.getField(bunnyHopParticleKey);
		if (event.getClick() == ClickType.RIGHT) {
			if (field.getSetting() == 2)
				field.setSetting((byte) 0, event.getCurrentItem());
			else
				field.setSetting((byte) (field.getSetting()+1), event.getCurrentItem());
			field.setLore(Utils.convertString((field.getSetting() == 0 ? DataUtils.getConfigString("language.misc.all") : (field.getSetting() == 1 ? DataUtils.getConfigString("language.misc.others") : DataUtils.getConfigString("language.misc.none")))
					+DataUtils.getConfigString("language.items.particleToggle")));
			base.refreshItemLore();
		}
	}
	public void loadEnchant(GenericItem base) {
		base.registerItemField(bunnyHopParticleKey, PersistentDataType.BYTE, (byte) 0);
		super.loadEnchant(base);
	}
	public EnchantRarity getRarity() {
		return EnchantRarity.LEGENDARY;
	}
	public static void register() {
		UIEnchantment enchant = UIEnchantment.registerEnchant(REGISTERED_KEY, BunnyHop.class);
		
		enchant.setApplicableTypes(applicableTypes);
	}
}

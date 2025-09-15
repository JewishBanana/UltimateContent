package com.github.jewishbanana.ultimatecontent.entities;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.random.RandomGenerator;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.SoundEffect;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class EntityVariant {
	
	private static JavaPlugin plugin = Main.getInstance();
	private static RandomGenerator random = Utils.random();
	
	public String configPath;
	
	public String displayName;
	public double health = -1;
	public double damage = -1;
	public double knockback = -1;
	public double movementSpeed = -1;
	public boolean nameVisible;
	public SoundEffect[] ambientSounds;
	public int ambientSoundFrequency = 4;
	public SoundEffect[] hurtSounds;
	public SoundEffect[] deathSounds;
	public double volume = 1.0;
	public CustomLoadout loadout = new CustomLoadout();
	
	public Queue<CustomDrop> drops = new ArrayDeque<>();
	public CustomLoadout defaultLoadout = new CustomLoadout();
	
	public EntityVariant(String path) {
		loadout.defaultLoad = defaultLoadout;
		overwriteFromPath(path);
	}
	public void overwriteFromPath(String path) {
		this.configPath = path;
		ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
		if (section == null) {
			Main.consoleSender.sendMessage(Utils.prefix + Utils.convertString("&cError while trying to find config path for entity variant &d'"+path.substring(0, path.length()-1)+"' &cthis entity type or variant will not function correctly until this config path is added and configured!"));
			return;
		}
		if (section.contains("name", true))
			displayName = Utils.convertString(DataUtils.getConfigString(path+".name"));
		if (section.contains("volume", true))
			volume = DataUtils.getConfigDouble(path+".volume", 1.0);
		if (section.contains("health", true))
			health = DataUtils.getConfigDouble(path+".health", -1.0);
		if (section.contains("damage", true))
			damage = DataUtils.getConfigDouble(path+".damage", -1.0);
		if (section.contains("knockback", true))
			knockback = DataUtils.getConfigDouble(path+".knockback", -1);
		if (section.contains("movementSpeed", true))
			movementSpeed = DataUtils.getConfigDouble(path+".movementSpeed", movementSpeed);
		if (section.contains("nameVisible", true))
			nameVisible = DataUtils.getConfigBoolean(path+".nameVisible", false);
		if (section.contains("drops", true)) {
			drops.clear();
			section.getStringList("drops").forEach((line) -> {
				String trimmed = line.replaceAll("\\s+", "");
				String[] values = trimmed.split("\\|");
				if (values.length < 1) {
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating a custom drop for entity &f"+displayName+" &cthe following line has a syntax error: &d'- "+line+"' &cplease look over the instructions in the config above the entities section to see how to properly set up drops. This drop will be omitted!"));
					return;
				}
				for (int i=0; i < values.length; i++)
					values[i] = values[i].strip();
				values[0] = values[0].replaceAll("\"", "");
				CustomDrop customDrop = new CustomDrop();
				UIItemType itemType = UIItemType.getItemType(values[0]);
				if (itemType == null) {
					Material material = Material.getMaterial(values[0].toUpperCase());
					if (material == null) {
						Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating a custom drop for entity &f"+displayName+" &cthe drop &b'"+values[0]+"' &cdoes not exist in line &d'- "+line+"'&c. This drop will be omitted!"));
						return;
					}
					customDrop.item = new ItemStack(material);
				} else
					customDrop.item = itemType.getItem();
				customDrop.min = 1;
				customDrop.max = 1;
				if (values.length > 1) {
					customDrop.min = 0;
					try {
						customDrop.chance = (float) Double.parseDouble(values[1]);
					} catch (NumberFormatException e) {
						Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating a custom drop for entity &f"+displayName+" &cthe following line has a syntax error: &d'- "+line+"' &cthe value &d'"+values[1]+"' &cmust be a decimal number! Please look over the instructions in the config above the entities section to see how to properly set up drops. This drop will be omitted!"));
						return;
					}
					if (values.length > 2) {
						String[] innerValues = values[2].split("-");
						if (innerValues.length != 2) {
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating a custom drop for entity &f"+displayName+" &cthe following line has a syntax error: &d'- "+line+"' &cplease look over the instructions in the config above the entities section to see how to properly set up drops. This drop will be omitted!"));
							return;
						}
						int tempMin = 0;
						try {
							tempMin = Integer.parseInt(innerValues[0]);
						} catch (NumberFormatException e) {
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating a custom drop for entity &f"+displayName+" &cthe following line has a syntax error: &d'- "+line+"' &cthe value &d'"+innerValues[0]+"' &cmust be an integer within the third section &d'"+values[2]+"'&c! Please look over the instructions in the config above the entities section to see how to properly set up drops. This drop will be omitted!"));
							return;
						}
						int tempMax = 0;
						try {
							tempMax = Integer.parseInt(innerValues[1]);
						} catch (NumberFormatException e) {
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating a custom drop for entity &f"+displayName+" &cthe following line has a syntax error: &d'- "+line+"' &cthe value &d'"+innerValues[1]+"' &cmust be an integer within the third section &d'"+values[2]+"'&c! Please look over the instructions in the config above the entities section to see how to properly set up drops. This drop will be omitted!"));
							return;
						}
						if (tempMin > tempMax) {
							Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating a custom drop for entity &f"+displayName+" &cthe following line has a syntax error: &d'- "+line+"' &cwithin the third section &d'"+values[2]+"'&c! The minimum value cannot be greater than the maximum value! This drop will be omitted!"));
							return;
						}
						customDrop.min = tempMin;
						customDrop.max = tempMax;
					}
				}
				drops.add(customDrop);
			});
		}
		if (section.contains("equipment", true))
			try {
				ConfigurationSection temp = section.getConfigurationSection("equipment");
				if (temp.contains("main_hand", true)) {
					LoadoutSlot slot = createLoadoutSlot(temp.getCurrentPath()+".main_hand", "main_hand", displayName);
					loadout.mainHand = slot != null ? slot : defaultLoadout.mainHand;
				}
				if (temp.contains("off_hand", true)) {
					LoadoutSlot slot = createLoadoutSlot(temp.getCurrentPath()+".off_hand", "off_hand", displayName);
					loadout.offHand = slot != null ? slot : defaultLoadout.offHand;
				}
				if (temp.contains("feet", true)) {
					LoadoutSlot slot = createLoadoutSlot(temp.getCurrentPath()+".feet", "feet", displayName);
					loadout.armor[0] = slot != null ? slot : defaultLoadout.armor[0];
				}
				if (temp.contains("legs", true)) {
					LoadoutSlot slot = createLoadoutSlot(temp.getCurrentPath()+".legs", "legs", displayName);
					loadout.armor[1] = slot != null ? slot : defaultLoadout.armor[1];
				}
				if (temp.contains("chest", true)) {
					LoadoutSlot slot = createLoadoutSlot(temp.getCurrentPath()+".chest", "chest", displayName);
					loadout.armor[2] = slot != null ? slot : defaultLoadout.armor[2];
				}
				if (temp.contains("head", true)) {
					LoadoutSlot slot = createLoadoutSlot(temp.getCurrentPath()+".head", "head", displayName);
					loadout.armor[3] = slot != null ? slot : defaultLoadout.armor[3];
				}
			} catch (Exception e) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in creating custom equipment loadout for entity &f"+displayName+" &cthe following section has a syntax error: &d'"+path+".equipment' &cplease look over the instructions in the config above the entities section to see how to properly set up custom equipment load outs. This loadout will be omitted!"));
				return;
			}
	}
	private static ItemStack createEquipmentItemStack(String item) {
		UIItemType itemType = UIItemType.getItemType(item);
		if (itemType != null)
			return itemType.getItem();
		Material material = Material.getMaterial(item.toUpperCase());
		return material == null ? null : new ItemStack(material);
	}
	private static LoadoutSlot createLoadoutSlot(String path, String slotName, String entityName) {
		LoadoutSlot slot = new LoadoutSlot();
		for (Map<?, ?> map : DataUtils.getConfigMapList(path)) {
			Object itemName = map.get("item");
			if (itemName == null)
				continue;
			ItemStack item = createEquipmentItemStack(String.valueOf(itemName));
			if (item == null) {
				Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in setting &e"+slotName+" &cfor entity &f"+entityName+" &cthe item &b'"+String.valueOf(itemName)+"' &cdoes not exist! The slot will ignore this item entry until it is fixed."));
				continue;
			}
			double dropRate = 0;
			if (map.containsKey("drop_rate"))
				try {
					dropRate = (Double) map.get("drop_rate");
				} catch (Exception e) {
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in setting &e"+slotName+" &cfor entity &f"+entityName+" &cthe items drop_rate &b'"+String.valueOf(map.get("drop_rate"))+"' &cis not a double value! The slot will ignore this item entry until it is fixed."));
					continue;
				}
			double chance = 0f;
			if (map.containsKey("chance"))
				try {
					chance = (Double) map.get("chance");
				} catch (Exception e) {
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in setting &e"+slotName+" &cfor entity &f"+entityName+" &cthe items chance &b'"+String.valueOf(map.get("chance"))+"' &cis not a double value! The slot will ignore this item entry until it is fixed."));
					continue;
				}
			boolean damaged = true;
			if (map.containsKey("damaged"))
				try {
					damaged = (Boolean) map.get("damaged");
				} catch (Exception e) {
					Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cError in setting &e"+slotName+" &cfor entity &f"+entityName+" &cthe items damaged value &b'"+String.valueOf(map.get("damaged"))+"' &cis not a boolean value! The slot will ignore this item entry until it is fixed."));
					continue;
				}
			if (chance > 0f)
				slot.items.add(new LoadoutSlotItem(item, (float) dropRate, (float) chance, damaged));
			else
				slot.defaults.add(new LoadoutSlotItem(item, (float) dropRate, (float) chance, damaged));
		}
		return slot.defaults.isEmpty() && slot.items.isEmpty() ? null : slot;
	}
	public void populateDropList(List<ItemStack> list) {
		if (drops.isEmpty())
			return;
		for (CustomDrop drop : drops) {
			ItemStack stack = drop.generateDrop();
			if (stack != null)
				list.add(stack);
		}
	}
	public void equipEntityWithLoadout(LivingEntity entity) {
		loadout.equipEntity(entity);
	}
	public LoadoutSlotItem getMainHandItem() {
		return loadout.mainHand != null ? loadout.mainHand.generateItem() : defaultLoadout.mainHand != null ? defaultLoadout.mainHand.generateItem() : null;
	}
	public LoadoutSlotItem getOffHandItem() {
		return loadout.offHand != null ? loadout.offHand.generateItem() : defaultLoadout.offHand != null ? defaultLoadout.offHand.generateItem() : null;
	}
	public LoadoutSlotItem getLoadoutArmor(LoadoutEquipmentSlot slot) {
		return loadout.armor[slot.slotIndex] != null ? loadout.armor[slot.slotIndex].generateItem() : defaultLoadout.armor[slot.slotIndex] != null ? defaultLoadout.armor[slot.slotIndex].generateItem() : null;
	}
	public static class CustomDrop {
		
		public ItemStack item;
		public float chance;
		public int min;
		public int max;
		
		public ItemStack generateDrop() {
			int amount = min;
			if (min != max) {
				for (int i=0; i < max-min; i++)
					if (random.nextFloat(100) < chance)
						amount++;
				if (amount == 0)
					return null;
			}
			ItemStack stack = item.clone();
			stack.setAmount(amount);
			return stack;
		}
	}
	public static enum LoadoutEquipmentSlot {
		
		FEET(0),
		LEGS(1),
		CHEST(2),
		HEAD(3),
		MAIN_HAND(4),
		OFF_HAND(5);
		
		public int slotIndex;
		
		private LoadoutEquipmentSlot(int slotIndex) {
			this.slotIndex = slotIndex;
		}
	}
	public static class CustomLoadout {
		
		public LoadoutSlot[] armor = new LoadoutSlot[4];
		public LoadoutSlot mainHand;
		public LoadoutSlot offHand;
		public CustomLoadout defaultLoad;
		
		private void equipEntity(LivingEntity entity) {
			EntityEquipment equipment = entity.getEquipment();
			LoadoutSlotItem slot = mainHand != null ? mainHand.generateItem() : defaultLoad.mainHand != null ? defaultLoad.mainHand.generateItem() : null;
			if (slot != null) {
				equipment.setItemInMainHand(slot.getItem(), true);
				equipment.setItemInMainHandDropChance(slot.dropRate);
			}
			slot = offHand != null ? offHand.generateItem() : defaultLoad.offHand != null ? defaultLoad.offHand.generateItem() : null;
			if (slot != null) {
				equipment.setItemInOffHand(slot.getItem(), true);
				equipment.setItemInOffHandDropChance(slot.dropRate);
			}
			slot = armor[0] != null ? armor[0].generateItem() : defaultLoad.armor[0] != null ? defaultLoad.armor[0].generateItem() : null;
			if (slot != null) {
				equipment.setBoots(slot.getItem(), true);
				equipment.setBootsDropChance(slot.dropRate);
			}
			slot = armor[1] != null ? armor[1].generateItem() : defaultLoad.armor[1] != null ? defaultLoad.armor[1].generateItem() : null;
			if (slot != null) {
				equipment.setLeggings(slot.getItem(), true);
				equipment.setLeggingsDropChance(slot.dropRate);
			}
			slot = armor[2] != null ? armor[2].generateItem() : defaultLoad.armor[2] != null ? defaultLoad.armor[2].generateItem() : null;
			if (slot != null) {
				equipment.setChestplate(slot.getItem(), true);
				equipment.setChestplateDropChance(slot.dropRate);
			}
			slot = armor[3] != null ? armor[3].generateItem() : defaultLoad.armor[3] != null ? defaultLoad.armor[3].generateItem() : null;
			if (slot != null) {
				equipment.setHelmet(slot.getItem(), true);
				equipment.setHelmetDropChance(slot.dropRate);
			}
		}
		private LoadoutSlot getOrCreateLoadoutSlot(LoadoutEquipmentSlot slot) {
			if (slot == LoadoutEquipmentSlot.MAIN_HAND) {
				if (mainHand == null)
					mainHand = new LoadoutSlot();
				return mainHand;
			}
			if (slot == LoadoutEquipmentSlot.OFF_HAND) {
				if (offHand == null)
					offHand = new LoadoutSlot();
				return offHand;
			}
			if (armor == null)
				armor = new LoadoutSlot[4];
			if (armor[slot.slotIndex] == null)
				armor[slot.slotIndex] = new LoadoutSlot();
			return armor[slot.slotIndex];
		}
		public void addEquipmentSlotDefaults(LoadoutEquipmentSlot slot, LoadoutSlotItem... items) {
			LoadoutSlot loadoutSlot = getOrCreateLoadoutSlot(slot);
			loadoutSlot.defaults.addAll(Arrays.asList(items));
		}
		public void addEquipmentSlotDefaults(LoadoutEquipmentSlot slot, ItemStack... items) {
			LoadoutSlot loadoutSlot = getOrCreateLoadoutSlot(slot);
			for (ItemStack item : items)
				loadoutSlot.defaults.add(new LoadoutSlotItem(item, 0, 0, true));
		}
		public void addEquipmentSlotItem(LoadoutEquipmentSlot slot, ItemStack item, float dropChance, float chance) {
			LoadoutSlot loadoutSlot = getOrCreateLoadoutSlot(slot);
			loadoutSlot.items.add(new LoadoutSlotItem(item, dropChance, chance, true));
		}
		public void addEquipmentSlotItem(LoadoutEquipmentSlot slot, ItemStack item, float chance) {
			addEquipmentSlotItem(slot, item, 0f, chance);
		}
	}
	public static class LoadoutSlot {
		
		private Set<LoadoutSlotItem> items = new HashSet<>();
		private List<LoadoutSlotItem> defaults = new ArrayList<>();
		
		public LoadoutSlot(LoadoutSlotItem... items) {
			defaults.addAll(Arrays.asList(items));
		}
		public LoadoutSlotItem generateItem() {
			if (!items.isEmpty()) {
				float roll = random.nextFloat(100f);
				float cumulative = 0;
				for (LoadoutSlotItem slot : items) {
					cumulative += slot.equipChance;
					if (roll < cumulative)
						return slot;
				}
			}
			return defaults.isEmpty() ? null : defaults.get(random.nextInt(defaults.size()));
		}
	}
	public static class LoadoutSlotItem {
		
		private ItemStack item;
		private float dropRate;
		private float equipChance;
		private boolean damaged;
		
		public LoadoutSlotItem(@NotNull ItemStack item, float dropRate, float equipChance, boolean damaged) {
			this.item = item;
			this.dropRate = dropRate / 100f;
			this.equipChance = equipChance;
			this.damaged = damaged && item.getType().getMaxDurability() > 1;
		}
		public ItemStack getItem() {
			if (!damaged)
				return item.clone();
			ItemStack temp = item.clone();
			Damageable meta = (Damageable) temp.getItemMeta();
			short limit = temp.getType().getMaxDurability();
			meta.setDamage(random.nextInt(limit > 2 ? limit / 2 : 0, limit - 1));
			temp.setItemMeta(meta);
			return temp;
		}
	}
}


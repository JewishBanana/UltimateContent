package com.github.jewishbanana.ultimatecontent.specialevents;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.uiframework.listeners.menus.InventoryHandler;
import com.github.jewishbanana.uiframework.listeners.menus.MenuManager;
import com.github.jewishbanana.uiframework.utils.UIFUtils;
import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.KillerChicken;
import com.github.jewishbanana.ultimatecontent.items.easter.BlueEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.GreenEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.OrangeEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.PurpleEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.RedEgg;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;
import com.mojang.datafixers.util.Pair;

public class EasterEvent extends SpecialEvent {
	
	public static boolean isEventActive;
	public static EasterEvent easterEvent;
	
	private int year;
	private Map<Class<?>, EggGoal> eggs = new LinkedHashMap<>();
	public Map<UUID, Pair<Class<? extends GenericItem>, Set<UUID>>> droppedEggs = new HashMap<>();
	public NamespacedKey eggKey;
	private Set<UUID> modifiedTrades = new HashSet<>();
	private double killerChickenSpawnRate;

	public EasterEvent(Main plugin) {
		super(plugin);
		isEventActive = true;
		easterEvent = this;
		this.eventMessage = Utils.convertString(DataUtils.getConfigString("language.events.easter.joinMessage"));
		this.year = LocalDate.now().getYear();
		this.eggKey = new NamespacedKey(plugin, "uc-easterEggDropKey");
		eggs.put(GreenEgg.class, new EggGoal(GreenEgg.class));
		eggs.put(BlueEgg.class, new EggGoal(BlueEgg.class));
		eggs.put(RedEgg.class, new EggGoal(RedEgg.class));
		eggs.put(OrangeEgg.class, new EggGoal(OrangeEgg.class));
		eggs.put(PurpleEgg.class, new EggGoal(PurpleEgg.class));
		
		Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(Item.class).forEach(item -> {
			if (!item.getPersistentDataContainer().has(eggKey, PersistentDataType.STRING))
				return;
			switch (item.getPersistentDataContainer().get(eggKey, PersistentDataType.STRING)) {
			case "greenEgg":
				droppedEggs.put(item.getUniqueId(), Pair.of(GreenEgg.class, new HashSet<>()));
				break;
			case "redEgg":
				droppedEggs.put(item.getUniqueId(), Pair.of(RedEgg.class, new HashSet<>()));
				break;
			default:
				break;
			}
		}));
	}
	public static EasterEvent checkIsActive(Main plugin) {
		if (!isWithinEasterSeason())
			return null;
		EasterEvent event = new EasterEvent(plugin);
		return event;
	}
	private static boolean isWithinEasterSeason() {
        Calendar now = Calendar.getInstance();
        Calendar easterSunday = getEasterSunday(now.get(Calendar.YEAR));

        Calendar threeWeeksBeforeEaster = (Calendar) easterSunday.clone();
        threeWeeksBeforeEaster.add(Calendar.DAY_OF_YEAR, -21);

        Calendar oneWeekAfterEaster = (Calendar) easterSunday.clone();
        oneWeekAfterEaster.add(Calendar.DAY_OF_YEAR, 7);

        return now.after(threeWeeksBeforeEaster) && now.before(oneWeekAfterEaster);
    }
    private static Calendar getEasterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        Calendar easter = Calendar.getInstance();
        easter.set(year, month - 1, day);
        return easter;
    }
    public void reload() {
    	super.reload();
    	killerChickenSpawnRate = UIEntityManager.getEntityType(KillerChicken.REGISTERED_KEY).getSpawnRate();
    }
    @EventHandler(ignoreCancelled = true)
	public void onChickenLayEgg(EntityDropItemEvent e) {
    	if (e.getEntityType() == EntityType.CHICKEN
    			&& e.getItemDrop().getItemStack().getType() == Material.EGG
    			&& random.nextDouble() * 100 < 5.0) {
    		e.getItemDrop().setItemStack(UIItemType.getItem(GreenEgg.class));
    		droppedEggs.put(e.getItemDrop().getUniqueId(), Pair.of(GreenEgg.class, new HashSet<>()));
    		e.getItemDrop().getPersistentDataContainer().set(eggKey, PersistentDataType.STRING, "greenEgg");
    	}
    }
	@EventHandler(ignoreCancelled = true)
	public void onItemPickup(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player player
				&& droppedEggs.containsKey(e.getItem().getUniqueId())) {
			Pair<Class<? extends GenericItem>, Set<UUID>> pair = droppedEggs.get(e.getItem().getUniqueId());
			if (eggs.get(pair.getFirst()).hasAcheived(player.getUniqueId())) {
				if (!pair.getSecond().contains(e.getEntity().getUniqueId())) {
					player.sendMessage(Utils.convertString(DataUtils.getConfigString("language.events.easter.restrictAction")));
					pair.getSecond().add(e.getEntity().getUniqueId());
				}
				e.setCancelled(true);
				return;
			}
			eggs.get(pair.getFirst()).setAcheived(player.getUniqueId());
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void onItemMerge(ItemMergeEvent e) {
		if (droppedEggs.containsKey(e.getEntity().getUniqueId()) || droppedEggs.containsKey(e.getTarget().getUniqueId()))
			e.setCancelled(true);
	}
    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
    	Player player = e.getPlayer();
    	if (e.getCaught() instanceof Item && !eggs.get(BlueEgg.class).hasAcheived(player.getUniqueId())) {
    		ItemStack item = player.getEquipment().getItem(e.getHand());
    		double chance = 3.0;
    		if (item != null)
    			chance += item.getEnchantmentLevel(VersionUtils.getLuckOfTheSea()) * 3.0;
    		if (random.nextDouble() * 100 < chance) {
    			if (player.getInventory().firstEmpty() == -1)
        			player.getWorld().dropItemNaturally(player.getLocation(), UIItemType.getItem(BlueEgg.class));
        		else
        			player.getInventory().addItem(UIItemType.getItem(BlueEgg.class));
        		eggs.get(BlueEgg.class).setAcheived(player.getUniqueId());
    		}
    	}
    }
    @EventHandler(ignoreCancelled = true)
    public void onLootGen(LootGenerateEvent e) {
    	if (e.getInventoryHolder() instanceof Chest chest
				&& chest.getBlock().getLocation().getBlockY() < 30
				&& e.getEntity() instanceof Player player
				&& !eggs.get(OrangeEgg.class).hasAcheived(player.getUniqueId())
				&& random.nextDouble() * 100 < 10.0) {
			e.getLoot().add(UIItemType.getItem(OrangeEgg.class));
			eggs.get(OrangeEgg.class).setAcheived(player.getUniqueId());
		}
	}
    @EventHandler(ignoreCancelled = true)
    public void onTradeAquire(VillagerAcquireTradeEvent e) {
    	if (e.getEntity() instanceof WanderingTrader
    			&& !modifiedTrades.contains(e.getEntity().getUniqueId())
    			&& random.nextDouble() * 100 < 20.0) {
    		MerchantRecipe newRecipe = new MerchantRecipe(UIItemType.getItem(PurpleEgg.class), 1);
    		newRecipe.addIngredient(new ItemStack(Material.EMERALD, random.nextInt(10)+6));
    		newRecipe.setVillagerExperience(20);
    		e.setRecipe(newRecipe);
    		modifiedTrades.add(e.getEntity().getUniqueId());
    	}
    }
    @EventHandler(ignoreCancelled = true)
	public void onClick(InventoryClickEvent e) {
		if (!(e.getClickedInventory() instanceof MerchantInventory))
			return;
		GenericItem base = GenericItem.getItemBase(e.getCurrentItem());
		if (base != null && base.getClass().equals(PurpleEgg.class)) {
			if (eggs.get(PurpleEgg.class).hasAcheived(e.getWhoClicked().getUniqueId())) {
				e.getWhoClicked().sendMessage(Utils.convertString(DataUtils.getConfigString("language.events.easter.restrictAction")));
				e.setCancelled(true);
				return;
			}
			eggs.get(PurpleEgg.class).setAcheived(e.getWhoClicked().getUniqueId());
		}
	}
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent e) {
    	if (e.getEntityType() == EntityType.CHICKEN
    			&& random.nextDouble() * 100.0 < killerChickenSpawnRate
    			&& UIEntityManager.getEntity(e.getEntity()) == null) {
    		e.setCancelled(true);
    		UIEntityManager.spawnEntity(e.getLocation(), KillerChicken.class);
    	}
    }
    @EventHandler
    public void onEntityLoad(EntitiesLoadEvent event) {
    	event.getEntities().stream().filter(entity -> entity.getType() == EntityType.ITEM).forEach(item -> {
    		if (!item.getPersistentDataContainer().has(eggKey, PersistentDataType.STRING))
				return;
			switch (item.getPersistentDataContainer().get(eggKey, PersistentDataType.STRING)) {
			case "greenEgg":
				droppedEggs.put(item.getUniqueId(), Pair.of(GreenEgg.class, new HashSet<>()));
				break;
			case "redEgg":
				droppedEggs.put(item.getUniqueId(), Pair.of(RedEgg.class, new HashSet<>()));
				break;
			default:
				break;
			}
    	});
    }
    public class EggGoal {
    	
    	private Set<UUID> acheived = new HashSet<>();
    	private ItemStack item;
    	private String progressPath;
    	
    	public EggGoal(Class<? extends GenericItem> eggClass) {
    		item = UIItemType.getItem(eggClass);
    		ItemMeta meta = item.getItemMeta();
    		List<String> lore = meta.getLore();
    		lore.add(" ");
    		String infoPath = eggClass.getSimpleName();
    		infoPath = Character.toLowerCase(infoPath.charAt(0)) + infoPath.substring(1);
    		lore.add(Utils.convertString(DataUtils.getConfigString("language.events.easter."+infoPath+"Info")));
    		meta.setLore(UIFUtils.chopLore(lore));
    		item.setItemMeta(meta);
    		
    		this.progressPath = "easter_event.progress."+year+'.'+infoPath;
    		for (String s : DataUtils.getDataFileStringList(progressPath))
    			acheived.add(UUID.fromString(s));
    	}
    	public boolean hasAcheived(UUID player) {
    		return acheived.contains(player);
    	}
    	public void setAcheived(UUID player) {
    		acheived.add(player);
    		if (notifyMessages)
    			Bukkit.getPlayer(player).sendMessage(Utils.convertString(DataUtils.getConfigString("language.events.easter.collectedEgg").replace("%egg%", item.getItemMeta().getDisplayName())));
    	}
    	public void saveData() {
    		DataUtils.getDataFile().set(progressPath, acheived.stream().map(e -> e.toString()).collect(Collectors.toList()));
    	}
    }
    public void openGUI(Player player) {
		EasterEventMenu menu = new EasterEventMenu(player.getUniqueId());
		MenuManager.registerInventory(menu.getInventory(), menu);
		player.openInventory(menu.getInventory());
	}
    public void saveData() {
    	eggs.values().forEach(egg -> egg.saveData());
	}
    public class EasterEventMenu extends InventoryHandler {

    	public EasterEventMenu(UUID player) {
    		this.inventory = createInventory();
    		this.decorate(player);
    	}
    	public void decorate(UUID player) {
    		ItemStack glass = ItemBuilder.create(Material.LIME_STAINED_GLASS_PANE).registerName(" ").build().getItem();
    		for (int i=0; i < 27; i++)
    			this.getInventory().setItem(i, glass);
    		this.getInventory().setItem(4, ItemBuilder.create(Material.NETHER_STAR)
    				.registerName(Utils.convertString(DataUtils.getConfigString("language.events.easter.menuHeader")))
    				.setLoreList(Arrays.asList(Utils.convertString(DataUtils.getConfigString("language.events.easter.eventInfo"))))
    				.build().getItem());
    		int slot = 11;
    		for (EggGoal egg : eggs.values()) {
    			ItemStack item = egg.item.clone();
    			ItemMeta meta = item.getItemMeta();
    			meta.setDisplayName(Utils.convertString(meta.getDisplayName() + " &f(" + (egg.hasAcheived(player) ? 1 : 0) + "/1)"));
    			item.setItemMeta(meta);
    			this.getInventory().setItem(slot, item);
    			slot++;
    		}
    		super.decorate();
    	}
    	public void onClick(InventoryClickEvent event) {
    		int slot = event.getRawSlot();
    		if (slot > 26 && !(event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT))
    			return;
    		event.setCancelled(true);
    		InventoryButton button = buttons.get(slot);
    		if (button != null) {
    			button.getFunction().accept(event);
    			return;
    		}
    	}
		@Override
		public Inventory createInventory() {
			return Bukkit.createInventory(null, 27, Utils.convertString(DataUtils.getConfigString("language.events.easter.menuHeader")));
		}
    }
}

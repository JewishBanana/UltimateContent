package com.github.jewishbanana.ultimatecontent.utils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;

import com.github.jewishbanana.uiframework.entities.CustomEntity;
import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.UltimateContent;

public class DependencyUtils {
	
	// Reflective bridge to DeadlyDisasters PRO's achievements API. UltimateContent never imports or compiles against
	// DeadlyDisasters (it is an optional dependency that may be absent or the non-PRO build), so the handler and award
	// methods are resolved lazily via reflection on first use. State: 0 = unchecked, 1 = available, 2 = unavailable.
	private static byte ddAchievementsState;
	private static java.lang.reflect.Method ddGetHandlerMethod;
	private static java.lang.reflect.Method ddAwardProgressMethod;
	private static java.lang.reflect.Method ddAwardProgressTierMethod;
	private static java.lang.reflect.Method ddSetTierProgressMethod;
	private static byte ddSelectorState;
	private static java.lang.reflect.Method ddGetSecondsMethod;

	private static volatile Predicate<Location> regionCheck;
	private static boolean worldGuardEnabled;

	// This plugin loads at STARTUP (required so the WorldGuard flags register in time via onLoad), but most protection
	// plugins load at POSTWORLD and are therefore not yet enabled when init() runs. Hooks for plugins that are installed but
	// not yet enabled are parked here and executed by LateHookListener the moment their plugin's onEnable completes.
	private static final Map<String, Consumer<UltimateContent>> pendingHooks = new LinkedHashMap<>();
	private static LateHookListener lateHookListener;
	
	private static boolean affectEntities;
	private static boolean damageBlocks;
	
	private static Set<Material> blacklistedMaterials;
	private static Set<EntityType> blacklistedEntities;
	private static Set<Class<? extends CustomEntity<?>>> blacklistedCustomEntities;

	public static void registerWorldGuardFlags(UltimateContent plugin) {
		if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") == null)
			return;
		try {
			WorldGuardHook.registerFlags();
			plugin.getLogger().info("Successfully registered UltimateContent WorldGuard flags");
		} catch (Throwable e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cFailed to register UltimateContent World Guard flags!");
		}
	}
	public static void init(UltimateContent plugin) {
		PluginManager pm = plugin.getServer().getPluginManager();
		// DeadlyDisasters achievement integration is resolved lazily on first award (see awardAchievementProgress), so it is
		// unaffected by plugin load order and needs no setup here.
		regionCheck = null;
		pendingHooks.clear();
		if (lateHookListener != null) {
			HandlerList.unregisterAll(lateHookListener);
			lateHookListener = null;
		}
		worldGuardEnabled = false;
		// WorldGuard is itself a STARTUP plugin (and the reason this plugin loads at STARTUP - the flags register in onLoad),
		// so it is always hooked right here.
		try {
			if (pm.getPlugin("WorldGuard") != null) {
				if (DataUtils.getConfigBoolean("external.region_protection_plugins.world_guard")) {
					worldGuardEnabled = true;
					plugin.getLogger().info("Successfully hooked into World Guard");
				} else
					plugin.getLogger().info("World Guard was detected, but region protection for this plugin is disabled in the config.yml file. World Guard regions will NOT be protected!");
			}
		} catch (Throwable e) {
			worldGuardEnabled = false;
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eWorld Guard &cregions from this plugin will NOT be protected!");
		}

		setupHook(plugin, "Towny", DependencyUtils::hookTowny);
		setupHook(plugin, "GriefPrevention", DependencyUtils::hookGriefPrevention);
		setupHook(plugin, "Lands", DependencyUtils::hookLands);
		setupHook(plugin, "Kingdoms", DependencyUtils::hookKingdoms);
		setupHook(plugin, "FieldZone", DependencyUtils::hookFieldZone);
		setupHook(plugin, "PlotSquared", DependencyUtils::hookPlotSquared);
		setupHook(plugin, "UltimateClans", DependencyUtils::hookUltimateClans);
		setupHook(plugin, "Factions", DependencyUtils::hookFactions);

		if (!pendingHooks.isEmpty()) {
			lateHookListener = new LateHookListener(plugin);
			pm.registerEvents(lateHookListener, plugin);
		}
	}
	/**
	 * Runs {@code hook} immediately when {@code pluginName} is already enabled, or parks it to run the moment that plugin
	 * enables (POSTWORLD plugins enable after this STARTUP plugin). Plugins that are not installed at all are skipped.
	 */
	private static void setupHook(UltimateContent plugin, String pluginName, Consumer<UltimateContent> hook) {
		PluginManager pm = plugin.getServer().getPluginManager();
		if (pm.isPluginEnabled(pluginName))
			hook.accept(plugin);
		else if (pm.getPlugin(pluginName) != null)
			pendingHooks.put(pluginName, hook);
	}
	private static final class LateHookListener implements Listener {
		private final UltimateContent plugin;

		private LateHookListener(UltimateContent plugin) {
			this.plugin = plugin;
		}
		@EventHandler
		public void onPluginEnable(PluginEnableEvent event) {
			Consumer<UltimateContent> hook = pendingHooks.remove(event.getPlugin().getName());
			if (hook == null)
				return;
			hook.accept(plugin);
			if (pendingHooks.isEmpty()) {
				HandlerList.unregisterAll(this);
				if (lateHookListener == this)
					lateHookListener = null;
			}
		}
	}
	/** Appends a protection plugin's predicate to the live region check - a location is protected when ANY hooked plugin claims it. */
	private static synchronized void addRegionPredicate(Predicate<Location> addition) {
		regionCheck = regionCheck == null ? addition : regionCheck.or(addition);
	}
	private static void hookTowny(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.towny")) {
				com.palmergames.bukkit.towny.TownyAPI townyHook = com.palmergames.bukkit.towny.TownyAPI.getInstance();
				addRegionPredicate(loc -> townyHook.getTownBlock(loc) != null);
				plugin.getLogger().info("Successfully hooked into Towny");
			} else
				plugin.getLogger().info("Towny was detected, but region protection for this plugin is disabled in the config.yml file. Towny regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eTowny &cregions from this plugin will NOT be protected!");
		}
	}
	private static void hookGriefPrevention(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.grief_prevention")) {
				me.ryanhamshire.GriefPrevention.DataStore api = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
				addRegionPredicate(loc -> api.getClaimAt(loc, true, null) != null);
				plugin.getLogger().info("Successfully hooked into Grief Prevention");
			} else
				plugin.getLogger().info("Grief Prevention was detected, but region protection for this plugin is disabled in the config.yml file. Grief Prevention regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eGrief Prevention &cregions from this plugin will NOT be protected!");
		}
	}
	private static void hookLands(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.lands")) {
				me.angeschossen.lands.api.LandsIntegration api = me.angeschossen.lands.api.LandsIntegration.of(plugin);
				addRegionPredicate(loc -> api.getArea(loc) != null);
				plugin.getLogger().info("Successfully hooked into Lands");
			} else
				plugin.getLogger().info("Lands was detected, but region protection for this plugin is disabled in the config.yml file. Lands regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eLands &cregions from this plugin will NOT be protected!");
		}
	}
	private static void hookKingdoms(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.kingdoms")) {
				addRegionPredicate(loc -> org.kingdoms.constants.land.Land.getLand(loc) != null);
				plugin.getLogger().info("Successfully hooked into Kingdoms");
			} else
				plugin.getLogger().info("Kingdoms was detected, but region protection for this plugin is disabled in the config.yml file. Kingdoms regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eKingdoms &cregions from this plugin will NOT be protected!");
		}
	}
	private static void hookFieldZone(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.field_zone")) {
				kr.rtustudio.fieldzone.region.RegionFlag flag = kr.rtustudio.fieldzone.region.RegionFlag.create(plugin, "ultimatecontent");
				kr.rtustudio.fieldzone.FieldZoneAPI.registerFlag(flag);
				addRegionPredicate(loc -> kr.rtustudio.fieldzone.FieldZoneAPI.hasFlag(loc, flag) == kr.rtustudio.fieldzone.region.FlagState.FALSE);
				plugin.getLogger().info("Successfully hooked into FieldZone");
			} else
				plugin.getLogger().info("FieldZone was detected, but region protection for this plugin is disabled in the main config.yml file. FieldZone regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eFieldZone &cregions from this plugin will NOT be protected!");
		}
	}
	private static void hookPlotSquared(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.plot_squared")) {
				addRegionPredicate(loc -> com.plotsquared.core.plot.Plot.getPlot(com.plotsquared.bukkit.util.BukkitUtil.adapt(loc)) != null);
				plugin.getLogger().info("Successfully hooked into PlotSquared");
			} else
				plugin.getLogger().info("PlotSquared was detected, but region protection for this plugin is disabled in the main config.yml file. PlotSquared regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &ePlotSquared &cregions from this plugin will NOT be protected!");
		}
	}
	private static void hookUltimateClans(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.ultimate_clans")) {
				me.ulrich.clans.interfaces.UClans api = (me.ulrich.clans.interfaces.UClans) Bukkit.getPluginManager().getPlugin("UltimateClans");
				Optional<me.ulrich.clans.interfaces.ClaimImplement> impl = api.getClaimAPI().getPreferentialOrFirstImplement();
				if(impl.isPresent()) {
					me.ulrich.clans.interfaces.ClaimImplement claimImpl = impl.get();
					addRegionPredicate(loc -> claimImpl.hasClaimLocation(loc));
					plugin.getLogger().info("Successfully hooked into UltimateClans");
				} else
					plugin.getLogger().info("UltimateClans was detected, but an implementation could not be found. UltimateClans regions will NOT be protected!");
			} else
				plugin.getLogger().info("UltimateClans was detected, but region protection for this plugin is disabled in the main config.yml file. UltimateClans regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eUltimateClans &cregions from this plugin will NOT be protected!");
		}
	}
	private static void hookFactions(UltimateContent plugin) {
		try {
			if (DataUtils.getConfigBoolean("external.region_protection_plugins.factions_uuid")) {
				dev.kitteh.factions.Factions factions = dev.kitteh.factions.Factions.factions();
				addRegionPredicate(loc -> !factions.getAt(loc).isWilderness());
				plugin.getLogger().info("Successfully hooked into FactionsUUID");
			} else
				plugin.getLogger().info("FactionsUUID was detected, but region protection for this plugin is disabled in the main config.yml file. FactionsUUID regions will NOT be protected!");
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			Utils.sendConsoleMessage("&cAn error has occurred while trying to hook into &eFactionsUUID &cregions from this plugin will NOT be protected!");
		}
	}
	public static void reload() {
		affectEntities = DataUtils.getConfigBoolean("general.protected_regions.affect_entities");
		damageBlocks = DataUtils.getConfigBoolean("general.protected_regions.damage_blocks");
		
		blacklistedMaterials = new HashSet<>();
		for (String s : DataUtils.getConfigStringList("general.blacklist.blocks")) {
			Material material = Material.matchMaterial(s);
			if (material == null) {
				UltimateContent.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding material type &d'"+s+"' &cto global blacklist in config section &bgeneral.blacklist.blocks &cplease fix this value to match the minecraft name. This material type will be omitted from the global blocks blacklist!"));
				continue;
			}
			blacklistedMaterials.add(material);
		}
		blacklistedEntities = new HashSet<>();
		blacklistedCustomEntities = new HashSet<>();
		for (String s : DataUtils.getConfigStringList("general.blacklist.entities")) {
			try {
				UIEntityManager customType = UIEntityManager.getEntityType(s);
				if (customType != null) {
					blacklistedCustomEntities.add(customType.getEntityClass());
					continue;
				}
				EntityType type = EntityType.valueOf(s.toUpperCase());
				blacklistedEntities.add(type);
			} catch (IllegalArgumentException e) {
				UltimateContent.consoleSender.sendMessage(Utils.prefix+Utils.convertString("&cError in adding entity type &d'"+s+"' &cto global blacklist in config section &bgeneral.blacklist.entities &cplease fix this value to match the minecraft name or custom entity type name. This entity type will be omitted from the global entities blacklist!"));
			}
		}
	}
	/**
	 * Awards DeadlyDisasters achievement progress through a reflective bridge, when DeadlyDisasters PRO (with the
	 * achievements feature) is installed. Does nothing when DeadlyDisasters is absent or is the non-PRO build, so
	 * UltimateContent stays fully functional on its own. {@code tier < 0} awards every currently-unlocked tier of the
	 * series; {@code tier >= 0} targets that specific tier index.
	 */
	public static void awardAchievementProgress(UUID uuid, String achievement, int amount, int tier) {
		if (ddAchievementsState == 2)
			return;
		if (ddAchievementsState == 0 && !resolveDeadlyDisastersAchievements())
			return;
		try {
			Object handler = ddGetHandlerMethod.invoke(null);
			if (handler == null)
				return;
			if (tier < 0)
				ddAwardProgressMethod.invoke(handler, uuid, achievement, amount);
			else
				ddAwardProgressTierMethod.invoke(handler, uuid, achievement, amount, tier);
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
		}
	}
	/**
	 * Seconds until the next disaster is rolled for {@code player} in their current world via DeadlyDisasters' reflective
	 * bridge, or {@code -1} when DeadlyDisasters is absent, disasters are disabled there, or no timer is running. Used by
	 * the baby end totem to warn its owner as a disaster approaches.
	 */
	public static int getSecondsUntilDisaster(org.bukkit.entity.Player player) {
		if (ddSelectorState == 2)
			return -1;
		if (ddSelectorState == 0 && !resolveDeadlyDisastersSelector())
			return -1;
		try {
			return (int) ddGetSecondsMethod.invoke(null, player);
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
			return -1;
		}
	}
	/**
	 * Resolves the DeadlyDisasters disaster-timer API by reflection. The PRO build's main class is {@code DeadlyDisasters}
	 * while the free build's is {@code Main}, so both are tried. Caches the result so it is attempted only once.
	 */
	private static boolean resolveDeadlyDisastersSelector() {
		for (String mainClassName : new String[] { "com.github.jewishbanana.deadlydisasters.DeadlyDisasters", "com.github.jewishbanana.deadlydisasters.Main" })
			try {
				Class<?> mainClass = Class.forName(mainClassName);
				ddGetSecondsMethod = mainClass.getMethod("getSecondsUntilDisaster", org.bukkit.entity.Player.class);
				ddSelectorState = 1;
				return true;
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				// Try the next candidate class name.
			}
		ddSelectorState = 2;
		return false;
	}
	/** Resolves the DeadlyDisasters PRO achievements API by reflection. Caches the result so it is attempted only once. */
	private static boolean resolveDeadlyDisastersAchievements() {
		try {
			Class<?> mainClass = Class.forName("com.github.jewishbanana.deadlydisasters.DeadlyDisasters");
			Class<?> handlerClass = Class.forName("com.github.jewishbanana.deadlydisasters.achievements.AchievementsHandler");
			ddGetHandlerMethod = mainClass.getMethod("getAchievementsHandler");
			ddAwardProgressMethod = handlerClass.getMethod("awardProgress", UUID.class, String.class, int.class);
			ddAwardProgressTierMethod = handlerClass.getMethod("awardProgress", UUID.class, String.class, int.class, int.class);
			ddSetTierProgressMethod = handlerClass.getMethod("setTierProgress", UUID.class, String.class, int.class, int.class);
			ddAchievementsState = 1;
			return true;
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			// DeadlyDisasters is absent or is the non-PRO build without the achievements API; disable the bridge.
			ddAchievementsState = 2;
			return false;
		}
	}
	/**
	 * Sets the progress of a specific achievement tier to an absolute value without triggering the achievement
	 * announcement. Used to sync intermediate display progress from external trackers. Never modifies a tier that is
	 * already achieved. Does nothing when DeadlyDisasters is absent or achievements are disabled.
	 */
	public static void setAchievementTierProgress(UUID uuid, String achievement, int tierIndex, int value) {
		if (ddAchievementsState == 2)
			return;
		if (ddAchievementsState == 0 && !resolveDeadlyDisastersAchievements())
			return;
		try {
			Object handler = ddGetHandlerMethod.invoke(null);
			if (handler == null)
				return;
			ddSetTierProgressMethod.invoke(handler, uuid, achievement, tierIndex, value);
		} catch (Exception e) {
			Utils.sendExceptionLog(e);
		}
	}
	private static boolean isWGRegion(Location location) {
		return worldGuardEnabled && WorldGuardHook.isRegion(location);
	}
	public static boolean canActivateAbilities(Location location) {
		return !worldGuardEnabled || WorldGuardHook.canActivateAbilities(location);
	}
	public static boolean canActivateAbilities(Entity entity) {
		return entity == null || canActivateAbilities(entity.getLocation());
	}
	public static boolean canSpawnCustomMobs(Location location) {
		return !worldGuardEnabled || WorldGuardHook.canSpawnCustomMobs(location);
	}
	private static boolean canDamageAbilityBlocks(Location location) {
		return !worldGuardEnabled || WorldGuardHook.canDamageAbilityBlocks(location);
	}
	public static boolean isLocationProtected(Location loc) {
		Predicate<Location> check = regionCheck;
		return isWGRegion(loc) || (check != null && check.test(loc));
	}
	public static boolean isEntityProtected(Entity entity) {
		CustomEntity<?> custom = UIEntityManager.getEntity(entity);
		if (custom != null && blacklistedCustomEntities.contains(custom.getClass()))
			return true;
		return blacklistedEntities.contains(entity.getType()) || (!affectEntities && isLocationProtected(entity.getLocation()));
	}
	public static boolean isBlockProtected(Block block) {
		Predicate<Location> check = regionCheck;
		return blacklistedMaterials.contains(block.getType()) || !canDamageAbilityBlocks(block.getLocation()) || (!damageBlocks && check != null && check.test(block.getLocation()));
	}
	private static final class WorldGuardHook {
		private static com.sk89q.worldguard.protection.regions.RegionQuery query;
		private static com.sk89q.worldguard.protection.flags.StateFlag activateAbilitiesFlag;
		private static com.sk89q.worldguard.protection.flags.StateFlag abilityBlockDamageFlag;
		private static com.sk89q.worldguard.protection.flags.StateFlag customMobSpawningFlag;

		private static void registerFlags() {
			com.sk89q.worldguard.protection.flags.registry.FlagRegistry registry = com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry();
			activateAbilitiesFlag = registerStateFlag(registry, "activate-abilities", true);
			abilityBlockDamageFlag = registerStateFlag(registry, "ability-block-damage", false);
			customMobSpawningFlag = registerStateFlag(registry, "custom-mob-spawning", true);
		}
		private static com.sk89q.worldguard.protection.flags.StateFlag registerStateFlag(com.sk89q.worldguard.protection.flags.registry.FlagRegistry registry, String name, boolean defaultValue) {
			com.sk89q.worldguard.protection.flags.Flag<?> existing = registry.get(name);
			if (existing instanceof com.sk89q.worldguard.protection.flags.StateFlag flag)
				return flag;
			if (existing != null) {
				Utils.sendConsoleMessage("&eWorld Guard flag &d'"+name+"' &ealready exists but is not a state flag. UltimateContent will ignore this flag.");
				return null;
			}
			com.sk89q.worldguard.protection.flags.StateFlag flag = new com.sk89q.worldguard.protection.flags.StateFlag(name, defaultValue);
			try {
				registry.register(flag);
				return flag;
			} catch (com.sk89q.worldguard.protection.flags.registry.FlagConflictException e) {
				existing = registry.get(name);
				return existing instanceof com.sk89q.worldguard.protection.flags.StateFlag stateFlag ? stateFlag : null;
			}
		}
		private static void initQuery() {
			query = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
		}
		private static com.sk89q.worldguard.protection.flags.StateFlag getStateFlag(String name) {
			if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
				return null;
			com.sk89q.worldguard.protection.flags.Flag<?> flag = com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry().get(name);
			return flag instanceof com.sk89q.worldguard.protection.flags.StateFlag stateFlag ? stateFlag : null;
		}
		private static com.sk89q.worldguard.protection.ApplicableRegionSet getRegions(Location location) {
			if (location == null || location.getWorld() == null || !Bukkit.getPluginManager().isPluginEnabled("WorldGuard"))
				return null;
			if (query == null)
				initQuery();
			return query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
		}
		private static com.sk89q.worldguard.protection.flags.StateFlag.State queryState(Location location, com.sk89q.worldguard.protection.flags.StateFlag flag) {
			if (flag == null)
				return null;
			com.sk89q.worldguard.protection.ApplicableRegionSet set = getRegions(location);
			if (set == null || set.size() == 0)
				return null;
			return set.queryState(null, flag);
		}
		private static boolean isRegion(Location location) {
			com.sk89q.worldguard.protection.ApplicableRegionSet set = getRegions(location);
			return set != null && set.size() != 0;
		}
		private static boolean canActivateAbilities(Location location) {
			if (activateAbilitiesFlag == null)
				activateAbilitiesFlag = getStateFlag("activate-abilities");
			return queryState(location, activateAbilitiesFlag) != com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
		}
		private static boolean canSpawnCustomMobs(Location location) {
			if (customMobSpawningFlag == null)
				customMobSpawningFlag = getStateFlag("custom-mob-spawning");
			return queryState(location, customMobSpawningFlag) != com.sk89q.worldguard.protection.flags.StateFlag.State.DENY;
		}
		private static boolean canDamageAbilityBlocks(Location location) {
			if (abilityBlockDamageFlag == null)
				abilityBlockDamageFlag = getStateFlag("ability-block-damage");
			com.sk89q.worldguard.protection.ApplicableRegionSet set = getRegions(location);
			if (set == null || set.size() == 0)
				return true;
			return abilityBlockDamageFlag != null && set.queryState(null, abilityBlockDamageFlag) == com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW;
		}
	}
}

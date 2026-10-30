package com.github.jewishbanana.ultimatecontent;

import com.github.jewishbanana.ultimatecontent.abilities.BlackRift;
import com.github.jewishbanana.ultimatecontent.abilities.Blinding;
import com.github.jewishbanana.ultimatecontent.abilities.CursedWinds;
import com.github.jewishbanana.ultimatecontent.abilities.DoubleJump;
import com.github.jewishbanana.ultimatecontent.abilities.JumpBoost;
import com.github.jewishbanana.ultimatecontent.abilities.PropulsionBlast;
import com.github.jewishbanana.ultimatecontent.abilities.RestoreHealth;
import com.github.jewishbanana.ultimatecontent.abilities.SaberParry;
import com.github.jewishbanana.ultimatecontent.abilities.SaberThrow;
import com.github.jewishbanana.ultimatecontent.abilities.SpawnPlatform;
import com.github.jewishbanana.ultimatecontent.abilities.StasisZone;
import com.github.jewishbanana.ultimatecontent.abilities.TeleportRay;
import com.github.jewishbanana.ultimatecontent.abilities.TidalWave;
import com.github.jewishbanana.ultimatecontent.abilities.UnstableMagic;
import com.github.jewishbanana.ultimatecontent.abilities.YetiRoar;
import com.github.jewishbanana.ultimatecontent.enchants.AncientCurse;
import com.github.jewishbanana.ultimatecontent.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.enchants.YetisBlessing;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Elf;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Frosty;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Grinch;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Santa;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.DarkMage;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.PlagueBat;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.PlagueRat;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.PrimedCreeper;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.ShadowLeech;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.SkeletonKnight;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.SwampBeast;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.UndeadMiner;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.ZombieKnight;
import com.github.jewishbanana.ultimatecontent.entities.desertentities.AncientMummy;
import com.github.jewishbanana.ultimatecontent.entities.desertentities.AncientSkeleton;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.EasterBunny;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.KillerChicken;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.RampagingGoat;
import com.github.jewishbanana.ultimatecontent.entities.endentities.BabyEndTotem;
import com.github.jewishbanana.ultimatecontent.entities.endentities.EndTotem;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidArcher;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidGuardian;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidStalker;
import com.github.jewishbanana.ultimatecontent.entities.endentities.VoidWorm;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedCreeper;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedDevourer;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedEnderman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedHowler;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSkeleton;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedSpirit;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedTribesman;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedWorm;
import com.github.jewishbanana.ultimatecontent.entities.infestedentities.InfestedZombie;
import com.github.jewishbanana.ultimatecontent.entities.netherentities.FirePhantom;
import com.github.jewishbanana.ultimatecontent.entities.netherentities.LostSoul;
import com.github.jewishbanana.ultimatecontent.entities.netherentities.SoulReaper;
import com.github.jewishbanana.ultimatecontent.entities.snowentities.Yeti;
import com.github.jewishbanana.ultimatecontent.entities.waterentities.CursedDiver;
import com.github.jewishbanana.ultimatecontent.items.books.AncientCurseBook;
import com.github.jewishbanana.ultimatecontent.items.christmas.BrokenSnowGlobe;
import com.github.jewishbanana.ultimatecontent.items.christmas.CandyCane;
import com.github.jewishbanana.ultimatecontent.items.christmas.CursedCandyCane;
import com.github.jewishbanana.ultimatecontent.items.christmas.Ornament;
import com.github.jewishbanana.ultimatecontent.items.christmas.SantaHat;
import com.github.jewishbanana.ultimatecontent.items.christmas.SnowGlobe;
import com.github.jewishbanana.ultimatecontent.items.easter.BlueEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.EasterBasket;
import com.github.jewishbanana.ultimatecontent.items.easter.GoldenEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.GreenEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.OrangeEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.PurpleEgg;
import com.github.jewishbanana.ultimatecontent.items.easter.RedEgg;
import com.github.jewishbanana.ultimatecontent.items.materials.AncientBone;
import com.github.jewishbanana.ultimatecontent.items.materials.AncientCloth;
import com.github.jewishbanana.ultimatecontent.items.materials.BloodIngot;
import com.github.jewishbanana.ultimatecontent.items.materials.YetiFur;
import com.github.jewishbanana.ultimatecontent.items.misc.BlindingTrap;
import com.github.jewishbanana.ultimatecontent.items.misc.BoosterPack;
import com.github.jewishbanana.ultimatecontent.items.misc.EnergyDrink;
import com.github.jewishbanana.ultimatecontent.items.misc.HeartCrystal;
import com.github.jewishbanana.ultimatecontent.items.misc.MatterSwap;
import com.github.jewishbanana.ultimatecontent.items.misc.MedKit;
import com.github.jewishbanana.ultimatecontent.items.misc.PropulsionCannon;
import com.github.jewishbanana.ultimatecontent.items.misc.SafetyNet;
import com.github.jewishbanana.ultimatecontent.items.misc.VoidTear;
import com.github.jewishbanana.ultimatecontent.items.tools.AbyssalShield;
import com.github.jewishbanana.ultimatecontent.items.weapons.AncientBlade;
import com.github.jewishbanana.ultimatecontent.items.weapons.CallOfTheVoid;
import com.github.jewishbanana.ultimatecontent.items.weapons.DarkMageWand;
import com.github.jewishbanana.ultimatecontent.items.weapons.GreenLightsaber;
import com.github.jewishbanana.ultimatecontent.items.weapons.SoulRipper;
import com.github.jewishbanana.ultimatecontent.items.weapons.StasisGun;
import com.github.jewishbanana.ultimatecontent.items.weapons.TritonsFang;
import com.github.jewishbanana.ultimatecontent.items.weapons.VoidsEdge;

public class UCRegistry {

	public static void cleanRegistries() {
		try {
			java.lang.reflect.Field abilityRegistry = com.github.jewishbanana.uiframework.items.UIAbilityType.class.getDeclaredField("registry");
			abilityRegistry.setAccessible(true);
			((java.util.Map<?, ?>) abilityRegistry.get(null)).keySet().removeIf(k -> k instanceof String && ((String) k).startsWith("uc:"));
		} catch (Throwable ignored) {}

		try {
			java.util.Map<String, com.github.jewishbanana.uiframework.items.UIEnchantment> reg = com.github.jewishbanana.uiframework.items.UIEnchantment.getRegistry();
			java.lang.reflect.Field idListField = com.github.jewishbanana.uiframework.items.UIEnchantment.class.getDeclaredField("registeredIdList");
			idListField.setAccessible(true);
			java.util.Set<?> idList = (java.util.Set<?>) idListField.get(null);
			java.util.Iterator<java.util.Map.Entry<String, com.github.jewishbanana.uiframework.items.UIEnchantment>> it = reg.entrySet().iterator();
			while (it.hasNext()) {
				java.util.Map.Entry<String, com.github.jewishbanana.uiframework.items.UIEnchantment> entry = it.next();
				if (entry.getKey() != null && entry.getKey().startsWith("uc:")) {
					if (entry.getValue() != null) {
						idList.remove(entry.getValue().getId());
					}
					it.remove();
				}
			}
		} catch (Throwable ignored) {}

		try {
			com.github.jewishbanana.uiframework.items.UIItemType.getRegistry().keySet().removeIf(k -> k != null && k.startsWith("uc:"));
		} catch (Throwable ignored) {}

		try {
			com.github.jewishbanana.uiframework.entities.UIEntityManager.getRegistry().keySet().removeIf(k -> k != null && k.startsWith("uc:"));
		} catch (Throwable ignored) {}

		try {
			java.lang.reflect.Field catPlacements = com.github.jewishbanana.uiframework.items.ItemCategory.class.getDeclaredField("placements");
			catPlacements.setAccessible(true);
			java.util.List<?> list = (java.util.List<?>) catPlacements.get(null);
			java.lang.reflect.Field idField = com.github.jewishbanana.uiframework.items.ItemCategory.class.getDeclaredField("identifier");
			idField.setAccessible(true);
			list.removeIf(item -> {
				try {
					String id = (String) idField.get(item);
					return id != null && id.startsWith("uc:");
				} catch (Throwable t) {
					return false;
				}
			});
		} catch (Throwable ignored) {}
	}

	public static void registerAbilities() {
		try { SaberParry.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SaberThrow.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { StasisZone.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { JumpBoost.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SpawnPlatform.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { RestoreHealth.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { PropulsionBlast.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { TeleportRay.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { Blinding.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { CursedWinds.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { BlackRift.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { TidalWave.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { YetiRoar.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { DoubleJump.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { UnstableMagic.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
	}
	public static void registerEnchants() {
		try { AncientCurse.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { YetisBlessing.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { BunnyHop.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
	}
	public static void registerItems() {
		// Crafting Materials
		try { AncientBone.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { AncientCloth.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { YetiFur.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { BloodIngot.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Weapons
		try { GreenLightsaber.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { StasisGun.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { AncientBlade.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { VoidsEdge.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { AbyssalShield.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { CallOfTheVoid.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { DarkMageWand.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { TritonsFang.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SoulRipper.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Enchant Books
		try { AncientCurseBook.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Miscellaneous
		try { BoosterPack.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SafetyNet.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { HeartCrystal.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { MedKit.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { EnergyDrink.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { PropulsionCannon.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { MatterSwap.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { BlindingTrap.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { VoidTear.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Easter
		try { GreenEgg.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { BlueEgg.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { RedEgg.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { OrangeEgg.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { PurpleEgg.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { GoldenEgg.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { EasterBasket.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Christmas
		try { CandyCane.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { CursedCandyCane.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { Ornament.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { BrokenSnowGlobe.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SantaHat.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SnowGlobe.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
	}
	public static void registerEntities() {
		// End Entities
		try { EndTotem.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { BabyEndTotem.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { VoidWorm.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { VoidArcher.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { VoidGuardian.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { VoidStalker.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Dark Entities
		try { PrimedCreeper.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { DarkMage.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { ShadowLeech.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SkeletonKnight.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { ZombieKnight.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SwampBeast.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { UndeadMiner.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { PlagueRat.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { PlagueBat.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Desert Entities
		try { AncientMummy.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { AncientSkeleton.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Water Entities
		try { CursedDiver.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Snow Entities
		try { Yeti.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Infested Entities
		try { InfestedZombie.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedSkeleton.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedCreeper.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedEnderman.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedSpirit.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedTribesman.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedDevourer.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedHowler.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { InfestedWorm.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Nether Entities
		try { LostSoul.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { SoulReaper.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { FirePhantom.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Christmas Entities
		try { Elf.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { Frosty.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { Grinch.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { Santa.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Easter Entities
		try { KillerChicken.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { RampagingGoat.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		try { EasterBunny.register(); } catch (Throwable t) { org.bukkit.Bukkit.getConsoleSender().sendMessage("[UltimateContent] Registration notice: " + t.getMessage()); }
		
		// Halloween Entities
//		Scarecrow.register();
	}
}

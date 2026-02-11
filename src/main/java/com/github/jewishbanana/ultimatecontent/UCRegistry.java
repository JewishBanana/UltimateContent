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
import com.github.jewishbanana.ultimatecontent.abilities.YetiRoar;
import com.github.jewishbanana.ultimatecontent.enchants.AncientCurse;
import com.github.jewishbanana.ultimatecontent.enchants.BunnyHop;
import com.github.jewishbanana.ultimatecontent.enchants.YetisBlessing;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Elf;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Frosty;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Grinch;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Santa;
import com.github.jewishbanana.ultimatecontent.entities.darkentities.DarkMage;
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

	public static void registerAbilities() {
		SaberParry.register();
		SaberThrow.register();
		StasisZone.register();
		JumpBoost.register();
		SpawnPlatform.register();
		RestoreHealth.register();
		PropulsionBlast.register();
		TeleportRay.register();
		Blinding.register();
		CursedWinds.register();
		BlackRift.register();
		TidalWave.register();
		YetiRoar.register();
		DoubleJump.register();
	}
	public static void registerEnchants() {
		AncientCurse.register();
		YetisBlessing.register();
		BunnyHop.register();
	}
	public static void registerItems() {
		// Crafting Materials
		AncientBone.register();
		AncientCloth.register();
		YetiFur.register();
		
		// Weapons
		GreenLightsaber.register();
		StasisGun.register();
		AncientBlade.register();
		VoidsEdge.register();
		AbyssalShield.register();
		CallOfTheVoid.register();
		DarkMageWand.register();
		TritonsFang.register();
		SoulRipper.register();
		
		// Enchant Books
		AncientCurseBook.register();
		
		// Miscellaneous
		BoosterPack.register();
		SafetyNet.register();
		HeartCrystal.register();
		MedKit.register();
		EnergyDrink.register();
		PropulsionCannon.register();
		MatterSwap.register();
		BlindingTrap.register();
		VoidTear.register();
		
		// Easter
		GreenEgg.register();
		BlueEgg.register();
		RedEgg.register();
		OrangeEgg.register();
		PurpleEgg.register();
		GoldenEgg.register();
		EasterBasket.register();
		
		// Christmas
		CandyCane.register();
		CursedCandyCane.register();
		Ornament.register();
		BrokenSnowGlobe.register();
		SantaHat.register();
		SnowGlobe.register();
	}
	public static void registerEntities() {
		// End Entities
		EndTotem.register();
		VoidWorm.register();
		VoidArcher.register();
		VoidGuardian.register();
		VoidStalker.register();
		
		// Dark Entities
		PrimedCreeper.register();
		DarkMage.register();
		ShadowLeech.register();
		SkeletonKnight.register();
		ZombieKnight.register();
		SwampBeast.register();
		UndeadMiner.register();
		
		// Desert Entities
		AncientMummy.register();
		AncientSkeleton.register();
		
		// Water Entities
		CursedDiver.register();
		
		// Snow Entities
		Yeti.register();
		
		// Infested Entities
		InfestedZombie.register();
		InfestedSkeleton.register();
		InfestedCreeper.register();
		InfestedEnderman.register();
		InfestedSpirit.register();
		InfestedTribesman.register();
		InfestedDevourer.register();
		InfestedHowler.register();
//		InfestedWorm.register();
		
		// Nether Entities
		LostSoul.register();
		SoulReaper.register();
		FirePhantom.register();
		
		// Christmas Entities
		Elf.register();
		Frosty.register();
		Grinch.register();
		Santa.register();
		
		// Easter Entities
		KillerChicken.register();
		RampagingGoat.register();
		EasterBunny.register();
		
		// Halloween Entities
//		Scarecrow.register();
	}
}

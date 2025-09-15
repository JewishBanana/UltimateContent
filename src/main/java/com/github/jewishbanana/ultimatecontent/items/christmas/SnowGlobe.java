package com.github.jewishbanana.ultimatecontent.items.christmas;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.christmasentities.Santa;
import com.github.jewishbanana.ultimatecontent.items.BossSpawnItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.SongPlayer;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class SnowGlobe extends BossSpawnItem {
	
	public static final String REGISTERED_KEY = "uc:snow_globe";

	public SnowGlobe(ItemStack item) {
		super(item);
	}
	public boolean placeBlock(BlockPlaceEvent event) {
		if (!super.placeBlock(event))
			return false;
		Block block = event.getBlock();
		if (!Utils.isBlockColdBiome(block)) {
			event.getPlayer().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.snowGlobePlaceError")));
			event.setCancelled(true);
			return false;
		}
		int count = 0;
		Location location = block.getLocation().add(.5, .5, .5);
		for (int i=0; i < 20; i++) {
			Location temp = Utils.findRandomSpotInCircle(location, 1.0, 15.0);
			Block tempBlock = Utils.getHighestExposedBlock(temp.getBlock(), 6);
			if (tempBlock == null || temp.getWorld().getHighestBlockAt(temp).getY() > tempBlock.getY())
				count++;
		}
		if (count > 12) {
			event.getPlayer().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.snowGlobePlaceError")));
			event.setCancelled(true);
			return false;
		}
		addToBossBlocks(block);
		Vector vec = Utils.getRandomizedVector(1.0, 0, 1.0).multiply(100);
		Location spawn = block.getLocation();
		spawn.setY(Math.min(spawn.getBlockY() + 60, 340));
		Location goalLoc = spawn.clone();
		Location boatGoal = spawn.clone().add(vec.clone().multiply(-1));
		spawn.add(vec);
		vec.multiply(-1).normalize();
		spawn.setDirection(vec);
		entities = new Entity[6];
		Santa santa = UIEntityManager.spawnEntity(spawn.clone().add(vec.clone().multiply(-4.5).setY(0.2)), Santa.class);
		entities[0] = santa.getEntity();
		entities[1] = spawn.getWorld().spawn(spawn, Boat.class);
		EntitiesHandler.attachRemoveKey(entities[1]);
		Vector angle = new Vector(-vec.getZ(), 0, vec.getX());
		Consumer<Horse> consumer = temp -> {
			temp.setLeashHolder(entities[1]);
			temp.setColor(Color.BROWN);
			EntitiesHandler.attachRemoveKey(temp);
		};
		entities[2] = spawn.getWorld().spawn(spawn.clone().add(vec.clone().multiply(2).add(angle)), Horse.class, consumer);
		entities[3] = spawn.getWorld().spawn(spawn.clone().add(vec.clone().multiply(2).add(angle.clone().multiply(-1))), Horse.class, consumer);
		entities[4] = spawn.getWorld().spawn(spawn.clone().add(vec.clone().multiply(7).add(angle)), Horse.class, consumer);
		entities[5] = spawn.getWorld().spawn(spawn.clone().add(vec.clone().multiply(7).add(angle.clone().multiply(-1))), Horse.class, consumer);
		for (Entity entity : entities) {
			entity.setGravity(false);
			if (entity instanceof LivingEntity alive)
				alive.setAI(false);
		}
		vec.multiply(0.5);
		SongPlayer songPlayer = SongPlayer.isEnabled() && CustomEntityType.SANTA.getSectionBoolean("playBossMusic", true) ?
				new SongPlayer(santa.getSongTheme()) : null;
		if (songPlayer != null) {
			songPlayer.setVolume(CustomEntityType.SANTA.getSectionDouble("bossMusicVolume", 1.0));
			songPlayer.setLooping();
			songPlayer.setPlaying(true);
			for (Entity entity : block.getWorld().getNearbyEntities(location, 30.0, 30.0, 30.0, temp -> temp instanceof Player))
				songPlayer.addPlayer((Player) entity);
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Entity entity : entities)
					if (entity != null)
						entity.teleport(entity.getLocation().add(vec));
				if (entities[0] != null && entities[0].getLocation().distanceSquared(goalLoc) <= 9) {
					entities[0].setGravity(true);
					((LivingEntity) entities[0]).setAI(true);
					new BukkitRunnable() {
						private LivingEntity entity = (LivingEntity) entities[0];
						
						@Override
						public void run() {
							if (entity.isOnGround())
								this.cancel();
							entity.addPotionEffect(new PotionEffect(VersionUtils.getResistance(), 20, 255, true, false));
						}
					}.runTaskTimer(plugin, 0, 10);
					entities[0] = null;
					block.setType(Material.AIR);
					block.getWorld().spawnParticle(VersionUtils.getBlockCrack(), block.getLocation().add(.5,.3,.5), 30, .2, .2, .2, 0.0001, Material.SNOW.createBlockData());
					block.getWorld().playSound(block.getLocation().add(.5,.3,.5), Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1, 0.5f);
					if (songPlayer != null)
						santa.setSongPlayer(songPlayer);
				}
				if (entities[1] != null && entities[1].getLocation().distanceSquared(boatGoal) <= 9) {
					for (Entity entity : entities)
						if (entity != null)
							entity.remove();
					removeBossBlock();
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 0, 1);
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), CustomHead.SNOW_GLOBE.getHead()).assembleLore().build();
	}
	public static void register() {
		UIItemType type = UIItemType.registerItem(REGISTERED_KEY, SnowGlobe.class);
		
		ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "snow_globe_recipe"), type.getItem());
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(Ornament.class)));
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(BrokenSnowGlobe.class)));
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(CandyCane.class)));
		type.registerRecipe(recipe);
	}
	public ItemCategory getItemCategory() {
		return CustomItemCategories.CHRISTMAS_ITEMS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "christmas_items";
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}

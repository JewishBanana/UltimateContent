package com.github.jewishbanana.ultimatecontent.items.easter;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.ItemBuilder;
import com.github.jewishbanana.uiframework.items.ItemCategory;
import com.github.jewishbanana.uiframework.items.UIItemType;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.easterentities.EasterBunny;
import com.github.jewishbanana.ultimatecontent.items.BossSpawnItem;
import com.github.jewishbanana.ultimatecontent.items.CustomItemBuilder;
import com.github.jewishbanana.ultimatecontent.items.Rarity;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.DataUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class EasterBasket extends BossSpawnItem {
	
	public static final String REGISTERED_KEY = "uc:easter_basket";

	public EasterBasket(ItemStack item) {
		super(item);
	}
	public boolean placeBlock(BlockPlaceEvent event) {
		if (!super.placeBlock(event))
			return false;
		Block block = event.getBlock();
		double temperature = block.getWorld().getTemperature(block.getX(), block.getY(), block.getZ());
		double humidity = block.getWorld().getHumidity(block.getX(), block.getY(), block.getZ());
		if (temperature < 0.45 || temperature > 0.85 || humidity < 0.3 || humidity > 0.805) {
			event.getPlayer().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.easterBasketPlaceError")));
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
		Location spawn = Utils.findRandomSpotInRadius(location, 25, 30, 3, 15);
		if (count > 12 || spawn == null) {
			event.getPlayer().sendMessage(Utils.convertString(DataUtils.getConfigString("language.items.easterBasketPlaceError")));
			event.setCancelled(true);
			return false;
		}
		addToBossBlocks(block);
		ArmorStand target = spawn.getWorld().spawn(spawn, ArmorStand.class, temp -> {
			ComplexEntity.initStand(temp);
			temp.setSmall(true);
			EntitiesHandler.attachRemoveKey(temp);
		});
		EasterBunny boss = UIEntityManager.spawnEntity(spawn, EasterBunny.class);
		Rabbit entity = boss.getCastedEntity();
		entity.setRemoveWhenFarAway(false);
		entity.setTarget(target);
		new BukkitRunnable() {
			private int tick;
			
			@Override
			public void run() {
				if (tick++ > 300) {
					this.cancel();
					target.remove();
					removeBossBlock();
					entity.setRemoveWhenFarAway(true);
					block.setType(Material.AIR);
					block.getWorld().spawnParticle(VersionUtils.getBlockCrack(), block.getLocation().add(.5,.3,.5), 30, .2, .2, .2, 0.0001, Material.BLUE_WOOL.createBlockData());
					block.getWorld().playSound(block.getLocation().add(.5,.3,.5), Sound.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 1, 0.5f);
					return;
				}
				if (entity.getTarget() == null || !entity.getTarget().isValid())
					entity.setTarget(target);
				for (int i=0; i < 8; i++) {
					DustTransition dust = random.nextInt(2) == 0 ? new DustTransition(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat())
							: new DustTransition(Color.fromRGB(random.nextInt(105)+150, 25, 255), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat() / 2f);
					spawn.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, spawn.clone().add(random.nextDouble()/1.5-.3, random.nextDouble()/1.5-.3, random.nextDouble()/1.5-.3), 1, 0, 0, 0, 0.001, dust);
				}
			}
		}.runTaskTimer(plugin, 0, 1);
		return true;
	}
	@Override
	public ItemBuilder createItem() {
		return CustomItemBuilder.create(getType(), CustomHead.EASTER_BASKET.getHead()).assembleLore().build();
	}
	public void unload(Block block) {
		if (block.getType() != Material.AIR)
			block.setType(Material.AIR);
		for (Entity entity : this.entities)
			if (entity != null)
				entity.remove();
	}
	public static void register() {
		UIItemType type = UIItemType.registerItem(REGISTERED_KEY, EasterBasket.class);
		
		ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "easter_basket_recipe"), type.getItem());
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(GreenEgg.class)));
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(BlueEgg.class)));
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(RedEgg.class)));
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(OrangeEgg.class)));
		recipe.addIngredient(new RecipeChoice.ExactChoice(UIItemType.getItem(PurpleEgg.class)));
		type.registerRecipe(recipe);
	}
	public ItemCategory getItemCategory() {
		return CustomItemCategories.EASTER_ITEMS.getItemCategory();
	}
	public String getConfigItemSection() {
		return "easter_items";
	}
	public Rarity getRarity() {
		return Rarity.EPIC;
	}
}

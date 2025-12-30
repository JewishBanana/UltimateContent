package com.github.jewishbanana.ultimatecontent.entities.easterentities;

import java.util.HashSet;
import java.util.List;
import java.util.random.RandomGenerator;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.entities.BaseEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.entities.EntityVariant.CustomDrop;
import com.github.jewishbanana.ultimatecontent.entities.pathfinders.PathfinderGoatRampage;
import com.github.jewishbanana.ultimatecontent.items.easter.RedEgg;
import com.github.jewishbanana.ultimatecontent.specialevents.EasterEvent;
import com.mojang.datafixers.util.Pair;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.goal.PathfinderFloat;
import me.gamercoder215.mobchip.ai.goal.PathfinderLookAtEntity;
import me.gamercoder215.mobchip.ai.goal.PathfinderRandomStrollLand;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderHurtByTarget;
import me.gamercoder215.mobchip.ai.goal.target.PathfinderNearestAttackableTarget;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class RampagingGoat extends BaseEntity<Goat> {

	public static final String REGISTERED_KEY = "uc:rampaging_goat";

	public RampagingGoat(Goat entity) {
		super(entity, CustomEntityType.RAMPAGING_GOAT);
		
		entity.setScreaming(true);
		
		scheduleTask(new BukkitRunnable() {
			private RandomGenerator random = RandomGenerator.of("SplittableRandom");
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				for (int i=0; i < 8; i++) {
					DustTransition dust = new DustTransition(Color.fromRGB(random.nextInt(125)+25, 255, random.nextInt(55)+25), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat());
					if (random.nextInt(2) == 0)
						dust = new DustTransition(Color.fromRGB(random.nextInt(105)+150, 25, 255), Color.fromRGB(25, random.nextInt(155)+100, 255), random.nextFloat()/2f);
					entity.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, entity.getLocation().add(random.nextDouble()*1.5-.75,0.7+(random.nextDouble()*1.5-.75),random.nextDouble()*1.5-.75), 1, 0, 0, 0, 0.001, dust);
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1));
	}
	public void setAIGoals(Goat entity) {
		EntityBrain brain = BukkitBrain.getBrain(entity);
		EntityAI goals = brain.getTargetAI();
		goals.clear();
		goals.put(new PathfinderHurtByTarget(entity, EntityType.RABBIT), 1);
		goals.put(new PathfinderNearestAttackableTarget<Player>(entity, Player.class), 2);
		
		goals = brain.getGoalAI();
		goals.clear();
		goals.put(new PathfinderFloat(entity), 1);
		goals.put(new PathfinderGoatRampage(entity, entityVariant.damage, 9.0, 80), 2);
		goals.put(new PathfinderRandomStrollLand(entity), 3);
		goals.put(new PathfinderLookAtEntity<>(entity, LivingEntity.class), 4);
	}
	public void onDeath(EntityDeathEvent event) {
		List<ItemStack> dropList = event.getDrops();
		dropList.clear();
		if (dropsItemsOnDeath() && !entityVariant.drops.isEmpty()) {
			for (CustomDrop drop : entityVariant.drops) {
				ItemStack stack = drop.generateDrop();
				if (stack == null)
					continue;
				if (EasterEvent.isEventActive && GenericItem.getItemBase(stack).getClass().equals(RedEgg.class)) {
					Item item = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), stack);
					item.getPersistentDataContainer().set(EasterEvent.easterEvent.eggKey, PersistentDataType.STRING, "redEgg");
					EasterEvent.easterEvent.droppedEggs.put(item.getUniqueId(), Pair.of(RedEgg.class, new HashSet<>()));
					continue;
				}
				dropList.add(stack);
			}
		}
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(RampagingGoat.REGISTERED_KEY, RampagingGoat.class);
		type.setRandomizeData(true);
		
		type.setSpawnConditions(event -> {
			if (!EasterEvent.isEventActive)
				return false;
			Location loc = event.getLocation();
			return !loc.getBlock().getRelative(BlockFace.DOWN).isPassable() && loc.getBlockY() > 150;
		});
	}
}

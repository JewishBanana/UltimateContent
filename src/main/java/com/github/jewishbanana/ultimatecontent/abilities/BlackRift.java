package com.github.jewishbanana.ultimatecontent.abilities;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.BlockUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class BlackRift extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:black_rift";
	
	private double damage;
	private double range;
	private double particleMultiplier;
	private int portalTicks;
	private boolean destroyBlocks;
	private boolean destroyItems;
	private boolean destroyProjectile;
	
	public BlackRift(UIAbilityType type) {
		super(type);
	}
	public void activate(Entity entity, GenericItem base) {
		if (entity instanceof Projectile projectile) {
			activate(entity.getLocation().add(0, entity.getHeight() / 2.0, 0), projectile.getShooter(), base);
			if (destroyProjectile)
				entity.remove();
			return;
		}
		activate(entity.getLocation().add(0, entity.getHeight() / 2.0, 0), null, base);
	}
	public void activate(Location loc, GenericItem base) {
		activate(loc, null, base);
	}
	private void activate(Location loc, ProjectileSource shooter, GenericItem base) {
		final World world = loc.getWorld();
		new BukkitRunnable() {
			private int tick = portalTicks;
			
			@Override
			public void run() {
				if (tick-- <= 0) {
					this.cancel();
					return;
				}
				if (tick % 5 != 0)
					return;
				if (particleMultiplier > 0) {
					world.spawnParticle(Particle.PORTAL, loc.getX(), loc.getY() + 0.8, loc.getZ(), (int) (20.0 * particleMultiplier), .2, .2, .2, 1.5);
					world.spawnParticle(Particle.SQUID_INK, loc, (int) (30.0 * particleMultiplier), .25, .25, .25, 0.0001);
				}
				loc.add(0, 0.2, 0);
				if (tick % 30 != 0)
					playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, .7f, 1.5f);
				for (Entity e : world.getNearbyEntities(loc, range, range, range)) {
						if (!canEntityBeHarmed(e))
							continue;
						Location temp = e.getLocation();
						e.setVelocity(Utils.getVectorTowards(temp, loc).multiply(0.3));
						final double dist = temp.distanceSquared(loc);
						if (dist < 2.25) {
							if (dist < 1 && e instanceof LivingEntity living) {
								EntityUtils.pureDamageEntity(living, damage, "deaths.unstableRiftSource", DamageCause.VOID, shooter instanceof LivingEntity source ? source : null);
//							if (DependencyUtils.DDHook != null && e.isDead() && shooter instanceof Player && !EntityUtils.isPlayerImmune((Player) shooter) && ((LivingEntity) e).getPersistentDataContainer().has(com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.VOIDARCHER.nameKey, PersistentDataType.BYTE))
//								DependencyUtils.awardAchievementProgress(((Player) shooter).getUniqueId(), "master.series.void_master", 1, 3);
							} else if (destroyItems || !(e instanceof Item))
								e.remove();
						}
					}
				if (destroyBlocks && random.nextInt(4) == 0)
					for (int i=0; i < 3; i++) {
						Block b = BlockUtils.rayTraceForBlock(loc, Utils.getRandomizedVector(), 4.0, t -> !t.isPassable());
						if (b == null || !canBlockBeDamaged(b))
							continue;
						FallingBlock fb = world.spawnFallingBlock(b.getLocation(), b.getBlockData());
						fb.setHurtEntities(true);
						fb.setDropItem(true);
						EntityUtils.markFallingBlock(fb);
						Location center = BlockUtils.getCenterOfBlock(b);
						if (b.getState() instanceof BlockInventoryHolder holder) {
							World world = center.getWorld();
							for (ItemStack item : holder.getInventory().getContents())
								if (item != null)
									world.dropItemNaturally(center, item);
						}
						b.setType(Material.AIR);
						fb.setVelocity(Utils.getVectorTowards(center, loc).multiply(0.3));
						break;
					}
			}
		}.runTaskTimer(plugin, 0, 1);
	}
	public SoundCategory getSoundCategory() {
		return SoundCategory.AMBIENT;
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, BlackRift.class);
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = registerSerializedDoubleField("damage", map);
		range = registerSerializedDoubleField("range", map);
		particleMultiplier = registerSerializedDoubleField("particleMultiplier", map);
		portalTicks = registerSerializedIntegerField("portalTicks", map);
		destroyBlocks = registerSerializedBooleanField("destroyBlocks", map);
		destroyItems = registerSerializedBooleanField("destroyItems", map);
		destroyProjectile = registerSerializedBooleanField("destroyProjectile", map);
	}
}

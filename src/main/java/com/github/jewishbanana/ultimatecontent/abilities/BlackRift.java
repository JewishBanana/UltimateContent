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
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.uiframework.items.UIAbilityType;
import com.github.jewishbanana.ultimatecontent.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class BlackRift extends AbilityAttributes {
	
	public static final String REGISTERED_KEY = "uc:black_rift";
	
	private double damage;
	private double range;
	private double particleMultiplier;
	private int portalTicks;
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
	public void activate(Location loc, ProjectileSource shooter, GenericItem base) {
		final World world = loc.getWorld();
		new BukkitRunnable() {
			private int tick = portalTicks;
			private double pos = 1;
			
			@Override
			public void run() {
				tick -= 5;
				if (tick <= 0) {
					this.cancel();
					return;
				}
				pos += 0.1;
				world.spawnParticle(Particle.PORTAL, loc.clone().add(0,1.3,0), (int) (20.0 * particleMultiplier), .2, .2, .2, 1.5);
				world.spawnParticle(Particle.SQUID_INK, loc.add(0,0.2,0).clone().add(0,0.3,0), (int) (30.0 * particleMultiplier), .25, .25, .25, 0.0001);
				playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, .7, 1);
				for (Entity e : world.getNearbyEntities(loc, range, range, range)) {
						if (!canEntityBeHarmed(e))
							continue;
						Location temp = e.getLocation();
						e.setVelocity(Utils.getVectorTowards(temp, loc).multiply(0.3));
						if (e instanceof LivingEntity && !e.isDead() && temp.distanceSquared(loc) < 1 && !(e instanceof ItemFrame)) {
							if (shooter != null)
								EntityUtils.pureDamageEntity((LivingEntity) e, damage, "deaths.unstableRiftSource", (LivingEntity) shooter, DamageCause.VOID);
							else
								EntityUtils.pureDamageEntity((LivingEntity) e, damage, "deaths.unstableRift", DamageCause.VOID);
//							if (DependencyUtils.DDHook != null && e.isDead() && shooter instanceof Player && !EntityUtils.isPlayerImmune((Player) shooter) && ((LivingEntity) e).getPersistentDataContainer().has(com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.VOIDARCHER.nameKey, PersistentDataType.BYTE))
//								DependencyUtils.awardAchievementProgress(((Player) shooter).getUniqueId(), "master.series.void_master", 1, 3);
						} else if (!(e instanceof LivingEntity) && temp.distanceSquared(loc) < 4 && !(e instanceof Item && !destroyItems))
							e.remove();
					}
				Block b = world.getBlockAt(loc.getBlockX()+(random.nextInt(8)-4), (int) (loc.getBlockY()-pos), loc.getBlockZ()+(random.nextInt(8)-4));
				if (b.getType().isBlock() && canBlockBeDamaged(b)) {
					FallingBlock fb = world.spawnFallingBlock(b.getLocation(), b.getBlockData());
					fb.setHurtEntities(true);
					fb.setDropItem(false);
					EntityUtils.markFallingBlock(fb);
					b.setType(Material.AIR);
					fb.setVelocity(Utils.getVectorTowards(b.getLocation().add(.5,.5,.5), loc).multiply(0.3));
				}
			}
		}.runTaskTimer(plugin, 0, 5);
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 1.0);
		this.range = getDoubleField("range", 4.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.portalTicks = getIntegerField("portalTicks", 80);
		this.destroyItems = getBooleanField("destroyItems", true);
		this.destroyProjectile = getBooleanField("destroyProjectile", true);
	}
	public SoundCategory getSoundCategory() {
		return SoundCategory.AMBIENT;
	}
	public static void register() {
		UIAbilityType.registerAbility(REGISTERED_KEY, BlackRift.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("damage", damage);
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		map.put("portalTicks", portalTicks);
		map.put("destroyItems", destroyItems);
		map.put("destroyProjectile", destroyProjectile);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = (double) map.get("damage");
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
		portalTicks = (int) map.get("portalTicks");
		destroyItems = (boolean) map.get("destroyItems");
		destroyProjectile = (boolean) map.get("destroyProjectile");
	}
}

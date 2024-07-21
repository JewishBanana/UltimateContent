package com.github.jewishbanana.ultimatecontent.items.abilities;

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

import com.github.jewishbanana.uiframework.items.AbilityType;
import com.github.jewishbanana.uiframework.items.GenericItem;
import com.github.jewishbanana.ultimatecontent.items.AbilityAttributes;
import com.github.jewishbanana.ultimatecontent.utils.DependencyUtils;
import com.github.jewishbanana.ultimatecontent.utils.RepeatingTask;
import com.github.jewishbanana.ultimatecontent.utils.Utils;

public class BlackRift extends AbilityAttributes {
	
	public static String REGISTERED_KEY = "ui:black_rift";
	
	private double damage;
	private double range;
	private double particleMultiplier;
	private int portalTicks;
	private boolean destroyItems;
	
	private ProjectileSource shooter;
	
	public void activate(Entity entity, GenericItem base) {
		if (entity instanceof Projectile && ((Projectile) entity).getShooter() instanceof LivingEntity)
			shooter = ((Projectile) entity).getShooter();
		activate(entity.getLocation().add(0,entity.getHeight()/2.0,0), base);
	}
	public void activate(Location loc, GenericItem base) {
		double[] var = {this.portalTicks, 1};
		final World world = loc.getWorld();
		new RepeatingTask(0, 5) {
			@Override
			public void run() {
				var[0] -= 5;
				if (var[0] <= 0) {
					cancel();
					return;
				}
				var[1] += 0.1;
				world.spawnParticle(Particle.PORTAL, loc.clone().add(0,1.3,0), (int) (20.0 * particleMultiplier), .2, .2, .2, 1.5);
				world.spawnParticle(Particle.SQUID_INK, loc.add(0,0.2,0).clone().add(0,0.3,0), (int) (30.0 * particleMultiplier), .25, .25, .25, 0.0001);
				world.playSound(loc, Sound.BLOCK_PORTAL_AMBIENT, SoundCategory.AMBIENT, .7f * volume, 1);
				for (Entity e : world.getNearbyEntities(loc, range, range, range)) {
						if (Utils.isEntityImmunePlayer(e) || DependencyUtils.isEntityProtected(e))
							continue;
						Location temp = e.getLocation();
						e.setVelocity(Utils.getVectorTowards(temp, loc).multiply(0.3));
						if (e instanceof LivingEntity && !e.isDead() && temp.distance(loc) < 1 && !(e instanceof ItemFrame)) {
							if (shooter != null)
								Utils.pureDamageEntity((LivingEntity) e, damage, "deaths.unstableRiftSource", false, (LivingEntity) shooter, DamageCause.VOID);
							else
								Utils.pureDamageEntity((LivingEntity) e, damage, "deaths.unstableRift", false, DamageCause.VOID);
							if (DependencyUtils.DDHook != null && e.isDead() && shooter instanceof Player && !Utils.isPlayerImmune((Player) shooter) && ((LivingEntity) e).getPersistentDataContainer().has(com.github.jewishbanana.deadlydisasters.entities.CustomEntityType.VOIDARCHER.nameKey, PersistentDataType.BYTE))
								DependencyUtils.awardAchievementProgress(((Player) shooter).getUniqueId(), "master.series.void_master", 1, 3);
						} else if (!(e instanceof LivingEntity) && temp.distanceSquared(loc) < 4 && !(e instanceof Item && !destroyItems))
							e.remove();
					}
				Block b = world.getBlockAt(loc.getBlockX()+(random.nextInt(8)-4), (int) (loc.getBlockY()-var[1]), loc.getBlockZ()+(random.nextInt(8)-4));
				if (b.getType().isBlock() && !DependencyUtils.isBlockProtected(b.getLocation())) {
					FallingBlock fb = world.spawnFallingBlock(b.getLocation(), b.getBlockData());
					fb.setHurtEntities(true);
					fb.setDropItem(false);
					Utils.markFallingBlock(fb);
					b.setType(Material.AIR);
					fb.setVelocity(Utils.getVectorTowards(b.getLocation().add(.5,.5,.5), loc).multiply(0.3));
				}
			}
		};
	}
	public void initFields() {
		this.damage = getDoubleField("damage", 1.0);
		this.range = getDoubleField("range", 4.0);
		this.particleMultiplier = getDoubleField("particleMultiplier", 1.0);
		this.portalTicks = getIntegerField("portalTicks", 80);
		this.destroyItems = getBooleanField("destroyItems", true);
	}
	public static void register() {
		AbilityType.registerAbility(REGISTERED_KEY, BlackRift.class);
	}
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		map.put("damage", damage);
		map.put("range", range);
		map.put("particleMultiplier", particleMultiplier);
		map.put("portalTicks", portalTicks);
		map.put("destroyItems", destroyItems);
		return map;
	}
	public void deserialize(Map<String, Object> map) {
		super.deserialize(map);
		damage = (double) map.get("damage");
		range = (double) map.get("range");
		particleMultiplier = (double) map.get("particleMultiplier");
		portalTicks = (int) map.get("portalTicks");
		destroyItems = (boolean) map.get("destroyItems");
	}
}

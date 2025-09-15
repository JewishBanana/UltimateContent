package com.github.jewishbanana.ultimatecontent.entities.infestedentities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;
import com.github.jewishbanana.ultimatecontent.utils.CustomHead;
import com.github.jewishbanana.ultimatecontent.utils.EntityUtils;
import com.github.jewishbanana.ultimatecontent.utils.Utils;
import com.github.jewishbanana.ultimatecontent.utils.Utils.AreaClearing;
import com.github.jewishbanana.ultimatecontent.utils.VersionUtils;

public class InfestedWorm extends ComplexEntity<ArmorStand> {

	public static final String REGISTERED_KEY = "uc:infested_worm";
	private static final ItemStack headMaterial;
	static {
		headMaterial = IS_VERSION_19_OR_ABOVE ? new ItemStack(Material.SCULK_SHRIEKER) : new ItemStack(Material.STICKY_PISTON);
	}
	
	private Block block;
	private Vector direction;
	private EulerAngle rotation;
	private BoundingBox attackZone;
	
	public InfestedWorm(ArmorStand entity) {
		super(entity, CustomEntityType.INFESTED_WORM, false);
		
		setInvisible(entity);
		entity.setSilent(true);
		entity.setInvulnerable(true);
		entity.setCollidable(false);
		entity.setMarker(true);
		entity.setSmall(true);
		entity.setGravity(false);
		
		entity.getEquipment().setHelmet(new ItemStack(Material.REDSTONE_BLOCK));
		
		block = entity.getLocation().getBlock();
		updateLocation();
		
		final BoundingBox renderBox = new BoundingBox(block.getX() - 4, block.getY() - 4, block.getZ() - 4, block.getX() + 5, block.getY() + 5, block.getZ() + 5);
		
		Consumer<ArmorStand> consumer = stand -> {
			initStand(stand);
			stand.setMarker(false);
			stand.setSmall(true);
			stand.getEquipment().setHelmet(CustomHead.INFESTED_WORM_BODY.getHead());
		};
		initStands(
				new CreatureStand<ArmorStand>(ArmorStand.class, stand -> {
					initStand(stand);
					stand.getEquipment().setHelmet(headMaterial);
				}),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer),
				new CreatureStand<ArmorStand>(ArmorStand.class, consumer)
				);
		setHeadStand(0);
		
		scheduleTask(new BukkitRunnable() {
			private boolean animation;
			
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (!block.getType().isSolid()) {
					entity.remove();
					return;
				}
				if (!animation) {
					Collection<Entity> entities = entity.getWorld().getNearbyEntities(attackZone, e -> e.isValid() && !(e instanceof ArmorStand) && ((e instanceof Player && !EntityUtils.isPlayerImmune((Player) e)) || e instanceof Animals));
					if (entities.isEmpty())
						return;
					animation = true;
					new BukkitRunnable() {
						private Entity target = entities.iterator().next();
						private int standCount = (int) Stream.of(stands).filter(temp -> temp.getEntityOrNull() != null).count();
						private Location loc = Utils.getCenterOfBlock(block);
						private int unearthTicks = 20;
						
						@Override
						public void run() {
							Vector forward = direction.clone().multiply(0.13);
							for (int i=0; i < standCount; i++) {
								Entity stand = getCreatureStandEntity(i);
								stand.teleport(stand.getLocation().add(forward));
							}
							if (standCount < getStandCount()) {
								Entity stand = getCreatureStandEntity(standCount, loc);
								if (standCount++ == 0)
									stand.teleport(stand.getLocation().subtract(direction.clone().multiply(1.2)));
							} else if (--unearthTicks == 0) {
								new WormWiggleAnimation(Stream.of(stands).map(temp -> (ArmorStand) temp.getEntity(loc)).collect(Collectors.toList()), headStand.getEntity(loc).getLocation(), 4, 1.0, 20).start();
								this.cancel();
							}
						}
					}.runTaskTimer(plugin, 0, 1);
				}
			}
		}.runTaskTimer(plugin, 0, 1));
		
		scheduleTask(new BukkitRunnable() {
			@Override
			public void run() {
				if (!entity.isValid())
					return;
				if (!Utils.isAreaClear(block.getRelative(direction.getBlockX() * 2, direction.getBlockY() * 2, direction.getBlockZ() * 2), AreaClearing.PLUS_SIGN_3D_FROM_CENTER))
					updateLocation();
			}
		}.runTaskTimer(plugin, 0, 20));
	}
	private void updateLocation() {
		for (BlockFace face : Set.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)) {
			if (!Utils.isAreaClear(block.getRelative(face, 2), AreaClearing.PLUS_SIGN_3D_FROM_CENTER))
				continue;
			switch (face) {
			default:
			case UP:
				direction = new Vector(0, 1, 0);
				rotation = new EulerAngle(Math.toRadians(0), 0, 0);
				break;
			case DOWN:
				direction = new Vector(0, -1, 0);
				rotation = new EulerAngle(Math.toRadians(180), 0, 0);
				break;
			case NORTH:
				direction = new Vector(0, 0, -1);
				rotation = new EulerAngle(Math.toRadians(90), 0, 0);
				break;
			case EAST:
				direction = new Vector(1, 0, 0);
				rotation = new EulerAngle(Math.toRadians(90), 0, 0);
				break;
			case SOUTH:
				direction = new Vector(0, 0, 1);
				rotation = new EulerAngle(Math.toRadians(90), 0, 0);
				break;
			case WEST:
				direction = new Vector(-1, 0, 0);
				rotation = new EulerAngle(Math.toRadians(90), 0, 0);
				break;
			}
			Block first = block.getRelative(face);
			Block second = block.getRelative(face, 3);
			attackZone = new BoundingBox(first.getX(), first.getY(), first.getZ(), second.getX() + 1, second.getY() + 1, second.getZ() + 1);
			attackZone.expand(1.5);
			return;
		}
		Bukkit.broadcastMessage("failed update");
		Entity entity = getEntity();
		if (entity != null)
			entity.remove();
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(InfestedWorm.REGISTERED_KEY, InfestedWorm.class);
		
		type.setSpawnConditions(event -> {
			Location loc = event.getLocation();
			if (!Utils.isEnvironment(loc.getWorld(), Environment.THE_END))
				return false;
			return true;
		});
		type.setSpawnByCommandConditions(parameters -> {
			if (!(parameters.sender instanceof Player player))
				return true;
			Block temp = player.getTargetBlockExact(5, FluidCollisionMode.NEVER);
			if (temp == null || !temp.getType().isSolid()) {
				parameters.sender.sendMessage(Utils.convertString("&cYou can only summon an infested worm on a solid block! Please look at a solid block when running this command!"));
				return false;
			}
			parameters.location = Utils.getCenterOfBlock(temp);
			return true;
		});
	}
	public class WormWiggleAnimation {

	    private final List<ArmorStand> segments; // head at index 0
	    private final Location basePosition; // base location of the head
	    private final int cycles;
	    private final int ticksPerCycle;
	    private final double amplitude;
	    private int tick;
	    private double currentCycleAmplitude;

	    public WormWiggleAnimation(List<ArmorStand> segments, Location basePosition, int cycles, double amplitude, int ticksPerCycle) {
	        this.segments = segments;
	        this.basePosition = basePosition.clone();
	        this.cycles = cycles;
	        this.amplitude = amplitude;
	        this.ticksPerCycle = ticksPerCycle;
	        this.currentCycleAmplitude = amplitude;
	    }

	    public void start() {
	        new BukkitRunnable() {
	            @Override
	            public void run() {
	                if (tick >= cycles * ticksPerCycle) {
	                    this.cancel();
	                    return;
	                }

	                if (tick % ticksPerCycle == 0) {
	                    // Randomize amplitude per full wave
	                    currentCycleAmplitude = amplitude * random.nextDouble(0.85, 1.15);
	                }

	                double progress = (double) (tick % ticksPerCycle) / ticksPerCycle;
	                double ease = easeInOut(progress);

	                List<Location> positions = new ArrayList<>();

	                // Move each segment with wave offset
	                for (int i = 0; i < segments.size(); i++) {
	                    double yOffset = -i * 0.5;
	                    double waveOffset = Math.sin(progress * Math.PI * 2 + i * 0.6) * currentCycleAmplitude * ease;

	                    Location segmentLoc = basePosition.clone().add(waveOffset, yOffset, 0); // X axis wiggle
	                    positions.add(segmentLoc);
	                    segments.get(i).teleport(segmentLoc);
	                }

	                // Rotate head-to-tail segments to face next one
	                for (int i = 0; i < segments.size(); i++) {
	                    ArmorStand segment = segments.get(i);
	                    Location from = positions.get(i);
	                    Location to;

	                    if (i + 1 < segments.size()) {
	                        to = positions.get(i + 1);
	                    } else if (i > 0) {
	                        to = positions.get(i - 1);
	                    } else {
	                        continue;
	                    }

	                    double dx = to.getX() - from.getX();
	                    double dz = to.getZ() - from.getZ();
	                    float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

	                    segment.setHeadPose(new EulerAngle(0, Math.toRadians(yaw), 0));
	                }

	                tick++;
	            }
	        }.runTaskTimer(plugin, 0, 1);
	    }
	    private double easeInOut(double t) {
	        return -(Math.cos(Math.PI * t) - 1) / 2;
	    }
	}
}

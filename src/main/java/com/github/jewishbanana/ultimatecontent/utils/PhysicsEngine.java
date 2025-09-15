package com.github.jewishbanana.ultimatecontent.utils;

import java.util.random.RandomGenerator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import com.github.jewishbanana.ultimatecontent.Main;
import com.github.jewishbanana.ultimatecontent.listeners.EntitiesHandler;

public class PhysicsEngine {
	
	private static final JavaPlugin plugin;
	private static final RandomGenerator random;
	static {
		plugin = Main.getInstance();
		random = RandomGenerator.of("SplittableRandom");
	}

	public static void dropBlockWithPhysics(Location spawnLocation, ItemStack item, float scale, Vector velocity, double gravity, int lifeTicks, int shrinkTicks) {
        ItemDisplay blockDisplay = spawnLocation.getWorld().spawn(spawnLocation, ItemDisplay.class, entity -> {
            entity.setItemStack(item);
            Transformation trans = entity.getTransformation();
            trans.getScale().mul(scale);
            entity.setTransformation(trans);
            EntitiesHandler.attachRemoveKey(entity);
        });
        final BlockData particleData = item.getType().createBlockData();
        new BukkitRunnable() {
            private int timer = lifeTicks;
            private float shrinkAmount = scale / shrinkTicks;
            private final double velocityThreshold = gravity; // Threshold to stop motion
            final double velocityDecay = 1.0 - gravity / 4.0; // Precomputed decay factor
            final double bounceDamping = 1.0 - gravity * 3; // Bounce damping factor
            final double angularVelocityRate = gravity * 1000 * 1.5;
            final double fluidAngularVelocityRate = angularVelocityRate / 5.0;
            private volatile boolean runNext = true;

            @Override
            public void run() {
                if (--timer <= 0 || blockDisplay == null || blockDisplay.isDead()) {
                    this.cancel();
                    if (blockDisplay != null)
                    	plugin.getServer().getScheduler().runTask(plugin, () -> blockDisplay.remove());
                    return;
                }
                if (!runNext)
                	return;
                if (timer <= shrinkTicks) {
                	plugin.getServer().getScheduler().runTask(plugin, () -> {
                		Transformation trans = blockDisplay.getTransformation();
                		trans.getScale().sub(shrinkAmount, shrinkAmount, shrinkAmount);
                		blockDisplay.setTransformation(trans);
                    	blockDisplay.teleport(blockDisplay.getLocation().subtract(0, shrinkAmount/ 2.0, 0));
                    });
                }
                if (velocity.isZero())
                	return;
                
                Location blockLoc = blockDisplay.getLocation();
                boolean isInFluid = blockLoc.getBlock().isLiquid();

                // Update rotation rates proportional to velocity
                double velocityMagnitude = velocity.length();
                double angularVelocity = isInFluid ? fluidAngularVelocityRate : angularVelocityRate;

                double rotationXRate = velocity.getX() * angularVelocity;
                double rotationYRate = velocity.getY() * angularVelocity;
                double rotationZRate = velocity.getZ() * angularVelocity;

                // Apply rotation
                Transformation trans = blockDisplay.getTransformation();
                trans.getLeftRotation().rotateX((float) Math.toRadians(rotationXRate));
                trans.getLeftRotation().rotateY((float) Math.toRadians(rotationYRate));
                trans.getLeftRotation().rotateZ((float) Math.toRadians(rotationZRate));
                blockDisplay.setTransformation(trans);

                // Apply velocity decay and gravity
                velocity.multiply(isInFluid ? velocityDecay * 0.6 : velocityDecay);
                velocity.setY(isInFluid ? -gravity : velocity.getY() - gravity);

                // Handle collisions
                Location loc = blockLoc.clone().add(velocity);
                Block downBlock = blockLoc.clone().add(0, (velocity.getY() > 0 ? scale : -scale) / 2.0, 0).add(new Vector(0, velocity.getY(), 0)).getBlock();

                if (blockLoc.getBlock().isPassable()) {
                	Block collision = blockLoc.clone().add((velocity.getX() > 0 ? scale : -scale) / 2.0, 0, 0).add(new Vector(velocity.getX(), 0, 0)).getBlock();
                	if (!collision.isPassable()) {
                		loc.setX(collision.getX() + (velocity.getX() > 0 ? -scale / 2.0 : scale / 2.0 + 1));
                    	velocity.setX(-velocity.getX() * bounceDamping);
                    }
                    if (!downBlock.isPassable()) {
                    	loc.setY(downBlock.getY() + (velocity.getY() > 0 ? -scale / 2.0 : scale / 2.0 + 1));
                    	velocity.setY(-velocity.getY() * bounceDamping);
                    	if (Math.abs(velocity.getY()) > gravity * 5)
                    		loc.getWorld().spawnParticle(VersionUtils.getBlockCrack(), downBlock.getLocation().add(.5,1.1,.5), (int) (scale * 4 * velocityMagnitude), scale / 2.0, .1, scale / 2.0, 0.1, particleData);
                    }
                    collision = blockLoc.clone().add(0, 0, (velocity.getZ() > 0 ? scale : -scale) / 2.0).add(new Vector(0, 0, velocity.getZ())).getBlock();
                    if (!collision.isPassable()) {
                    	loc.setZ(collision.getZ() + (velocity.getZ() > 0 ? -scale / 2.0 : scale / 2.0 + 1));
                    	velocity.setZ(-velocity.getZ() * bounceDamping);
                    }
                }
                if (!isInFluid && downBlock.isLiquid())
                	loc.getWorld().spawnParticle(VersionUtils.getWaterSplash(), downBlock.getLocation().add(0.5, 1.5, 0.5), (int) (velocityMagnitude * 16.0 * scale), scale, scale / 2.0, scale, 0.05);
                else if (isInFluid && loc.getBlock().getType() == Material.WATER && random.nextInt(Math.max((int) (5 - scale), 1)) == 0)
                	loc.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, loc, 1, scale / 3.0, scale / 3.0, scale / 3.0, 0.05);

                // Stop motion and rotation when velocity is too low
                if (velocityMagnitude <= velocityThreshold && !downBlock.isPassable())
                    velocity.zero();
                
                // Update block location
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                	blockDisplay.teleport(loc);
                	runNext = true;
                });
                runNext = false;
            }
        }.runTaskTimerAsynchronously(plugin, 0, 1);
    }
	public static void dropBlockWithPhysics(Location spawnLocation, ItemStack item, float scale, Vector velocity, double gravity, int lifeTicks) {
		dropBlockWithPhysics(spawnLocation, item, scale, velocity, gravity, lifeTicks, lifeTicks / 5);
	}
	public static void dropBlockWithPhysics(Location spawnLocation, Material material, float scale, Vector velocity, double gravity, int lifeTicks) {
		dropBlockWithPhysics(spawnLocation, new ItemStack(material), scale, velocity, gravity, lifeTicks, lifeTicks / 5);
	}
}

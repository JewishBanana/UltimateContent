package com.github.jewishbanana.ultimatecontent.entities.halloweenentities;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jewishbanana.uiframework.entities.UIEntityManager;
import com.github.jewishbanana.ultimatecontent.entities.AnimatedEntity;
import com.github.jewishbanana.ultimatecontent.entities.CustomEntityType;

public class Ghoul extends AnimatedEntity<Zombie> {
	
	public static final String REGISTERED_KEY = "uc:ghoul";
	
	private AnimationHandler grabAnimation;
	private AnimationHandler riseAnimation;
	private boolean walking;

	public Ghoul(Zombie entity, boolean spawnAnimation) {
		super(entity, CustomEntityType.GHOUL);
		
		this.walkAnimation = new AnimationHandler(true, true, true);
		this.walkAnimation.setContinueFrame(1);
		this.walkAnimation.setAnimations(this.walkAnimation.new Animation(
					this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, -30, 0, 10, 12, false, 0.5, 0.5),
					this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, 30, 0, -10, 12, false, 0.5, 0.5),
					this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, 20, 0, 10, 12, false, 0.5, 0.5),
					this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -20, 0, -10, 12, false, 0.5, 0.5)
				),
				this.walkAnimation.new Animation(
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, -30, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 30, 0, -10, 0, 0, 0, 12, false, 0.5, 0),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 20, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, -20, 0, -10, 0, 0, 0, 12, false, 0.5, 0)
				),
				this.walkAnimation.new Animation(
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, 30, 0, 10, 12, false, 0, 0.5),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, -30, 0, -10, 12, false, 0, 0.5),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -20, 0, 10, 12, false, 0, 0.5),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, 20, 0, -10, 12, false, 0, 0.5)
				),
				this.walkAnimation.new Animation(
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 30, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, -30, 0, -10, 0, 0, 0, 12, false, 0.5, 0),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, -20, 0, 10, 0, 0, 0, 12, false, 0.5, 0),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 20, 0, -10, 0, 0, 0, 12, false, 0.5, 0)
				),
				this.walkAnimation.new Animation(
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_LEG, 0, 0, 0, -30, 0, 10, 12, false, 0, 0.5),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_LEG, 0, 0, 0, 30, 0, -10, 12, false, 0, 0.5),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, 20, 0, 10, 12, false, 0, 0.5),
						this.walkAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -20, 0, -10, 12, false, 0, 0.5)
				)
			);
		
		this.grabAnimation = new AnimationHandler(true, false, true);
		this.grabAnimation.setAnimations(this.grabAnimation.new Animation(
					this.grabAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 260, 36, 0, 245, -20, 15, 14, true, 0.5, 0.5),
					this.grabAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 260, 325, 0, 245, 380, -5, 14, true, 1.0, 0.5),
					this.grabAnimation.new AnimationCheckpoint(BodyPart.HEAD, 0, 0, -20, 0, 0, 20, 14, true, 0.5, 0.5)
				)
			);
		this.grabAnimation.go();
		
		this.riseAnimation = new AnimationHandler(true, true, false);
		this.riseAnimation.setAnimations(this.riseAnimation.new Animation(
					this.riseAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 187, 44, 0, 260, 36, 0, 40, false, 0.5, 0.5),
					this.riseAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 199, 316, 0, 260, 325, 0, 40, false, 0.5, 0.5),
					this.riseAnimation.new AnimationCheckpoint(BodyPart.HEAD, 319, 0, 20, 0, 0, -20, 40, false, 0.5, 0.5)
				)
			);
		
		final Block block = entity.getLocation().getBlock();
		walking = block == null;
		if (spawnAnimation && block.getType().isSolid()) {
			ArmorStand stand = getAnimatedStand().getEntity(entity.getLocation());
			this.riseAnimation.startAnimation(stand);
			this.riseAnimation.tick(stand);
			final Location toGround = block.getRelative(BlockFace.DOWN).getLocation().add(.5, .1, .5);
			new BukkitRunnable() {
				private int tick = 40;
				private double diff = 0.8 / 40.0;
				
				@Override
				public void run() {
					if (entity == null || tick-- <= 0) {
						this.cancel();
						return;
					}
					entity.teleport(toGround);
					toGround.add(0, diff, 0);
					if (IS_VERSION_19_OR_ABOVE && tick % 5 == 0)
						entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_SCULK_BREAK, SoundCategory.HOSTILE, 2f, .5f);
				}
			}.runTaskTimer(plugin, 0, 1);
		}
	}
	public Ghoul(Zombie entity) {
		this(entity, true);
	}
	public static void register() {
		UIEntityManager type = UIEntityManager.registerEntity(Ghoul.REGISTERED_KEY, Ghoul.class);
		
		type.setSpawnConditions(event -> {
			return false;
		});
	}
}

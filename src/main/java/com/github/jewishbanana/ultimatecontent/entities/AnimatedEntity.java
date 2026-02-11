package com.github.jewishbanana.ultimatecontent.entities;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import com.github.jewishbanana.ultimatecontent.entities.ComplexEntity.CreatureStand;

import me.gamercoder215.mobchip.EntityBody;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;

public class AnimatedEntity<T extends Mob> extends BaseEntity<T> {
	
	private static final double WALK_THRESHOLD = 0.01;
	
	private CreatureStand<ArmorStand> stand;
	protected AnimationHandler walkAnimation;
	private AnimationHandler attackAnimation;
	private boolean shouldWalk = true;

	public AnimatedEntity(T entity, CustomEntityType type) {
		super(entity, type);
		this.attackAnimation = new AnimationHandler(true, false, false);
		this.attackAnimation.setAnimations(this.attackAnimation.new Animation(
					this.attackAnimation.new AnimationCheckpoint(BodyPart.RIGHT_ARM, 0, 0, 0, -112, 30, 0, 8, true, 0.5f, 0.5f),
					this.attackAnimation.new AnimationCheckpoint(BodyPart.LEFT_ARM, 0, 0, 0, -112, -30, 0, 8, true, 0.5f, 0.5f)
				)
			);
		EntityBody body = BukkitBrain.getBrain(entity).getBody();
		scheduleTask(new BukkitRunnable() {
			private Location step = entity.getLocation();
			
			@Override
			public void run() {
				Location entityLoc = entity.getLocation();
				float rot = body.getBodyRotation();
				ArmorStand mainStand = stand.getEntity(entityLoc);
				mainStand.setHeadPose(new EulerAngle(Math.toRadians(entityLoc.getPitch()), Math.toRadians(entityLoc.getYaw() - rot), 0));
				entityLoc.setYaw(rot);
				mainStand.teleport(entityLoc);
				if (shouldWalk && step.distanceSquared(entityLoc) > WALK_THRESHOLD && entity.isOnGround()) {
					step = entityLoc;
					walkAnimation.go();
				} else
					walkAnimation.stop();
				walkAnimation.tick(mainStand);
				attackAnimation.tick(mainStand);
			}
		}.runTaskTimer(plugin, 0, 1));
	}
	public void hitEntity(EntityDamageByEntityEvent event) {
		super.hitEntity(event);
		swingArms();
	}
	public void unload() {
		super.unload();
		if (stand != null) {
			ArmorStand standEntity = stand.getEntityOrNull();
			if (standEntity != null)
				standEntity.remove();
		}
	}
	public boolean shouldEquipBaseEntity() {
		return false;
	}
	public void swingArms() {
		if (stand != null) {
			ArmorStand standEntity = stand.getEntityOrNull();
			if (standEntity != null)
				attackAnimation.startAnimation(standEntity);
		}
	}
	public CreatureStand<ArmorStand> getAnimatedStand() {
		return stand;
	}
	public void setAnimatedStand(CreatureStand<ArmorStand> animatedStand) {
		this.stand = animatedStand;
	}
	public enum BodyPart {
		HEAD,
		BODY,
		RIGHT_ARM,
		LEFT_ARM,
		RIGHT_LEG,
		LEFT_LEG
	}
	public class AnimationHandler {
		
		private Animation[] animations;
		private int currentPoint = 0, continueFrame = 0, frameRate = 1;
		public boolean shouldStop = true, resetOnStop, adjustOnStop, isDone, continous;
		
		/**
		 * @param resetOnStop - True if the animation sequence should reset to frame 0 when interrupted
		 * @param adjustOnStop - True if the stand should be set to frame 0 pos on interrupt
		 * @param continous - True if the animation should naturally loop after ending
		 */
		public AnimationHandler(boolean resetOnStop, boolean adjustOnStop, boolean continous) {
			this.resetOnStop = resetOnStop;
			this.adjustOnStop = adjustOnStop;
			this.continous = continous;
		}
		public void tick(ArmorStand stand) {
			if (shouldStop) {
				if (!continous && isDone)
					return;
				Animation currentAnim = animations[currentPoint];
				if (!currentAnim.isFinished) {
					currentAnim.tick(stand);
					if (currentAnim.isFinished) {
						if (adjustOnStop) {
							AnimationCheckpoint[] points = animations[0].points;
							for (int i = 0; i < points.length; i++)
								points[i].adjustToFrame(stand, 0);
						}
						if (resetOnStop) {
							currentPoint = 0;
							animations[currentPoint].isFinished = false;
						}
						isDone = true;
					}
				} else
					isDone = true;
				return;
			}
			Animation currentAnim = animations[currentPoint];
			if (!currentAnim.isFinished)
				currentAnim.tick(stand);
			else {
				currentPoint++;
				if (currentPoint >= animations.length)
					currentPoint = continueFrame;
				animations[currentPoint].tick(stand);
			}
		}
		@SafeVarargs
		public final void setAnimations(AnimationHandler.Animation... animations) {
			this.animations = animations;
		}
		public void go() {
			shouldStop = false;
			isDone = false;
		}
		public void stop() {
			shouldStop = true;
		}
		public void startAnimation(ArmorStand stand) {
			go();
			tick(stand);
			stop();
		}
		public void forceStop(ArmorStand stand) {
			for (int i = 0; i < animations.length; i++)
				animations[i].forceStop(stand);
			shouldStop = true;
		}
		public boolean isFinished() {
			return isDone;
		}
		public int getContinueFrame() {
			return continueFrame;
		}
		public void setContinueFrame(int continueFrame) {
			this.continueFrame = continueFrame;
		}
		public int getFrameRate() {
			return frameRate;
		}
		public void setFrameRate(int frameRate) {
			this.frameRate = frameRate;
		}
		public class Animation {
			
			private final AnimationCheckpoint[] points;
			private boolean isFinished;
			
			@SafeVarargs
			public Animation(AnimationCheckpoint... animationCheckpoints) {
				this.points = animationCheckpoints;
			}
			public void tick(ArmorStand stand) {
				isFinished = false;
				boolean ticked = false;
				for (int i = 0; i < points.length; i++) {
					if (!points[i].isFinished) {
						points[i].tick(stand);
						ticked = true;
					}
				}
				if (!ticked) {
					isFinished = true;
					for (int i = 0; i < points.length; i++)
						points[i].reset();
				}
			}
			public void forceStop(ArmorStand stand) {
				for (int i = 0; i < points.length; i++)
					points[i].forceStop(stand);
				isFinished = true;
			}
		}
		public class AnimationCheckpoint {
			
			private final BodyPart part;
			private final boolean reverse;
			private final boolean overrideValues;
			private final float[][] frames;
			private int frame = 1;
			private boolean flipped, isFinished;
			
			public AnimationCheckpoint(BodyPart part, float initialX, float initialY, float initialZ, float targetX, float targetY, float targetZ, int ticks, boolean reverse, float acceleration, float decceleration, boolean overrideValues) {
				this.part = part;
				this.reverse = reverse;
				this.overrideValues = overrideValues;
				frames = new float[ticks + 1][];
				frames[0] = new float[] {initialX, initialY, initialZ};
				float speedX = (targetX - initialX) / ticks;
				float speedY = (targetY - initialY) / ticks;
				float speedZ = (targetZ - initialZ) / ticks;
				int halfSteps = ticks / 2;
				int quarterSteps = halfSteps / 2;
				if (acceleration > 0 && decceleration == 0) {
					quarterSteps = halfSteps;
					halfSteps = ticks;
				} else if (decceleration > 0 && acceleration == 0) {
					quarterSteps = halfSteps;
					halfSteps = 0;
				}
				float accStep = acceleration / quarterSteps;
				float decStep = decceleration / quarterSteps;
				for (int i = 0; i < frames.length - 1; i++) {
					if (frames[i + 1] == null)
						frames[i + 1] = new float[]{speedX, speedY, speedZ};
					if (i < quarterSteps && quarterSteps < halfSteps) {
						float multiplier = accStep * (quarterSteps - i);
						float[] startSpeeds = new float[3];
						startSpeeds[0] = speedX * (1 - multiplier);
						startSpeeds[1] = speedY * (1 - multiplier);
						startSpeeds[2] = speedZ * (1 - multiplier);
						frames[i + 1] = startSpeeds;
						float[] endSpeeds = new float[3];
						endSpeeds[0] = speedX * (1 + multiplier);
						endSpeeds[1] = speedY * (1 + multiplier);
						endSpeeds[2] = speedZ * (1 + multiplier);
						frames[halfSteps - (i + 1) + 1] = endSpeeds;
					} else if (i >= halfSteps) {
						if (i >= halfSteps + quarterSteps)
							continue;
						float multiplier = decStep * (quarterSteps - (i - halfSteps));
						float[] startSpeeds = new float[3];
						startSpeeds[0] = speedX * (1 + multiplier);
						startSpeeds[1] = speedY * (1 + multiplier);
						startSpeeds[2] = speedZ * (1 + multiplier);
						frames[i + 1] = startSpeeds;
						float[] endSpeeds = new float[3];
						endSpeeds[0] = speedX * (1 - multiplier);
						endSpeeds[1] = speedY * (1 - multiplier);
						endSpeeds[2] = speedZ * (1 - multiplier);
						frames[ticks - ((i - halfSteps) + 1) + 1] = endSpeeds;
					}
				}
				for (int i = 1; i < frames.length; i++) {
					initialX += frames[i][0];
					initialY += frames[i][1];
					initialZ += frames[i][2];
					frames[i] = new float[] {initialX, initialY, initialZ};
				}
			}
			public AnimationCheckpoint(BodyPart part, float initialX, float initialY, float initialZ, float targetX, float targetY, float targetZ, int ticks, boolean reverse, float acceleration, float decceleration) {
				this(part, initialX, initialY, initialZ, targetX, targetY, targetZ, ticks, reverse, acceleration, decceleration, false);
			}
			public AnimationCheckpoint(BodyPart part, float initialX, float initialY, float initialZ, float targetX, float targetY, float targetZ, int ticks, boolean reverse) {
				this(part, initialX, initialY, initialZ, targetX, targetY, targetZ, ticks, reverse, 0.0f, 0.0f, false);
			}
			public void tick(ArmorStand stand) {
				if (isFinished)
					reset();
				adjustToFrame(stand, frame);
				if (!flipped)
					frame += frameRate;
				else
					frame -= frameRate;
				if (frame >= frames.length || frame < 0) {
					if (flipped || !reverse) {
						isFinished = true;
						return;
					}
					frame = frames.length - 1;
					flipped = true;
				}
			}
			public void adjustToFrame(ArmorStand stand, int frame) {
				float[] frameData = frames[frame];
				EulerAngle angle = new EulerAngle(Math.toRadians(frameData[0]), Math.toRadians(frameData[1]), Math.toRadians(frameData[2]));
				EulerAngle pose;
				switch (part) {
				case BODY:
					if (overrideValues) {
						stand.setBodyPose(angle);
					} else {
						pose = stand.getBodyPose();
						stand.setBodyPose(new EulerAngle(angle.getX() != 0 ? angle.getX() : pose.getX(), angle.getY() != 0 ? angle.getY() : pose.getY(), angle.getZ() != 0 ? angle.getZ() : pose.getZ()));
					}
					break;
				case HEAD:
					if (overrideValues) {
						stand.setHeadPose(angle);
					} else {
						pose = stand.getHeadPose();
						stand.setHeadPose(new EulerAngle(angle.getX() != 0 ? angle.getX() : pose.getX(), angle.getY() != 0 ? angle.getY() : pose.getY(), angle.getZ() != 0 ? angle.getZ() : pose.getZ()));
					}
					break;
				case RIGHT_ARM:
					if (overrideValues) {
						stand.setRightArmPose(angle);
					} else {
						pose = stand.getRightArmPose();
						stand.setRightArmPose(new EulerAngle(angle.getX() != 0 ? angle.getX() : pose.getX(), angle.getY() != 0 ? angle.getY() : pose.getY(), angle.getZ() != 0 ? angle.getZ() : pose.getZ()));
					}
					break;
				case LEFT_ARM:
					if (overrideValues) {
						stand.setLeftArmPose(angle);
					} else {
						pose = stand.getLeftArmPose();
						stand.setLeftArmPose(new EulerAngle(angle.getX() != 0 ? angle.getX() : pose.getX(), angle.getY() != 0 ? angle.getY() : pose.getY(), angle.getZ() != 0 ? angle.getZ() : pose.getZ()));
					}
					break;
				case RIGHT_LEG:
					if (overrideValues) {
						stand.setRightLegPose(angle);
					} else {
						pose = stand.getRightLegPose();
						stand.setRightLegPose(new EulerAngle(angle.getX() != 0 ? angle.getX() : pose.getX(), angle.getY() != 0 ? angle.getY() : pose.getY(), angle.getZ() != 0 ? angle.getZ() : pose.getZ()));
					}
					break;
				case LEFT_LEG:
					if (overrideValues) {
						stand.setLeftLegPose(angle);
					} else {
						pose = stand.getLeftLegPose();
						stand.setLeftLegPose(new EulerAngle(angle.getX() != 0 ? angle.getX() : pose.getX(), angle.getY() != 0 ? angle.getY() : pose.getY(), angle.getZ() != 0 ? angle.getZ() : pose.getZ()));
					}
					break;
				}
			}
			public void reset() {
				frame = 1;
				isFinished = false;
				flipped = false;
			}
			public void forceStop(ArmorStand stand) {
				adjustToFrame(stand, 0);
				isFinished = true;
			}
		}
	}
}
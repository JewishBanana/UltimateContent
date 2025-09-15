package com.github.jewishbanana.ultimatecontent.utils;

import java.util.function.Supplier;

import org.bukkit.Sound;

import com.github.jewishbanana.ultimatecontent.entities.EntityVariant;

public class SoundEffect {

	private Sound sound;
	private float volume;
	private float pitch;
	private Supplier<Float> pitchSupplier;
	
	public SoundEffect(EntityVariant type, Sound sound, double volume, double pitch) {
		this.sound = sound;
		this.volume = (float) (volume * type.volume);
		this.pitch = (float) pitch;
	}
	public SoundEffect(EntityVariant type, Sound sound, double volume, Supplier<Float> pitchSupplier) {
		this.sound = sound;
		this.volume = (float) (volume * type.volume);
		this.pitchSupplier = pitchSupplier;
	}
	public Sound getSound() {
		return sound;
	}
	public float getVolume() {
		return volume;
	}
	public float getPitch() {
		return pitchSupplier == null ? pitch : pitchSupplier.get();
	}
}

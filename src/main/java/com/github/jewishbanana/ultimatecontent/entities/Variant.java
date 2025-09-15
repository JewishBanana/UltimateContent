package com.github.jewishbanana.ultimatecontent.entities;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.bukkit.NamespacedKey;

import com.github.jewishbanana.ultimatecontent.Main;

public interface Variant {
	
	public static final Map<CustomEntityType, Queue<Variant>> variants = new HashMap<>();
	public static final NamespacedKey variantKey = new NamespacedKey(Main.getInstance(), "uc-variant");
	public static final Map<Variant, EntityVariant> entityVariants = new HashMap<>();
	public static final Map<Variant, String> entityKeys = new HashMap<>();
	
	default EntityVariant getEntityVariant() {
		return entityVariants.get(this);
	}
	
	default String getKey() {
		return entityKeys.getOrDefault(this, this.getClass().getSimpleName());
	}
	
	default void registerVariant(CustomEntityType type, String variantPathName) {
		EntityVariant variant = new EntityVariant(type.configPath);
		variant.overwriteFromPath(variant.configPath+"variants."+variantPathName);
		if (useDefaultLoadout())
			variant.loadout.defaultLoad = type.normalVariant.defaultLoadout;
		Queue<Variant> queue = variants.getOrDefault(type, new ArrayDeque<>());
		queue.add(this);
		variants.put(type, queue);
		entityVariants.put(this, variant);
		entityKeys.put(this, variantPathName);
	}
	
	default void initVariant() {};
	
	default boolean useDefaultLoadout() {
		return true;
	}
	
	public static <T extends Enum<T> & Variant> void initVariants(Class<T> enumClass) {
	    for (T constant : enumClass.getEnumConstants())
	        constant.initVariant();
	}
}

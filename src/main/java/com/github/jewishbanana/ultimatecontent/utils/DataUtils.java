package com.github.jewishbanana.ultimatecontent.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.github.jewishbanana.ultimatecontent.Main;

public class DataUtils {
	
	private static Main plugin;
	private static DecimalFormat decimalFormat;
	static {
		plugin = Main.getInstance();
		decimalFormat = new DecimalFormat("0.#");
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Entry.comparingByValue());
        Map<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());
        return result;
    }
	public static <K, V> Map<K, V> reverseMap(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		Collections.reverse(list);
		Map<K, V> newMap = new LinkedHashMap<>();
		for (Entry<K, V> entry : list)
			newMap.put(entry.getKey(), entry.getValue());
		return newMap;
	}
	public static int getConfigInt(String path) {
		try {
			return plugin.getConfig().getInt(path);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dinteger &evalue from config path '"+path+"' please fix this value!"));
			return 0;
		}
	}
	public static double getConfigDouble(String path) {
		try {
			return plugin.getConfig().getDouble(path);
		} catch (NumberFormatException e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &ddouble &evalue from config path '"+path+"' please fix this value!"));
			return 0.0;
		}
	}
	public static boolean getConfigBoolean(String path) {
		try {
			return plugin.getConfig().getBoolean(path);
		} catch (Exception e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dboolean &evalue from config path '"+path+"' please fix this value!"));
			return false;
		}
	}
	public static String getConfigString(String path) {
		try {
			return plugin.getConfig().getString(path);
		} catch (Exception e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dstring &evalue from config path '"+path+"' please fix this value!"));
			return null;
		}
	}
	public static List<String> getConfigStringList(String path) {
		try {
			return plugin.getConfig().getStringList(path);
		} catch (Exception e) {
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&eWARNING while reading &dstring list &evalue from config path '"+path+"' please fix this value!"));
			return null;
		}
	}
	public static String capitalizeFormat(String string) {
		StringBuilder temp = new StringBuilder(string);
		if (temp.length() >= 1)
			temp.setCharAt(0, Character.toUpperCase(temp.charAt(0)));
		for (int i=0; i < temp.length(); i++)
			if (temp.charAt(i) == ' ' && temp.length() > i+1)
				temp.setCharAt(i+1, Character.toUpperCase(temp.charAt(i+1)));
		return temp.toString();
	}
	public static <T> String getDecimalFormatted(T num) {
		return decimalFormat.format(num);
	}
	public static double map(double value, double istart, double istop, double ostart, double ostop) {
		return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
	}
	public static boolean isEqualsNoNull(Object arg1, Object arg2) {
		if (arg1 == null || arg2 == null)
			return false;
		return arg1.equals(arg2);
	}
	public static <T extends Recipe & Keyed> void writeRecipeToSection(YamlConfiguration file, T recipe, String section) {
		String recipeType = null;
		if (recipe instanceof ShapedRecipe)
			recipeType = "shaped";
		else if (recipe instanceof ShapelessRecipe)
			recipeType = "shapeless";
		else if (recipe instanceof CookingRecipe)
			recipeType = "cooking";
		if (recipeType == null)
			throw new IllegalArgumentException("[UltimateContent]: Cannot register recipe because the recipe type is not supported!");
		file.set(section+".type", recipeType);
		switch (recipeType) {
		case "shaped":
			ShapedRecipe shaped = (ShapedRecipe) recipe;
			file.set(section+".shape", Arrays.asList(shaped.getShape()));
			file.createSection(section+".exact");
			file.createSection(section+".material");
			shaped.getChoiceMap().forEach((k, v) -> {
				if (v != null) {
					if (v instanceof RecipeChoice.ExactChoice)
						file.set(section+".exact."+k, ((RecipeChoice.ExactChoice) v).getChoices());
					else
						file.set(section+".material."+k, ((RecipeChoice.MaterialChoice) v).getChoices().stream().map(e -> e.toString()).collect(Collectors.toList()));
				}
			});
			break;
		case "shapeless":
			ShapelessRecipe shapeless = (ShapelessRecipe) recipe;
			file.set(section+".exact", shapeless.getChoiceList().stream().filter(k -> k instanceof RecipeChoice.ExactChoice).map(k -> ((RecipeChoice.ExactChoice) k).getChoices()).collect(Collectors.toList()));
			file.set(section+".material", shapeless.getChoiceList().stream().filter(k -> k instanceof RecipeChoice.MaterialChoice).map(k -> ((RecipeChoice.MaterialChoice) k).getChoices().stream().map(l -> l.toString()).collect(Collectors.toList())).collect(Collectors.toList()));
			break;
		}
	}
	@SuppressWarnings("unchecked")
	public static <T extends Recipe & Keyed> T createRecipeFromSection(YamlConfiguration file, String section, ItemStack result, NamespacedKey key) {
		try {
			String shape = file.getString(section+".type");
			switch (shape) {
			default:
				return null;
			case "shaped":
				ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
				shapedRecipe.shape(file.getStringList(section+".shape").toArray(new String[0]));
				for (String s : file.getConfigurationSection(section+".exact").getKeys(false)) {
					shapedRecipe.setIngredient(s.charAt(0), new RecipeChoice.ExactChoice(((List<ItemStack>) file.get(section+".exact."+s))));
				}
				for (String s : file.getConfigurationSection(section+".material").getKeys(false))
					shapedRecipe.setIngredient(s.charAt(0), new RecipeChoice.MaterialChoice(((List<String>) file.get(section+".material."+s)).stream().map(e -> Material.getMaterial(e)).collect(Collectors.toList())));
				return (T) shapedRecipe;
			case "shapeless":
				ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result);
				for (List<ItemStack> list : ((List<List<ItemStack>>) file.get(section+".exact")))
					shapelessRecipe.addIngredient(new RecipeChoice.ExactChoice(list));
				for (List<String> list : ((List<List<String>>) file.get(section+".material")))
					shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(list.stream().map(k -> Material.getMaterial(k)).collect(Collectors.toList())));
				return (T) shapelessRecipe;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Main.consoleSender.sendMessage(Utils.convertString(Utils.prefix+"&cERROR could not read recipe data from &e'"+section+"' &cplease fix this!"));
			return null;
		}
	}
}

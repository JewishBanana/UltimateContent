package com.github.jewishbanana.ultimatecontent.items;

import com.github.jewishbanana.ultimatecontent.utils.DataUtils;

public enum Rarity {
	
	COMMON,
	UNCOMMON,
	RARE,
	EPIC,
	LEGENDARY,
	MYTHIC;
	
	private String label;
	
	private Rarity() {
		this.label = DataUtils.getConfigString("language.rarity."+toString().toLowerCase());
	}
	public String getLabel() {
		return label;
	}
}

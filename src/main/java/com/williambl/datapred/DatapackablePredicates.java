package com.williambl.datapred;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatapackablePredicates implements ModInitializer {
	public static final String MODID = "datapackable-predicates";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
	}
}
package com.williambl.dfunc.impl;

import com.mojang.serialization.Codec;
import com.williambl.dfunc.api.DFunction;
import com.williambl.dfunc.api.functions.*;
import com.williambl.dfunc.impl.groovy.FunctionArithmetics;
import com.williambl.dfunc.impl.groovy.GroovyFunction;
import com.williambl.dfunc.impl.groovy.GroovyFunctionCompiler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataFunctionsMod implements ModInitializer {
	public static final String MODID = "dfunc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final Codec<EntityPredicate> ADVANCEMENT_ENTITY_PREDICATE_CODEC = ExtraCodecs.JSON.xmap(EntityPredicate::fromJson, EntityPredicate::serializeToJson);
	public static final Codec<BlockPredicate> ADVANCEMENT_BLOCK_PREDICATE_CODEC = ExtraCodecs.JSON.xmap(BlockPredicate::fromJson, BlockPredicate::serializeToJson);
	public static final Codec<ItemPredicate> ADVANCEMENT_ITEM_PREDICATE_CODEC = ExtraCodecs.JSON.xmap(ItemPredicate::fromJson, ItemPredicate::serializeToJson);

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MODID, path);
	}

	@Override
	public void onInitialize() {
		DPredicates.init();
		EntityDFunctions.init();
		BlockInWorldDFunctions.init();
		ItemStackDFunctions.init();
		NumberDFunctions.init();
		LevelDFunctions.init();
		DamageSourceDFunctions.init();

		final GroovyFunctionCompiler<Boolean> predicateCompiler = new GroovyFunctionCompiler<>(
				DFunction.PREDICATE,
				new FunctionArithmetics() {
					@Override
					public @Nullable GroovyFunction equalsTo(GroovyFunction function, Object other) {
						return other instanceof Number number ? GroovyFunction.of(
								"type", "dfunc:comparison",
								"a", function.parameters,
								"b", number,
								"comparison","=="
						) : null;
					}
				}
		);
		System.out.println(predicateCompiler.compile("dfunc.health() == 5").type());
		System.exit(0);
	}
}
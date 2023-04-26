package com.williambl.dfunc.predicate;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.williambl.dfunc.DFunction;
import com.williambl.dfunc.DFunctionType;
import com.williambl.dfunc.DPredicates;
import com.williambl.dfunc.DataFunctions;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.williambl.dfunc.DataFunctions.id;

public final class EntityDPredicates {
    public static final DFunctionType<Entity, Boolean, ? extends Function<Boolean, ? extends DFunction<Entity, Boolean>>> CONSTANT = DPredicates.constant(DFunction.ENTITY_PREDICATE_TYPE_REGISTRY);
    public static final DFunctionType<Entity, Boolean, ? extends Function<List<DFunction<Entity, Boolean>>, ? extends DFunction<Entity, Boolean>>> AND = DPredicates.and(DFunction.ENTITY_PREDICATE_TYPE_REGISTRY);
    public static final DFunctionType<Entity, Boolean, ? extends Function<List<DFunction<Entity, Boolean>>, ? extends DFunction<Entity, Boolean>>> OR = DPredicates.or(DFunction.ENTITY_PREDICATE_TYPE_REGISTRY);
    public static final DFunctionType<Entity, Boolean, ? extends Function<DFunction<Entity, Boolean>, ? extends DFunction<Entity, Boolean>>> NOT = DPredicates.not(DFunction.ENTITY_PREDICATE_TYPE_REGISTRY);

    public static final DFunctionType<Entity, Boolean, ? extends Function<DFunction<Level, Boolean>, ? extends DFunction<Entity, Boolean>>> LEVEL_PREDICATE = DPredicates.delegate(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY,
            DFunction.LEVEL_PREDICATE_TYPE_REGISTRY,
            Entity::getLevel);

    public static final DFunctionType<Entity, Boolean, ? extends BiFunction<DFunction<Entity, Double>, DFunction<Double, Boolean>, ? extends DFunction<Entity, Boolean>>> NUMBER_PREDICATE = DPredicates.delegateWithDFunction(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY,
            DFunction.NUMBER_PREDICATE_TYPE_REGISTRY,
            DFunction.ENTITY_TO_NUMBER_FUNCTION_TYPE_REGISTRY);

    public static final DFunctionType<Entity, Boolean, ? extends Function<EntityPredicate, ? extends DFunction<Entity, Boolean>>> ADVANCEMENT_PREDICATE = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("advancement_predicate"),
            DFunction.<EntityPredicate, Entity, Boolean>create(
                    DataFunctions.ADVANCEMENT_ENTITY_PREDICATE_CODEC.fieldOf("predicate"),
                    (predicate, e) -> e.level instanceof ServerLevel sLevel && predicate.matches(sLevel, null, e)));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> DEAD_OR_DYING = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("dead_or_dying"),
            DFunction.create(
                    e -> e instanceof LivingEntity l && l.isDeadOrDying()));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> ON_FIRE = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("on_fire"),
            DFunction.create(Entity::isOnFire));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> SNEAKING = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("sneaking"),
            DFunction.create(Entity::isShiftKeyDown));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> SPRINTING = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("sprinting"),
            DFunction.create(Entity::isSprinting));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> SWIMMING = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("swimming"),
            DFunction.create(Entity::isSwimming));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> FALL_FLYING = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("fall_flying"),
            DFunction.<Entity, Boolean>create(
                    e -> e instanceof LivingEntity l && l.isFallFlying()));

    public static final DFunctionType<Entity, Boolean, ? extends Function<TagKey<Fluid>, ? extends DFunction<Entity, Boolean>>> SUBMERGED_IN = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("submerged_in"),
            DFunction.<TagKey<Fluid>, Entity, Boolean>create(
                    TagKey.codec(Registries.FLUID).fieldOf("fluid"),
                    (f, e) -> e.isEyeInFluid(f)));

    public static final DFunctionType<Entity, Boolean, ? extends Function3<Attribute, DFunction<Double, Boolean>, Boolean, ? extends DFunction<Entity, Boolean>>> ATTRIBUTE_VALUE = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("attribute_value"),
            DFunction.<Attribute, DFunction<Double, Boolean>, Boolean, Entity, Boolean>create(
                    BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute"),
                    DFunction.NUMBER_PREDICATE_TYPE_REGISTRY.codec().fieldOf("predicate"),
                    Codec.BOOL.optionalFieldOf("relative_to_base", false),
                    (attr, predicate, relative, e) -> e instanceof LivingEntity l
                            && predicate.apply(relative ? l.getAttributeValue(attr) - l.getAttributeBaseValue(attr) : l.getAttributeValue(attr))));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> CAN_SEE_SKY = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("can_see_sky"),
            DFunction.<Entity, Boolean>create(e -> e.getLevel().canSeeSky(e.blockPosition())));

    public static final DFunctionType<Entity, Boolean, ? extends Supplier<? extends DFunction<Entity, Boolean>>> IS_SURVIVAL_LIKE = Registry.register(
            DFunction.ENTITY_PREDICATE_TYPE_REGISTRY.registry(),
            id("is_survival_like"),
            DFunction.<Entity, Boolean>create(e -> !e.isSpectator() && !(e instanceof Player p && p.isCreative())));

    public static void init() {}
}
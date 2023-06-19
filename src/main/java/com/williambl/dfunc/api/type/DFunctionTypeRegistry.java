package com.williambl.dfunc.api.type;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.williambl.dfunc.impl.DFunctionImplementations;
import com.williambl.dfunc.api.DFunction;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Holds a registry and a codec for DFunctionTypes.
 * @param registry  the registry
 * @param codec     the codec
 */
public record DFunctionTypeRegistry<R>(Registry<DFunctionType<R, ?>> registry, Codec<DFunction<R>> codec) {
    public static final Set<DFunctionTypeRegistry<?>> REGISTRIES = new HashSet<>();
    /**
     * Creates a new DFunctionTypeRegistry. If a codec is provided, then constant functions will be encoded with it as
     * just their value.
     * @param name      the resource location name of the registry
     * @param rCodec    the codec for the result type of the DFunctions, or null
     * @return          the new DFunctionTypeRegistry
     */
    public static <R> DFunctionTypeRegistry<R> createRegistry(ResourceLocation name, @Nullable Codec<R> rCodec) {
        ResourceKey<Registry<DFunctionType<R, ?>>> key = ResourceKey.createRegistryKey(name);
        Registry<DFunctionType<R, ?>> registry = FabricRegistryBuilder.createSimple(key).buildAndRegister();
        Codec<DFunction<R>> dispatchCodec = registry.byNameCodec().dispatch(
                DFunction::type,
                DFunctionType::codec
        );

        Codec<DFunction<R>> codec = rCodec == null ? dispatchCodec : Codec.either(dispatchCodec, rCodec)
                .xmap(e -> e.map(Function.identity(), r -> DFunction.create(rCodec).factory().apply(r)),
                        df -> df instanceof DFunctionImplementations.ConstantDFunction<R> constantDFunction
                                ? Either.right(constantDFunction.apply(null))
                                : Either.left(df));

        final var reg = new DFunctionTypeRegistry<>(registry, codec);
        REGISTRIES.add(reg);
        return reg;
    }

    /**
     * Creates a new DFunctionTypeRegistry, with no codec for the result type.
     * @param name  the resource location name of the registry
     * @return      the new DFunctionTypeRegistry
     * @see #createRegistry(ResourceLocation, Codec)
     */
    static <R> DFunctionTypeRegistry<R> createRegistry(ResourceLocation name) {
        return createRegistry(name, null);
    }
}

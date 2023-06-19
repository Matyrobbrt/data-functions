package com.williambl.dfunc.impl.groovy;

import com.williambl.dfunc.api.DFunction;
import com.williambl.dfunc.api.type.DFunctionTypeRegistry;

import java.util.HashMap;
import java.util.Map;

public class GroovyFunction {
    public final Map<String, Object> parameters;

    public GroovyFunction(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public static GroovyFunction of(Object... args) {
        if (args.length % 2 != 0) throw new IllegalArgumentException();
        final var map = new HashMap<String, Object>();
        for (int i = 0; i < args.length; i += 2) {
            map.put((String) args[i], args[i + 1]);
        }
        return new GroovyFunction(map);
    }

    public <R> DFunction<R> decode(DFunctionTypeRegistry<R> registry) {
        return registry.codec().decode(ObjectOps.INSTANCE, parameters).get().orThrow().getFirst();
    }
}

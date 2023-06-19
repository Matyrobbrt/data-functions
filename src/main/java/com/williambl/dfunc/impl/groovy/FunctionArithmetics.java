package com.williambl.dfunc.impl.groovy;

import org.jetbrains.annotations.Nullable;

public interface FunctionArithmetics {
    @Nullable
    default GroovyFunction equalsTo(GroovyFunction function, Object other) {
        return null;
    }

    @Nullable
    default GroovyFunction lessThan(GroovyFunction function, Object other, boolean equal) {
        return null;
    }

    @Nullable
    default GroovyFunction greaterThan(GroovyFunction function, Object other, boolean equal) {
        return null;
    }
}

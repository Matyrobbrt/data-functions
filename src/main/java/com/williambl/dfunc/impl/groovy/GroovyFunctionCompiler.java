package com.williambl.dfunc.impl.groovy;

import com.williambl.dfunc.api.DFunction;
import com.williambl.dfunc.api.type.DFunctionTypeRegistry;
import groovy.lang.Binding;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class GroovyFunctionCompiler<R> {
    private final GroovyShell shell = new GroovyShell(new Binding(Map.of("compiler", this)) {
        @Override
        public Object getVariable(String name) {
            final Object ours = GroovyFunctionCompiler.this.getVariable(name);
            return ours == null ? super.getVariable(name) : ours;
        }
    }, new CompilerConfiguration()
            .addCompilationCustomizers(new ASTTransformationCustomizer(ArithmeticTransformation.class)));
    private final DFunctionTypeRegistry<R> registry;
    private final FunctionArithmetics arithmetics;

    public GroovyFunctionCompiler(DFunctionTypeRegistry<R> registry, FunctionArithmetics arithmetics) {
        this.registry = registry;
        this.arithmetics = arithmetics;
    }

    public DFunction<R> compile(String script) {
        return ((GroovyFunction) shell.evaluate(script)).decode(registry);
    }

    public Object redirectEquals(Object a, Object b) {
        System.out.println("Equals was called between " + a + " and " + b);
        if (a instanceof GroovyFunction gFunc) {
            final GroovyFunction result = arithmetics.equalsTo(gFunc, b);
            return result == null ? ScriptBytecodeAdapter.compareEqual(a, b) : result;
        } else if (b instanceof GroovyFunction gFunc) {
            final GroovyFunction result = arithmetics.equalsTo(gFunc, a);
            return result == null ? ScriptBytecodeAdapter.compareEqual(a, b) : result;
        }
        return ScriptBytecodeAdapter.compareEqual(a, b);
    }

    public Object redirectLessThan(Object a, Object b, boolean equal) {
        if (a instanceof GroovyFunction function) {
            final GroovyFunction result = arithmetics.lessThan(function, b, equal);
            return result == null ? (equal ? ScriptBytecodeAdapter.compareLessThanEqual(a, b) : ScriptBytecodeAdapter.compareLessThan(a, b)) : result;
        } else if (b instanceof GroovyFunction function) {
            final GroovyFunction result = arithmetics.greaterThan(function, b, equal);
            return result == null ? (equal ? ScriptBytecodeAdapter.compareLessThanEqual(a, b) : ScriptBytecodeAdapter.compareLessThan(a, b)) : result;
        }
        return equal ? ScriptBytecodeAdapter.compareLessThanEqual(a, b) : ScriptBytecodeAdapter.compareLessThan(a, b);
    }

    public Object redirectGreaterThan(Object a, Object b, boolean equal) {
        if (a instanceof GroovyFunction function) {
            final GroovyFunction result = arithmetics.greaterThan(function, b, equal);
            return result == null ? (equal ? ScriptBytecodeAdapter.compareGreaterThanEqual(a, b) : ScriptBytecodeAdapter.compareGreaterThan(a, b)) : result;
        } else if (b instanceof GroovyFunction function) {
            final GroovyFunction result = arithmetics.lessThan(function, b, equal);
            return result == null ? (equal ? ScriptBytecodeAdapter.compareGreaterThanEqual(a, b) : ScriptBytecodeAdapter.compareGreaterThan(a, b)) : result;
        }
        return equal ? ScriptBytecodeAdapter.compareGreaterThanEqual(a, b) : ScriptBytecodeAdapter.compareGreaterThan(a, b);
    }

    private final Callable<GroovyFunction> defaultFuncCreate = funcCreate(HashMap::new)::get;
    private final Map<String, Object> byNamespaceTyped = new HashMap<>();
    public Object getVariable(String name) {
        if (name.equals("compiler")) {
            return this;
        }
        if (name.equals("function")) {
            return defaultFuncCreate;
        }
        return byNamespaceTyped.computeIfAbsent(name, $ -> {
            final GroovyProxy proxy = new GroovyProxy();
            collectFrom(name, registry.registry(), proxy.properties::putIfAbsent);
            DFunctionTypeRegistry.REGISTRIES.stream()
                    .filter(d -> !d.equals(registry))
                    .forEach(reg -> collectFrom(name, reg.registry(), proxy.properties::putIfAbsent));
            return proxy.properties.isEmpty() ? null : proxy;
        });
    }

    private void collectFrom(String namespace, Registry<?> registry, BiConsumer<String, Supplier<GroovyFunction>> callable) {
        registry.holders()
                .filter(ref -> ref.key().location().getNamespace().equals(namespace))
                .forEach(holder -> {
                    final String type = holder.key().location().getPath();
                    callable.accept(type, funcCreate(() -> Util.make(new HashMap<>(), map -> map.put("type", type))));
                });
    }

    public static Supplier<GroovyFunction> funcCreate(Supplier<Map<String, Object>> argsGetter) {
        return () -> new GroovyFunction(argsGetter.get());
    }

    @GroovyASTTransformationClass("com.williambl.dfunc.impl.groovy.ArithmeticCustomizer")
    public @interface ArithmeticTransformation {

    }

    public static final class GroovyProxy implements GroovyObject {
        private final Map<String, Supplier<GroovyFunction>> properties = new HashMap<>();
        private MetaClass metaClass;
        {
            final MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
            MetaClassRegistry.MetaClassCreationHandle mccHandle = metaClassRegistry.getMetaClassCreationHandler();
            metaClass = mccHandle.create(GroovyProxy.class, metaClassRegistry);
        }

        @Override
        public Object invokeMethod(String name, Object args) {
            final Supplier<GroovyFunction> sup = properties.get(name);
            if (sup != null) {
                final GroovyFunction function = sup.get();
                final Object[] theArgs = (Object[]) args;
                if (theArgs.length == 1) {
                    function.parameters.putAll((Map<String, ?>) theArgs[0]);
                }
                return function;
            }
            return GroovyObject.super.invokeMethod(name, args);
        }

        @Override
        public MetaClass getMetaClass() {
            return metaClass;
        }

        @Override
        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }
    }
}

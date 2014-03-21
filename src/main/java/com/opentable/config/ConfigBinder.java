package com.opentable.config;

import com.google.inject.Binder;
import com.google.inject.Scopes;

/**
 * Helper class for binding Configuration beans in Guice modules.
 */
public class ConfigBinder
{
    /**
     * Create a {@code ConfigBinder} with the given {@link Binder}.
     */
    public static ConfigBinder of(Binder binder)
    {
        return new ConfigBinder(binder);
    }

    private final Binder binder;

    private ConfigBinder(Binder binder)
    {
        this.binder = binder;
    }

    /**
     * Create default bindings for the given beans.
     */
    @SafeVarargs
    public final <T> ConfigBinder bind(Class<T>... configBeanClasses)
    {
        for (Class<T> klass : configBeanClasses) {
            binder.bind(klass).toProvider(ConfigProvider.of(klass)).in(Scopes.SINGLETON);
        }
        return this;
    }
}

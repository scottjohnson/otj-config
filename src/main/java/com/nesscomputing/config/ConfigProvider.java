/**
 * Copyright (C) 2012 Ness Computing, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nesscomputing.config;


import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import javax.annotation.Nullable;

/**
 * Provides an arbitrary configuration bean which is configured from the main Config object.
 *
 * @param <T> The type of the configuration bean.
 */
public class ConfigProvider<T> implements Provider<T>
{
    private final String prefix;
    private final Class<T> clazz;
    private final Map<String, String> overrides;

    private Config config = null;

    /**
     * Returns a Provider for a configuration bean. This method should be used in Modules
     * that require access to configuration.
     * @param <TYPE> The type of the Configuration bean.
     * @param clazz The class of the Configuration bean.
     * @return A provider.
     */
    public static final <TYPE> Provider<TYPE> of(final Class<TYPE> clazz)
    {
        return new ConfigProvider<TYPE>(null, clazz, null);
    }

    /**
     * Returns a Provider for a configuration bean. This method should be used in Modules
     * that require access to configuration.
     * @param <TYPE> The type of the Configuration bean.
     * @param prefix The Config bean prefix, as referenced below (may be null)
     * @param clazz The class of the Configuration bean.
     * @return A provider.
     * @see Config#getBean(String, Class)
     */
    public static final <TYPE> Provider<TYPE> of(@Nullable final String prefix, final Class<TYPE> clazz)
    {
        return new ConfigProvider<TYPE>(prefix, clazz, null);
    }

    /**
     * Returns a Provider for a configuration bean. This method should be used in Modules
     * that require access to configuration.
     * @param <TYPE> The type of the Configuration bean.
     * @param prefix The Config bean prefix, as referenced below (may be null)
     * @param clazz The class of the Configuration bean.
     * @return A provider.
     * @see Config#getBean(String, Class)
     */
    public static final <TYPE> Provider<TYPE> of(@Nullable final String prefix, final Class<TYPE> clazz,
            @Nullable final Map<String, String> overrides)
    {
        return new ConfigProvider<TYPE>(prefix, clazz, overrides);
    }

    private ConfigProvider(final String prefix, final Class<T> clazz, final Map<String, String> overrides)
    {
        this.prefix = prefix;
        this.clazz = clazz;
        this.overrides = overrides;
    }

    @Inject
    public void setInjector(final Injector injector)
    {
        this.config = injector.getInstance(Config.class);
    }

    @Override
    public T get()
    {
        return config.getBean(prefix, clazz, overrides);
    }
}

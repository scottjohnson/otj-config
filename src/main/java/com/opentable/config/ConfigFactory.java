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
package com.opentable.config;

import java.net.URI;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opentable.config.util.ClasspathConfigStrategy;
import com.opentable.config.util.ConfigStrategy;
import com.opentable.config.util.FileConfigStrategy;
import com.opentable.config.util.HttpConfigStrategy;

class ConfigFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(ConfigFactory.class);

    private static final Map<String, ? extends ConfigStrategyProvider> STRATEGY_PROVIDERS;

    private interface ConfigStrategyProvider
    {
        ConfigStrategy getStrategy(URI configLocation);
    }

    static {
        STRATEGY_PROVIDERS = ImmutableMap.of(
            "classpath", ClasspathConfigStrategy::new,
            "file", FileConfigStrategy::new,
            "http", HttpConfigStrategy::new,
            "https", HttpConfigStrategy::new
        );
    }

    private final String configName;
    private final URI configLocation;
    private final ConfigStrategy configStrategy;

    ConfigFactory(@Nonnull final URI configLocation, @Nullable final String configName)
    {
        this.configLocation = configLocation;
        this.configName = Objects.firstNonNull(configName, "default");
        this.configStrategy = selectConfigStrategy(configLocation);
    }

    CombinedConfiguration load()
    {
        LOG.info("Begin loading configuration '{}' from '{}'", configName, configLocation);
        try {
            if (configName.contains(",")) {
                return loadOTStrategy();
            } else {
                return loadNessStrategy();
            }
        } catch (ConfigurationException e) {
            throw Throwables.propagate(e);
        }
    }

    private CombinedConfiguration loadOTStrategy() throws ConfigurationException
    {
        final String [] configPaths = StringUtils.stripAll(StringUtils.split(configName, ","));
        final CombinedConfiguration cc = new CombinedConfiguration(new OverrideCombiner());

        // All properties can be overridden by the System properties.
        cc.addConfiguration(new SystemConfiguration(), "systemProperties");
        LOG.info("Configuration source: SYSTEM");

        for (int i = configPaths.length-1; i >= 0; i--) {
            final String configPath = configPaths[i];
            final AbstractConfiguration subConfig = configStrategy.load(configPath, configPath);
            if (subConfig == null) {
                throw new IllegalStateException(String.format("Configuration '%s' does not exist!", configPath));
            }
            cc.addConfiguration(subConfig, configPath);
            LOG.info("New-style configuration source: {}", configPath);
        }

        return cc;
    }

    private CombinedConfiguration loadNessStrategy()
    {
        // Allow foo/bar/baz and foo:bar:baz
        final String [] configNames = StringUtils.stripAll(StringUtils.split(configName, "/:"));

        final CombinedConfiguration cc = new CombinedConfiguration(new OverrideCombiner());

        // All properties can be overridden by the System properties.
        cc.addConfiguration(new SystemConfiguration(), "systemProperties");
        LOG.info("Configuration source: SYSTEM");

        boolean loadedConfig = false;
        for (int i = 0; i < configNames.length; i++) {
            final String configFileName = configNames[configNames.length - i - 1];
            final String configFilePath = StringUtils.join(configNames, "/", 0, configNames.length - i);

            try {
                final AbstractConfiguration subConfig = configStrategy.load(configFileName, configFilePath);
                if (subConfig == null) {
                    throw new IllegalStateException(String.format("Configuration '%s' does not exist!", configFileName));
                }
                else {
                    cc.addConfiguration(subConfig, configFileName);
                    LOG.info("Configuration source: {}", configFileName);
                    loadedConfig = true;
                }
            } catch (ConfigurationException ce) {
                LOG.error(String.format("While loading configuration '%s'", configFileName), ce);
            }
        }

        if (!loadedConfig && configNames.length > 0) {
            throw new IllegalStateException(String.format(
                "Config name '%s' was given but no config file could be found, this looks fishy!", configName));
        }

        return cc;
    }

    private ConfigStrategy selectConfigStrategy(final URI configLocation)
    {
        final ConfigStrategyProvider configStrategyProvider = STRATEGY_PROVIDERS.get(configLocation.getScheme());
        if (configStrategyProvider == null) {
            throw new IllegalStateException("No strategy for " + configLocation + " available!");
        }

        return configStrategyProvider.getStrategy(configLocation);
    }
}

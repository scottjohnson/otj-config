/*
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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.concurrent.GuardedBy;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opentable.lifecycle.LifecycleStage;
import com.opentable.lifecycle.guice.OnStage;

/**
 * Export Config objects and all ConfigMagic beans in JMX, if the MBeanServer
 * is bound in the Guice injector.
 */
@Singleton
class ConfigJmxExporter implements Closeable
{
    private static final Logger LOG = LoggerFactory.getLogger(ConfigJmxExporter.class);
    private static final String ROOT = ConfigJmxExporter.class.getPackage().getName();

    private final Config config;

    @GuardedBy("this")
    private MBeanServer server;

    @GuardedBy("this")
    private final List<Entry<? extends Class<?>, Object>> delayedBeanExports = new ArrayList<Entry<? extends Class<?>, Object>>();

    @GuardedBy("this")
    private final Set<ObjectName> currentExports = Sets.newHashSet();

    @Inject
    ConfigJmxExporter(Config config)
    {
        this.config = config;
    }

    @Inject(optional=true)
    synchronized void setMBeanServer(MBeanServer server)
    {
        Preconditions.checkArgument(server != null, "null MBeanServer");

        if (this.server != server) {
            close();
        }

        this.server = server;

        try {
            exportConfig();
        } catch (JMException e) {
            LOG.error("Unable to export configuration tree to JMX", e);
        }

        for (Entry<? extends Class<?>, Object> e : delayedBeanExports) {
            export(e.getKey(), e.getValue());
        }
    }

    private void exportConfig() throws JMException
    {
        server.registerMBean(new ConfigDynamicMBean("com.opentable.config.Config", config), new ObjectName(ROOT + ":config=ROOT"));
    }

    synchronized void export(Class<?> realClass, Object configBean)
    {
        MBeanServer server = this.server;
        if (server == null)
        {
            delayedBeanExports.add(Maps.immutableEntry(realClass, configBean));
            return;
        }

        ObjectName objectName;
        try {
            objectName = new ObjectName(munge(realClass.getName()));
        } catch (MalformedObjectNameException e) {
            throw new UnsupportedOperationException("bad munged name for " + realClass, e);
        }

        if (!currentExports.add(objectName)) {
            return; // Already exported
        }

        try {
            server.registerMBean(new ConfigMagicDynamicMBean(realClass.getName(), configBean),
                    objectName);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            throw new RuntimeException("Unable to export bean for " + realClass, e);
        }
    }

    @OnStage(LifecycleStage.STOP)
    @Override
    public synchronized void close() {
        for (ObjectName name : currentExports) {
            try {
                server.unregisterMBean(name);
            } catch (MBeanRegistrationException e) {
                LOG.warn("Unable to unregister {}", name, e);
            } catch (InstanceNotFoundException e) {
                LOG.trace("Not unregistering {}", name, e);
            }
        }
        currentExports.clear();
        LOG.info("Unexported all logging MBeans.");
    }

    private String munge(String name)
    {
        int i = 0;
        StringBuilder result = new StringBuilder(ROOT);
        result.append(':');
        for (String part : Splitter.on('.').split(name))
        {
            if (i > 0)
            {
                result.append(',');
            }

            result.append('n');
            result.append(i++);
            result.append('=');
            result.append(part);
        }
        return result.toString();
    }
}

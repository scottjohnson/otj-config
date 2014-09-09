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

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.apache.commons.configuration.AbstractConfiguration;
import org.junit.Test;

public class TestNewstyleConfig
{
    @Test
    public void testNewstyle() throws Exception
    {
        final URL path = TestNewstyleConfig.class.getResource("/newstyle-config");
        final Config config = Config.getConfig(path.toString(), "common,common/test,app,app/test");
        final AbstractConfiguration props = config.getConfiguration();

        assertEquals("a", props.getString("prop1"));
        assertEquals("b", props.getString("prop2"));
        assertEquals("c", props.getString("prop3"));
        assertEquals("d", props.getString("prop4"));
    }
}

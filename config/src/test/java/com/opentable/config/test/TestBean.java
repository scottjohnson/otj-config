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
package com.opentable.config.test;

import java.net.URI;

import org.skife.config.Config;
import org.skife.config.Default;

public abstract class TestBean
{
    @Config("string-value")
    public abstract String getStringValue();

    @Config("int-value")
    @Default("0")
    public abstract int getIntValue();

    @Config("boolean-value")
    @Default("false")
    public abstract boolean getBooleanValue();

    @Config("uri-value")
    public abstract String internalUriValue();

    public URI getUriValue()
    {
        return URI.create(internalUriValue());
    }
}

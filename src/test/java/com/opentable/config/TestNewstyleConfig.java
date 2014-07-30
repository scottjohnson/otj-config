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


[![Build Status](https://travis-ci.org/opentable/otj-config.svg)](https://travis-ci.org/opentable/otj-config)

OpenTable Configuration Component
=================================

Component Charter
-----------------

The `otj-config` component ties together a few configuration libraries.

[commons-configuration](http://commons.apache.org/proper/commons-configuration/) is used to load configuration files from files, HTTP, or the classpath.

[config-magic](https://github.com/brianm/config-magic) takes the final configuration built from `commons-configuration` and allows easy mapping on to Java objects.

`otj-config` then provides helpers to bind configuration objects into a Guice `Injector`.

Code Example
------------

```java
public interface DemoConfig
{
    @Config("ot.demo.greeting")
    @Default("Hello, world!")
    String getGreeting();
}

public class DemoConfigModule extends AbstractModule
{
    @Override
    public void configure()
    {
        // Bind a synthesized instance of DemoConfig
        ConfigBinder.of(binder()).bind(DemoConfig.class);
    }
}

public class DemoConfigUser
{
    private final DemoConfig config;

    @Inject
    DemoConfigUser(DemoConfig config)
    {
        this.config = config;
    }

    public void getGreeting()
    {
        return config.getGreeting();
    }
}
```

Component Level
---------------

*Foundation component*

The configuration component is pulled in at a very early and delicate stage of service bootstrapping.  It is crucial that the dependency footprint is minimal.

----
Copyright (C) 2014 OpenTable, Inc.

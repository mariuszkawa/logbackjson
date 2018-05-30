package com.codigeria.logbackjson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

final class LogbackJsonApp
{
    public static void main(String[] args)
    {
        new LogbackJsonApp().run(args);
    }

    private void run(String[] args)
    {
        logger.info("You have provided the following properties: numberOfArgs={} customMessage=\"Hello, World!\"",
                args.length);
    }

    private final Logger logger;

    private LogbackJsonApp()
    {
        logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }

    LogbackJsonApp(Logger logger)
    {
        this.logger = logger;
    }
}

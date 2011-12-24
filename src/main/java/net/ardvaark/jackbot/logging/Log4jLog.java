package net.ardvaark.jackbot.logging;

import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

class Log4jLog extends LogBase
{
    private Logger realLogger;
    
    static Log getLoggerImpl(Class<?> clazz)
    {
        return new Log4jLog(LogManager.getLogger(clazz));
    }
    
    static void initLoggingImpl()
    {
        URL url = Log4jLog.class.getResource("log4j.properties");
        PropertyConfigurator.configure(url);
    }
    
    static void configureLoggingImpl(String logLevel)
    {
        Level newLevel = null;

        if (logLevel.length() == 0)
        {
            newLevel = Level.INFO;
        }
        else
        {
            newLevel = Level.toLevel(logLevel.toUpperCase(), Level.INFO);
        }

        String packageName = "net.ardvaark.jackbot";
        Logger.getLogger(packageName).setLevel(newLevel);
    }
    
    private Log4jLog(Logger logger)
    {
        this.realLogger = logger;
    }
    
    @Override
    public boolean isFatalEnabled()
    {
        return this.realLogger.isEnabledFor(Level.FATAL);
    }
    
    @Override
    public void fatal(Object msg)
    {
        this.realLogger.fatal(msg);
    }
    
    @Override
    public void fatal(Object msg, Throwable e)
    {
        this.realLogger.fatal(msg, e);
    }

    @Override
    public boolean isErrorEnabled()
    {
        return this.realLogger.isEnabledFor(Level.ERROR);
    }
    
    @Override
    public void error(Object msg)
    {
        this.realLogger.error(msg);
    }
    
    @Override
    public void error(Object msg, Throwable e)
    {
        this.realLogger.error(msg, e);
    }

    @Override
    public boolean isWarnEnabled()
    {
        return this.realLogger.isEnabledFor(Level.WARN);
    }
    
    @Override
    public void warn(Object msg)
    {
        this.realLogger.warn(msg);
    }
    
    @Override
    public void warn(Object msg, Throwable e)
    {
        this.realLogger.warn(msg, e);
    }

    @Override
    public boolean isInfoEnabled()
    {
        return this.realLogger.isEnabledFor(Level.INFO);
    }
    
    @Override
    public void info(Object msg)
    {
        this.realLogger.info(msg);
    }
    
    @Override
    public void info(Object msg, Throwable e)
    {
        this.realLogger.info(msg, e);
    }

    @Override
    public boolean isTraceEnabled()
    {
        return this.realLogger.isEnabledFor(Level.TRACE);
    }
    
    @Override
    public void trace(Object msg)
    {
        this.realLogger.trace(msg);
    }
    
    @Override
    public void trace(Object msg, Throwable e)
    {
        this.realLogger.trace(msg, e);
    }
}

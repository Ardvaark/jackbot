package net.ardvaark.jackbot.logging;

public abstract class Log
{
    public static Log getLogger(Class<?> clazz)
    {
        return Log4jLog.getLoggerImpl(clazz);
    }
    
    public static void initLogging()
    {
        Log4jLog.initLoggingImpl();
    }
    
    public static void configureLogging(String logLevel)
    {
        Log4jLog.configureLoggingImpl(logLevel);
    }
    
    public abstract boolean isFatalEnabled();
    public abstract void fatal(Object msg);
    public abstract void fatal(Object msg, Object...args);
    public abstract void fatal(Object msg, Throwable e);
    public abstract void fatal(Object msg, Throwable e, Object...args);    
    
    public abstract boolean isErrorEnabled();
    public abstract void error(Object msg);
    public abstract void error(Object msg, Object...args);
    public abstract void error(Object msg, Throwable e);
    public abstract void error(Object msg, Throwable e, Object...args);    

    public abstract boolean isWarnEnabled();
    public abstract void warn(Object msg);
    public abstract void warn(Object msg, Object...args);
    public abstract void warn(Object msg, Throwable e);
    public abstract void warn(Object msg, Throwable e, Object...args);    

    public abstract boolean isInfoEnabled();
    public abstract void info(Object msg);
    public abstract void info(Object msg, Object...args);
    public abstract void info(Object msg, Throwable e);
    public abstract void info(Object msg, Throwable e, Object...args);

    public abstract boolean isTraceEnabled();
    public abstract void trace(Object msg);
    public abstract void trace(Object msg, Object...args);
    public abstract void trace(Object msg, Throwable e);
    public abstract void trace(Object msg, Throwable e, Object...args);    
}

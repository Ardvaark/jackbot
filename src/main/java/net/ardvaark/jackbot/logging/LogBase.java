package net.ardvaark.jackbot.logging;

import java.text.MessageFormat;

public abstract class LogBase extends Log
{
    @Override
    public void fatal(Object msg, Object... args)
    {
        if (this.isFatalEnabled())
        {
            this.fatal(format(msg, args));
        }
    }

    @Override
    public void fatal(Object msg, Throwable e, Object... args)
    {
        if (this.isFatalEnabled())
        {
            this.fatal(format(msg, args), e);
        }
    }

    @Override
    public void error(Object msg, Object... args)
    {
        if (this.isErrorEnabled())
        {
            this.error(format(msg, args));
        }
    }

    @Override
    public void error(Object msg, Throwable e, Object... args)
    {
        if (this.isErrorEnabled())
        {
            this.error(format(msg, args), e);
        }
    }

    @Override
    public void warn(Object msg, Object... args)
    {
        if (this.isWarnEnabled())
        {
            this.warn(format(msg, args));
        }
    }

    @Override
    public void warn(Object msg, Throwable e, Object... args)
    {
        if (this.isWarnEnabled())
        {
            this.warn(format(msg, args), e);
        }
    }

    @Override
    public void info(Object msg, Object... args)
    {
        if (this.isInfoEnabled())
        {
            this.info(format(msg, args));
        }
    }

    @Override
    public void info(Object msg, Throwable e, Object... args)
    {
        if (this.isInfoEnabled())
        {
            this.info(format(msg, args), e);
        }
    }

    @Override
    public void trace(Object msg, Object... args)
    {
        if (this.isTraceEnabled())
        {
            this.trace(format(msg, args));
        }
    }

    @Override
    public void trace(Object msg, Throwable e, Object... args)
    {
        if (this.isTraceEnabled())
        {
            this.trace(format(msg, args), e);
        }
    }

    private Object format(Object msg, Object[] args)
    {
        if (args.length > 0)
        {
            msg = MessageFormat.format(msg.toString(), args);
        }

        return msg;
    }
}

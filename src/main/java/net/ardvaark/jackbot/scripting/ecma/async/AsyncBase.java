package net.ardvaark.jackbot.scripting.ecma.async;

import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ecma.ECMAEngine;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AsyncBase
{
    private static final Log log = Log.getLogger(AsyncBase.class);
    private ECMAEngine engine;
    
    protected AsyncBase(ECMAEngine engine)
    {
        if (engine == null)
        {
            throw new NullPointerException();
        }
        
        this.engine = engine;
    }
    
    protected Runnable wrap(final Runnable code)
    {
        return new Runnable() { public void run() {
            enterContext();
            
            try
            {
                code.run();
            }
            catch (RuntimeException e)
            {
                log.error("{0}: Caught unexpected RuntimeException.", e, this.getClass().getName());
                throw e;
            }
            finally
            {
                Context.exit();
            }
        }};
    }
    
    private Context enterContext()
    {
        Context cx = ContextFactory.getGlobal().enterContext();
        cx.putThreadLocal(ECMAEngine.class, this.engine);
        
        return cx;
    }
    
    protected static void cleanupExecutor(ExecutorService service)
    {
        service.shutdown();
        
        try
        {
            if (!service.awaitTermination(5, TimeUnit.SECONDS))
            {
                service.shutdownNow();
                service.awaitTermination(5, TimeUnit.SECONDS);
            }
        }
        catch (InterruptedException e)
        {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public abstract void destroy();
}

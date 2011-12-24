package net.ardvaark.jackbot.scripting.ecma.async;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ecma.ECMAEngine;

public class TimeoutScheduler extends AsyncBase
{
    private static final Log log = Log.getLogger(TimeoutScheduler.class);
    private ScheduledExecutorService executor;
    private Map<Object, ScheduledFuture<?>> scheduledTasks = new HashMap<Object, ScheduledFuture<?>>();
    
    public TimeoutScheduler(ECMAEngine engine)
    {
        super(engine);
        
        this.executor = Executors.newScheduledThreadPool(1);
    }
    
    @Override
    public void destroy()
    {
        cleanupExecutor(executor);
    }

    public void cancelTimeout(Object key)
    {
        if (key == null)
        {
            throw new NullPointerException();
        }
        
        synchronized (scheduledTasks)
        {
            if (scheduledTasks.containsKey(key))
            {
                ScheduledFuture<?> future = scheduledTasks.remove(key);
                
                if (!future.isCancelled() && !future.isDone())
                {
                    future.cancel(false);
                }
            }
        }
    }
    
    public Object scheduleTimeout(final Object key, final Runnable code, long millis)
    {
        if (key == null || code == null)
        {
            throw new NullPointerException();
        }
        
        Runnable safeCode = wrap(new Runnable(){
            public void run()
            {
                boolean shouldExecute = false;
                
                synchronized(scheduledTasks)
                {
                    if (scheduledTasks.containsKey(key))
                    {
                        ScheduledFuture<?> future = scheduledTasks.remove(key);
                        
                        if (!future.isCancelled() && !future.isDone())
                        {
                            shouldExecute = true;
                        }
                    }
                }
                
                if (shouldExecute)
                {
                    code.run();
                }
            }
        });
        
        synchronized (scheduledTasks)
        {
            if (scheduledTasks.containsKey(key))
            {
                throw new IllegalArgumentException(MessageFormat.format("Timeout already registered: {0}", key));
            }
            
            log.trace("Scheduling timeout in {1} ms for key: {0}", key, millis);
            ScheduledFuture<?> future = this.executor.schedule(safeCode, millis, TimeUnit.MILLISECONDS);

            scheduledTasks.put(key, future);
        }
        
        return key;
    }
}

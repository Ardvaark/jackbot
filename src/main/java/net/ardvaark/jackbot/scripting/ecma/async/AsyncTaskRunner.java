package net.ardvaark.jackbot.scripting.ecma.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ecma.ECMAEngine;

public class AsyncTaskRunner extends AsyncBase
{
    private static final Log log = Log.getLogger(AsyncTaskRunner.class);
    private ExecutorService pool = Executors.newCachedThreadPool();
    
    public AsyncTaskRunner(ECMAEngine engine)
    {
        super(engine);
    }
    
    @Override
    public void destroy()
    {
        cleanupExecutor(pool);
    }
    
    public void run(final Runnable code)
    {
        pool.execute(wrap(new Runnable() { public void run() {
            log.trace("Running task: " + code);
            code.run();
            }
        }));
    }
}

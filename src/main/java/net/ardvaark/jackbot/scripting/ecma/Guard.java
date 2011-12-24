package net.ardvaark.jackbot.scripting.ecma;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import net.ardvaark.jackbot.scripting.ScriptException;

class Guard
{
    private Semaphore lock = new Semaphore(1);
    
    public void acquire() throws ScriptException
    {
        try
        {
            this.lock.tryAcquire(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            throw new ScriptException(e);
        }
    }
    
    public void release() throws ScriptException
    {
        this.lock.release();
    }
}

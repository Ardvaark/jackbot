/*
 * This file is part of JackBot IRC Bot (JackBot).
 * 
 * JackBot is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * JackBot is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * JackBot; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.ardvaark.jackbot.scripting.ecma;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.mozilla.javascript.Context;

import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ScriptException;

/**
 * A native executed process on the JackBot host system.  Execution does not
 * begin until the {@link #jsFunction_start() start} function is called.
 * Exposes the {@link #HANDLER_ON_LINE onLine} event and the
 * {@link #HANDLER_ON_ERROR_LINE onErrorLine} event.  After all lines have been
 * read from <tt>stdout</tt> and <tt>stderr</tt>, the handlers will be called
 * one final time with <tt>null</tt>.
 * 
 * <p>Example:
 * <pre>
 * var exec = new Exec("cmd").arg("/c").arg("dir");
 * exec.onLine = function(line) { if (line != null) cmd.respond(line); };
 * exec.start();
 * </pre>
 * </p>
 * 
 * @version $Revision: 82 $ $Date: 2009-03-23 11:08:39 -0400 (Mon, 23 Mar 2009) $
 * @see #jsFunction_start()
 * @see #jsFunction_arg(String)
 */
public class Exec extends HostObject
{
    private static final Log log = Log.getLogger(Exec.class);
    private static final long  serialVersionUID = 1L;
    
    /**
     * The name of the handler to be fired for the <tt>onLine</tt>
     * event.  This event is fired whenever a line is read from standard
     * output.  After the last line is read, the handler is culled with
     * <tt>null</tt>.
     */
    public static final String HANDLER_ON_LINE = "onLine";

    /**
     * The name of the handler to be fired for the <tt>onErrorLine</tt>
     * event.  This event is fired whenever a line is read from standard
     * error.
     */
    public static final String HANDLER_ON_ERROR_LINE = "onErrorLine";
    
    /**
     * The name of the handler to be fire for the <tt>onExecComplete</tt>
     * event.  This event is fired when the execution of the process is
     * completed, and all data has been pumped from its output streams.
     */
    public static final String HANDLER_ON_EXEC_COMPLETE = "onExecComplete";

    /**
     * The ECMAScript class name for this class. This is the name by which the
     * script will refer to the class.
     */
    public static final String ECMA_CLASS_NAME  = "Exec";

    /**
     * Default constructor.
     */
    public Exec()
    {
        ECMAEngine engine = (ECMAEngine)Context.getCurrentContext().getThreadLocal(ECMAEngine.class);
        this.setEngine(engine);
    }

    /**
     * Gets the ECMAScript class name for this object.
     * 
     * @return The ECMAScript class name for this object.
     */
    @Override
    public String getClassName()
    {
        return Exec.ECMA_CLASS_NAME;
    }

    /**
     * JavaScript constructor. This constructor takes a command-line and
     * executes it as a child process on the system on which JackBot is running.
     * If any errors occur during construction, this constructor will kill the
     * child process if (it has been created), and will write a message to the
     * log.
     * 
     * @param cmdLine The command-line to execute.  This may contain whitespace
     *        seperated arguments.  No quoting or escaping is handled, though.
     * @see #jsFunction_arg(String)
     */
    public void jsConstructor(String cmdLine)
    {
        String[] args = cmdLine.split("\\s+");
        
        this.addEvent(HANDLER_ON_LINE);
        this.addEvent(HANDLER_ON_ERROR_LINE);
        this.addEvent(HANDLER_ON_EXEC_COMPLETE);
        
        this.commandParts = new ArrayList<String>(args.length);
        
        for (String arg : args)
        {
            this.commandParts.add(arg);
        }
    }

    /**
     * Finalizer. If the object is finalized, it kills its child process if it
     * exists.
     */
    @Override
    protected void finalize()
    {
        if (this.childProcess != null)
        {
            this.childProcess.destroy();
        }
    }
    
    /**
     * Starts the external process.
     */
    public void jsFunction_start()
    {
        log.trace("Starting external process: {0}", this.commandParts.get(0));
        ProcessBuilder pb = new ProcessBuilder(this.commandParts);
        
        try
        {
            this.childProcess = pb.start();
        }
        catch (Exception e)
        {
            log.warn("Cannot execute native command '{0}'", e, this.commandParts.get(0)); 

            if (this.childProcess != null)
            {
                this.childProcess.destroy();
            }
        }
        
        this.in = new BufferedReader(new InputStreamReader(new BufferedInputStream(this.childProcess.getInputStream())));
        this.err = new BufferedReader(new InputStreamReader(new BufferedInputStream(this.childProcess.getErrorStream())));
        
        this.getEngine().runAsync(new Pump(this.in, HANDLER_ON_LINE));
        this.getEngine().runAsync(new Pump(this.err, HANDLER_ON_ERROR_LINE));
        this.getEngine().runAsync(new ProcessCompletionWaiter());
    }
    
    /**
     * Adds an argument to the command line.
     * 
     * @param arg The argument to be added.  Whitespace is treated as literal.
     * @return Returns the current {@link Exec} object.  This permits arguments
     *         to be easily added, as in a builder pattern.
     */
    public Exec jsFunction_arg(String arg)
    {
        this.commandParts.add(arg);
        return this;
    }
    
    /**
     * Blocks until the subprocess has terminated and the output channels
     * have all been cleared.  There is no guarantee of ordering between
     * this call returning and the {@link #HANDLER_ON_EXEC_COMPLETE onExecComplete}
     * event firing.
     * 
     * @return Returns the status code from the child process.
     */
    public int jsFunction_waitFor()
    {
        return this.waitForCompletion();
    }
    
    private int waitForCompletion()
    {
        int processResult;
        
        try
        {
            log.trace("Waiting for child process to exit.");
            processResult = childProcess.waitFor();
            
            if (processResult == 0)
                log.trace("Child process exited with code: {0}", processResult);
            else
            {
                StringBuilder sb = new StringBuilder();
                
                for (String part : this.commandParts)
                    sb.append(part).append(' ');
                
                log.warn("Child process exited with code {0}: {1}", processResult, sb);
            }
        }
        catch (InterruptedException e)
        {
            log.warn("Interrupted while waiting for child process to exit.", e);
            processResult = Integer.MIN_VALUE;
        }
        
        try
        {
            log.trace("Waiting for pumps to clear output channels.");
            this.pumpLatch.await();
            log.trace("Pumps completed.");
        }
        catch (InterruptedException e)
        {
            log.warn("Interrupted while waiting for child process output to be read.", e);
        }
        
        return processResult;
    }
    
    private ArrayList<String> commandParts;
    private Process childProcess;
    private BufferedReader in;  // stdout
    private BufferedReader err; // stderr
    private CountDownLatch pumpLatch = new CountDownLatch(2);
    
    /**
     * Pumps lines from the given {@link BufferedReader reader} and fires
     * an event handler on the parent {@link Exec} object.
     */
    private class Pump implements Runnable
    {
        public Pump(BufferedReader reader, String handler)
        {
            this.reader = reader;
            this.handler = handler;
        }
        
        public void run()
        {
            log.trace("Pumping to {0}", this.handler);
            
            try
            {
                String line;
                
                do
                {
                    line = this.reader.readLine();
                    fireHandler(this.handler, line);
                } while (line != null);
            }
            catch (IOException e)
            {
                log.error("An error occurred while reading from the child process.", e);
            }
            catch (ScriptException e)
            {
                log.error("An error occurred while reading from the child process.", e);
            }
            finally
            {
                log.trace("Done pumping {0}", this.handler);
                pumpLatch.countDown();
            }
        }
        
        private BufferedReader reader;
        private String handler;
    }
    
    private class ProcessCompletionWaiter implements Runnable
    {
        public void run()
        {
            int processResult = waitForCompletion();
            
            try
            {
                log.trace("Firing {1} handler with process result: {0}", processResult, HANDLER_ON_EXEC_COMPLETE);
                fireHandler(HANDLER_ON_EXEC_COMPLETE, processResult);
            }
            catch (ScriptException e)
            {
                log.error("Unable to execute asynchronous handler: {0}", e, HANDLER_ON_EXEC_COMPLETE);
            }
            finally
            {
                log.trace("ProcessCompletionWaiter done.");
            }
        }
    }
}

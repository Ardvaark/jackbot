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

import net.ardvaark.jackbot.CTCPMessage;
import net.ardvaark.jackbot.EventIRCClient;
import net.ardvaark.jackbot.IRC;
import net.ardvaark.jackbot.IRCMessage;
import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ScriptException;
import net.ardvaark.jackbot.scripting.ecma.async.AsyncTaskRunner;
import net.ardvaark.jackbot.scripting.ecma.async.TimeoutScheduler;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.util.*;

/**
 * A JackBot scripting engine that utilizes the Rhino JavaScript engine to
 * provide ECMAScript scripting capabilities.
 * 
 * @author Brian Vargas
 * @since JackBot v1.1
 * @version $Revision: 70 $ $Date: 2008-08-05 12:02:51 -0400 (Tue, 05 Aug 2008) $
 */
public class ECMAEngine implements net.ardvaark.jackbot.scripting.ScriptingEngine
{
    private static final Log log = Log.getLogger(ECMAEngine.class);
    
    /**
     * Constructs an <CODE>ECMAEngine</CODE>.
     * 
     * @throws ScriptException If an exception occurs during initialization.
     */
    public ECMAEngine() throws ScriptException
    {
        this.executedScripts = new ArrayList<Element>();
        this.executedFiles = Collections.synchronizedSet(new HashSet<String>());
        this.timeoutScheduler = new TimeoutScheduler(this);
        this.asyncRunner = new AsyncTaskRunner(this);
        this.initializeEngine();
    }

    /**
     * Cleans up the scripting context if it wasn't done by the thread using the
     * scripting engine.
     */
    @Override
    protected void finalize()
    {
        if (this.context != null)
        {
            Context.exit();
        }
    }

    /**
     * Sets the client associated with the engine.
     * 
     * @param client The client to associate with the engine.
     */
    public void setClient(EventIRCClient client)
    {
        this.client = client;
    }

    /**
     * Executes a script in the engine.
     * 
     * @param scriptElement The element containing the script to execute.
     * @return The return value of the execution.
     */
    public Object executeScript(Element scriptElement)
    {
        Object retVal = null;

        try
        {
            this.executedScripts.add(scriptElement);
            retVal = this.internalExecute(scriptElement);
        }
        catch (Exception e)
        {
            String scriptName = scriptElement.getAttribute("name");

            if (scriptName.length() == 0)
            {
                scriptName = scriptElement.getAttribute("file");
            }

            if (scriptName.length() == 0)
            {
                scriptName = "<unknown>";
            }

            log.error("An exception occurred while executing the script: {0}\n", e, scriptName);
        }

        return retVal;
    }

    /**
     * This is an internal helper method that actually performs the execution of
     * the script.
     * 
     * @param scriptElement The element containing the script to execute.
     * @throws Exception If any exceptions occur.
     * @return The return value of the execution.
     */
    private Object internalExecute(Element scriptElement) throws Exception
    {
        Object retVal = null;

        // Determine if the node is an include or an actual named script,
        // and act accordingly.
        if (scriptElement.hasAttribute("file"))
        {
            // Get the filename, and then parse the XML document.
            // After it is parsed, execute that script.
            String fileName = scriptElement.getAttribute("file");

            if (this.executedFiles.contains(fileName))
            {
                log.warn("The script file \"{0}\" has been included more " +
                		"than once.  The include has been skipped in order " +
                		"to avoid circular references.", fileName);
            }
            else
            {
                log.trace("Loading script file: {0}", fileName);
                
                this.executedFiles.add(fileName);
                retVal = this.executeFile(fileName);
            }
        }
        else if (scriptElement.hasAttribute("name"))
        {
            // First execute any include scripts.
            this.internalExecuteIncludes(scriptElement);

            // Now execute the script itself.
            String scriptName = scriptElement.getAttribute("name");
            String script = scriptElement.getTextContent();
            Context cx = Context.getCurrentContext();
            
            log.trace("Executing script: {0}", scriptName);
            retVal = cx.evaluateString(this.topLevelScope, script, scriptName, 1, null);
        }
        else
        {
            throw new IllegalArgumentException("Either the \"name\" attribute or the \"file\" attribute is required.");
        }

        return retVal;
    }

    /**
     * Opens the file given by the parameter and executes the script contained
     * therein.
     * 
     * @param fileName The name of the file to open.
     * @throws Exception If any exceptions occur.
     * @return The result of executing the script in the file.
     */
    Object executeFile(String fileName) throws Exception
    {
        Element rootElement;
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        try {
            Document doc = docBuilder.parse(fileName);
            rootElement = doc.getDocumentElement();
        }
        catch (SAXParseException e) {
            // If we're unable to parse this as an XML file,
            // let's try wrapping the file in a made-up XML document, so we can cleanly load bare script files
            // (i.e. with no XML wrapper).
            String scriptText = IOUtils.toString(new FileInputStream(fileName), "UTF-8");

            Document doc = docBuilder.newDocument();
            rootElement = doc.createElement("script");
            rootElement.setTextContent(scriptText);
            rootElement.setAttribute("name", fileName);
        }

        return this.internalExecute(rootElement);
    }

    /**
     * Searches the script element for any script include elements and executes
     * them.
     * 
     * @param scriptElement The script to search for includes.
     * @throws Exception If any exceptions occur.
     */
    private void internalExecuteIncludes(Element scriptElement) throws Exception
    {
        NodeList childNodes = scriptElement.getElementsByTagName("script");
        Element curElement = null;

        for (int i = 0; i < childNodes.getLength(); i++)
        {
            curElement = (Element) childNodes.item(i);
            this.internalExecute(curElement);
        }
    }

    /**
     * Reloads the ECMAScript engine. The following events occur:
     * <ol>
     * <li>The bot parts all channels.</li>
     * <li>The Rhino JavaScript engine is shut down and restarted.</li>
     * <li>All scripts that have been executed before are re-executed int the
     * same order.</li>
     * </ol>
     * 
     * @throws Exception If any exceptions occur.
     */
    void reload() throws Exception
    {
        this.bot.partAllChannels();
        this.cleanup();
        this.initializeEngine();

        for (Element scriptElement : this.executedScripts)
        {
            this.internalExecute(scriptElement);
        }
    }

    /**
     * Performs necessary cleanup of the Rhino JavaScript engine.
     */
    public void cleanup()
    {
        asyncRunner.destroy();
        timeoutScheduler.destroy();
        
        Context.exit();
        this.context = null;
        this.executedFiles.clear();
    }
    
    /**
     * Called when an IRC message is received from the server.
     * 
     * @param msg The message.
     * @param client The client from which the message was received.
     */
    public void ircMessageReceived(IRCMessage msg, EventIRCClient client)
    {
        try
        {
            String cmd = msg.getCommand();

            this.bot.onMessage(msg);

            if (cmd.equalsIgnoreCase(IRC.CMD_PRIVMSG))
            {
                this.onPrivMsg(msg);
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_NOTICE))
            {
                // BCV TODO
                // this.onNotice()
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_JOIN))
            {
                this.onJoin(msg);
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_PART))
            {
                this.onPart(msg);
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_KICK))
            {
                this.onKick(msg);
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_QUIT))
            {
                this.onQuit(msg);
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_NICK))
            {
                this.onNick(msg);
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_TOPIC))
            {
                this.onTopic(msg);
            }
            else if (cmd.equalsIgnoreCase(IRC.CMD_MODE))
            {
                this.onMode(msg);
            }
        }
        catch (ScriptException e)
        {
            log.warn("A ScriptException occured while processing the IRC " +
            		"message: {0}\n", e, msg.toString());
        }
        catch (Exception e)
        {
            log.warn("An Exception occured while processing the IRC " +
                    "message: {0}\n", e, msg.toString());
        }
    }
    
    /**
     * Schedules a timeout with the scripting engine.
     * 
     * @param key The key of the timeout.  Must not be null.  This key may
     *            be used to cancel the timeout using the
     *            {@link #cancelTimeout(Object)} method.
     * @param func The {@link Runnable} to run after the timeout expires.
     * @param millis The length of time in milliseconds to wait before running
     *               the code.
     */
    public Object scheduleTimeout(Object key, Runnable func, long millis)
    {
        return timeoutScheduler.scheduleTimeout(key, func, millis);
    }
    
    /**
     * Cancels a timeout that has been previously scheduled.  If the timeout
     * does not exist, or is already running, nothing happens.
     * 
     * @param key The key of the timeout to cancel.  This would be the same
     *            key passed to {@link #scheduleTimeout(Object, Runnable, long)}
     *            to schedule the timeout being canceled.
     */
    public void cancelTimeout(Object key)
    {
        timeoutScheduler.cancelTimeout(key);
    }
    
    public void runAsync(Runnable func)
    {
        asyncRunner.run(func);
    }

    /**
     * Gets the client associated with the engine.
     * 
     * @return The client.
     */
    EventIRCClient getClient()
    {
        return this.client;
    }

    /**
     * Initializes the Rhino JavaScript engine for use with the JackBot
     * environment.
     * 
     * @throws ScriptException If any exceptions occur.
     */
    private void initializeEngine() throws ScriptException
    {
        try
        {
            // Create the script context and initialize the top level scope.
            Context cx = this.context = ContextFactory.getGlobal().enterContext();
            cx.putThreadLocal(ECMAEngine.class, this);
            Scriptable scope = this.topLevelScope = cx.initStandardObjects(null);

            // Add the host class definitions.
            Bot bot = new Bot();
            Channel channel = new Channel();
            Name name = new Name();
            Exec exec = new Exec();

            ScriptableObject.defineClass(scope, bot.getClass());
            ScriptableObject.defineClass(scope, channel.getClass());
            ScriptableObject.defineClass(scope, name.getClass());
            ScriptableObject.defineClass(scope, exec.getClass());
            ScriptableObject.defineClass(scope, XmlHttpRequest.class);

            // Create the top-level Bot instance.
            this.bot = HostObjectFactory.newBot(this, scope);

            // Put the Bot instance in the top-level scope, in the property
            // "bot".
            int propAttribs = ScriptableObject.DONTENUM | ScriptableObject.PERMANENT | ScriptableObject.READONLY;
            ScriptableObject.defineProperty(this.topLevelScope, "bot", this.bot, propAttribs);
        }
        catch (Exception e)
        {
            throw new ScriptException("Error in ECMAEngine initialization.", e);
        }
    }

    /**
     * Called when a <CODE>JOIN</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onJoin(IRCMessage msg) throws ScriptException
    {
        String channel = "";
        String name = msg.getPrefix();
        StringTokenizer tok = new StringTokenizer(msg.getParam(0), ",");

        while (tok.hasMoreTokens())
        {
            channel = tok.nextToken();
            this.bot.onJoin(channel, name);
        }
    }

    /**
     * Called when a <CODE>PART</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onPart(IRCMessage msg) throws ScriptException
    {
        String partMessage = "";
        String name = msg.getPrefix();
        StringTokenizer tok = new StringTokenizer(msg.getParam(0), ",");
        String channel = "";

        if (msg.getParamCount() > 1)
        {
            partMessage = msg.getParam(1);
        }

        while (tok.hasMoreTokens())
        {
            channel = tok.nextToken();
            this.bot.onPart(channel, name, partMessage);
        }
    }

    /**
     * Called when a <CODE>KICK</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onKick(IRCMessage msg) throws ScriptException
    {
        String kickMessage = "";
        String kicker = msg.getPrefix();
        StringTokenizer chanTok = new StringTokenizer(msg.getParam(1), ",");
        StringTokenizer nameTok = new StringTokenizer(msg.getParam(0), ",");
        String name = "";
        String channel = "";

        if (msg.getParamCount() > 2)
        {
            kickMessage = msg.getParam(2);
        }

        while (chanTok.hasMoreElements())
        {
            channel = chanTok.nextToken();

            if (nameTok.hasMoreTokens())
            {
                name = nameTok.nextToken();
            }

            this.bot.onKick(channel, name, kicker, kickMessage);
        }
    }

    /**
     * Called when a <CODE>QUIT</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onQuit(IRCMessage msg) throws ScriptException
    {
        String name = msg.getPrefix();
        String quitMessage = "";

        if (msg.getParamCount() > 0)
        {
            quitMessage = msg.getParam(0);
        }

        this.bot.onQuit(name, quitMessage);
    }

    /**
     * Called when a <CODE>TOPIC</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onTopic(IRCMessage msg) throws ScriptException
    {
        String channel = msg.getParam(0);
        String newTopic = msg.getLastParam();
        String changer = msg.getPrefix();
        this.bot.onTopic(channel, newTopic, changer);
    }

    /**
     * Called when a <CODE>PRIVMSG</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onPrivMsg(IRCMessage msg) throws ScriptException
    {
        String sender = msg.getPrefix();
        String target = msg.getParam(0);
        String text = msg.getLastParam();
        this.bot.onPrivMsg(sender, target, text);

        int ctcpMsgCount = msg.getCTCPMessageCount();

        if (ctcpMsgCount > 0)
        {
            for (int i = 0; i < ctcpMsgCount; i++)
            {
                this.onCTCP(msg.getCTCPMessage(i));
            }
        }
    }

    /**
     * Called when a <CODE>NICK</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onNick(IRCMessage msg) throws ScriptException
    {
        String name = msg.getPrefix();
        String newNick = msg.getParam(0);
        this.bot.onNick(name, newNick);
    }

    /**
     * Called when a <CODE>MODE</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onMode(IRCMessage msg) throws ScriptException
    {
        // BCV WORKING
        /*
         * String name = msg.getPrefix(); String channel = msg.getParam(0);
         * String modes = ""; String params = "";
         * 
         * if (msg.getParamCount() > 1) { modes = msg.getParam(1); }
         */
    }

    /**
     * Called when a <CODE>CTCP</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onCTCP(CTCPMessage msg) throws ScriptException
    {
        String cmd = msg.getCommand();

        if (cmd.equalsIgnoreCase(IRC.CTCP_CMD_ACTION))
        {
            this.onAction(msg);
        }
    }

    /**
     * Called when a <CODE>ACTION</CODE> message is received from the server.
     * 
     * @param msg The message received.
     * @throws ScriptException If any exceptions occur in the underlying script
     *         engine.
     */
    void onAction(CTCPMessage msg) throws ScriptException
    {
        this.bot.onAction(msg.getSender(), msg.getTarget(), msg.getParams());
    }

    /**
     * The client to which the engine is attached.
     */
    private EventIRCClient            client;

    /**
     * The scripting context.
     */
    private Context                   context;

    /**
     * The top-level bot host object.
     */
    private Bot                       bot;

    /**
     * The top-level scope in which the host objects will be created.
     */
    private Scriptable                topLevelScope;

    /**
     * A list of the scripts that have been executed by the engine.
     */
    private ArrayList<Element>        executedScripts;

    /**
     * A set of the files that have been loaded and executed by the engine.
     * Used to detect loading the same script multiple times.
     */
    private Set<String> executedFiles;
    
    /**
     * For executing asynchronous events.
     */
    private AsyncTaskRunner asyncRunner;
    
    /**
     * For executing timeout events.
     */
    private TimeoutScheduler timeoutScheduler;
}

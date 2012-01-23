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

package net.ardvaark.jackbot;

import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.plugin.BasicIRCFunctions;
import net.ardvaark.jackbot.scripting.ScriptingEngine;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.classworlds.NoSuchRealmException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * The main class of the JackBot IRC Bot.
 * 
 * @author Brian Vargas
 * @version $Revision: 72 $ $Date: 2009-02-20 08:37:00 -0500 (Fri, 20 Feb 2009) $
 */
public class JackBot
{
    private static Log log = Log.getLogger(JackBot.class);

    // Error codes
    /**
     * JackBot process return value: Everything is hunky dorey!
     */
    public static int SUCCESS                            = 0;

    /**
     * JackBot process return value: An error occured during startup.
     */
    public static int ERROR_STARTUP_EXCEPTION            = 1;

    /**
     * JackBot process return value: There was an error parsing the command
     * line.
     */
    public static int ERROR_COMMAND_LINE_PARSE_EXCEPTION = 2;

    /**
     * JackBot process return value: The configuration file is bad.
     */
    public static int ERROR_BAD_CONFIG                   = 3;
        
    /**
     * The class world in which the bot is loaded.
     */
    private ClassWorld world;
    
    public ClassWorld getClassWorld()
    {
        return world;
    }
    
    void setClassWorld(ClassWorld newWorld)
    {
        assert newWorld != null;
        world = newWorld;
    }

    /**
     * Program execution starts here.
     * 
     * @param args The arguments passed to the program.
     */
    public void run(String[] args)
    {
        initLogging();

        log.info("Starting JackBot...");

        EventIRCClient bot = null;
        Server currentServer = null;
        String configFileName = "jackbot.xml";
        Element logonScript = null;
        ScriptingEngine scriptEngine = null;
        NodeList scriptElements = null;
        ArrayList<Server> servers = new ArrayList<Server>();

        try
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i].equals("-c"))
                {
                    configFileName = args[i + 1];
                }
            }
        }
        catch (Exception e)
        {
            log.fatal("Error parsing commandline.", e);
            System.exit(ERROR_COMMAND_LINE_PARSE_EXCEPTION);
        }

        try
        {
            // First open the config file and get some important info.
            log.info("Reading configuration file \"" + configFileName + "\".");
            Config config = new Config(configFileName);
            Element root = config.getConfigElement("JackBot");
            String nick = root.getAttribute("nick");
            String desc = root.getAttribute("description");
            
            // Initialize the logging.
            configureLogging(root.getAttribute("logLevel"));
            
            // Set up the classworlds.
            NodeList classworldsElements = root.getElementsByTagName("load");
            this.initPluginRealm(classworldsElements);

            // Get the servers the bot will connect to.
            NodeList serverNodes = root.getElementsByTagName("server");

            for (int i = 0; i < serverNodes.getLength(); i++)
            {
                String serverName = ((Element) serverNodes.item(i)).getAttribute("name");
                String serverPort = ((Element) serverNodes.item(i)).getAttribute("port");
                String useSsl = ((Element) serverNodes.item(i)).getAttribute("ssl");
                

                if (serverPort == null || serverPort.isEmpty()) {
                    serverPort = "6667";
                }
                
                if (useSsl == null || useSsl.isEmpty()) {
                    useSsl = Boolean.FALSE.toString();
                }

                servers.add(new Server(serverName, serverPort, useSsl));
            }

            if (servers.isEmpty()) {
                log.fatal("No servers were specified in the configuration file.  Exiting.");
                System.exit(JackBot.ERROR_BAD_CONFIG);
            }

            // Create the bot.
            bot = new EventIRCClient(nick, desc);

            // Load the plugins for the EventIRCClient.
            NodeList pluginNodes = root.getElementsByTagName("plugin");
            ClassRealm pluginRealm = this.world.getRealm("jackbot.plugins");
            PluginLoader pluginLoader = new PluginLoader(pluginRealm.getClassLoader(), pluginNodes);
            
            for (JackBotPlugin plugin : pluginLoader.getPlugins()) {
                bot.addMessageListener(plugin);
            }
            
            // Add the default Basic IRC Functions.
            bot.addMessageListener(new BasicIRCFunctions());

            // Get any script includes.
            scriptElements = root.getElementsByTagName("script");

            // Get the login script. If there is more than one,
            // just take the first one.
            NodeList nodes = root.getElementsByTagName("logon-script");

            if (nodes.getLength() > 0)
            {
                logonScript = (Element) nodes.item(0);
            }

            try
            {
                // Load the scripting engine.
                nodes = root.getElementsByTagName("script-engine");

                if (nodes.getLength() > 1)
                {
                    log.warn("Multiple script-engine tags were found in the configuration file.  No script engine was initialized.");
                }
                else if (nodes.getLength() == 1)
                {
                    Element engineElement = (Element) nodes.item(0);
                    String className = engineElement.getAttribute("class");
                    Object newObject = bot.getClass().getClassLoader().loadClass(className).newInstance();

                    if (newObject instanceof ScriptingEngine)
                    {
                        scriptEngine = (ScriptingEngine) newObject;
                        scriptEngine.setClient(bot);
                        log.info("Script engine \"" + className + "\" initialized.");
                    }
                    else
                    {
                        log.warn("The class \"" + className + "\" does not implement the ScriptingEngine interface.");
                    }
                }
            }
            catch (Exception e)
            {
                log.error("An exception occurred while initializing the scripting engine.", e);
                scriptEngine = null;
            }
        }
        catch (Exception e)
        {
            log.fatal("Unhandled exception during startup.", e);
            System.exit(ERROR_STARTUP_EXCEPTION);
        }

        try
        {
            if (scriptEngine != null)
            {
                for (int i = 0; i < scriptElements.getLength(); i++)
                {
                    scriptEngine.executeScript((Element) scriptElements.item(i));
                }

                bot.addMessageListener(scriptEngine);
            }

            int currentServerIndex = 0;

            // Here is the main run loop.
            while (bot.getRunning())
            {
                log.trace("Starting main run loop.");

                try
                {
                    currentServer = servers.get(currentServerIndex++);
                    currentServerIndex %= servers.size();

                    bot.connect(currentServer.getName(), currentServer.getPort(), currentServer.getUseSsl());
                    bot.logon();

                    this.ensureValidNick(bot);

                    if (scriptEngine != null)
                    {
                        log.trace("Executing logon script...");
                        scriptEngine.executeScript(logonScript);
                        log.trace("Logon script complete.");
                    }

                    log.trace("Entering bot event loop.");
                    bot.run();
                }
                catch (IOException e)
                {
                    JackBot.onConnectionLost(e);
                }
                catch (IRCConnectionLostException e)
                {
                    JackBot.onConnectionLost(e);
                }
            }
        }
        catch (Exception e)
        {
            log.fatal("Unhandled exception.", e);
        }
        finally
        {
            try
            {
                bot.logoff();
            }
            catch (IOException e)
            {
            }

            try
            {
                bot.disconnect();
            }
            catch (IOException e)
            {
            }

            scriptEngine.cleanup();
        }
    }

    /**
     * Helper method called when the connection is lost from the main run loop.
     * This will wait for 5 seconds before trying again.
     * 
     * @param e The exception that was caught signifying that the connection was
     *        lost.
     */
    private static void onConnectionLost(Exception e)
    {
        log.info("Connection lost. (" + e.getMessage() + ") Retrying in 5 seconds...");

        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e2)
        {
        }
    }

    /**
     * Initializes the logging. This sets up a default
     * <CODE>ConsoleHandler</CODE> and adds sets the formatter to the JackBot
     * formatting style.
     */
    private void initLogging()
    {
        Log.initLogging();
    }
    
    private void initPluginRealm(NodeList loadNodes) throws JackBotConfigurationException, NoSuchRealmException, DuplicateRealmException
    {
        log.trace("Creating class realm: jackbot.plugins");
        ClassRealm realm = this.world.getRealm("jackbot.core").createChildRealm("jackbot.plugins");
        
        if (loadNodes.getLength() > 0)
        {
            for (int i = 0; i < loadNodes.getLength(); i++)
            {
                Element element = (Element)loadNodes.item(i);
                String path = element.getAttribute("path");
                
                log.trace("Examining path: {0}", path);

                try
                {
                    File file = new File(path);
                    
                    if (file.isFile() && file.exists())
                    {
                        URL url = file.toURI().toURL();
                        log.trace("Adding constituent to realm: {0}", url);
                        realm.addConstituent(url);
                    }
                    else if (file.isDirectory())
                    {
                        log.trace("A bare directory can't be loaded: {0}", path);
                    }
                    else
                    {
                        File dir = file.getParentFile();
                        String glob = file.getName();
                        
                        if (!dir.exists())
                        {
                            log.warn("Directory not found: {0} ", dir);
                        }
                        else
                        {
                            File[] children = dir.listFiles();
                            
                            for (File childFile : children)
                            {
                                String fileName = childFile.getName();
    
                                if (FilenameUtils.wildcardMatchOnSystem(fileName, glob))
                                {
                                    URL url = childFile.toURI().toURL();
                                    log.trace("Adding constituent to realm: " + url);
                                    realm.addConstituent(url);
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new JackBotConfigurationException(MessageFormat.format("Unable to load JAR: {0}", path), e);
                }
            }
        }
    }

    /**
     * Configures the logging system.
     * 
     * @param logLevel The string name of the log level.
     */
    private void configureLogging(String logLevel)
    {
        Log.configureLogging(logLevel);
    }

    private void ensureValidNick(EventIRCClient bot) throws IOException, IRCConnectionLostException
    {
        log.trace("Checking for accepted nickname.");
        IRCMessageWaitRestore waiter = new IRCMessageWaitRestore(bot);
        IRCMessage curMessage = waiter.nextMsg();
        String curCmd = null;

        while (curMessage != null)
        {
            log.trace("Current message: " + curMessage.toString());

            curCmd = curMessage.getCommand();

            if (curCmd.equalsIgnoreCase(IRC.ERR_NICKNAMEINUSE))
            {
                log.info("Nickname rejected.");

                if (bot.getName().endsWith("___"))
                {
                    bot.setName(bot.getName().substring(bot.getName().lastIndexOf("___")));
                }
                else
                {
                    bot.setName(bot.getName() + "_");
                }

                log.info("Logging in as " + bot.getName());
                bot.ircWrite(IRC.CMD_NICK + " " + bot.getName());
            }
            else if (curCmd.equalsIgnoreCase(IRC.RPL_WELCOME))
            {
                break;
            }

            curMessage = waiter.nextMsg();
        }

        waiter.restore();

        log.trace("Nickname accepted.");
    }
}

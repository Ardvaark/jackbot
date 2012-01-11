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

import net.ardvaark.jackbot.IRC;
import net.ardvaark.jackbot.IRCMessage;
import net.ardvaark.jackbot.IRCUtils;
import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ScriptException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

/**
 * ECMAScript host object <CODE>Bot</CODE>. The <code>Bot</code> class is
 * the top-level object of the IRC Object Model (IOM). It's children include the
 * <code>Channel</code> class.
 * 
 * @author Brian Vargas
 * @since JackBot v1.1
 * @version $Revision: 70 $ $Date: 2008-08-05 12:02:51 -0400 (Tue, 05 Aug 2008) $
 */
public class Bot extends HostObject
{
    private static final Log log = Log.getLogger(Bot.class);
    
    private static final long  serialVersionUID   = 1L;

    /**
     * The ECMAScript class name for this class. This is the name by which the
     * script will refer to the class.
     */
    public static final String ECMA_CLASS_NAME    = "Bot";

    /**
     * The name of the handler to be called when any IRC message is received
     * from the server.
     */
    public static final String HANDLER_ON_MSG     = "onMessage";

    /**
     * The ECMAScript handler to be called when a <CODE>JOIN</CODE> message is
     * received from the server.
     */
    public static final String HANDLER_ON_JOIN    = "onJoin";

    /**
     * The ECMAScript handler to be called when a <CODE>PART</CODE> message is
     * received from the server.
     */
    public static final String HANDLER_ON_PART    = "onPart";

    /**
     * The ECMAScript handler to be called when a <CODE>KICK</CODE> message is
     * received from the server.
     */
    public static final String HANDLER_ON_KICK    = "onKick";

    /**
     * The ECMAScript handler to be called when a <CODE>QUIT</CODE> message is
     * received from the server.
     */
    public static final String HANDLER_ON_QUIT    = "onQuit";

    /**
     * The ECMAScript handler to be called when a <CODE>T0PIC</CODE> message
     * is received from the server.
     */
    public static final String HANDLER_ON_TOPIC   = "onTopic";

    /**
     * The ECMAScript handler to be called when a <CODE>NICK</CODE> message is
     * received from the server.
     */
    public static final String HANDLER_ON_NICK    = "onNick";

    /**
     * The ECMAScript handler to be called when a <CODE>PRIVMSG</CODE> message
     * is received from the server.
     */
    public static final String HANDLER_ON_PRIVMSG = "onPrivMsg";

    /**
     * The ECMAScript handler to be called when a CTCP <CODE>ACTION</CODE>
     * message is received from the server.
     */
    public static final String HANDLER_ON_ACTION  = "onAction";

    /**
     * Default constructor.
     */
    public Bot()
    {
    }

    /**
     * Gets the ECMAScript class name for this object.
     * 
     * @return The ECMAScript class name for this object.
     */
    @Override
    public String getClassName()
    {
        return Bot.ECMA_CLASS_NAME;
    }

    /**
     * ECMAScript property get <CODE>channels</CODE>.
     * 
     * @return The <CODE>channels</CODE> member.
     */
    public Scriptable jsGet_channels()
    {
        return this.channels;
    }

    /**
     * ECMAScript property get <CODE>name</CODE>. This will be the nick of
     * the bot.
     * 
     * @return The nick of the bot.
     */
    public String jsGet_name()
    {
        return this.getEngine().getClient().getName();
    }

    /**
     * ECMAScript property set <CODE>name</CODE>. This will fire an
     * <CODE>NICK</CODE> message to the server.
     * 
     * @param newName The new desired nickname of the bot.
     */
    public void jsSet_name(String newName)
    {
        this.getEngine().getClient().ircWrite(IRC.CMD_NICK + " " + newName);
    }

    /**
     * ECMAScript function <CODE>join(channel)</CODE>. This will cause the
     * bot to join a channel.
     * 
     * @param channelName The name of the channel to join.
     */
    public void jsFunction_join(String channelName)
    {
        this.getEngine().getClient().ircWrite(IRC.CMD_JOIN + " " + channelName);
    }

    /**
     * ECMAScript function <CODE>write(msg)</CODE>. This will write raw text
     * to the server.
     * 
     * @param rawMessage The message to write.
     */
    public void jsFunction_write(String rawMessage)
    {
        this.getEngine().getClient().ircWrite(rawMessage);
    }

    /**
     * ECMAScript function <CODE>part(channel, reason)</CODE>. Causes the bot
     * to leave a channel, siting the given reason.
     * 
     * @param channel The channel to part.
     * @param partMessage The message to display with the part.
     */
    public void jsFunction_part(String channel, String partMessage)
    {
        this.getEngine().getClient().ircWrite(IRC.CMD_PART + " " + channel + " :" + partMessage);
    }

    /**
     * ECMAScript function <CODE>log(msg)</CODE>. Writes the given message to
     * a line in the JackBot log file.
     * 
     * @param message The message to log.
     */
    public void jsFunction_log(String message)
    {
        log.info(MessageFormat.format("Script: {0}", message));
    }
    
    /**
     * ECMAScript function <CODE>exit()</CODE>. Causes the bot to exit
     * cleanly.
     */
    public void jsFunction_exit()
    {
        this.getEngine().getClient().setRunning(false);
    }

    /**
     * ECMAScript function <CODE>say(target, msg)</CODE>. Causes the bot to
     * write a <CODE>PRIVMSG</CODE> to the given target with the given
     * message.
     * 
     * @param target The target of the message.
     * @param message Tne message to send to the target.
     */
    public void jsFunction_say(String target, String message)
    {
        this.getEngine().getClient().ircWrite(IRC.CMD_PRIVMSG + " " + target + " :" + message);
    }

    /**
     * ECMAScript function <CODE>notice(target, msg)</CODE>. Causes the bot
     * to write a <CODE>NOTICE</CODE> to the given target with the given
     * message.
     * 
     * @param target The target of the message.
     * @param message The message to send to the target.
     */
    public void jsFunction_notice(String target, String message)
    {
        this.getEngine().getClient().ircWrite(IRC.CMD_NOTICE + " " + target + " :" + message);
    }

    /**
     * ECMAScript function <CODE>ctcpWrite(target, msg)</CODE>. Causes the
     * bot to write a <CODE>PRIVMSG</CODE> to the given target, with the given
     * message escaped in CTCP.
     * 
     * @param target The target for the message.
     * @param message The message to send to the target.
     */
    public void jsFunction_ctcpWrite(String target, String message)
    {
        this.jsFunction_say(target, IRCUtils.ctcpEscape(message));
    }

    /**
     * ECMAScript function <CODE>action(target, msg)</CODE>. Causes the bot
     * to write a CTCP message <CODE>ACTION</CODE> to the given target with
     * the given action text.
     * 
     * @param target The target for the action.
     * @param action The action to send to the target.
     */
    public void jsFunction_action(String target, String action)
    {
        this.jsFunction_ctcpWrite(target, "ACTION " + action);
    }

    /**
     * ECMAScript function
     * <CODE>respond(sender, target, msg, respondAsPrivMsg)</CODE>. This will
     * send a <CODE>NOTICE</CODE> as a response to a message. If the target is
     * a channel, the response will be to the channel. Otherwise, the response
     * will be sent to the sender.
     * 
     * @param sender The sender of the message to which is being responded.
     * @param target The target of the message to which is being responded.
     * @param message The message with which to respond.
     * @param respondAsPrivMsg If <code>true</code>, the response will be
     *        sent as a <code>PRIVMSG</code> instead of a <code>NOTICE</code>.
     *        Defaults to <tt>false</tt>.
     */
    public void jsFunction_respond(String sender, String target, String message, boolean respondAsPrivMsg)
    {
        String myTarget = null;

        if (target.charAt(0) == '#')
        {
            myTarget = target;
        }
        else
        {
            myTarget = IRCUtils.extractNickFromMask(sender);
        }

        String ircCmd = respondAsPrivMsg ? IRC.CMD_PRIVMSG : IRC.CMD_NOTICE;

        this.getEngine().getClient().ircWrite(ircCmd + " " + myTarget + " :" + message);
    }

    /**
     * ECMAScript function <CODE>reload()</CODE>. Causes the scripting engine
     * to reload itself and any scripts that have been loaded.
     * 
     * @throws Exception If anything bad happens.
     */
    public void jsFunction_reload() throws Exception
    {
        this.getEngine().reload();
    }

    /**
     * ECMAScript function <CODE>loadXML(file)</CODE>. Loads an XML document
     * and returns a <CODE>Document</CODE> DOM node.
     * 
     * @param fileName The name of the file to load.
     * @throws Exception If anything bad happens.
     * @return The <CODE>Document</CODE> DOM node for the parsed XML file.
     */
    public Document jsFunction_loadXML(String fileName) throws Exception
    {
        Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileName);
        return doc;
    }

    /**
     * ECMAScript function <CODE>saveTextFile(text, file)</CODE>. Saves the
     * given text to a file with the given filename.
     * 
     * @param text The text to save.
     * @param fileName The name of the file to which the text should be saved.
     * @throws Exception If anything bad happens.
     */
    public void jsFunction_saveTextFile(String text, String fileName) throws Exception
    {
        PrintWriter out = new PrintWriter(new FileOutputStream(fileName));
        out.println(text);
        out.flush();
        out.close();
    }
    
    public Object jsFunction_setTimeout(final Object key, final Function code, String millis) throws ScriptException
    {
        long timeoutMills = Long.parseLong(millis);

        Runnable funcRunner = new Runnable() {
            public void run()
            {
                log.trace("Executing task: {0}", key);
                
                // The getCurrentContext() call will pull the context
                // from the scheduler.
                code.call(Context.getCurrentContext(), code, Bot.this, new Object[]{});
            }
        };
        
        this.getEngine().scheduleTimeout(key, funcRunner, timeoutMills);
        
        return key;
    }
    
    public void jsFunction_cancelTimeout(Object key)
    {
        this.getEngine().cancelTimeout(key);
    }

    public String jsFunction_decodeURIComponent(String encodedUriComponent) throws ScriptException {
        try {
            return URIUtil.decode(encodedUriComponent);
        }
        catch (URIException e) {
            throw new ScriptException(e);
        }
    }

    public String jsFunction_encodeURIComponent(String rawUriComponent) throws ScriptException {
        try {
            return URIUtil.encodeWithinQuery(rawUriComponent);
        }
        catch (URIException e) {
            throw new ScriptException(e);
        }
    }

    public void jsFunction_persistData(String key, Object value) {
        this.getEngine().persistData(key, value);
    }

    public Object jsFunction_retrieveData(String key) {
        return this.getEngine().retrieveData(key);
    }

    /**
     * Causes the bot to part all channels in which it is currently listening.
     * 
     * @throws Exception If any exceptions occur.
     */
    void partAllChannels() throws Exception
    {
        Object[] ids = this.channels.getIds();

        for (Object element : ids)
        {
            this.getEngine().getClient().ircWrite(IRC.CMD_PART + " " + element.toString());
        }
    }

    /**
     * Called when an <CODE>IRCMessage</CODE> is received from the server.
     * This will fire the handler <CODE>HANDLER_ON_MSG</CODE>.
     * 
     * @param msg The message that was received from the server.
     * @throws ScriptException If any exceptions occur during the firing of the
     *         event.
     */
    void onMessage(IRCMessage msg) throws ScriptException
    {
        // Call the onMessage JavaScript handler.
        Object[] args =
        { msg };
        this.fireHandler(Bot.HANDLER_ON_MSG, args);
    }

    /**
     * Called when a <CODE>JOIN</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>If the bot is the target of the join, then a new
     * <CODE>Channel</CODE> object will be created and added to the
     * <CODE>channels</CODE> collection.</li>
     * <li>Otherwise, the channel's {@link Channel#onJoin(String) onJoin()}
     * method will be called.</li>
     * <li>The Bot's <CODE>HANDLER_ON_JOIN</CODE> handler will be fired.</li>
     * </ol>
     * 
     * @param channel The name of the channel that was joined.
     * @param name The name that joined the channel.
     * @throws ScriptException If any errors occur during event firing.
     */
    void onJoin(String channel, String name) throws ScriptException
    {
        String nick = IRCUtils.extractNickFromMask(name);

        if (nick.equalsIgnoreCase(this.getEngine().getClient().getName()))
        {
            Channel channelInstance = HostObjectFactory.newChannel(channel, this);
            ScriptableObject.putProperty(this.channels, channel, channelInstance);
        }
        else
        {
            // First, call the channel's onJoin() so that it can set up
            // its own state.
            Channel channelInstance = this.getChildChannel(channel);

            if (channelInstance != null)
            {
                channelInstance.onJoin(name);
            }
        }

        // Then fire the JavaScript onJoin handler.
        Object[] args =
        { channel, name };
        this.fireHandler(Bot.HANDLER_ON_JOIN, args);
    }

    /**
     * Called when a <CODE>PART</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The Bot's <CODE>HANDLER_ON_PART</CODE> handler will be fired.</li>
     * <li>If the bot is the target of the part, then the <CODE>Channel</CODE>
     * object will be removed from the <CODE>channels</CODE> collection.</li>
     * <li>Otherwise, the channel's
     * {@link Channel#onPart(String, String) onPart()} method will be called.</li>
     * </ol>
     * 
     * @param channel The channel that was parted.
     * @param name The name of the person who parted.
     * @param msg The part reason.
     * @throws ScriptException If any exceptions occur during event firing.
     */
    void onPart(String channel, String name, String msg) throws ScriptException
    {
        String nick = IRCUtils.extractNickFromMask(name);

        Object[] args =
        { channel, name, msg };
        this.fireHandler(Bot.HANDLER_ON_PART, args);

        Channel channelInstance = this.getChildChannel(channel);

        if (channelInstance != null)
        {
            channelInstance.onPart(name, msg);
        }

        if (nick.equalsIgnoreCase(this.getEngine().getClient().getName()))
        {
            ScriptableObject.deleteProperty(this.channels, channel);
        }
    }

    /**
     * Called when a <CODE>KICK</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The Bot's <CODE>HANDLER_ON_KICK</CODE> handler will be fired.</li>
     * <li>If the bot is the target of the kick, then the <CODE>Channel</CODE>
     * object will be removed from the <CODE>channels</CODE> collection.</li>
     * <li>Otherwise, the channel's
     * {@link Channel#onKick(String, String, String) onKick()} method will be
     * called.</li>
     * </ol>
     * 
     * @param channel The channel from which the user was kicked.
     * @param name The name of the user kicked.
     * @param kicker The name of the kicker.
     * @param msg The reason for the kick.
     * @throws ScriptException If any exceptions are thrown during event firing.
     */
    void onKick(String channel, String name, String kicker, String msg) throws ScriptException
    {
        String nick = IRCUtils.extractNickFromMask(name);

        Object[] args =
        { channel, name, kicker, msg };
        this.fireHandler(Bot.HANDLER_ON_KICK, args);

        Channel channelInstance = this.getChildChannel(channel);

        if (channelInstance != null)
        {
            channelInstance.onKick(name, kicker, msg);
        }

        if (nick.equalsIgnoreCase(this.getEngine().getClient().getName()))
        {
            ScriptableObject.deleteProperty(this.channels, channel);
        }
    }

    /**
     * Called when a <CODE>QUIT</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The Bot's <CODE>HANDLER_ON_QUIT</CODE> handler will be fired.</li>
     * <li>Each channel object will be checked. If the quitter exists in that
     * channel, the channels {@link Channel#onQuit(String, String) onQuit()}
     * will be called.</li>
     * </ol>
     * 
     * @param name The name of the quitter.
     * @param msg The reason for the quit.
     * @throws ScriptException If any exceptions occur during event firing.
     */
    void onQuit(String name, String msg) throws ScriptException
    {
        Object[] args = { name, msg };
        this.fireHandler(Bot.HANDLER_ON_QUIT, args);

        Object[] ids = this.channels.getIds();
        String nick = IRCUtils.extractNickFromMask(name);

        for (Object element : ids)
        {
            String curID = element.toString();
            Object curObject = ScriptableObject.getProperty(this.channels, curID);

            if (curObject instanceof Channel)
            {
                Channel curChannel = (Channel) curObject;

                if (ScriptableObject.hasProperty(curChannel.names, nick))
                {
                    curObject = ScriptableObject.getProperty(curChannel.names, nick);

                    if (curObject instanceof Name)
                    {
                        curChannel.onQuit(name, msg);
                    }
                }
            }
        }
    }

    /**
     * Called when a <CODE>TOPIC</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The channel's {@link Channel#onTopic(String, String) onTopic()}
     * method will be called.</li>
     * <li>The Bot's <CODE>HANDLER_ON_TOPIC</CODE> handler will be fired.</li>
     * </ol>
     * 
     * @param channel The channel for which the topic was changed.
     * @param newTopic The new topic.
     * @param changer The changer of the topic.
     * @throws ScriptException If any exceptions occur during event firing.
     */
    void onTopic(String channel, String newTopic, String changer) throws ScriptException
    {
        Channel channelInstance = this.getChildChannel(channel);

        if (channelInstance != null)
        {
            channelInstance.onTopic(newTopic, changer);
        }

        Object[] args = { channel, newTopic, changer };
        this.fireHandler(Bot.HANDLER_ON_TOPIC, args);
    }

    /**
     * Called when a <CODE>NICK</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>Call the {@link Channel#onNick(String, String) onNick()} method of
     * each channel in which the user changing their nick exists.</li>
     * <li>Fire the Bot object's <CODE>HANDLER_ON_NICK</CODE> handler.</li>
     * </ol>
     * 
     * @param name The name of user whose nick is changing.
     * @param newNick The new nick for the name
     * @throws ScriptException If any exceptions occur during event firing.
     */
    void onNick(String name, String newNick) throws ScriptException
    {
        Object[] ids = this.channels.getIds();
        Object curObject = null;
        Channel curChannel = null;
        String oldNick = IRCUtils.extractNickFromMask(name);

        // Call the onNick method of
        // each channel in which the user changing their nick exists.
        for (Object element : ids)
        {
            curObject = ScriptableObject.getProperty(this.channels, element.toString());

            if (curObject != null && curObject instanceof Channel)
            {
                curChannel = (Channel) curObject;

                if (ScriptableObject.hasProperty(curChannel.names, oldNick))
                {
                    curChannel.onNick(name, newNick);
                }
            }
        }

        // Fire the Bot object's onNick handler.
        Object[] args =
        { name, newNick };
        this.fireHandler(Bot.HANDLER_ON_NICK, args);
    }

    /**
     * Called when a <CODE>PRIVMSG</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>Call the
     * {@link Channel#onPrivMsg(String, String) onPrivMsg() method} of the
     * channel.</li>
     * <li>Fire the Bot object's <CODE>HANDLER_ON_PRIVMSG</CODE> handler.</li>
     * </ol>
     * 
     * @param sender The sender of the message.
     * @param target The target of the message.
     * @param text The text of the message.
     * @throws ScriptException If any exceptions occur during event firing.
     */
    void onPrivMsg(String sender, String target, String text) throws ScriptException
    {
        if (ScriptableObject.hasProperty(this.channels, target))
        {
            Channel channelInstance = (Channel) ScriptableObject.getProperty(this.channels, target);
            channelInstance.onPrivMsg(sender, text);
        }

        Object[] args =
        { sender, target, text };
        this.fireHandler(Bot.HANDLER_ON_PRIVMSG, args);
    }

    /**
     * Called when a CTCP <CODE>ACTION</CODE> message is received from the
     * server. The following sequence of actions is performed:
     * <ol>
     * <li>Call the {@link Channel#onAction(String, String) onAction() method}
     * of the channel.</li>
     * <li>Fire the Bot object's <CODE>HANDLER_ON_ACTION</CODE> handler.</li>
     * </ol>
     * 
     * @param sender The sender of the action.
     * @param target The target of the action.
     * @param action The action text.
     * @throws ScriptException If any exceptions occur during event firing.
     */
    void onAction(String sender, String target, String action) throws ScriptException
    {
        Channel channelInstance = this.getChildChannel(target);

        if (channelInstance != null)
        {
            channelInstance.onAction(sender, action);
        }

        Object[] args =
        { sender, target, action };
        this.fireHandler(Bot.HANDLER_ON_ACTION, args);
    }

    /**
     * Gets the child channel of the <CODE>Bot</CODE> object with the given
     * name.
     * 
     * @param channelName The name of the channel.
     * @throws ScriptException If any exceptions occur.
     * @return The <CODE>Channel</CODE> with the given name, or
     *         <CODE>null</CODE> if it does not exist.
     */
    private Channel getChildChannel(String channelName) throws ScriptException
    {
        Channel channelInstance = null;
        Object childObject = ScriptableObject.getProperty(this.channels, channelName);

        if (childObject != null && childObject instanceof Channel)
        {
            channelInstance = (Channel) childObject;
        }

        return channelInstance;
    }

    /**
     * The channels collection in which the bot is currently listening.
     */
    Scriptable channels;
}

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
import net.ardvaark.jackbot.IRCUtils;
import net.ardvaark.jackbot.scripting.ScriptException;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * ECMAScript host object <CODE>Channel</CODE>. This represents a channel in
 * the host object model.
 * 
 * @author Brian Vargas
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 * @since JackBot 1.1
 */
public class Channel extends HostObject
{
    private static final long  serialVersionUID   = 1L;

    /**
     * The ECMAScript class name for this class. This is the name by which the
     * script will refer to the class.
     */
    public static final String ECMA_CLASS_NAME    = "Channel";

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
    public Channel()
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
        return Channel.ECMA_CLASS_NAME;
    }

    /**
     * ECMAScript property get <CODE>names</CODE>. This will get the
     * <CODE>names</CODE> member, which is the collection of names in the
     * channel.
     * 
     * @return The <CODE>names</CODE> member.
     * @see #names
     */
    public Scriptable jsGet_names()
    {
        return this.names;
    }

    /**
     * ECMAScript property get <CODE>name</CODE>. Gets the name of the
     * channel.
     * 
     * @return The name of the channel.
     */
    public String jsGet_name()
    {
        return this.name;
    }

    /**
     * ECMAScript property get <CODE>topic</CODE>. Gets the topic of the
     * channel.
     * 
     * @return The topic of the channel.
     */
    public String jsGet_topic()
    {
        return this.topic;
    }

    /**
     * ECMAScript property set <CODE>topic</CODE>. Causes the client to send
     * the topic message to the server.
     * 
     * @param newTopic The new topic for the channel.
     */
    public void jsSet_topic(String newTopic)
    {
        this.getEngine().getClient().ircWrite(IRC.CMD_TOPIC + " " + this.name + " :" + newTopic);
    }

    /**
     * Called when a <CODE>JOIN</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>A new <CODE>Name</CODE> object is created and added to the
     * <CODE>names</CODE> collection.</li>
     * <li>The <CODE>HANDLER_ON_JOIN</CODE> handler is called.</li>
     * </ol>
     * 
     * @param name The name of the joiner.
     * @throws ScriptException If an exception occurs during event firing.
     */
    void onJoin(String name) throws ScriptException
    {
        String nick = IRCUtils.extractNickFromMask(name);

        if (!nick.equalsIgnoreCase(this.getEngine().getClient().getName()))
        {
            Name nameInstance = HostObjectFactory.newName(nick, this);
            ScriptableObject.putProperty(this.names, nick, nameInstance);
        }

        Object[] args =
        { name };
        this.fireHandler(Channel.HANDLER_ON_JOIN, args);
    }

    /**
     * Called when a <CODE>PART</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The channels's <CODE>HANDLER_ON_PART</CODE> handler will be fired.</li>
     * <li>The <CODE>Name</CODE> object corresponding to the parting name
     * will be removed from the <CODE>names</CODE> collection.</li>
     * </ol>
     * 
     * @param name The name of the parting user.
     * @param msg The part reason.
     * @throws ScriptException If an exception occurs during event firing.
     */
    void onPart(String name, String msg) throws ScriptException
    {
        String nick = IRCUtils.extractNickFromMask(name);

        Object[] args =
        { name, msg };
        this.fireHandler(Channel.HANDLER_ON_PART, args);

        if (!nick.equalsIgnoreCase(this.getEngine().getClient().getName()))
        {
            ScriptableObject.deleteProperty(this.names, nick);
        }
    }

    /**
     * Called when a <CODE>KICK</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The channels's <CODE>HANDLER_ON_KICK</CODE> handler will be fired.</li>
     * <li>The <CODE>Name</CODE> object corresponding to the kicked name will
     * be removed from the <CODE>names</CODE> collection.</li>
     * </ol>
     * 
     * @param name The name that was kicked.
     * @param kicker The user that did the kicking.
     * @param msg The reason for the kick.
     * @throws ScriptException If an exception occurs during event firing.
     */
    void onKick(String name, String kicker, String msg) throws ScriptException
    {
        String nick = IRCUtils.extractNickFromMask(name);

        Object[] args =
        { name, kicker, msg };
        this.fireHandler(Channel.HANDLER_ON_KICK, args);

        if (!nick.equalsIgnoreCase(this.getEngine().getClient().getName()))
        {
            ScriptableObject.deleteProperty(this.names, name);
        }
    }

    /**
     * Called when a <CODE>QUIT</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The channel's <CODE>HANDLER_ON_QUIT</CODE> handler will be fired.</li>
     * <li>The <CODE>Name</CODE> object corresponding to the quitting name
     * will be removed from the <CODE>names</CODE> collection.</li>
     * </ol>
     * 
     * @param name The name of the quitter.
     * @param msg The quit reason.
     * @throws ScriptException If an exception occurs during event firing.
     */
    void onQuit(String name, String msg) throws ScriptException
    {
        String nick = IRCUtils.extractNickFromMask(name);

        Object[] args =
        { name, msg };
        this.fireHandler(Channel.HANDLER_ON_QUIT, args);

        if (!nick.equalsIgnoreCase(this.getEngine().getClient().getName()))
        {
            ScriptableObject.deleteProperty(this.names, name);
        }
    }

    /**
     * Called when a <CODE>TOPIC</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>The channels's <CODE>HANDLER_ON_TOPIC</CODE> handler will be
     * fired.</li>
     * </ol>
     * 
     * @param newTopic The new topic.
     * @param changer The name that changed the topic.
     * @throws ScriptException If an exception occurs during event firing.
     */
    void onTopic(String newTopic, String changer) throws ScriptException
    {
        this.topic = newTopic;

        Object[] args =
        { newTopic, changer };
        this.fireHandler(Channel.HANDLER_ON_TOPIC, args);
    }

    /**
     * Called when the nick of a Name that is in this Channel object is changed.
     * The following sequence of actions is performed:
     * <ol>
     * <li>Call the child Name object's
     * {@link Name#onNick(String) onNick method}.</li>
     * <li>Fire the Channel object's onNick handler.</li>
     * <li>Change the name of the property in the names collection from the old
     * nick to the new nick.</li>
     * </ol>
     * 
     * @param name The name of user whose nick is changing.
     * @param newNick The new nick for the name
     * @throws ScriptException If an error occurs during event firing.
     */
    void onNick(String name, String newNick) throws ScriptException
    {
        String oldNick = IRCUtils.extractNickFromMask(name);
        Name nameInstance = this.getChildName(oldNick);

        // Call the child Name object's onNick method.
        if (nameInstance != null)
        {
            nameInstance.onNick(newNick);
        }

        // Fire the Channel object's onNick handler.
        Object[] args =
        { name, newNick };
        this.fireHandler(Channel.HANDLER_ON_NICK, args);

        // hange the name of the property in the names collection from the
        // old nick to the new nick.
        if (nameInstance != null)
        {
            ScriptableObject.deleteProperty(this.names, oldNick);
            ScriptableObject.putProperty(this.names, newNick, nameInstance);
        }
    }

    /**
     * Called when a <CODE>PRIVMSG</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>Fire the channel object's <CODE>HANDLER_ON_PRIVMSG</CODE> handler.</li>
     * </ol>
     * 
     * @param sender The sender of the message.
     * @param text The text of the message.
     * @throws ScriptException If an error occurs during event firing.
     */
    void onPrivMsg(String sender, String text) throws ScriptException
    {
        Object[] args =
        { sender, text };
        this.fireHandler(Channel.HANDLER_ON_PRIVMSG, args);
    }

    /**
     * Called when a CTCP <CODE>ACTION</CODE> message is received from the
     * server. The following sequence of actions is performed:
     * <ol>
     * <li>Call the {@link Name#onAction(String) onAction() method} of the
     * <CODE>Name</CODE> that performed the action.</li>
     * <li>Fire the channel object's <CODE>HANDLER_ON_ACTION</CODE> handler.</li>
     * </ol>
     * 
     * @param sender The sender of the action.
     * @param action The action text.
     * @throws ScriptException If an exception occurs during event firing.
     */
    void onAction(String sender, String action) throws ScriptException
    {
        Name nameInstance = this.getChildName(IRCUtils.extractNickFromMask(sender));

        if (nameInstance != null)
        {
            nameInstance.onAction(action);
        }

        Object[] args =
        { sender, action };
        this.fireHandler(Channel.HANDLER_ON_ACTION, args);
    }

    /**
     * Gets the child channel of the <CODE>Bot</CODE> object with the given
     * name.
     * 
     * @param name The name of the <CODE>Name</CODE> to locate.
     * @throws ScriptException If an exception occurs during the location.
     * @return The <CODE>Name</CODE> corresponding to the passed name, or
     *         <CODE>null</CODE> if it does not exist.
     */
    private Name getChildName(String name) throws ScriptException
    {
        Name nameInstance = null;
        Object childObject = ScriptableObject.getProperty(this.names, name);

        if (childObject != null && childObject instanceof Name)
        {
            nameInstance = (Name) childObject;
        }

        return nameInstance;
    }

    /**
     * The name of the channel.
     */
    String     name;

    /**
     * The collection of names that are in this channel.
     */
    Scriptable names;

    /**
     * The topic of the channel.
     */
    String     topic;
}

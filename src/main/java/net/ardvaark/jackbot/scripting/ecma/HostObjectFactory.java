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

import java.util.StringTokenizer;

import net.ardvaark.jackbot.IRC;
import net.ardvaark.jackbot.IRCMessage;
import net.ardvaark.jackbot.IRCMessageWaitRestore;
import net.ardvaark.jackbot.scripting.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A class for encapsulating the complexities of setting up host objects in the
 * Rhino runtime.
 * 
 * @version $Revision: 70 $ $Date: 2008-08-05 12:02:51 -0400 (Tue, 05 Aug 2008) $
 */
final class HostObjectFactory
{
    /**
     * Creates a new <CODE>Bot</CODE> object.
     * 
     * @param engine The engine in which the object is created.
     * @param scope The scope in which to create the object.
     * @throws ScriptException If any exceptions occur during creation or
     *         initialization.
     * @return A new <CODE>Bot</CODE> object.
     */
    static final Bot newBot(ECMAEngine engine, Scriptable scope) throws ScriptException
    {
        Bot botInstance = null;

        try
        {
            Context cx = Context.getCurrentContext();
            Object[] args = {};
            botInstance = (Bot) cx.newObject(scope, Bot.ECMA_CLASS_NAME, args);

            botInstance.setEngine(engine);

            botInstance.addEvent(Bot.HANDLER_ON_JOIN);
            botInstance.addEvent(Bot.HANDLER_ON_PART);
            botInstance.addEvent(Bot.HANDLER_ON_KICK);
            botInstance.addEvent(Bot.HANDLER_ON_QUIT);
            botInstance.addEvent(Bot.HANDLER_ON_MSG);
            botInstance.addEvent(Bot.HANDLER_ON_TOPIC);
            botInstance.addEvent(Bot.HANDLER_ON_PRIVMSG);
            botInstance.addEvent(Bot.HANDLER_ON_ACTION);

            botInstance.channels = cx.newObject(botInstance);
        }
        catch (Exception e)
        {
            throw new ScriptException("An exception occurred while create a class of type " + Bot.ECMA_CLASS_NAME, e);
        }

        return botInstance;
    }

    /**
     * Creates a new <CODE>Channel</CODE> object.
     * 
     * @param channelName The name of the channel.
     * @param bot The parent <CODE>Bot</CODE> object of the channel.
     * @throws ScriptException If any exceptions occur during creation or
     *         initialization.
     * @return A new <CODE>Channel</CODE> object.
     */
    static final Channel newChannel(String channelName, Bot bot) throws ScriptException
    {
        Channel channelInstance = null;
        IRCMessageWaitRestore waiter = null;

        try
        {
            ECMAEngine engine = bot.getEngine();
            Context cx = Context.getCurrentContext();
            Object[] args = {};
            channelInstance = (Channel) cx.newObject(bot, "Channel", args);

            channelInstance.setEngine(engine);
            channelInstance.setParent(bot);

            channelInstance.addEvent(Channel.HANDLER_ON_JOIN);
            channelInstance.addEvent(Channel.HANDLER_ON_PART);
            channelInstance.addEvent(Channel.HANDLER_ON_KICK);
            channelInstance.addEvent(Channel.HANDLER_ON_QUIT);
            channelInstance.addEvent(Channel.HANDLER_ON_TOPIC);
            channelInstance.addEvent(Channel.HANDLER_ON_PRIVMSG);
            channelInstance.addEvent(Channel.HANDLER_ON_ACTION);

            channelInstance.name = channelName;
            channelInstance.names = cx.newObject(channelInstance);

            waiter = new IRCMessageWaitRestore(engine.getClient());
            IRCMessage msg = null;
            String curCmd = null;
            boolean gotNames = false;
            boolean gotTopic = false;

            while (!gotNames || !gotTopic)
            {
                msg = waiter.nextMsg();

                curCmd = msg.getCommand();

                if (curCmd.equalsIgnoreCase(IRC.RPL_NAMEREPLY))
                {
                    String msgChannel = msg.getParam(msg.getParamCount() - 2);
                    String nicks = msg.getLastParam();

                    if (msgChannel.equalsIgnoreCase(channelName))
                    {
                        StringTokenizer tok = new StringTokenizer(nicks, " ");
                        String curToken = null;
                        char firstChar;
                        Name nameInstance = null;

                        while (tok.hasMoreTokens())
                        {
                            curToken = tok.nextToken();
                            firstChar = curToken.charAt(0);

                            if (firstChar == '@' || firstChar == '+')
                            {
                                curToken = curToken.substring(1);
                            }

                            nameInstance = HostObjectFactory.newName(curToken, channelInstance);

                            ScriptableObject.putProperty(channelInstance.names, curToken, nameInstance);
                        }
                    }
                }
                else if (curCmd.equalsIgnoreCase(IRC.RPL_ENDOFNAMES))
                {
                    String msgChannel = msg.getParam(msg.getParamCount() - 2);

                    if (msgChannel.equalsIgnoreCase(channelName))
                    {
                        gotNames = true;

                        // Some broken servers will just skip sending the
                        // RPL_NOTOPIC if no topic is sent. So if the
                        // RPL_ENDOFNAMES is sent, make this implicit.
                        gotTopic = true;
                    }
                }
                else if (curCmd.equalsIgnoreCase(IRC.RPL_TOPIC))
                {
                    // The bottom version is for RFC-compliant servers.
                    // This top is for servers that inexplicably
                    // send <nick> <channel> :<topic> instead of just
                    // <channel> :topic
                    if (msg.getParamCount() == 3)
                    {
                        String msgChannel = msg.getParam(1);
                        String channelTopic = msg.getLastParam();

                        if (msgChannel.equalsIgnoreCase(channelName))
                        {
                            channelInstance.topic = channelTopic;
                            gotTopic = true;
                        }
                    }
                    else
                    {
                        String msgChannel = msg.getParam(0);
                        String channelTopic = msg.getLastParam();

                        if (msgChannel.equalsIgnoreCase(channelName))
                        {
                            channelInstance.topic = channelTopic;
                            gotTopic = true;
                        }
                    }
                }
                else if (curCmd.equalsIgnoreCase(IRC.RPL_NOTOPIC))
                {
                    channelInstance.topic = null;
                    gotTopic = true;
                }
            }
        }
        catch (Exception e)
        {
            throw new ScriptException("An exception occurred while create a class of type " + Channel.ECMA_CLASS_NAME, e);
        }
        finally
        {
            if (waiter != null)
            {
                waiter.restore();
            }
        }

        return channelInstance;
    }

    /**
     * Creates a new <CODE>Name</CODE> object.
     * 
     * @param name The name of the new <CODE>Name</CODE>.
     * @param channel The parent <CODE>Channel</CODE> of this name.
     * @throws ScriptException If any exceptions occur during creation or
     *         initialization.
     * @return A new <CODE>Name</CODE> object.
     */
    static final Name newName(String name, Channel channel) throws ScriptException
    {
        Name nameInstance = null;

        try
        {
            ECMAEngine engine = channel.getEngine();
            Context cx = Context.getCurrentContext();
            Object[] args = {};
            nameInstance = (Name) cx.newObject(channel, "Name", args);

            nameInstance.setEngine(engine);
            nameInstance.setParent(channel);

            nameInstance.addEvent(Name.HANDLER_ON_NICK);
            nameInstance.addEvent(Name.HANDLER_ON_ACTION);

            nameInstance.name = name;
        }
        catch (Exception e)
        {
            throw new ScriptException("An exception occurred while create a class of type " + Name.ECMA_CLASS_NAME, e);
        }

        return nameInstance;
    }
}

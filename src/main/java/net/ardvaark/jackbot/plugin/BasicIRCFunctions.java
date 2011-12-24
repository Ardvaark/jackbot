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

package net.ardvaark.jackbot.plugin;

import java.io.IOException;

import net.ardvaark.jackbot.EventIRCClient;
import net.ardvaark.jackbot.IRC;
import net.ardvaark.jackbot.IRCMessage;
import net.ardvaark.jackbot.JackBotPlugin;

/**
 * A basic plugin for providing handling very basic IRC functions. Currently,
 * this handles the PING message, returning the requisite PONG. It also handles
 * the !quit command, although this will eventually be moved into a script.
 * 
 * @author Brian Vargas
 * @version $Revision: 59 $ $Date: 2008-05-29 23:00:24 -0400 (Thu, 29 May 2008) $
 */
public class BasicIRCFunctions implements JackBotPlugin
{
    /**
     * Default constructor. Does nothing.
     */
    public BasicIRCFunctions()
    {
    }

    /**
     * Handles IRC events from the client. This currently handles the PING
     * message, and does a special case PRIVMSG handling for the !quit command,
     * causing the bot to exit. This !quit handling will eventually be done in a
     * script, and this code will be removed.
     * 
     * @param msg The message received from the server.
     * @param client The client that is connected to the server.
     */
    public void ircMessageReceived(IRCMessage msg, EventIRCClient client)
    {
        if (msg.getCommand().equalsIgnoreCase(IRC.CMD_PING))
        {
            String arg = "";

            if (msg.getParamCount() > 0)
            {
                arg = " " + msg.getParam(0);
            }

            try
            {
                client.ircWriteNow("PONG" + arg);
            }
            catch (IOException e)
            {
            }
        }/*
             * else if (msg.getCommand().equalsIgnoreCase("PRIVMSG")) { if
             * (msg.getParamCount() >= 2) { if
             * (msg.getParam(1).equalsIgnoreCase("!quit")) {
             * client.setRunning(false); } } }
             */
    }
}

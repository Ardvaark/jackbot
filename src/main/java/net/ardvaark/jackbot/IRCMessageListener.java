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

/**
 * Interface that defines a listener for IRC Messages.
 * 
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public interface IRCMessageListener
{
    /**
     * Called when an IRC message is received from the server.
     * 
     * @param msg The message that was received.
     * @param client The client that sent the message.
     */
    public void ircMessageReceived(IRCMessage msg, EventIRCClient client);
}

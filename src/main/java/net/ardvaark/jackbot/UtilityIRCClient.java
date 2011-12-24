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
 * Provides some helpful IRC client methods to subclasses.
 * 
 * @author Brian Vargas
 * @since JackBot v1.0
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public class UtilityIRCClient extends net.ardvaark.jackbot.FloodControlIRCClient
{
    /**
     * Constructs a <CODE>UtilityIRCClient</CODE> object.
     * 
     * @param name The name of the client.
     * @param desc The description of the client.
     */
    public UtilityIRCClient(String name, String desc)
    {
        super(name, desc);
    }

    /**
     * Sends a <CODE>NOTICE</CODE> message to the server.
     * 
     * @param target The target of the notice.
     * @param msg The message in the notice.
     */
    public void ircNotice(String target, String msg)
    {
        this.ircWrite("NOTICE " + target + " :" + msg);
    }

    /**
     * Responds to a sender/target pair with the given message in a
     * <CODE>NOTICE</CODE>. If the target is a channel, the responds will be
     * sent to the target. If the target is not the channel (and is therefore
     * the client), then the response will be sent to the sender.
     * 
     * @param sender The sender of the message to which the client is
     *        responding.
     * @param target The target of the message to which the client is
     *        responding.
     * @param msg The message to send.
     */
    public void ircRespond(String sender, String target, String msg)
    {
        String myTarget;

        if (target.charAt(0) == '#')
        {
            myTarget = target;
        }
        else
        {
            myTarget = IRCUtils.extractNickFromMask(sender);
        }

        this.ircNotice(myTarget, msg);
    }
}

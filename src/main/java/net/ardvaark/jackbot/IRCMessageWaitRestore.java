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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Like the {@link IRCMessageWait IRCMessageWait} class, the
 * <CODE>IRCMessageWaitRestore</CODE> class will wait on an
 * {@link EventIRCClient EventIRCClient}. However, it provides the
 * {@link #restore() restore()} method for returning methods that have been
 * retrieved back to the input queue.
 * 
 * @since JackBot v1.1
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 * @author Brian Vargas
 */
public class IRCMessageWaitRestore extends IRCMessageWait
{
    /**
     * Constructs a new <CODE>IRCMessageWaitRestore</CODE> object.
     * 
     * @param client The client on which to wait.
     */
    public IRCMessageWaitRestore(EventIRCClient client)
    {
        super(client);
        this.queueRestore = new LinkedList<String>();
    }

    /**
     * Gets the next raw IRC message from the server. The message is saved in an
     * internal list for later restoration to the input queue.
     * 
     * @throws IRCConnectionLostException If the connection to the server is
     *         lost during the wait.
     * @return The next raw IRC message from the server.
     */
    @Override
    public String nextMsgString() throws IOException, IRCConnectionLostException
    {
        String msg = super.nextMsgString();
        this.queueRestore.addLast(msg);
        return msg;
    }

    /**
     * Restores the saved messages to the input queue of the client.
     */
    public void restore()
    {
        List<String> inputQueue = this.getInputQueue();
        inputQueue.addAll(0, this.queueRestore);
    }

    /**
     * The list of messages to be restored.
     */
    private LinkedList<String> queueRestore;
}

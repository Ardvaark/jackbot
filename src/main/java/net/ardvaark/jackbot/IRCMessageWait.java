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
import java.util.List;

/**
 * <p>
 * The <code>IRCMessageWait</code> class can be used to implement message
 * loops external to the <code>EventIRCClient</code> class. This is especially
 * useful when trying to provide the illusion of synchronous work in the
 * asynchronous IRC environment
 * </p>
 * <p>
 * See the {@link IRCMessageWaitRestore IRCMessageWaitRestore class} for a more
 * robust solution that allows you to automatically return the input queue to
 * its state before the waiting occurred.
 * </p>
 * 
 * @since JackBot v1.1
 * @version $Revision: 61 $ $Date: 2008-06-08 00:48:18 -0400 (Sun, 08 Jun 2008) $
 * @author Brian Vargas
 */
public class IRCMessageWait
{
    /**
     * Constructs an <code>IRCMessageWait</code> object that interacts with
     * the given client.
     * 
     * @param client The client with which this waiter should interact.
     */
    public IRCMessageWait(EventIRCClient client)
    {
        this.client = client;
        this.inputQueue = this.client.getInputQueue();
    }

    /**
     * Gets the next <CODE>IRCMessage</CODE> from the input queue.
     * 
     * @throws IRCConnectionLostException If the connection to the server has
     *         been lost during the wait.
     * @return An <CODE>IRCMessage</CODE> object that is the next message in
     *         the input queue.
     */
    public IRCMessage nextMsg() throws IOException, IRCConnectionLostException
    {
        return IRCMessage.parseMessage(this.nextMsgString());
    }

    /**
     * Gets the next raw IRC message in the input queue.
     * 
     * @throws IRCConnectionLostException If the connection to the server is
     *         lost during the wait.
     * @return The next raw IRC message in the input queue.
     */
    public String nextMsgString() throws IOException, IRCConnectionLostException
    {
        String msg = null;

        if (this.client.getRunning())
        {
            if (this.inputQueue.size() > 0)
            {
                msg = this.client.ircRead();
            }
            else
            {
                this.client.waitForIrcInput();
                msg = this.client.ircRead();
            }
        }

        return msg;
    }

    /**
     * Gets the client on which this <CODE>IRCMessageWait</CODE> object is
     * waiting.
     * 
     * @return The client on which this <CODE>IRCMessageWait</CODE> object is
     *         waiting.
     */
    protected EventIRCClient getClient()
    {
        return this.client;
    }

    /**
     * Gets the input queue.
     * 
     * @return The input queue.
     */
    protected List<String> getInputQueue()
    {
        return this.inputQueue;
    }

    /**
     * The client on which the <CODE>IRCMessageWait</CODE> object is waiting.
     */
    private EventIRCClient client;

    /**
     * The input queue.
     */
    private List<String>   inputQueue;
}

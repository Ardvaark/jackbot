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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.ardvaark.jackbot.logging.Log;

/**
 * <p>
 * An IRC client that allows for arbitrary listeners to register to be informed
 * when an IRC message arrives from the server.
 * </p>
 * 
 * <p>
 * This class also will continually ping the server in order to make sure the
 * connection is still in operation. If it detects a disconnect, the
 * <code>run()</code> method will throw an exception in order for an attempt
 * at reconnection.
 * </p>
 * 
 * @see IRCMessageListener
 * @author Brian Vargas
 * @version $Revision: 64 $ $Date: 2008-06-08 12:55:02 -0400 (Sun, 08 Jun 2008) $
 */
public class EventIRCClient extends UtilityIRCClient
{
    private static final Log log = Log.getLogger(EventIRCClient.class);
    
    /**
     * Constructs an EventIRCClient with the give name and description.
     * 
     * @param name The name of the client. This is used for both the nick and
     *        the real name.
     * @param desc The description of the client.
     */
    public EventIRCClient(String name, String desc)
    {
        super(name, desc);

        this.ircMessageListeners = new ArrayList<IRCMessageListener>(4);
        this.inputQueue = Collections.synchronizedList(new LinkedList<String>());
    }

    /**
     * Implements the basic run loop of an IRC client.
     * 
     * @throws IRCConnectionLostException if the connection to the server is
     *         lost for any reason.
     */
    public void run() throws IOException, IRCConnectionLostException
    {
        String ircString;

        while (this.getRunning())
        {
            this.waitForIrcInput();

            while (this.inputQueue.size() > 0)
            {
                ircString = this.ircRead();

                if (ircString == null || ircString.length() == 0)
                {
                    continue;
                }

                IRCMessage msg = IRCMessage.parseMessage(ircString);
                this.dispatchMessage(msg);
            }
        }
    }

    @Override
    public String ircRead()
    {
        String msg = this.inputQueue.remove(0);
        // Logger.getLogger("net.ardvaark.jackbot").finest("Message removed from
        // queue: " + msg.toString());
        return msg;
    }

    public void waitForIrcInput() throws IOException, IRCConnectionLostException
    {
        do
        {
            String line = super.ircRead();
            this.inputQueue.add(line);

        } while (!super.willBlock());
    }

    /**
     * Dispatches a message to any listeners registered with the client for that
     * message. This method also does a little bit of special case handling for
     * the PONG message.
     * 
     * @param msg The message to dispatch.
     */
    private void dispatchMessage(IRCMessage msg)
    {
        for (IRCMessageListener listener : this.ircMessageListeners)
        {
            try
            {
                listener.ircMessageReceived(msg, this);
            }
            catch (Exception e)
            {
                log.error("Error dispatching IRC message.", e);
            }
        }
    }

    /**
     * Adds a message listener to the collection of listeners.
     * 
     * @param l The listener to add.
     */
    public void addMessageListener(IRCMessageListener l)
    {
        if (l != null)
        {
            this.ircMessageListeners.add(l);
        }
    }

    /**
     * Sets the running flag.
     * 
     * @param flag The value to store as the running flag.
     */
    public synchronized void setRunning(boolean flag)
    {
        this.running = flag;
    }

    /**
     * Gets the running flag.
     * 
     * @return <CODE>true</CODE> if the bot is running, or <CODE>false</CODE>
     *         if it should exit.
     */
    public synchronized boolean getRunning()
    {
        return this.running;
    }

    /**
     * Gets the input queue. IRC messages are added to this queue by the
     * InputThread. Note that these are not <code>IRCMessage</code> objects,
     * but are simply the strings received from the server.
     * 
     * @return the queue to which IRC messages are added.
     */
    public List<String> getInputQueue()
    {
        return this.inputQueue;
    }

    /**
     * The running flag.
     */
    private boolean                       running = true;

    /**
     * The collection of IRCMessageListeners.
     */
    private ArrayList<IRCMessageListener> ircMessageListeners;

    /**
     * The input queue of unparsed messages from the server.
     */
    private List<String>                  inputQueue;
}

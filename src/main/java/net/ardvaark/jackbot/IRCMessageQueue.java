package net.ardvaark.jackbot;

import java.util.LinkedList;

/**
 * A queue of IRCMessage objects. This class is not synchronized, and should be
 * manually synchronized for multi-threaded operations.
 * 
 * @author Brian Vargas
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 *          since v.1a
 */
public final class IRCMessageQueue
{
    private static IRCMessage IDLE_MESSAGE;

    static
    {
        IRCMessageQueue.IDLE_MESSAGE = new IRCMessage(IRCMessage.MSG_TYPE_IDLE);
    }

    /**
     * Creates an empty IRCMessageQueue.
     */
    public IRCMessageQueue()
    {
        this.queue = new LinkedList<IRCMessage>();
    }

    /**
     * Adds an item to the end of the queue.
     * 
     * @param message The message to add to the end of the queue.
     */
    public void put(IRCMessage message)
    {
        this.queue.addLast(message);
    }

    /**
     * Gets a message from the front of the queue. This will actually remove the
     * item from the queue.
     * 
     * @return The front item in the queue.
     */
    public IRCMessage get()
    {
        IRCMessage ret;

        if (this.queue.size() == 0)
        {
            ret = IRCMessageQueue.IDLE_MESSAGE;
        }
        else
        {
            ret = this.queue.removeFirst();
        }

        return ret;
    }

    /**
     * Gets a message from the front of the queue. This will not remove the item
     * from the queue.
     * 
     * @return The front item in the queue.
     */
    public IRCMessage peek()
    {
        IRCMessage ret;

        if (this.queue.size() == 0)
        {
            ret = IRCMessageQueue.IDLE_MESSAGE;
        }
        else
        {
            ret = this.queue.getFirst();
        }

        return ret;
    }

    /**
     * The linked list on top of which the queue is built.
     */
    private LinkedList<IRCMessage> queue;
}

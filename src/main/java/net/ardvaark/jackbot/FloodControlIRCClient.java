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

import net.ardvaark.jackbot.logging.Log;

/**
 * <p>
 * Extends IRCClient and adds support for writing to the IRC server with flood
 * control.
 * </p>
 * <p>
 * Flood control is implemented using a seperate thread to actually send
 * messages to the server. The <code>ircWrite()</code> method does not write
 * to the server immediately, instead adding the message to a queue of outgoing
 * messages. It then wakes up the send thread to send the message, in order to
 * minimize latency.
 * </p>
 * <p>
 * The flood control thread assigns a weight value to every message sent, and
 * adds that value to an internal counter. If the counter passes some threshold,
 * then the message is not sent. Every second, another thread decrements that
 * weight value and signals the send thread to check to see if it can send now.
 * Thus, the goal of limiting the client to sending no more than x messages in n
 * seconds is achieved.
 * </p>
 * 
 * @author Brian Vargas
 * @version $Revision: 61 $ $Date: 2008-06-08 00:48:18 -0400 (Sun, 08 Jun 2008) $
 */
public class FloodControlIRCClient extends IRCClient
{
    private static final Log log = Log.getLogger(FloodControlIRCClient.class);
    
    /**
     * Basic constructor. Creates and starts the send thread.
     * 
     * @param name The name of the client.
     * @param desc The description of the client.
     * @see IRCClient#IRCClient(String, String)
     */
    public FloodControlIRCClient(String name, String desc)
    {
        super(name, desc);
        this.workerThread = new OutputThread(this);
        this.workerThread.start();
    }

    /**
     * Writes a message to the IRC server, bypassing the flood queue. This
     * method is thread-safe.
     * 
     * @param msg The raw IRC to write to the server.
     * @throws IOException Thrown if the underlying IO operations fail.
     */
    public void ircWriteNow(String msg) throws IOException
    {
        log.trace("Writing \"{0}\"", msg);
        super.ircWrite(msg);
    }

    /**
     * Writes to the IRC server using flood control.
     * 
     * @param msg The message to send.
     */
    @Override
    public void ircWrite(String msg)
    {
        this.workerThread.ircWrite(msg);
    }

    /**
     * The worker thread that actually sends stuff to the server.
     */
    private OutputThread workerThread;

    /**
     * This inner class is the thread that actually sends messages to the
     * server. It implements the weighting algorithm. This thread runs as a
     * daemon thread.
     */
    private class OutputThread extends Thread
    {
        /**
         * Constructs the output thread for the client. Creates and starts
         * another child thread that decrements its internal flood weight
         * counter.
         * 
         * @param client The client that has created the thread.
         */
        OutputThread(FloodControlIRCClient client)
        {
            super("FloodControlIRCClient Output Thread");

            this.client = client;
            this.outputQueue = new LinkedList<String>();
            this.floodWeight = 0.0;
            this.setDaemon(true);
            this.floodThreshhold = 3.9;
            this.weightThread = new WeightDecrementThread(this);
            this.weightThread.start();
        }

        /**
         * The action of the thread. This runs an infinite loop that performs
         * the follwing checks, aborting if any of them fail:
         * <ol>
         * <li>Is the flood weight less than the threshold?</li>
         * <li>Are there messages to be sent?</li>
         * <li>Send messages until there are no more to send, or until the
         * flood weight exceeds the threshold.</li>
         * <li>Sleep for 10 seconds.</li>
         * </ol>
         */
        @Override
        public void run()
        {
            try
            {
                int queueLength;
                String msg;

                while (true)
                {
                    synchronized (this.outputQueue)
                    {
                        while (this.outputQueue.size() <= 0)
                        {
                            this.outputQueue.wait();
                        }

                        queueLength = this.outputQueue.size();
                    }

                    synchronized (this)
                    {
                        while (queueLength-- > 0)
                        {
                            if (this.floodWeight >= this.floodThreshhold)
                            {
                                while (!this.outputSignaled)
                                {
                                    this.wait();
                                }

                                this.outputSignaled = false;
                            }

                            synchronized (this.outputQueue)
                            {
                                if (this.outputQueue.size() > 0)
                                {
                                    msg = this.outputQueue.remove(0);
                                }
                                else
                                {
                                    msg = null;
                                }
                            }

                            if (msg != null)
                            {
                                this.client.ircWriteNow(msg);
                                this.floodWeight++;
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
            }
        }

        /**
         * Places the given message in the outgoing queue.
         * 
         * @param msg The message to be sent to the server.
         */
        public void ircWrite(String msg)
        {
            synchronized (this.outputQueue)
            {
                this.outputQueue.add(msg);
                this.outputQueue.notify();
            }
        }

        /**
         * A link back to the client.
         */
        private FloodControlIRCClient client;

        /**
         * The queue of outgoing messages to be sent to the IRC server.
         */
        private LinkedList<String>    outputQueue;

        /**
         * The current flood weight of the client.
         */
        private double                floodWeight;

        /**
         * The threshold value at which the implementation will stop sending
         * messages because it is flooding the server.
         */
        public double                 floodThreshhold;

        /**
         * The thread that decremements the weight value.
         */
        private WeightDecrementThread weightThread;

        /**
         * Whether or not the output has been signaled.
         */
        private boolean               outputSignaled;

        /**
         * This class is a thread that decrements the flood weight counter of
         * the output thread. It runs an infinite loop that decrements its
         * parent's weight counter every 1 second. This thread runs as a daemon
         * thread.
         */
        private class WeightDecrementThread extends Thread
        {
            /**
             * Constructs the thread.
             * 
             * @param outputThread The output thread for which this thread has
             *        been created.
             */
            public WeightDecrementThread(OutputThread outputThread)
            {
                super("FloodControlIRCClient Weight Decrement Thread");

                this.outputThread = outputThread;
                this.setDaemon(true);
            }

            /**
             * The thread entry point. Sleeps for 1 second, decrememnts the
             * counter, and wakes up its conatining output thread.
             */
            @Override
            public void run()
            {
                try
                {
                    while (true)
                    {
                        Thread.sleep(1000);

                        synchronized (this.outputThread)
                        {
                            this.outputThread.floodWeight -= 0.33;

                            if (this.outputThread.floodWeight < 0.0)
                            {
                                this.outputThread.floodWeight = 0.0;
                            }

                            this.outputThread.outputSignaled = true;
                            this.outputThread.notify();
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }

            /**
             * Reference to the output thread for which this thread is working.
             */
            private OutputThread outputThread;
        }
    }
}

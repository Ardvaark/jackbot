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

import net.ardvaark.jackbot.logging.Log;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Implements an IRC client with minimal functionality. This includes:
 * connection, logon, logoff, disconnection. This code is based on the Java IRC
 * bot by David Seagler.
 * http://www-106.ibm.com/developerworks/java/library/j-javabot/#h2
 * 
 * @author Brian Vargas
 * @version $Revision: 61 $ $Date: 2008-06-08 00:48:18 -0400 (Sun, 08 Jun 2008) $
 */
public class IRCClient
{
    private static final Log log = Log.getLogger(IRCClient.class);
    
    /**
     * The default socket timeout in ticks.
     */
    public static final int DEFAULT_SOCKET_TIMEOUT             = 60000 * 3;

    /**
     * The minimum size of the receive buffer in the socket.
     */
    public static final int MINIMUM_SOCKET_RECEIVE_BUFFER_SIZE = 1024 * 8;

    /**
     * Constructs a IRCClient with the given name and description.
     * 
     * @param name The name of the bot. This will be used for both its username
     *        and nick.
     * @param desc The description string of the bot.
     */
    public IRCClient(String name, String desc)
    {
        this.name = name;
        this.desc = desc;
        this.initializeObject();
    }

    private void initializeObject()
    {
        // Set the timeout.
        this.socketTimeout = IRCClient.DEFAULT_SOCKET_TIMEOUT;

        // Set the receive buffer.
        this.socketReceiveBufferMinimumSize = IRCClient.MINIMUM_SOCKET_RECEIVE_BUFFER_SIZE;

        // Initialize the ping state variable.
        this.waitingForPong = false;
    }

    /**
     * Connects the client to the specified server. This method does the work of
     * connecting to the actual server host. It sets constructs a socket
     * connection and creates buffered readers and writers for that connection.
     * 
     * @param hostname The name of the host server to which to connect.
     * @param port The port number to which to connect.
     * @param useSsl Whether the connection should be made over SSL.
     * @throws IOException Thrown if any errors occur in the underlying IO
     *         classes.
     */
    public void connect(String hostname, int port, boolean useSsl) throws IOException
    {
        SocketFactory socketFactory;
        
        if (useSsl) {
            socketFactory = SSLSocketFactory.getDefault();
        }
        else {
            socketFactory = SocketFactory.getDefault();
        }

        // Create a channel to the server.
        this.socket = socketFactory.createSocket(hostname, port);

        // Set the timeout on the socket read.
        this.socket.setSoTimeout(this.socketTimeout);

        // Set the buffer size, if necessary.
        int receiveBufferSize = this.socket.getReceiveBufferSize();

        if (receiveBufferSize < this.socketReceiveBufferMinimumSize)
        {
            this.socket.setReceiveBufferSize(this.socketReceiveBufferMinimumSize);
        }

        // Set up the reader.
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

        // Set up the writer.
        this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    }

    /**
     * Connects the client to the specified server. Calls connect(String, int)
     * with a default port of 6667.
     * 
     * @param hostname The name of the host server to which to connect.
     * @throws IOException Thrown if any errors occur in the underlying IO
     *         classes.
     * @see #connect(String, int, boolean)
     */
    public void connect(String hostname) throws IOException
    {
        this.connect(hostname, 6667, false);
    }

    /**
     * Logs on the bot to an IRC server and sets the nick. The nick and logon
     * name are both set to the bot's name. The host is currently set to "host"
     * and the server is currently set to "server".
     * 
     * @throws IOException Thrown if any errors occur in the underlying IO
     *         classes.
     */
    public void logon() throws IOException
    {
        log.info("Logging in as {0}", this.name);

        this.writeString("USER " + this.name + " host irc :" + this.desc + IRC.NEWLINE);
        this.writeString("NICK " + this.name + IRC.NEWLINE);

        log.info("Login complete");
    }

    /**
     * Logs the user off the system cleanly. Sends the QUIT message to the
     * server.
     * 
     * @param quitMsg The quit message to print while leaving.
     */
    public void logoff(String quitMsg) throws IOException
    {
        log.info("Quitting IRC ({0})", quitMsg);
        this.writeString("QUIT :" + quitMsg + "\r\n");
    }

    /**
     * Logs the user off the system cleanly. Calls logoff(String) with
     * "Leaving.".
     * 
     * @see #logoff(String)
     */
    public void logoff() throws IOException
    {
        this.logoff("Leaving.");
    }

    /**
     * Disconnects the client from the server in a clean fashion.
     */
    public void disconnect() throws IOException
    {
        log.info("Disconnecting from server.");
        this.socket.close();
    }

    /**
     * Writes a message to the IRC server.
     * 
     * @param msg The raw IRC to write to the server.
     * @throws IOException Thrown if the underlying write to the socket
     *         encounters any errors.
     */
    public void ircWrite(String msg) throws IOException
    {
        this.writeString(msg + IRC.NEWLINE);
    }

    /**
     * Reads a string from the IRC server. This method will block if there is no
     * data to be read. Use the {@link #willBlock() willBlock()} method to be
     * sure this method will not block.
     * 
     * @return A string read from the IRC server, or <c>null</c> if the end of
     *         the stream has been reached.
     * @throws IOException Thrown if the underlying read operation fails.
     * @throws IRCConnectionLostException Thrown if the socket times out,
     *         indicating that the connection was lost.
     * @see #willBlock()
     */
    public String ircRead() throws IOException, IRCConnectionLostException
    {
        String input = null;

        this.waitingForPong = false;

        do
        {
            try
            {
                input = this.readString();
                this.waitingForPong = false;
            }
            catch (SocketTimeoutException e)
            {
                if (this.waitingForPong)
                {
                    throw new IRCConnectionLostException("The connection to the IRC server has timed out.", e);
                }

                this.ircWrite("PING :" + this.name);
                this.waitingForPong = true;
            }
            catch (IOException e)
            {
                throw new IRCConnectionLostException("The connection to the IRC server has timed out.", e);
            }

        } while (this.waitingForPong);

        return input;
    }

    /**
     * Determines if a call to the {@link #ircRead() ircRead()} method will
     * block the calling thread.
     * 
     * @return Returns <c>true</c> if the next call to
     *         {@link #ircRead() ircRead()} will block. Note that a return value
     *         of <c>false</c> does not guarantee that a call to
     *         {@link #ircRead() ircRead()} will not block.
     */
    public boolean willBlock() throws IOException
    {
        return !this.in.ready();
    }

    /**
     * Gets the name of the client.
     * 
     * @return The name of the client.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the name of the client.
     * 
     * @param newName The new name of the bot.
     */
    public void setName(String newName)
    {
        this.name = newName;
    }

    /**
     * Gets the timeout for the IO channel.
     * 
     * @return The timeout for the IO channel in milliseconds.
     */
    public int getTimeout()
    {
        return this.socketTimeout;
    }

    /**
     * Sets the timeout fot he IO channel.
     * 
     * @param timeout The timeout for the IO channel in milliseconds.
     */
    public void setTimeout(int timeout)
    {
        this.socketTimeout = timeout;
    }

    /**
     * Gets the real name of the client.
     * 
     * @return The real name of the client.
     */
    public String getRealName()
    {
        return this.realName;
    }

    /**
     * Sets the real name of the client.
     * 
     * @param realName The real name of the client.
     */
    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    /**
     * Waits on the socket channel.
     * 
     * @return Returns <code>true</code> if there is data to be read from the
     *         IO channel. Returns <code>false</code> if there is not, which
     *         is likely caused by a timeout on the wait.
     * @throws IOException Thrown by the underlying channel operations.
     * @see #socketTimeout
     * @see #setTimeout(int)
     * @see #getTimeout()
     */
    /*
     * public void waitForIrcInput() throws IOException,
     * IRCConnectionLostException {
     * Logger.getLogger("net.ardvaark.jackbot").finest("Waiting for IRC
     * input...");
     * 
     * do { int readyCount = this.selector.select(this.socketTimeout);
     * 
     * if (readyCount == 0 || !this.key.isValid()) { throw new
     * IRCConnectionLostException("The connection to the IRC server has been
     * lost."); } } while (!this.key.isReadable());
     * 
     * Logger.getLogger("net.ardvaark.jackbot").finest("Wait complete!"); }
     */

    /**
     * Writes the given string to the socket.
     * 
     * @param stringToWrite The string to write to the socket.
     * @throws IOException Thrown by the underlying socket operations.
     */
    private void writeString(String stringToWrite) throws IOException
    {
        this.out.write(stringToWrite);
        this.out.flush();
    }

    /**
     * Reads a string fron the socket's input stream. This method will block if
     * there is no line to be read.
     * 
     * @return The string that was read from the input stream.
     * @throws IOException Thrown by the underlying socket implementation.
     */
    private String readString() throws IOException
    {
        String line = this.in.readLine();

        if (line == null)
        {
            throw new EOFException("End of stream received from server.");
        }

        return line;
    }

    /**
     * Gets whether or not the communication channel is connected to the server.
     * 
     * @return Returns <c>true</c> if the channel is connected, or <c>false</c>
     *         if it is not.
     */
    public boolean isConnected()
    {
        return this.socket.isConnected();
    }

    /**
     * The bot's username and nick.
     */
    private String         name;

    /**
     * The bot's real name.
     */
    private String         realName;

    /**
     * The bot's description string.
     */
    private String         desc;

    /**
     * The amount of time until a timeout is considered to have occured.
     */
    private int            socketTimeout;

    /**
     * The minimum size of the socket's receive buffer.
     */
    private int            socketReceiveBufferMinimumSize;

    /**
     * The socket with which to connect to the IRC server.
     */
    private Socket         socket;

    /**
     * The input reader.
     */
    private BufferedReader in;

    /**
     * The output writer.
     */
    private BufferedWriter out;

    /**
     * Whether or not a ping has been sent to the server.
     */
    private boolean        waitingForPong;
}

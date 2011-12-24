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
 * A server to which the bot can connect. These are read from the configuration
 * file.
 * 
 * @author Brian Vargas
 * @since JackBot v1.1
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
final class Server
{
    /**
     * Constructs a server object with the given name and port number.
     * 
     * @param name The name of the server.
     * @param port The port number.
     */
    public Server(String name, int port)
    {
        this.name = name;
        this.port = port;
    }

    /**
     * Constructs a server with the given name and port number. The port number
     * will be parsed as an integer.
     * 
     * @param name The name of the server.
     * @param port The port number of the server.
     */
    public Server(String name, String port)
    {
        this.name = name;
        this.port = Integer.parseInt(port);
    }

    /**
     * Gets the name of the server.
     * 
     * @return The name of the server.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Gets the port number of the server.
     * 
     * @return The port number of the server.
     */
    public int getPort()
    {
        return this.port;
    }

    /**
     * The name of the server.
     */
    private String name;

    /**
     * The port of the server.
     */
    private int    port;
}

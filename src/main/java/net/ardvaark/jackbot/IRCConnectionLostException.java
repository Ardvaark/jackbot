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
 * Indicates the network connection to the server has been lost.
 * 
 * @author Brian Vargas
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public class IRCConnectionLostException extends IRCException
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new IRCConnectionLostException.
     * 
     * @param msg The message.
     */
    public IRCConnectionLostException(String msg)
    {
        this(msg, null);
    }

    /**
     * Constructs a new IRCConnectionLostException.
     * 
     * @param msg The message.
     * @param orig The original cause exception of this exception.
     */
    public IRCConnectionLostException(String msg, Exception orig)
    {
        super(msg, orig);
    }
}

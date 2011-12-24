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
 * A class for exceptions occuring inside the IRC bot. The IRCException class
 * extends regular Exceptions by allowing for inner exceptions, which are an
 * exception that caused this exception to be throw.
 * 
 * @author Brian Vargas
 * @version $Revision: 66 $ $Date: 2008-07-04 14:29:04 -0400 (Fri, 04 Jul 2008) $
 */
public class IRCException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an IRCException.
     * 
     * @param message The message.
     * @param originalException The original exception.
     */
    public IRCException(String message, Exception originalException)
    {
        super(message, originalException);
    }

    /**
     * Constructs an IRCException.
     * 
     * @param originalException The original exception.
     */
    public IRCException(Exception originalException)
    {
        this(originalException.getMessage(), originalException);
    }
    
    /**
     * Constructs an IRCException.
     * 
     * @param message The message.
     */
    public IRCException(String message)
    {
        super(message);
    }

    /**
     * Constructs an IRCException.
     */
    public IRCException()
    {
        super("An exception occured in the IRC client.");
    }
}

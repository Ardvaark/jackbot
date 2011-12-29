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

package net.ardvaark.jackbot.scripting;

/**
 * An exception that is thrown by JackBot scripting engines. Check the inner
 * exception for more details.
 * 
 * @author Brian Vargas
 * @since JackBot v1.1
 * @version $Revision: 66 $ $Date: 2008-07-04 14:29:04 -0400 (Fri, 04 Jul 2008) $
 */
public class ScriptException extends net.ardvaark.jackbot.IRCException
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new <CODE>ScriptException</CODE>.
     * 
     * @param msg The message.
     */
    public ScriptException(String msg)
    {
        super(msg);
    }
    
    /**
     * Constructs a new <CODE>ScriptException</CODE>.
     * 
     * @param inner The exception which caused this exception.
     */
    public ScriptException(Exception inner)
    {
        super(inner);
    }

    /**
     * Constructs a new <CODE>ScriptException</CODE>.
     * 
     * @param msg The message.
     * @param innerException The exception which caused this exception.
     */
    public ScriptException(String msg, Exception innerException)
    {
        super(msg, innerException);
    }
}

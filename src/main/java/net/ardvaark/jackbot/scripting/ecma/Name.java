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

package net.ardvaark.jackbot.scripting.ecma;

import net.ardvaark.jackbot.scripting.ScriptException;

/**
 * ECMAScript host object <CODE>Name</CODE>.
 * 
 * @author Brian Vargas
 * @since JackBot v1.1
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public class Name extends HostObject
{
    private static final long  serialVersionUID  = 1L;

    /**
     * The ECMAScript class name for this class. This is the name by which the
     * script will refer to the class.
     */
    public static final String ECMA_CLASS_NAME   = "Name";

    /**
     * The ECMAScript handler to be called when a <CODE>NICK</CODE> message is
     * received from the server.
     */
    public static final String HANDLER_ON_NICK   = "onNick";

    /**
     * The ECMAScript handler to be called when a CTCP <CODE>ACTION</CODE>
     * message is received from the server.
     */
    public static final String HANDLER_ON_ACTION = "onAction";

    /**
     * Default constructor.
     */
    public Name()
    {
    }

    /**
     * Gets the ECMAScript class name for this object.
     * 
     * @return The ECMAScript class name for this object.
     */
    @Override
    public String getClassName()
    {
        return Name.ECMA_CLASS_NAME;
    }

    /**
     * ECMAScript property get <CODE>name</CODE>. Gets the <CODE>name</CODE>
     * member, which is the name of the <CODE>Name</CODE>.
     * 
     * @return The <CODE>name</CODE> member.
     */
    public String jsGet_name()
    {
        return this.name;
    }

    /**
     * Called when the <CODE>NICK</CODE> message is received from the server.
     * The following sequence of actions is performed:
     * <ol>
     * <li>Fire the <CODE>HANDLER_ON_NICK</CODE> handler.</li>
     * <li>Update the this.name to be the new nick.</li>
     * </ol>
     * 
     * @param newNick the new nick for the name
     * @throws ScriptException If an exception occurs during event firing.
     */
    void onNick(String newNick) throws ScriptException
    {
        // Fire the JavaScript handler onNick.
        Object[] args =
        { newNick };
        this.fireHandler(Name.HANDLER_ON_NICK, args);

        // Update the this.name to be the new nick.
        this.name = newNick;
    }

    /**
     * Called when the <CODE>ACTION</CODE> message is received from the
     * server. The following sequence of actions is performed:
     * <ol>
     * <li>Fire the <CODE>HANDLER_ON_ACTION</CODE> handler.</li>
     * <li>Update the this.name to be the new nick.</li>
     * </ol>
     * 
     * @param action The action that was performed.
     * @throws ScriptException If any exceptions occur during event firing.
     */
    void onAction(String action) throws ScriptException
    {
        Object[] args =
        { action };
        this.fireHandler(Name.HANDLER_ON_ACTION, args);
    }

    /**
     * The name of the <CODE>Name</CODE>.
     */
    String name;
}

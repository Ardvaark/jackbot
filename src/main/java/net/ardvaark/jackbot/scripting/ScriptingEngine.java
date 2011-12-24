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

import net.ardvaark.jackbot.EventIRCClient;
import net.ardvaark.jackbot.IRCMessageListener;

import org.w3c.dom.Element;

/**
 * A scripting engine for the JackBot IRC Bot.
 * 
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public interface ScriptingEngine extends IRCMessageListener
{
    /**
     * Sets the client for the <CODE>ScriptingEngine</CODE>.
     * 
     * @param client The client for which this engine will be operating.
     */
    public void setClient(EventIRCClient client);

    /**
     * Executes a script that is assumed to be the text within the given XML
     * element.
     * 
     * @param scriptElement The XML DOM element in which the script resides.
     * @return The return value of the script execution.
     */
    public Object executeScript(Element scriptElement);

    /**
     * Override to provide any engine-specific cleanup that is necessary. This
     * method will be called by JackBot just before the termination of the bot.
     */
    public void cleanup();
}

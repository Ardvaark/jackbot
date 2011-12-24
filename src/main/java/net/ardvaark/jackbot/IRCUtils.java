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

import java.util.Hashtable;

/**
 * A group of useful functions relating to IRC.
 * 
 * @author Brian Vargas
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public final class IRCUtils
{
    static
    {
        IRCUtils.extractedNicksCache = new Hashtable<String, String>(10);
    }

    /**
     * Extracts the nick from a hostmask. A hostmask is of the form
     * "Nick!username@host.com". This function finds and returns the nick. For
     * example, the above hostmast would yield "Nick" as the nick.
     * 
     * @param mask The mask from which to extract the nick.
     * @return The nick portion of the hostmask.
     */
    public final static String extractNickFromMask(String mask)
    {
        String nick = IRCUtils.extractedNicksCache.get(mask);

        if (nick == null)
        {
            int bangIndex = mask.indexOf('!');
            nick = bangIndex < 0 ? mask : mask.substring(0, bangIndex);
            IRCUtils.extractedNicksCache.put(mask, nick);
        }

        return nick;
    }

    /**
     * Escapes a string in CTCP message delimiters.
     * 
     * @param message The message to escape.
     * @return A <CODE>String</CODE> that has been escaped for use in a CTCP
     *         message.
     */
    public final static String ctcpEscape(String message)
    {
        StringBuffer buffer = new StringBuffer(message);

        buffer.insert(0, IRC.CTCP_DELIM);
        buffer.append(IRC.CTCP_DELIM);

        return buffer.toString();
    }

    /**
     * A map of hostmasks to nicks. This is used by
     * {@link #extractNickFromMask(String) extractNickFromMask(String)} to cache
     * the results of the string processing necessary to get the nick from a
     * hostmask.
     */
    private static Hashtable<String, String> extractedNicksCache;
}

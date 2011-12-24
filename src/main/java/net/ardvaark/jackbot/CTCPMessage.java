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

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A Client-to-Client Protocol (CTCP) message that has been received from the
 * server embedded in an IRC message.
 * 
 * @since JackBot 1.1
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 * @author Brian Vargas
 */
public final class CTCPMessage
{
    /**
     * Parses CTCP messages. All CTCP messages in the params
     * <code>StringBuffer</code> will be found, parsed, and added to the
     * <code>ArrayList</code> returned. Any CTCP messages will be removed from
     * the passed <code>StringBuffer</code>.
     * 
     * @param sender The sender of the message.
     * @param command The command in which the CTCP messages was embedded. This
     *        should be either <code>PRIVMSG</code> or <code>NOTICE</code>.
     * @param params The parameter string of the IRC message.
     * @return An <code>ArrayList</code> of the parsed CTCP messages.
     */
    public static final ArrayList<CTCPMessage> parseMessages(String sender, String command, StringBuffer params)
    {
        ArrayList<CTCPMessage> parsedMsgs = new ArrayList<CTCPMessage>();
        int oddDelim = params.indexOf(IRC.CTCP_DELIM);
        int evenDelim = params.indexOf(IRC.CTCP_DELIM, oddDelim + 1);
        String target = params.substring(0, params.indexOf(" "));
        String ctcpMsg = null;

        while (oddDelim >= 0 && evenDelim >= 0)
        {
            ctcpMsg = params.substring(oddDelim + 1, evenDelim);
            params.delete(oddDelim, evenDelim + 1);
            parsedMsgs.add(CTCPMessage.parseMessage(sender, target, command, ctcpMsg));
            oddDelim = params.indexOf(IRC.CTCP_DELIM);
            evenDelim = params.indexOf(IRC.CTCP_DELIM, oddDelim + 1);
        }

        return parsedMsgs;
    }

    /**
     * Parses a CTCP message and returns a new instance of the
     * <code>CTCPMessage</code> class.
     * 
     * @param sender The sender of the message.
     * @param target The target of the message.
     * @param parentCommand The command in which the CTCP messages was embedded.
     *        This should be either <code>PRIVMSG</code> or
     *        <code>NOTICE</code>.
     * @param ctcpMsg The CTCP message to parse.
     * @return A new <code>CTCPMessage</code> object.
     */
    public static final CTCPMessage parseMessage(String sender, String target, String parentCommand, String ctcpMsg)
    {
        CTCPMessage newMsg = new CTCPMessage();
        int spaceIndex = ctcpMsg.indexOf(' ');

        if (spaceIndex >= 0)
        {
            newMsg.command = ctcpMsg.substring(0, spaceIndex).toUpperCase();
            newMsg.paramsString = ctcpMsg.substring(spaceIndex + 1);
        }
        else
        {
            newMsg.command = ctcpMsg.toUpperCase();
            newMsg.paramsString = "";
        }

        newMsg.sender = sender;
        newMsg.target = target;
        newMsg.parentCommand = parentCommand;

        return newMsg;
    }

    /**
     * A default, private, empty constructor. All instances of the
     * <code>CTCPMessage</code> class will be created using the
     * {@link #parseMessage(String, String, String, String) parseMessage()}
     * static method.
     */
    private CTCPMessage()
    {
    }

    /**
     * Gets the sender.
     * 
     * @return The sender of the CTCP message.
     */
    public String getSender()
    {
        return this.sender;
    }

    /**
     * Gets the target.
     * 
     * @return The target of the CTCP message.
     */
    public String getTarget()
    {
        return this.target;
    }

    /**
     * Gets the command.
     * 
     * @return The command.
     */
    public String getCommand()
    {
        return this.command;
    }

    /**
     * Gets the unparsed parameter string.
     * 
     * @return The unparsed parameter string.
     */
    public String getParams()
    {
        return this.paramsString;
    }

    /**
     * Gets a specific parameter from the parameter list, zero-based index. This
     * will cause the parameters to be parsed if they have not already been.
     * 
     * @return The parameter at the given index.
     * @see #getParamCount()
     * @param index The index of the parameter to get.
     */
    public String getParam(int index)
    {
        if (this.params == null)
        {
            this.parseParams();
        }

        return this.params.get(index);
    }

    /**
     * Returns the number of parameters in the parameter list. This will cause
     * the parameters to be parsed if they have not already been.
     * 
     * @return The count of the parameters in the command.
     */
    public int getParamCount()
    {
        if (this.params == null)
        {
            this.parseParams();
        }

        return this.params.size();
    }

    /**
     * Parses the parameter string for this CTCP message.
     */
    private void parseParams()
    {
        if (this.params == null)
        {
            this.params = new ArrayList<String>();
            StringTokenizer tok = new StringTokenizer(this.paramsString, " ");
            String curTok = null;

            while (tok.hasMoreTokens())
            {
                curTok = tok.nextToken();
                this.params.add(curTok);
            }
        }
    }

    /**
     * The sender.
     */
    private String            sender;

    /**
     * The target of the CTCP command.
     */
    private String            target;

    /**
     * The command.
     */
    private String            command;

    /**
     * The command in which the CTCP was sent. According to the IRC spec, this
     * can only be NOTICE or PRIVMSG.
     */
    private String            parentCommand;

    /**
     * The unparsed parameter string.
     */
    private String            paramsString;

    /**
     * The collection of unparsed parameters.
     */
    private ArrayList<String> params = null;
}

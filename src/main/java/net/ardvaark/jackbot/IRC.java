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
 * A final class defining the constants defined by RFC 1459. See <a
 * href="ftp://ftp.irc.org/irc/docs/rfc1459.txt">ftp://ftp.irc.org/irc/docs/rfc1459.txt</a>
 * for more information.
 * 
 * @author Brian Vargas
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public final class IRC
{
    public static final String NEWLINE           = "\r\n";

    /** Parameters: <server1> [<server2>] */
    public static final String CMD_PING          = "PING";

    /**
     * Parameters: ( <channel> *( "," <channel> ) [ <key> *( "," <key> ) ] ) /
     * "0"
     */
    public static final String CMD_JOIN          = "JOIN";

    /** Parameters: <channel> *( "," <channel> ) [ <Part Message> ] */
    public static final String CMD_PART          = "PART";

    /** Parameters: <channel> *( "," <channel> ) <user> *( "," <user> ) [<comment>] */
    public static final String CMD_KICK          = "KICK";

    /** Parameters: [ <Quit Message> ] */
    public static final String CMD_QUIT          = "QUIT";

    /** Parameters: <channel> [ <topic> ] */
    public static final String CMD_TOPIC         = "TOPIC";

    /** Parameters: <nickname> */
    public static final String CMD_NICK          = "NICK";

    /** Parameters: <msgtarget> <text to be sent> */
    public static final String CMD_PRIVMSG       = "PRIVMSG";

    /** Parameters: <msgtarget> <text> */
    public static final String CMD_NOTICE        = "NOTICE";

    /**
     * Parameters: <channel> *( ( "-" / "+" ) *<modes> *<modeparams> )
     * 
     * O - give "channel creator" status; o - give/take channel operator
     * privilege; v - give/take the voice privilege;
     * 
     * a - toggle the anonymous channel flag; i - toggle the invite-only channel
     * flag; m - toggle the moderated channel; n - toggle the no messages to
     * channel from clients on the outside; q - toggle the quiet channel flag; p -
     * toggle the private channel flag; s - toggle the secret channel flag; r -
     * toggle the server reop channel flag; t - toggle the topic settable by
     * channel operator only flag;
     * 
     * k - set/remove the channel key (password); l - set/remove the user limit
     * to channel;
     * 
     * b - set/remove ban mask to keep users out; e - set/remove an exception
     * mask to override a ban mask; I - set/remove an invitation mask to
     * automatically override the invite-only flag;
     */
    public static final String CMD_MODE          = "MODE";

    /** The CTCP Message delimiter: The character 0x01. */
    public static final String CTCP_DELIM        = "\001";

    /** ACTION */
    public static final String CTCP_CMD_ACTION   = "ACTION";

    /** "Welcome to the Internet Relay Network <nick>!<user>@<host>" */
    public static final String RPL_WELCOME       = "001";

    /** "Your host is <servername>, running version <ver>" */
    public static final String RPL_YOURHOST      = "002";

    /** "This server was created <date>" */
    public static final String RPL_CREATED       = "003";

    /**
     * "<servername> <version> <available user modes> <available channel
     * modes>"
     */
    public static final String RPL_MYINFO        = "004";

    /** "<channel> :No topic is set" */
    public static final String RPL_NOTOPIC       = "331";

    /** "<channel> :<topic>" */
    public static final String RPL_TOPIC         = "332";

    /** "<channel> :[[@|+]<nick> [[@|+]<nick> [...]]]" */
    public static final String RPL_NAMEREPLY     = "353";

    /** "<channel> :End of /NAMES list" */
    public static final String RPL_ENDOFNAMES    = "366";

    /** "<nick> :Nickname is already in use" */
    public static final String ERR_NICKNAMEINUSE = "433";
}

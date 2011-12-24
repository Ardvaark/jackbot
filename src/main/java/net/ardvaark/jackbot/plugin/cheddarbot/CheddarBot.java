/*
 *  This file is part of JackBot IRC Bot (JackBot).
 *
 *  JackBot is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  JackBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JackBot; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.ardvaark.jackbot.plugin.cheddarbot;


import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import net.ardvaark.jackbot.EventIRCClient;
import net.ardvaark.jackbot.IRC;
import net.ardvaark.jackbot.IRCMessage;
import net.ardvaark.jackbot.IRCMessageListener;
import net.ardvaark.jackbot.IRCUtils;

public class CheddarBot implements IRCMessageListener
{
    public CheddarBot()
    {
        this.lex = new dLexicon();
        this.keepLearning = true;
    }
    
    /** Called when an IRC message is received from the server.
     * @param msg The message that was received.
     * @param client The client that sent the message.
     */
    public void ircMessageReceived(IRCMessage msg, EventIRCClient client)
    {
        // Only process if this is a PRIVMSG and if it's not from me.
        if (msg.getCommand().equalsIgnoreCase(IRC.CMD_PRIVMSG) && !IRCUtils.extractNickFromMask(msg.getPrefix()).equalsIgnoreCase(client.getName()))
        {
            String text = msg.getLastParam();

            // Toggle the speaking state.
            if (text.equalsIgnoreCase("!speak"))
            {
                this.chatter = !this.chatter;
                this.nextSpeakProb = 1.0;
            }

            if (text.equalsIgnoreCase("!stupify")) {
               String response = "";
               if (this.keepLearning) {
                  //make stupid
                  this.keepLearning = false;
                  response = "Stupification level [8] reached";
               } else {
                  this.keepLearning = true;
                  response = "Learning mode resumed";
               }

               String target = "";

               if (msg.getParam(0).charAt(0) == '#')
               {
                   target = msg.getParam(0);
               }
               else
               {
                   target = IRCUtils.extractNickFromMask(msg.getPrefix());
               }

               client.ircWrite("PRIVMSG " + target + " :" + response);

            }

            if (text.equalsIgnoreCase("!wordstats"))
            {
              String target = "";

              if (msg.getParam(0).charAt(0) == '#')
              {
                  target = msg.getParam(0);
              }
              else
              {
                  target = IRCUtils.extractNickFromMask(msg.getPrefix());
              }

              client.ircWrite("PRIVMSG " + target + " :" + this.lex.stats());

            }

            if (text.equalsIgnoreCase("!wordsave"))
            {
               String target = "";

              if (msg.getParam(0).charAt(0) == '#') {
                  target = msg.getParam(0);
              } else {
                  target = IRCUtils.extractNickFromMask(msg.getPrefix());
              }
              try {
                 FileOutputStream ostream = new FileOutputStream("cheddar.saved");
                 ObjectOutputStream p = new ObjectOutputStream(ostream);

                 p.writeObject(this.lex);
                 p.flush();
                 ostream.close();
              } catch (IOException e) {
                 System.out.println("EXCEPTION: " + e);
              }
              client.ircWrite("PRIVMSG " + target + " :" + "successful save");
            }

            if (text.equalsIgnoreCase("!wordrestore"))
            {
               String target = "";

              if (msg.getParam(0).charAt(0) == '#') {
                  target = msg.getParam(0);
              } else {
                  target = IRCUtils.extractNickFromMask(msg.getPrefix());
              }
              try {
                 FileInputStream istream = new FileInputStream("cheddar.saved"
);
                 ObjectInputStream p = new ObjectInputStream(istream);

                 this.lex = (dLexicon)p.readObject();
                 
                 istream.close();
              } catch (IOException e) {
                 System.out.println("EXCEPTION: " + e);
              }
               catch (ClassNotFoundException e) {
                 client.ircWrite("PRIVMSG " + target + " :savefile corrupt. no wordnet.");
              }
              client.ircWrite("PRIVMSG " + target + " :" + "wordnet restored." + this.lex.stats());
            }

            if (text.equalsIgnoreCase("!rant")) {
               String target = "";

               if (msg.getParam(0).charAt(0) == '#') {
                  target = msg.getParam(0);
               } else {
                  target = IRCUtils.extractNickFromMask(msg.getPrefix());
               }

               client.ircWrite("PRIVMSG " + target + " :" + this.lex.speak().substring(1));
            }

            // Ignore any command strings.
            if (text.charAt(0) != '!')
            {
                // Learn the text.
                if (this.keepLearning) {
                  this.lex.learn(text);
                }

                // Determine if we should say anything.
                if (chatter && Math.random() < this.nextSpeakProb)
                {
                    // Respond to wherever the message was written.
                    // Yes, I know this leaves out the & channles, and such.
                    // Big deal.  ;-)
                    String target = "";

                    if (msg.getParam(0).charAt(0) == '#')
                    {
                        target = msg.getParam(0);
                    }
                    else
                    {
                        target = IRCUtils.extractNickFromMask(msg.getPrefix());
                    }

                    client.ircWrite("PRIVMSG " + target + " :" + this.lex.speak().substring(1));
                    
                    // Never have less than a 10% chance we'll talk next time.
                    this.nextSpeakProb = 0.5;
                    //this.nextSpeakProb = Math.max(0.1, Math.random());
                    //this.nextSpeakProb = Math.max(1.0, Math.random());
                }
            }
        }
    }
    
    double nextSpeakProb = 2.0;
    private dLexicon lex;
    private boolean chatter = false;
    private boolean keepLearning = true;
}


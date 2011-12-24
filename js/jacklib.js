<script name="JackBot Core Library">
   <script file="js/util.js" /><![CDATA[

   ////////////////////////////////////////////////////////
   // This file implements core JackBot command routing. //
   ////////////////////////////////////////////////////////

   ////////////////////////////////////////////////////////////////////////////
   // Class: BotCommand
   // Purpose: Represents a command for the bot.  The constructor takes
   //          a net.ardvaark.jackbot.IRCMessage object.
   // Properties: sender      - The hostmask of the sender of the command.
   //             target      - The target of the command. This could be a
   //                           channel or a name.
   //             msg         - The ???.
   //             cmd         - The command to be executed.
   //             paramString - The unparsed string of parameters.
   //             params      - An Array of the parsed parameters.
   ////////////////////////////////////////////////////////////////////////////
   function BotCommand(ircMsg)
   {
      this.sender = String(ircMsg.getPrefix());
      this.target = String(ircMsg.getParam(0));
      this.msg = String(ircMsg.getParam(1));

      var matches = /\!(\w+)\s*(.*)/.exec(this.msg);

      this.cmd = matches[1].toLowerCase();
      this.paramString = matches[2];
      this.params = this.paramString.split(" ");
      this.level = 100;
      
      this.toString =
      function()
      {
         return "Cmd:" + this.cmd + ", From:" + this.sender + ", Target:" + this.target + ", Params:" + this.paramString;
      }

      this.targetsChannel =
      function()
      {
         return (this.target.charAt(0) == '#');
      }
      
      this.targetsName =
      function()
      {
         return !this.targetsChannel();
      }
      
      this.respond =
      function(msg, respondAsPrivMsg)
      {
         if (respondAsPrivMsg == undefined)
         {
            bot.respond(this.sender, this.target, msg);
         }
         else
         {
            bot.respond(this.sender, this.target, msg, respondAsPrivMsg);
         }
      }      
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Class: BotUser
   // Purpose: Represents a user to the bot.  A user is someone the bot should
   //          recognize for the purpose of executing commands.
   // Properties: username - The username of the user.  This is independent of
   //                        the nick or hostmask.
   //             mask     - The hostmask with which the user is associated.
   //             maskRE   - A RegExp object that contains a regular expression
   //                        for matching against a live hostmask.
   //             autoOp   - Whether or not the user should be opped upon
   //                        entering a channel.
   //             level    - The permission level of the user. 
   //                        Highest: 1000; Lowest: 0
   ////////////////////////////////////////////////////////////////////////////
   function BotUser(username, hostmask, autoOp, level)
   {
      this.username = username;
      this.mask = hostmask;
      this.maskRE = new RegExp(hostmask.replace(/\./g, "\\.").replace(/\*/g, ".*"), "i");
      this.autoOp = autoOp;
      this.level = level;
      
      // Checks to see if a mask matches this user.
      this.matchesMask =
      function(hostmask)
      {
         return (hostmask.match(this.maskRE) != null);
      }
      
      this.toString =
      function()
      {
         return "username=" + this.username + "; mask=" + this.mask + "; autoOp=" + this.autoOp + "; level=" + this.level
      }
      
      this.toXML =
      function()
      {
         return "<user username=\"" + this.username + "\" mask=\"" + this.mask + "\" autoOp=\"" + this.autoOp + "\" level=\"" + this.level + "\" />";
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////
   // Class: BotCmdListener
   // Purpose: Represents a CmdListener.
   // Properties: callback - The callback function to use when the command
   //                        is executed.
   //             level    - The level required of a user to execute the
   //                        command listener.
   ////////////////////////////////////////////////////////////////////////////
   function BotCmdListener(callback, level)
   {
      this.callback = callback;
      this.level = level;
   }
   

   // The cmdHandlers object will hold the command handlers
   // for the bot.
   bot.cmdHandlers = new Object();
   bot.cmdHelp = new Object();
   
   // The users object will hold all of the known users, indexed by their username.
   // The username is a unique ID to the bot.  It will probably typically be their
   // nick, but that is not necessary.
   bot.users = new Object();
   
   ////////////////////////////////////////////////////////////////////////////
   // Function: addUser()
   // Purpose: Adds a user to the bot.
   ////////////////////////////////////////////////////////////////////////////
   bot.addUser =
   function(username, hostmask, autoOp, level)
   {
      var users = bot.users;
      var success = false;
      
      username = username.toLowerCase();
      
      if (users[username] == undefined)
      {
         users[username] = new BotUser(username, hostmask, autoOp, level);
         success = true;
      }
      
      return success;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Function: delUser()
   // Purpose: Removes a user from the bot.
   ////////////////////////////////////////////////////////////////////////////
   bot.delUser =
   function(username)
   {
      var users = bot.users;
      var success = false;
      
      if (users[username] != undefined)
      {
         delete users[username];
         success = true;
      }
      
      return success;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Function: findUser()
   // Purpose: Finds and returns a BotUser object matching the given mask.
   ////////////////////////////////////////////////////////////////////////////
   bot.findUser =
   function(hostmask)
   {
      var theUser = null;
      var users = bot.users;
      var curUser;
      
      for (var username in users)
      {
         curUser = users[username];
         
         if (curUser.matchesMask(hostmask))
         {
            theUser = curUser;
            break;
         }
      }
      
      return theUser;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Function: validateUser()
   // Purpose: Returns true if the user can be found in the users collection.
   ////////////////////////////////////////////////////////////////////////////
   bot.validateUser =
   function(hostmask)
   {
      return this.findUser(hostmask) != null;
   }
   

   ////////////////////////////////////////////////////////////////////////////
   // Function: checkLevel()
   // Purpose: Returns true if a user's level is >= the level passed in.
   ////////////////////////////////////////////////////////////////////////////
   bot.checkLevel =
   function(hostMask, level)
   {
      var theUser = this.findUser(hostMask);
      
      if (level == undefined)
      {
         level = 100;
      }
      
      return theUser != null && theUser.level >= level;
   }
   
   
   // Set up a handler for the onMessage event.
   // If the message is a PRIVMSG, this creates a new BotCommand
   // object and then calls dispatchCommand().
   bot.onMessage = chain(bot.onMessage,
      function(msg)
      {
         if (msg.getCommand() == "PRIVMSG")
         {
            if (msg.getParamCount() >= 2)
            {
               var target = msg.getParam(0);
               var text = msg.getParam(1);
   
               if (text.length() > 1 && text.substring(0, 1) == "!")
               {
                  var cmd = new BotCommand(msg);
                  bot.dispatchCommand(cmd);
               }
            }
         }
      }
   );
   
   // Dispatches a command to anybody listening for it.
   // For internal use only.
   bot.dispatchCommand =
   function(cmd)
   {
      var cmdName = cmd.cmd;
      var f;
      var botCmdHandler = null;
      var secFunc = null;
      var canRun = false;
      var cmdHandlers = bot.cmdHandlers;
      
      if (bot.securityCheck == undefined || bot.securityCheck == null)
      {
         // Install a simple security function if none is defined.
         bot.securityCheck =
         function(cmd, f)
         {
            return bot.validateUser(cmd.sender) && bot.checkLevel(cmd.sender, cmd.level);
         }
      }
      
      secFunc = bot.securityCheck;

      if (cmdHandlers[cmdName] != undefined)
      {
         for (var i = 0; i < cmdHandlers[cmdName].length; i++)
         {
            botCmdHandler = cmdHandlers[cmdName][i];
            f = botCmdHandler.callback;
            cmd.level = botCmdHandler.level;
            
            try
            {
               canRun = true; //secFunc(cmd, f);
            }
            catch (e)
            {
               bot.log("An exception occured while performing the security check for handler #" + i + " for command \"" + cmdName + "\".  The command will not be allowed to execute: " + e);
               cmd.respond("An exception occured while performing the security check for handler #" + i + " for command \"" + cmdName + "\".  The command will not be allowed to execute: " + e);
               canRun = false;
            }

            if (canRun)
            {
               bot.log("Executing command: " + cmd.toString());
               
               try
               {
                  f(cmd);
               }
               catch (e)
               {
                  bot.log("Caught exception running handler #" + i + " for command \"" + cmdName + "\": " + e);
                  cmd.respond("Caught exception running handler #" + i + " for command \"" + cmdName + "\": " + e);
               }
            }
         }
      }
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Function: loadUsers()
   // Purpose: Loads the users from the given filename.  Returns true on
   //          success, or the exception that occurred (if one occurs).
   ////////////////////////////////////////////////////////////////////////////
   bot.loadUsers =
   function(fileName)
   {
      var success = false;
      
      try
      {
         var doc = bot.loadXML(fileName);
         var root = doc.getDocumentElement();
         var users = root.getElementsByTagName("user");
         var user;
         var username;
         var mask;
         var autoOp;
         var level;
         
         for (var i = 0; i < users.getLength(); i++)
         {
            user = users.item(i);
            username = String(user.getAttribute("username"));
            mask = String(user.getAttribute("mask"));
            autoOp = Boolean(user.getAttribute("autoOp"));
            level = Number(user.getAttribute("level"));
            
            bot.addUser(username, mask, autoOp, level);
         }
         
         success = true;
      }
      catch (e)
      {
         success = e;
      }
      
      return success;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Function: saveUsers()
   // Purpose: Saves the users to the given file name.  Returns true on
   //          success, or the exception that occurred (if one occurs).
   ////////////////////////////////////////////////////////////////////////////
   bot.saveUsers =
   function(fileName)
   {
      var success = false;
      
      try
      {
         var xml = "";
         
         for (var username in bot.users)
         {
            bot.log("Saving username=" + username + "; value=" + bot.users[username]);
            xml += "\t" + bot.users[username].toXML() + "\n";
         }
         
         xml = "<users>\n" + xml + "</users>\n";
         bot.saveTextFile(xml, fileName);
         success = true;
      }
      catch (e)
      {
         success = e;
      }
      
      return success;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Function: addCmdListener()
   // Purpose: Adds a command listener to the bot.
   ////////////////////////////////////////////////////////////////////////////
   bot.addCmdListener =
   function(cmdName, l, level, help)
   {
      var cmdHandlers = bot.cmdHandlers;
      var cmdHelp = bot.cmdHelp;
      
      if (help == undefined)
      {
         help = "No help available.";
      }
      
      if (cmdHandlers[cmdName] == undefined)
      {
         cmdHandlers[cmdName] = new Array();
         cmdHelp[cmdName] = new Array();
         cmdHandlers[cmdName][0] = new BotCmdListener(l, level);
         cmdHelp[cmdName][0] = help;
      }
      else
      {
         cmdHandlers[cmdName][cmdHandlers[cmdName].length] = new BotCmdListener(l, level);
         cmdHelp[cmdName][cmdHelp[cmdName].length] = help;
      }
   }
   
   ////////////////////////////
   // Standard Command Setup //
   ////////////////////////////
   
   bot.addCmdListener("quit",
      function(cmd)
      {
         bot.exit();
      }, 1000, "Causes the bot to exit."
   );
   
   
   
   bot.addCmdListener("users",
      function(cmd)
      {
         var users = bot.users;
         var curUser;
      
         cmd.respond("Listing Users:");
         
         for (var username in bot.users)
         {
            curUser = users[username];
            cmd.respond(curUser.toString());
         }
         
         cmd.respond("End User List");
      }, 100, "Lists the users of which the bot is aware."
   );
   
   bot.addCmdListener("adduser",
      function(cmd)
      {
         var username = cmd.params[0];
         var hostmask = cmd.params[1];
         var autoOp = cmd.params[2];
         var level = cmd.params[3];
         
         if (bot.addUser(username, hostmask, autoOp, level))
         {
            var saveResult = bot.saveUsers("users.xml");

            if (saveResult == true)
            {
               cmd.respond("User \"" + username + "\" added.");
            }
            else
            {
               cmd.respond("User \"" + username + "\" added, but could not save users file: " + saveResult);
            }
         }
         else
         {
            cmd.respond("User could not be added.");
         }
      }, 750, "Adds a user to the bot. Usage: adduser <username> <mask> <autoOp> <leve>"
   );
   
   bot.addCmdListener("deluser",
      function(cmd)
      {
         var username = cmd.params[0];

         if (bot.delUser(username))
         {
            var saveResult = bot.saveUsers("users.xml");

            if (saveResult == true)
            {
               cmd.respond("User \"" + username + "\" deleted.");
            }
            else
            {
               cmd.respond("User \"" + username + "\" deleted, but could not save users file: " + saveResult);
            }
         }
         else
         {
            cmd.respond("User could not be deleted.");
         }
      }, 800, "Deletes a user from the bot.  Usage: deluser <username>"
   );
   
   bot.addCmdListener("help",
      function(cmd)
      {
         if (cmd.params.length == 0 || (cmd.params.length > 0 && cmd.params[0] == ""))
         {
            cmd.respond("Registered commands are:");
   
            var i = 0;
            var cmdHandlers = bot.cmdHandlers;
            var out = "";
            
            for (var curCmd in cmdHandlers)
            {
               out += (curCmd.toString() + " ");
               
               if (++i == 7)
               {
                  cmd.respond(out);
                  i = 0;
                  out = "";
               }
            }
            
            if (out.length > 0)
            {
               cmd.respond(out);
            }
   
            cmd.respond("End Command List");
         }
         else
         {
            cmdName = cmd.params[0];
            helpArray = bot.cmdHelp[cmdName];
            
            cmd.respond("Help for " + cmdName + ":");
            
            if (helpArray.length <= 1)
            {
               cmd.respond(helpArray[0]);
            }
            else
            {
               for (var i = 0; i < helpArray.length; i++)
               {
                  cmd.respond(i + ": " + helpArray[i]);
               }
            }
            
            cmd.respond("End Help");
         }
      }, 50, "Provides help on a command, or lists the available commands.  Usage: help [<command>]"
   );
   
   // Provide the auto-op capability.
   bot.onJoin = chain(bot.onJoin,
      function(channel, name)
      {
         var user = bot.findUser(name);
      
         if (user != null && user.autoOp)
         {
            bot.write("MODE " + channel + " +o :" + mask2Nick(name));
         }
      }
   );
   
]]></script>

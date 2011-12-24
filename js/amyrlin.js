<script name="BCV Library">
   <script file="js/jacklib.js" />
   <![CDATA[
   
      bot.addCmdListener("action",
         function(cmd)
         {
            if (cmd.params.length >= 2)
            {
               var target = cmd.params[0];
               var text = "";
               
               for (var i = 1; i < cmd.params.length; i++)
               {
                  text += cmd.params[i] + " ";
               }
               
               bot.action(target, text);
            }
         }, 250, "Makes me perform an action. Usage: action <channe> <what to do>"
      );
   

      bot.addCmdListener("say",
         function(cmd)
         {
            if (cmd.params.length >= 2)
            {
               var target = cmd.params[0];
               var text = "";
               
               for (var i = 1; i < cmd.params.length; i++)
               {
                  text += cmd.params[i] + " ";
               }
               
               bot.say(target, text);
            }
         }, 250, "Makes me say something. Usage: say <channel> <what to say>"
      );


      bot.addCmdListener("join",
         function(cmd)
         {
            for (var i = 0; i < cmd.params.length; i++)
            {
               bot.join(cmd.params[i]);
            }
         }, 500, "Causes the bot to join a channel. Usage: join <channel>"
      );
      
      bot.addCmdListener("channels",
         function(cmd)
         {
            var channel;
            
            cmd.respond("I am currently in the following channels:");
            
            for (channel in bot.channels)
            {
               cmd.respond(bot.channels[channel].name);
            }

            cmd.respond("End Channel List");
         }, 250, "Lists the channels in which the bot is resident."
      );
      
      bot.addCmdListener("names",
         function(cmd)
         {
            var channel = cmd.params[0];
            
            cmd.respond("Currently in " + channel + ":");
            
            for (var name in bot.channels[channel].names)
            {
               cmd.respond(name);
            }
            
            cmd.respond("End Names List");
         }, 250, "Lists the nicks of the users in the given channel.  Usage: names <channel>"
      );
      
      bot.addCmdListener("part",
         function(cmd)
         {
            for (var i = 0; i < cmd.params.length; i++)
            {
               bot.part(cmd.params[i]);
            }
         }, 500, "Causes the bot to leave a channel.  Usage: part <channel>"
      );
      
      bot.addCmdListener("reload",
         function(cmd)
         {
            try
            {
               bot.reload();
            }
            catch (e)
            {
               cmd.respond("Error during reload: " + e);
            }
         }, 1000, "Reloads the bot.  You probably shouldn't do this."
      );
      
      bot.addCmdListener("loadusers",
         function(cmd)
         {
            bot.loadUsers(cmd.param[0]);
         }, 900, "Loads the users in the specified file. Usage: loadusers <filename>"
      );
      
      // Re-sets the topic when re-joining.
      if (bot.oldTopics == undefined)
      {
         bot.oldTopics = new Object();
      }

      bot.onJoin = chain(bot.onJoin,
         function(channelName, name)
         {
            var channel = bot.channels[channelName];
            var topic   = channel.topic;
            var nick    = mask2Nick(name);

            if (nick == bot.name)
            {
                if (channel.topic == null)
                {
                   var oldTopic = bot.oldTopics[channelName];

                   if (oldTopic != null && oldTopic != undefined)
                   {
                      channel.topic = oldTopic;
                   }
                   else
                   {
                      channel.topic = "Somebody set up us the topic!!";
                   }
                }
            }
            else
            {
               bot.oldTopics[channelName] = topic;
            }
         }
      );

      bot.onTopic = chain(bot.onTopic,
         function(channelName, topic, changer)
         {
            bot.oldTopics[channelName] = topic;
         }
      );
   ]]>
</script>

<script name="Fortune">
   <script file="js/jacklib.js" />
   <![CDATA[
	    // A secret incantation to make the bot dance.
	    bot.danceState = 0;
	    bot.danceText = new Array();
	    bot.danceText[0] = "get up";
	    bot.danceText[1] = "get on up";
	    bot.danceText[2] = "get up";
	    bot.danceText[3] = "get on up";
	    bot.danceText[4] = "and DANCE";
	    bot.danceSender = "";
	
	    bot.onPrivMsg = chain(bot.onPrivMsg,
	       function(sender, target, text)
	       {
	          if(sender != bot.danceSender)
	          {
	             // new person.  are they interloping or starting a new
	             // incantation?
	             if(text != bot.danceText[0])
	             {
	                // interloping.  ignore.
	                return;
	             }
	             else
	             {
	                // new incantation.
	                bot.danceSender = sender;
	                bot.danceState = 0;
	             }
	          }
	          if(text == bot.danceText[bot.danceState])
	          {
	             // valid step.
	             bot.danceState++;
	          }
	          else
	          {
	             // invalid step.
	             bot.danceState = 0;
	          }
	          if(bot.danceState >= bot.danceText.length)
	          {
	             bot.action(target, "dances :D\\-<");
	             bot.action(target, "dances :D|-<");
	             bot.action(target, "dances :D/-<");
	             bot.danceState = 0;
	             bot.danceSender = "";
	          }
	       }
	    );
   ]]>
</script>

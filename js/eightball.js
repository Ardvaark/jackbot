<script name="Fortune">
	<script file="js/jacklib.js" />
	<![CDATA[
         // Magic 8-ball
         bot.eightBallRE = new RegExp("shakes\\s+" + bot.name + ".*", "ig");
         bot.eightBall = new Array();
         bot.eightBall[0] = "Outlook Good";
         bot.eightBall[1] = "Outlook Not So Good";
         bot.eightBall[2] = "My Reply Is No";
         bot.eightBall[3] = "Don't Count On It";
         bot.eightBall[4] = "You May Rely On It";
         bot.eightBall[5] = "Ask Again Later";
         bot.eightBall[6] = "Most Likely";
         bot.eightBall[7] = "Cannot Predict Now";
         bot.eightBall[8] = "Yes";
         bot.eightBall[9] = "Yes Definitely";
         bot.eightBall[10] = "Better Not Tell You Now";
         bot.eightBall[11] = "It Is Certain";
         bot.eightBall[12] = "Very Doubtful";
         bot.eightBall[13] = "It Is Decidedly So";
         bot.eightBall[14] = "Concentrate and Ask Again";
         bot.eightBall[15] = "Signs Point to Yes";
         bot.eightBall[16] = "My Sources Say No";
         bot.eightBall[17] = "Without a Doubt";
         bot.eightBall[18] = "Reply Hazy, Try Again";
         bot.eightBall[19] = "As I See It, Yes";
         bot.eightBall[20] = "I don't know if Velveeta *can* go bad.";

         bot.onAction = chain(bot.onAction,
            function(sender, target, action)
            {
               var match = action.match(bot.eightBallRE);
               
               if (match != null)
               {
                  var fortune = bot.eightBall[Math.floor((Math.random() * 100) % 21)];
                  bot.say(target, "The Magic 8-Ball says to " + mask2Nick(sender) + ": " + fortune);
               }
            }
         );
    ]]>
</script>

<script name="Fortune">
   <script file="js/jacklib.js" />
   <![CDATA[
      bot.getFortune = function(length, onFortuneReady)
      {
    	  if (!length) length = 400;
    	  
    	  var result = null;
    	  
    	  if (bot.fortuneBinPath)
    	  {
	    	  var fortune = "";
	    	  var exec = new Exec(bot.fortuneBinPath).arg("-a").arg("-n").arg(length).arg("-s");
	    	  
	    	  exec.onLine = function(line) {
	    		  if (line != null && line != "")
	    		  {
	        		  line = line.replace(/^\s+/, "").replace(/\s+$/, "");
	        		  
	        		  if (line != "")
	        		  {
		    			  if (fortune == "")
		    				  fortune = line;
		    			  else
		    				  fortune += " " + line;
	        		  }
	    		  }
			  }
	    	  
	    	  if (onFortuneReady)
	    	  {
	    		  exec.onExecComplete = function(result) { onFortuneReady(fortune); }
	    		  exec.start();
	    	  }
	    	  else
	    	  {
	    		  exec.start();
	    		  exec.waitFor();
	    		  result = fortune;
	    	  }
    	  }
    	  
    	  return result;
      }
      
      bot.addCmdListener("fortune", function(cmd) {
    	  bot.getFortune(400, function(fortune) {
				  cmd.respond(fortune);
			  });
      }, 100, "Spout a delicious pearl of wisdom.");
      
      // Auto-kick when "kick me" shows up anywhere.
      bot.onPrivMsg = chain(bot.onPrivMsg,
         function(sender, target, text)
         {
            if (text.match(/.*kick me.*/gi) != null)
            {
               var fortune = bot.getFortune(150);
               bot.write("KICK " + target + " " + mask2Nick(sender) + " :" + fortune);
            }
         }
      );


   ]]>
</script>

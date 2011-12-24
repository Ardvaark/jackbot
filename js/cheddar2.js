<script name="CheddarBot Driver">
    <script file="js/jacklib.js" />
    <![CDATA[

    bot.cheddar = new Object();
    
    bot.addCmdListener("rantsync",
        function(cmd)
        {
        	var client = new XMLHttpRequest();
        	client.open("GET", "http://nullship.net:3000/cheddartalk", false);
        	client.send();
            cmd.respond(client.responseText);
        },
        50, "Causes the bot say one line, synchronously.");
        
    bot.addCmdListener("rantasync",
        function(cmd)
        {
        	var client = new XMLHttpRequest();
        	client.onReadyStateChange = function() {
        		if (client.readyState == client.DONE) {
        			cmd.respond(client.responseText, true);
        		}
        	};
        	client.open("GET", "http://nullship.net:3000/cheddartalk");
        	client.send();
        },
        50, "Causes the bot say one line, asynchronously.");
    
    bot.addCmdListener("rantxmltest",
        function(cmd)
        {
        	var xml =
      			<foo>
      				<bar>
      					<blue>test</blue>
      				</bar>
      			</foo>;
      			
        	var client = new XMLHttpRequest();
        	client.open("GET", "http://nullship.net:3000/cheddartalk", false);
        	client.send(xml);
            cmd.respond(client.responseText);
        },
        50, "Causes the bot say one line, asynchronously.");
    ]]>
</script>

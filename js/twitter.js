<script name="Twitter">
    <script file="js/jacklib.js" />
    <![CDATA[
             
	function TwitterClient(channel, twitterUsername, twitterPassword)
	{
		this.lastStatus = 0;
		this.username = twitterUsername;
		this.password = twitterPassword;
		this.channel = channel;
		this.prototype = new Object();
		
		var twitter = this;
		
		this.open = function(method, command)
		{
	    	var client = new XMLHttpRequest();
	    	client.open(method, "http://www.twitter.com" + command, true, twitter.username, twitter.password); 
	    	return client;
		}

		this.cmdTest = function(cmd)
	   	{
			if (cmd.target != twitter.channel) return;
			
			var client = twitter.open("GET", "/help/test.xml");
			
			client.onReadyStateChange = function () {
				if (client.readyState == client.DONE) {
					var result = new XML(client.responseXML);
					cmd.respond(result);
				}
			};
	    		
	   		client.send();	    		
	   	};
		   	
		this.cmdTweet = function(cmd)
	   	{
			if (cmd.target != twitter.channel) return;
			
	   		var client = twitter.open("POST", "/statuses/update.xml");
	   		
			client.onReadyStateChange = function () {
				if (client.readyState == client.DONE) {
					var result = new XML(client.responseXML);
					
					if (client.statusText == "OK")
					{
						// Don't respond if everything was ok.
						// cmd.respond("Twitter said: " + client.statusText);
					}
					else
					{
						cmd.respond("Twitter said: " + client.statusText + " - " + result.error.text());
						bot.log(client.responseText);
					}
				}
			};

			client.send("status=" + cmd.paramString + "&source=jackbot");   		
	   	};

		this.cmdFollow = function(cmd)
	   	{
			if (cmd.target != twitter.channel) return;
			
	   		for each (id in cmd.params)
	   		{
	    		var client = twitter.open("POST", "/friendships/create/" + id + ".xml");
	    		
	    		client.onReadyStateChange = function () {
	    			if (client.readyState == client.DONE) {
						var result = new XML(client.responseXML);
						
						if (client.statusText == "OK")
						{
							cmd.respond("Now following " + id + ".");
						}
						else
						{
							cmd.respond("Twitter said: " + client.statusText + " - " + result.error.text());
							bot.log(client.responseText);
						}
	    			}
	    		};
	    		
	    		client.send();	    		
	   		}
	   	};

	   	this.cmdDestroy = function(cmd)
	   	{
			if (cmd.target != twitter.channel) return;
			
	   		for each (var id in cmd.params)
	   		{
	    		var client = twitter.open("DELETE", "/friendships/destroy/" + id + ".xml");
	    		
	    		client.onReadyStateChange = function () {
	    			if (client.readyState == client.DONE) {
						var result = new XML(client.responseXML);
						
						if (client.statusText == "OK")
						{
							cmd.respond("No longer following " + id + ".");
						}
						else
						{
							cmd.respond("Twitter said: " + client.statusText + " - " + result.error.text());
							bot.log(client.responseText);
						}
	    			}
	    		};
	    		
	    		client.send();	    		
	   		}
	   	};
	   	
	   	this.cmdFriends = function(cmd)
	   	{
			if (cmd.target != twitter.channel) return;
			
	   		var client = twitter.open("GET", "/statuses/friends.xml?lite=true");
	   		
	   		client.onReadyStateChange = function () {
	   			if (client.readyState == client.DONE) {
					var result = new XML(client.responseXML);
					
					if (client.statusText == "OK")
					{
						/*
						<users type="array">
							<user>
							  <id>6060732</id>
							  <name>Brian Vargas</name>
							  <screen_name>Ardvaark</screen_name>
							  <location>iPhone: 38.915039,-77.031281</location>
							  <description>A computer geek in the city.</description>
							  <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/3
							4389972/98746563_N00_normal.jpg</profile_image_url>
							  <url>http://ardvaark.net</url>
							  <protected>false</protected>
							  <followers_count>40</followers_count>
							</user>
						</users>*/
						
						var users = result;
						var friends = new Array();
						
						for each (var name in users.user.screen_name)
						{
							friends.push(name);
						}
						
						if (friends.length == 0)
						{
							cmd.respond("I'm so lonely.");
						}
						else
						{
							friends.sort();
							
							for (var i = 0; i < friends.length; i += 5)
							{
								cmd.respond("Following: " + friends.slice(i, i + 5).join(", "));
							}
						}
					}
					else
					{
						cmd.respond("Twitter said: " + client.statusText + " - " + result.error.text());
						bot.log(client.responseText);
					}
	   			}
	   		};

	   		client.send();
	   	};
	   	
	   	this.cmdCheckTimeline = function(cmd)
	   	{
			if (cmd.target != twitter.channel) return;
			
	   		var lastStatus = twitter.lastStatus;
	   		var url = "/statuses/friends_timeline.xml";
	   		
	   		switch (lastStatus)
	   		{
	   		case 0:		url += "?count=1";					break;
	   		default:	url += "?since_id=" + lastStatus;	break;
	   		}

	   		var client = twitter.open("GET", url);
	   		
	   		client.onReadyStateChange = function () {
	   			if (client.readyState == client.DONE) {
					var result = new XML(client.responseXML);
					
					if (client.statusText == "OK")
					{
						/*
							<statuses type="array">
							  <status>
							    <created_at>Sun Jul 27 02:13:53 +0000 2008</created_at>
							    <id>869424917</id>
							    <text>Getting close!</text>
							    <source>web</source>
							    <truncated>false</truncated>
							    <in_reply_to_status_id></in_reply_to_status_id>
							    <in_reply_to_user_id></in_reply_to_user_id>
							    <favorited></favorited>
							    <user>
							      <id>15615707</id>
							      <name>PoundTVD</name>
							      <screen_name>PoundTVD</screen_name>
							      <location></location>
							      <description></description>
							      <profile_image_url>http://static.twitter.com/images/default_profile_normal
							.png</profile_image_url>
							      <url></url>
							      <protected>false</protected>
							      <followers_count>0</followers_count>
							    </user>
							  </status>
						  */					
						var statuses = result;
						
						if (statuses.status.length() > 0)
						{
							if (lastStatus == 0)
							{
								var status = statuses.status[0];
								lastStatus = status.id;
								cmd.respond(status.user.screen_name + ": " + status.text, true);
							}
							else
							{
								lastStatus = statuses.status[0].id;
								
								for (var i = statuses.status.length() - 1; i >= 0; i--)
								{
									var status = statuses.status[i];
									
									if (status.user.screen_name != twitter.username)
									{
										cmd.respond(status.user.screen_name + ": " + status.text, true);
									}
								}
							}
							
							twitter.lastStatus = lastStatus;
						}
						else
						{
							cmd.respond("Nothing to say.", true);
						}
					}
					else
					{
						cmd.respond("Twitter said: " + client.statusText + " - " + result.error.text());
						bot.log(client.responseText);
					}
	   			}
	   		};

	   		client.send();
	   	};
	    
	   	this.checkTimelineForChannel = function(timeout)
	   	{
	   		var lastStatus = twitter.lastStatus;
	   		var url = "/statuses/friends_timeline.xml";
	   		
	   		switch (lastStatus)
	   		{
	   		case 0:		url += "?count=1";					break;
	   		default:	url += "?since_id=" + lastStatus;	break;
	   		}

	   		var client = twitter.open("GET", url);

	   		client.onReadyStateChange = function () {
	   			if (client.readyState == client.DONE) {
	   				// Reset the timeout.
	   				bot.setTimeout("twitter-" + twitter.channel, function() { twitter.checkTimelineForChannel(timeout); }, timeout);
	   				
					var result = new XML(client.responseXML);
					
					if (client.statusText == "OK")
					{
						/*
							<statuses type="array">
							  <status>
							    <created_at>Sun Jul 27 02:13:53 +0000 2008</created_at>
							    <id>869424917</id>
							    <text>Getting close!</text>
							    <source>web</source>
							    <truncated>false</truncated>
							    <in_reply_to_status_id></in_reply_to_status_id>
							    <in_reply_to_user_id></in_reply_to_user_id>
							    <favorited></favorited>
							    <user>
							      <id>15615707</id>
							      <name>PoundTVD</name>
							      <screen_name>PoundTVD</screen_name>
							      <location></location>
							      <description></description>
							      <profile_image_url>http://static.twitter.com/images/default_profile_normal
							.png</profile_image_url>
							      <url></url>
							      <protected>false</protected>
							      <followers_count>0</followers_count>
							    </user>
							  </status>
						  */					
						var statuses = result;
						
						if (statuses.status.length() > 0)
						{
							if (lastStatus == 0)
							{
								lastStatus = statuses.status[0].id;
							}
							else
							{
								lastStatus = statuses.status[0].id;
								
								for (var i = statuses.status.length() - 1; i >= 0; i--)
								{
									var status = statuses.status[i];
									
									if (status.user.screen_name != twitter.username)
									{
										bot.say(twitter.channel, status.user.screen_name + ": " + status.text);
									} 
								}
							}
							
							twitter.lastStatus = lastStatus;
						}
					}
					else
					{
						bot.log(client.responseText);
					}
	   			}
	   		};

	   		client.send();
	   	};
	   	
		bot.onTopic = chain(bot.onTopic,
		        function(channelName, topic, changer)
		        {
		            if (channelName == twitter.channel && topic != ".")
		            {
		                var client = twitter.open("POST", "/statuses/update.xml");
		                
		                client.onReadyStateChange = function () {
		                    if (client.readyState == client.DONE) {
		                        var result = new XML(client.responseXML);
		                        
		                        if (client.statusText != "OK")
		                        {
		                            cmd.respond("Unable to tweet topic.  Twitter said: " + client.statusText + " - " + result.error.text());
		                            bot.log(client.responseText);
		                        }
		                    }
		                };

		                client.send("status=" + topic + "&source=jackbot");   		
		            }
		        }
			);
		
	    bot.addCmdListener("twittertest", this.cmdTest,				50, "Follow a person in Twitter.");
	   	bot.addCmdListener("tweet",       this.cmdTweet,			50, "Makes me post to Twitter.");
	    bot.addCmdListener("follow",      this.cmdFollow,			50, "Follow a person in Twitter.");
	    bot.addCmdListener("nofollow",    this.cmdDestroy,			50, "Stop following a person in Twitter.");
	    bot.addCmdListener("friends",     this.cmdFriends,			50, "List the people I'm following.");
	    bot.addCmdListener("timeline",    this.cmdCheckTimeline,	50, "Check the timeline for updates.");

	    bot.setTimeout("twitter-" + this.channel, function() { twitter.checkTimelineForChannel(181 * 1000); }, (7 + Math.floor(Math.random() * 60)) * 1000);
	}

	]]>
</script>

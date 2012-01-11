<script name="Twitter">
    <script file="js/jacklib.js" />
    <script file="js/jsOAuth-1.3.1.js" />

    <![CDATA[
             
	function TwitterClient(channel, twitterUsername)
	{
		this.lastStatus = 0;
		this.username = twitterUsername;
		this.channel = channel;
		this.prototype = new Object();
		this.timerFailureCount = 0;

        this.oauth = OAuth({
            consumerKey: 'iN7sKvihWUeB691wmERpjA',
            consumerSecret: 'kpoZ00ET5gG9oR4eSjQ5yvk3fQf4yFKZ7ciEFuE8NRk'
        });

        var oauthToken = bot.retrieveData("Twitter-" + this.username + "-OAuthToken");
        var oauthTokenSecret = bot.retrieveData("Twitter-" + this.username + "-OAuthTokenSecret");

        if (oauthToken && oauthTokenSecret) {
            this.oauth.setAccessToken([oauthToken, oauthTokenSecret]);
        }

		var twitter = this;
		
		this.cmdAuthorize = function(cmd) {
			if (cmd.target != twitter.channel) return;

			bot.log("Authorizing with Twitter...");
			twitter.oauth.post('http://api.twitter.com/oauth/request_token', "",

			    function(data) {
			        bot.log("Authorization URL: https://api.twitter.com/oauth/authorize?" + data.text);
                    cmd.respond("Click here to authorize me with Twitter: https://api.twitter.com/oauth/authorize?" + data.text);
                    twitter.authData = data.text; // Save for later
			    },

			    function(data) {
			        bot.log("Unable to get OAuth token: " + data);
			        cmd.respond("Whoops! I couldn't get an OAuth request token from Twitter: " + data);
			    }
			);
		}

		this.cmdCompleteAuthorization = function(cmd) {
		    if (cmd.target != twitter.channel) return;
		    if (!twitter.authData) { cmd.respond("Try doing an !authorize first."); return; }

		    var pin = cmd.params[0];
		    if (!pin) { cmd.respond("You didn't specify a PIN."); return; }

		    bot.log("Completing authorization with Twitter.");
		    twitter.oauth.get("https://twitter.com/oauth/access_token?oauth_verifier=" + pin + "&" + twitter.authData,
		        function(data) { // Success
		            bot.log("Success! Response from verifier: " + data.text);

                    // split the query string as needed
                    var accessParams = {};
                    var qvars_tmp = data.text.split('&');

                    for (var i = 0; i < qvars_tmp.length; i++) {
                        var y = qvars_tmp[i].split('=');
                        accessParams[y[0]] = decodeURIComponent(y[1]);
                    }

                    var authorizedAs = accessParams.screen_name;

                    if (authorizedAs != twitter.username) {
                        cmd.respond("You authorized me for the wrong account! (" + authorizedAs + " instead of " + twitter.username + ").");
                    }

                    twitter.oauth.setAccessToken([accessParams.oauth_token, accessParams.oauth_token_secret]);
                    bot.persistData("Twitter-" + twitter.username + "-OAuthToken", accessParams.oauth_token);
                    bot.persistData("Twitter-" + twitter.username + "-OAuthTokenSecret", accessParams.oauth_token_secret);

                    cmd.respond("I am now authorized for access to @" + twitter.username);
		        },

		        function(data) { // Failure
		            bot.log("Failure! Response from verifier: " + data.text);
		            cmd.respond("PIN Authorizatin failed: " + data.text);
		        }
		    );
		}

		this.tweet = function(message, successFunc, failureFunc) {
            var statusUpdate = "status=" + message + "&wrap_links=true";
            // bot.log("Posting: \"" + statusUpdate + "\"");

            twitter.oauth.post("https://api.twitter.com/1/statuses/update.json?" + statusUpdate, null,
                successFunc,
                failureFunc
            );
		}

		this.cmdTweet = function(cmd) {
			if (cmd.target != twitter.channel) return;

			if (cmd.paramString == "") {
			    cmd.respond("Mum's the word.");
			    return;
			}

			twitter.tweet(cmd.paramString,
                function(data) { // Success
                    // Succeed quietly.
                    // cmd.respond("Posted successfully: " + data.text);
                },

                function(data) { // Failure
                    cmd.respond("Sorry, I was unable to post that to Twitter: " + data.text);
                }
            );
	   	};

		this.cmdFollow = function(cmd) {
			if (cmd.target != twitter.channel) return;

			if (cmd.paramString == "") {
			    cmd.respond("Who now?");
			}
			
	   		for each (var id in cmd.params)
	   		{
	   		    var url = "https://api.twitter.com/1/friendships/create.json?screen_name=" + id + "&follow=true";
	   		    twitter.oauth.post(url, null,

	   		        function(data) { // Success
                        cmd.respond("Now following " + id + ".");
	   		        },

	   		        function(data) { // Failure
	   		            cmd.respond("Twitter said: " + data.text);
	   		        }
	   		    );
	   		}
	   	};

	   	this.cmdDestroy = function(cmd) {
			if (cmd.target != twitter.channel) return;

			if (cmd.paramString == "") {
			    cmd.respond("Who now?");
			}

	   		for each (var id in cmd.params)
	   		{
	   		    var url = "https://api.twitter.com/1/friendships/destroy.json?screen_name=" + id;
	   		    twitter.oauth.post(url, null,

	   		        function(data) { // Success
                        cmd.respond("Unfollowed " + id + ".");
	   		        },

	   		        function(data) { // Failure
	   		            cmd.respond("Twitter said: " + data.text);
	   		        }
	   		    );
	   		}
	   	};
	   	
	   	this.cmdFriends = function(cmd) {
			if (cmd.target != twitter.channel) return;

			twitter.getFriendsIds(
			    function(friendsIds) { // Success
			        twitter.lookupUserInfo(friendsIds,
			            function(userInfos) { // Success
			                if (userInfos.length == 0) {
			                    cmd.respond("I am so lonely.");
			                    return;
			                }

			                // Extract and sort the usernames.
			                var userNames = [];

                            for (var i = 0; i < userInfos.length; i++) {
			                    userNames.push(userInfos[i].screen_name);
			                }

			                userNames.sort(function(l, r) {
			                    l = l.toLowerCase();
			                    r = r.toLowerCase();
			                    return l == r? 0 : l < r? -1 : 1;
			                });

			                // Now print them, five at a time.
			                while (userNames.length > 0) {
			                    var names = userNames.slice(0,5);
			                    userNames = userNames.slice(5);

			                    if (names.length > 1) {
			                        names = names.join(", ");
			                    }
			                    else {
			                        names = names[0];
			                    }

			                    cmd.respond(names);
			                }
			            },

			            function(failureMessage) {
			                cmd.respond("Unable to lookup user info: " + failureMessage);
			            }
			        );
			    },

			    function(failureMessage) { // Failure
			        cmd.respond("Unable to lookup friends IDs: " + failureMessage);
			    }
			);
	   	};

	   	this.getFriendsIds = function(successFunc, failureFunc) {
	   	    twitter.getFriendsIdsInternal(-1, [], successFunc, failureFunc);
	   	}

	   	this.getFriendsIdsInternal = function(cursor, friendsIds, successFunc, failureFunc) {
            var urlBase = "https://api.twitter.com/1/friends/ids.json?stringify_ids=true&screen_name=" + twitter.username;
            var url = urlBase + "&cursor=" + cursor;

            twitter.oauth.getJSON(url,

                function(data) { // Success
                    for each (var id in data.ids) {
                        friendsIds.push(id);
                    }

                    var nextCursor = data.next_cursor_str;
                    var prevCursor = data.previous_cursor_str;

                    if (nextCursor != prevCursor) {
                        twitter.getFriendsIdsInternal(nextCursor, friendsIds, func);
                    }
                    else {
                        successFunc(friendsIds);
                    }
                },

                function(data) { // Failure
                    failureFunc(data.text);
                }
            );
	   	}

	   	this.lookupUserInfo = function(ids, successFunc, failureFunc) {
	   	    if (ids.length == 0) {
	   	        successFunc([]);
	   	    }
            else {
	   	        twitter.lookupUserInfoInternal([], ids.concat(), successFunc, failureFunc);
	   	    }
	   	}

	   	this.lookupUserInfoInternal = function(infos, ids, successFunc, failureFunc) {
            var urlBase = "https://api.twitter.com/1/users/lookup.json?";
            var remainingIds = ids.slice(100);
            var ids = ids.slice(0, 100).join(',');
            var url = urlBase + "user_id=" + ids;

            twitter.oauth.getJSON(url,
                function(data) { // Success
                    infos = infos.concat(data);

                    if (remainingIds.length == 0) {
                        successFunc(infos);
                    }
                    else {
                        twitter.lookupUserInfoInternal(infos, remainingIds, successFunc, failureFunc);
                    }
                },

                function(data) { // Failure
                    failureFunc(data.text);
                }
            );
	   	}

	   	this.checkTimeline = function(successFunc, failureFunc) {
	   		var lastStatus = twitter.lastStatus;
	   		var url = "https://api.twitter.com/1/statuses/home_timeline.json";

	   		switch (lastStatus)
	   		{
	   		case 0:		url += "?count=1";					break;
	   		default:	url += "?since_id=" + lastStatus;	break;
	   		}

	   		twitter.oauth.getJSON(url,
	   		    function(statuses) { // Success
	   		        if (statuses.length > 0) {
	   		            twitter.lastStatus = statuses[0].id_str;
	   		        }

                    successFunc(statuses);
	   		    },

	   		    function(data) { // Failure
	   		        failureFunc(data);
	   		    }
	   		);
	    }
	   	
	   	this.cmdCheckTimeline = function(cmd) {
			if (cmd.target != twitter.channel) return;

			twitter.checkTimeline(
			    function(statuses) { // Success
                    for (var i = statuses.length - 1; i >= 0; i--) {
                        var status = statuses[i];

                        if (status.user.screen_name != twitter.username) {
                            cmd.respond(status.user.screen_name + ": " + status.text, true);
                        }
                    }
			    },

			    function(data) { // Failure
			        cmd.respond("Failed to check timeline: " + data);
			    }
			);
	   	};
	    
	   	this.checkTimelineForChannel = function(timeout) {
			twitter.checkTimeline(
			    function(statuses) { // Success
			        twitter.timelineFailureCount = 0;

                    for (var i = statuses.length - 1; i >= 0; i--) {
                        var status = statuses[i];

                        if (status.user.screen_name != twitter.username) {
                            bot.say(twitter.channel, status.user.screen_name + ": " + status.text, true);
                        }
                    }

	   				bot.setTimeout("twitter-" + twitter.channel, function() { twitter.checkTimelineForChannel(timeout); }, timeout);
			    },

			    function(data) { // Failure
			        if (++twitter.timelineFailureCount % 37 == 0) {
			            bot.notice(twitter.channel, "I haven't been able to successfully check Twitter in a while.");
			        }

	   				bot.setTimeout("twitter-" + twitter.channel, function() { twitter.checkTimelineForChannel(timeout); }, timeout);
			    }
			);

	   	};
	   	
		bot.onTopic = chain(bot.onTopic,
		        function(channelName, topic, changer)
		        {
		            if (channelName == twitter.channel && topic != ".")
		            {
		                twitter.tweet(topic,
		                    function(data) { // Success
		                        // Succeed quietly
		                    },

		                    function(data) { // Failure
		                        bot.notice(channelName, "Unable to tweet topic: " + data.text);
		                    }
		                );
		            }
		        }
			);
		
	   	bot.addCmdListener("tweet",        this.cmdTweet,			        50, "Makes me post to Twitter.");
	    bot.addCmdListener("follow",       this.cmdFollow,			        50, "Follow a person in Twitter.");
	    bot.addCmdListener("unfollow",     this.cmdDestroy,			        50, "Stop following a person in Twitter.");
	    bot.addCmdListener("friends",      this.cmdFriends,			        50, "List the people I'm following.");
	    bot.addCmdListener("timeline",     this.cmdCheckTimeline,	        50, "Check the timeline for updates.");
	    bot.addCmdListener("authorize",    this.cmdAuthorize,               50, "Authorize me with Twitter.");
	    bot.addCmdListener("authpin",      this.cmdCompleteAuthorization,   50, "Complete my Twitter authorization.");

	    bot.setTimeout("twitter-" + this.channel, function() { twitter.checkTimelineForChannel(181 * 1000); }, (7 + Math.floor(Math.random() * 60)) * 1000);
	}

	]]>
</script>

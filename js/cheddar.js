<script name="CheddarBot Driver">
    <script file="js/jacklib.js" />
    <![CDATA[

    bot.cheddar = new Object();
    bot.cheddar.lex = new Packages.jackbot.plugin.cheddarbot.dLexicon();
    bot.cheddar.keepLearning = true;
    bot.cheddar.nextSpeakProb = 2.0;
    bot.cheddar.chatter = false;

    bot.addCmdListener("speak",
        function(cmd)
        {
            bot.cheddar.chatter = !bot.cheddar.chatter;
            bot.cheddar.nextSpeakProb = 1.0;
        },
        50, "Causes the bot to start or stop talking.");

    bot.addCmdListener("rant",
        function(cmd)
        {
            cmd.respond(bot.cheddar.lex.speak().substring(1), true);
        },
        50, "Causes the bot say one line.");

    bot.addCmdListener("wordstats",
        function(cmd)
        {
            cmd.respond(bot.cheddar.lex.stats());
        },
        50, "Causes the bot return information about the current word net.");

    bot.addCmdListener("stupify",
        function(cmd)
        {
            if (bot.cheddar.keepLearning)
            {
                this.keepLearning = false;
                cmd.respond("Stupification level [8] reached");
            }
            else
            {
                this.keepLearning = true;
                cmd.respond("Learning mode resumed");
            }
        },
        500, "Turns off the bot's word learning.");

     bot.addCmdListener("wordsave",
        function(cmd)
        {
            var file = new java.io.ObjectOutputStream(new java.io.FileOutputStream("cheddar.saved"));

            try
            {
                file.writeObject(bot.cheddar.lex);
                file.flush();
                cmd.respond("Word net saved.");
            }
            catch (e)
            {
                cmd.respond("Exception while saving word net: " + e);
            }
            finally
            {
                if (file != null)
                {
                    file.close();
                }
            }
        },
        500, "Saves the current word net.");

     bot.addCmdListener("wordrestore",
        function(cmd)
        {
            var file = new java.io.ObjectInputStream(new java.io.FileInputStream("cheddar.saved"));

            try
            {
                bot.cheddar.lex = file.readObject();
                cmd.respond("Word net loaded.");
            }
            catch (e)
            {
                cmd.respond("Exception while loading word net: " + e);
            }
            finally
            {
                if (file != null)
                {
                    file.close();
                }
            }
        },
        500, "Loads the saved word net.");

     bot.onPrivMsg = chain(bot.onPrivMsg,
        function(sender, target, text)
        {
            if (text.length > 0 && mask2Nick(sender) != bot.name && text.charAt(0) != '!')
            {
                if (bot.cheddar.keepLearning)
                {
                    bot.cheddar.lex.learn(text);
                }

                if (bot.cheddar.chatter && java.lang.Math.random() < bot.cheddar.nextSpeakProb)
                {
                    bot.respond(sender, target, bot.cheddar.lex.speak().substring(1), true);
                    bot.cheddar.nextSpeakProb = 0.5;
                }
            }
        });

    ]]>
</script>

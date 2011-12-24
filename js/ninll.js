<script name="Non-Intelligent Language Learner">
   <script file="jacklib.js" />
   <![CDATA[

   /* The general algorithms and structures for this are taken from gNiall
    * by Gary Benson.
    */

   // The lang object will hold all of the data necessary for the NILL.
   // This helps prevent name-space overlaps.
   bot.lang = new Object();

   bot.lang.Word =
      function(word)
      {
         this.word = word;
         this.assocs = new Array();

         this.getAssoc =
            function(word)
            {
               var retAssoc = null;
               var curAssoc = null;

               for (var i = 0; i < this.assocs.length; i++)
               {
                  curAssoc = this.assocs[i];

                  if (curAssoc.word == word)
                  {
                     retAssoc = curAssoc;
                  }
               }

               if (retAssoc == null)
               {
                  retAssoc = new bot.lang.Assoc(word);
                  this.assocs[this.assocs.length] = retAssoc;
               }

               return retAssoc;
            }

         this.hasNext =
            function()
            {
               return this.assocs.length > 0;
            }

         this.getAssociatedWord =
            function()
            {
               // For now, just pick a random assoc.
               var rnd = Math.random();
               rnd = Math.floor(rnd * 10000000000) % this.totalAssocProb();
               var total = 0;

               for (var i = 0; i < this.assocs.length; i++)
               {
                  total += this.assocs[i].prob;

                  if (total >= rnd)
                  {
                     return this.assocs[i].word;
                  }
               }

               return this.assocs[0].word;
            }

         this.logAssocs =
            function()
            {
               for (var i = 0; i < this.assocs.length; i++)
               {
                  bot.log("Assoc #" + i + ": \"" + this.assocs[i].word + "\"");
               }
            }

         this.totalAssocProb =
            function()
            {
               var total = 0;

               for (var i = 0; i < this.assocs.length; i++)
               {
                  total += this.assocs[i].prob;
               }

               return total;
            }
      }

   bot.lang.Assoc =
      function(word)
      {
         this.word = word;
         this.prob = 0;
      }


   // The first (dummy) item in the structure.
   bot.lang.firstNode = new bot.lang.Word("");

   // The words the bot has seen.
   bot.lang.words = new Object();

   // The number of words in the words hash.
   bot.lang.wordCount = 0;

   bot.lang.chatEnabled = false;

   bot.lang.getWord =
      function(word)
      {
         var curWord = bot.lang.words[word];

         if (curWord == undefined)
         {
            curWord = new bot.lang.Word(word);
            bot.lang.words[word] = curWord;
            bot.lang.wordCount++;
         }

         return curWord;
      }

   bot.lang.speak =
      function()
      {
         var str = "";
         var curWord = bot.lang.firstNode;
         while (curWord.hasNext())
         {
            curWord = bot.lang.words[curWord.getAssociatedWord()];
            str += curWord.word + " ";
         }

         return str;
      }

   // The onPrivMsg event.
   bot.onPrivMsg = chain(bot.onPrivMsg,
      function(sender, target, text)
      {
         if (String(text).charAt(0) == '!' || mask2Nick(sender) == bot.name)
         {
            return;
         }

         var words = text.split(/\s+/);
         var curStr = null;
         var curWord = null;
         var lastWord = bot.lang.firstNode;

         for (var i = 0; i < words.length; i++)
         {
            curStr = words[i];

            if (curWord != "")
            {
               curWord = bot.lang.getWord(curStr);
               lastWord.getAssoc(curStr).prob++;
               lastWord = curWord;
            }
         }
      }
   );

   bot.onPrivMsg = chain(bot.onPrivMsg,
      function(sender, target, text)
      {
         if (bot.lang.chatEnabled)
         {
            if (Math.rand() > 0.25)
            {
               target = String(target);

               if (target.charAt(0) == '#')
               {
                  bot.say(target, bot.lang.speak());
               }
               else
               {
                  bot.say(mask2Nick(sender), bot.lang.speak());
               }
            }
         }
      }
   );

   bot.addCmdListener("chat",
      function(cmd)
      {
         bot.lang.chatEnabled = !bot.lang.chatEnabled;
      }, 500, "Does stuff."
   );

   ]]>
</script>

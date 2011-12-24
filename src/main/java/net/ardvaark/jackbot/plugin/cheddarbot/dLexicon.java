/*
 *  This file is part of JackBot IRC Bot (JackBot).
 *
 *  JackBot is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  JackBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JackBot; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

//
//  dLexicon.java
//  chatterbot
//
//  Created by Doug Grim on Fri Aug 09 2002.
//
package net.ardvaark.jackbot.plugin.cheddarbot;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.Serializable;

//TODO put length limit on SPEAK

@SuppressWarnings("unchecked")
public class dLexicon implements Serializable {

   private static final long serialVersionUID = 1L;
    

   Hashtable wordList;
   dWord initialWord;
   dWord terminalPeriod;
   dWord terminalBang;
   dWord terminalQuestion;
   
   
   
   public dLexicon() {
      wordList = new Hashtable();
      initialWord = new dWord("");
      wordList.put("", initialWord);
      terminalPeriod = new dWord(".");
      wordList.put(".", terminalPeriod);
      terminalBang = new dWord("!");
      wordList.put("!", terminalBang);
      terminalQuestion = new dWord("?");
      wordList.put("?", terminalQuestion);
   }
   
   public void learn(String input) {  //break strings on .?!
      StringTokenizer lst = new StringTokenizer(input, ".?!", true);
      String firstString = new String();
      if (input.toLowerCase().indexOf("http") != -1) {
         return;  //ignore all lines that contain "HTTP"
      }
      if (input.toLowerCase().indexOf("smsbot:") != -1) {
         return; //ignore all lines with the sms gateway bot in them
         //it would be bad for cheddarbot to start sending SMS messages
         //to people's cell phones and pagers
      }
      if (lst.hasMoreTokens())
         firstString = lst.nextToken();
      if (!lst.hasMoreTokens())   //there was only "she ran"
         singleLearn(firstString + " .");
      while (lst.hasMoreTokens()) {
         String secondString = lst.nextToken();
         if (secondString.equals(".") || secondString.equals("!") || secondString.equals("?") ) {
            singleLearn(firstString + " " + secondString);
            if (lst.hasMoreTokens()) {
               firstString = lst.nextToken();
            } else {
               return;
            }
         }
         singleLearn(firstString);
         firstString = secondString;
      }
   }
   
   public String speak() {
      //return calculateSpeak(35, initialWord);
      String outputString = calculateSpeak(35, initialWord);
      if (outputString.endsWith(".") ) { //it's a period
         String shorterOutputString = outputString.substring(0, outputString.length() - 2) + ".";
         return shorterOutputString;
      } else if (outputString.endsWith("?") ) { //it's a question
         String shorterOutputString = outputString.substring(0, outputString.length() - 2) + "?";
         return shorterOutputString;
      } else if (outputString.endsWith("!") ) { //it's a bang
         String shorterOutputString = outputString.substring(0, outputString.length() - 2) + "!";
         return shorterOutputString;
      } else {
         return outputString;
      }
   }

   public String stats() {
      String returnString = "The wordlist contains " +(wordList.size() - 4)+ " words and ";
      Enumeration allwords = wordList.elements();
      int linkCount = 0;
      while (allwords.hasMoreElements()) {
         linkCount += ((dWord)allwords.nextElement()).countLinks();
      }
      returnString += linkCount + " links.";
      return returnString;
   }

   protected String calculateSpeak(int n, dWord currWord) {
      if (n > 0 && currWord.hasNext()) {
//System.out.println("Calling speak with n = " +n);
//System.out.println("Current word has keys: " + currWord.links.keySet());
         return currWord.value() + " " + calculateSpeak(n - 1, currWord.next());
      } else {
         return currWord.value();
      }
   }

   protected dWord doLookup(String word) {
      if (wordList.containsKey(word)) {
         return (dWord)wordList.get(word);
      } else { //is not already in list
         dWord tempWord = new dWord(word);
         wordList.put(word, tempWord);
         return tempWord;
      }
   }

   protected void singleLearn(String input) {
      String currWordlet = new String();
      dWord prevWord = initialWord;
      StringTokenizer st = new StringTokenizer(input);
      while (st.hasMoreTokens()) {
         currWordlet = st.nextToken();
         //currWordlet = currWordlet.replace('[', '');
         //currWordlet = currWordlet.replace(']', '');
         //currWordlet = currWordlet.replace('(', '');
         //currWordlet = currWordlet.replace(')', '');
         //currWordlet = currWordlet.replace('{', '');
         //currWordlet = currWordlet.replace('}', '');
         //currWordlet = currWordlet.replace(',', '');
         //currWordlet = currWordlet.replace('\'', '');
         //currWordlet = currWordlet.replace('\"', '');
         //currWordlet = currWordlet.replace('', '');
         //currWordlet = currWordlet.replace('', '');
         //currWordlet = currWordlet.replace('', '');
         dWord currWord = doLookup(currWordlet);
         prevWord.addLink(currWordlet, currWord);
         prevWord = currWord;
//System.out.println("Adding word [" + currWordlet + "]");
         if (currWordlet.equals(".") ||
             currWordlet.equals("!") ||
             currWordlet.equals("?") ) {
//System.out.println("terminal condition reached for " + currWordlet);
            return;
         }
      }
      // add terminal word link
      dWord currWord = terminalPeriod;
      prevWord.addLink(currWordlet, currWord);
//System.out.println("added terminal condition on own");
   }
   
} //end class dLexicon

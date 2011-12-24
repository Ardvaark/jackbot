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
//  dWord.java
//  chatterbot
//
//  Created by Doug Grim on Fri Aug 09 2002.
//
package net.ardvaark.jackbot.plugin.cheddarbot;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.Serializable;

@SuppressWarnings("unchecked")
public class dWord implements Serializable {
   
    private static final long serialVersionUID = 1L;
    

   String theWord;
   public Hashtable links;
   Hashtable weights;
   int probabilitySum;
   //Random rand;
   
   dWord(String theWord) {
      links = new Hashtable();
      weights = new Hashtable();
      this.theWord = theWord;
      probabilitySum = 0;
//System.out.println("Created: new dWord- " + theWord);
      //rand = new Random();
   }
   
   public boolean hasNext() { return (weights.size() > 0); }
   
   public void addLink(String word, dWord otherWord) {  //creates a link, or adds to the weight
      if ( weights.containsKey(word) ) {
         int tempWeight = ((Integer)weights.get(word)).intValue();
         tempWeight++;
         weights.put(word, new Integer(tempWeight));
         probabilitySum++;
      } else {  //create the link
         weights.put(word, new Integer(1));
         
         links.put(word, otherWord);
         probabilitySum++;
      }
   }
   
   public String value() {return theWord;}
   public int countLinks() {return links.size();}
   
   //check hasNext() before calling next(), or you could crash if there's no links
   public dWord next() {
//      Random rand = new Random();
//      int nth = rand.nextInt( probabilitySum );
      int nth = (int)Math.round(Math.random() * probabilitySum);
//System.out.println("Random walk nth is " + nth);
      //nth = 17;
      int runningSum = 0;
      String tempString = "";

      Enumeration weightlist = weights.keys();
      while (weightlist.hasMoreElements() && (runningSum <= nth)) {
         tempString = (String)weightlist.nextElement();
         runningSum += ((Integer)weights.get( tempString )).intValue();
      }
      if (weightlist.hasMoreElements()) {
         //found our word
//System.out.println("Found our word, returning link to " + tempString);
         return (dWord)links.get(tempString);
      } else { //reached end of the list, just return an element
         return (dWord)links.elements().nextElement();
      }
   }

}

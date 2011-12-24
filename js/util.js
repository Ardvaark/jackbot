<script name="JackBot Utility Library"><![CDATA[

   function chain(func1, func2)
   {
      if (func1 == null)
      {
         return func2;
      }
      else if (func2 == null)
      {
         return func1;
      }
      else
      {
         var newFunc =
         function()
         {
            func1.apply(func1, arguments);
            func2.apply(func2, arguments);
         }
         
         return newFunc;
      }
   }

   function mask2Nick(mask)
   {
      return /([^!]+)!.*/.exec(mask)[1];
   }
   
]]></script>

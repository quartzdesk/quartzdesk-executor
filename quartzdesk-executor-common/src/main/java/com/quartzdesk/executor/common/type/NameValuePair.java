 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.common.type;

 /**
  * A generic holder for a name and value.
  *
  * @param <T> the value type.
  */
 public class NameValuePair<T>
 {
   private String name;

   private T value;


   /**
    * Creates a new {@link com.quartzdesk.executor.common.type.NameValuePair} instance
    * with the specified name and value.
    *
    * @param name  a name.
    * @param value a value.
    */
   public NameValuePair( String name, T value )
   {
     this.name = name;
     this.value = value;
   }


   /**
    * Returns the name.
    *
    * @return the name.
    */
   public String getName()
   {
     return name;
   }


   /**
    * Sets the name.
    *
    * @param name a name.
    */
   public void setName( String name )
   {
     this.name = name;
   }


   /**
    * Returns the value.
    *
    * @return the value.
    */
   public T getValue()
   {
     return value;
   }


   /**
    * Sets the value.
    *
    * @param value a value.
    */
   public void setValue( T value )
   {
     this.value = value;
   }


   /**
    * Returns the string representation of this name-value pair.
    *
    * @return the string representation of this name-value pair.
    */
   @Override
   public String toString()
   {
     StringBuilder sb = new StringBuilder( NameValuePair.class.getName() )
         .append( '[' )
         .append( "name=" ).append( name )
         .append( ", value=" ).append( value )
         .append( ']' );
     return sb.toString();
   }
 }

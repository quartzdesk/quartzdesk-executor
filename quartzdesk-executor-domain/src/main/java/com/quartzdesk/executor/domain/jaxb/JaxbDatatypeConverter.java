 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.domain.jaxb;

 import java.util.TimeZone;

 /**
  * Custom JAXB data type converter for JAXB objects.
  */
 public final class JaxbDatatypeConverter
 {
   private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone( "UTC" );


   /**
    * Private constructor of a utility class..
    */
   private JaxbDatatypeConverter()
   {
   }


   /**
    * Parses the specified string value and returns an {@link Integer}.
    *
    * @param value a string value.
    * @return the {@link Integer} value.
    */
   public static Integer parseInteger( String value )
   {
     return value == null ? null : Integer.valueOf( value );
   }


   /**
    * Converts the specified {@link Integer} value to a string value.
    *
    * @param value an {@link Integer} value.
    * @return the string value.
    */
   public static String printInteger( Integer value )
   {
     return value == null ? null : value.toString();
   }


   /**
    * Parses the specified string value and returns an {@link Long}.
    *
    * @param value a string value.
    * @return the {@link Long} value.
    */
   public static Long parseLong( String value )
   {
     return value == null ? null : Long.valueOf( value );
   }


   /**
    * Converts the specified {@link Long} value to a string value.
    *
    * @param value an {@link Long} value.
    * @return the string value.
    */
   public static String printLong( Long value )
   {
     return value == null ? null : value.toString();
   }


   /**
    * Parses the specified string value and returns an {@link Boolean}.
    *
    * @param value a string value.
    * @return the {@link Boolean} value.
    */
   public static Boolean parseBoolean( String value )
   {
     return value == null ? null : Boolean.valueOf( value );
   }


   /**
    * Converts the specified {@link Boolean} value to a string value.
    *
    * @param value an {@link Boolean} value.
    * @return the string value.
    */
   public static String printBoolean( Boolean value )
   {
     return value == null ? null : value.toString();
   }
 }

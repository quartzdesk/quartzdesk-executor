 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.common.text;

 import com.quartzdesk.executor.common.CommonConst;
 import com.quartzdesk.executor.common.CommonUtils;
 import com.quartzdesk.executor.common.io.IOUtils;

 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.concurrent.ConcurrentHashMap;

 /**
  * A {@link ConcurrentHashMap} decorator that allows this map to be initialized
  * from a UTF-8 encoded Java properties resource. This class is meant as
  * a replacement for {@link java.util.Properties} that is fully synchronized
  * and in addition does not support UTF-8 encoded resources.
  */
 public class UTF8Map
     extends ConcurrentHashMap<String, String>
 {
   private static final String WHITE_SPACE_CHARS = " \t\r\n\f";

   private static final String KEY_VALUE_SEPARATORS = "=:\t\r\n\f";

   private static final String STRICT_KEY_VALUE_SEPARATORS = "=:";


   /**
    * Creates a new and empty {@link UTF8Map} instance.
    */
   public UTF8Map()
   {
   }


   /**
    * Creates a new {@link UTF8Map} instance and initializes it
    * from the specified resource. If the resource path is relative,
    * then it is made absolute using the package of the specified
    * class. The resource is loaded with the class loader of the
    * specified class.
    *
    * @param resourcePath a resource path (Java properties format).
    * @param clazz        a class.
    */
   public UTF8Map( String resourcePath, Class<?> clazz )
   {
     InputStream ins = null;
     try
     {
       ins = CommonUtils.getResourceAsStream( resourcePath, clazz );
       load( ins );
     }
     catch ( IOException e )
     {
       throw new RuntimeException( "Error initializing from resource: " + resourcePath, e );
     }
     finally
     {
       IOUtils.close( ins );
     }
   }


   /**
    * Creates a new {@link UTF8Map} instance and initializes it
    * from the specified file.
    *
    * @param file a file (Java properties format).
    */
   public UTF8Map( File file )
   {
     Reader reader = null;
     try
     {
       reader = new InputStreamReader( new FileInputStream( file ), CommonConst.ENCODING_UTF8 );
       load( reader );
     }
     catch ( IOException e )
     {
       throw new RuntimeException( "Error initializing from file: " + file, e );
     }
     finally
     {
       IOUtils.close( reader );
     }
   }


   /**
    * Creates a new {@link UTF8Map} instance and initializes it
    * from the specified input stream (UTF-8 encoded).
    *
    * @param ins an input stream (Java properties format).
    */
   public UTF8Map( InputStream ins )
   {
     try
     {
       load( ins );
     }
     catch ( IOException e )
     {
       throw new RuntimeException( "Error initializing from input stream: " + ins, e );
     }
   }


   /**
    * Creates a new {@link UTF8Map} instance and initializes it
    * from the specified reader.
    *
    * @param reader a reader (Java properties format).
    */
   public UTF8Map( Reader reader )
   {
     try
     {
       load( reader );
     }
     catch ( IOException e )
     {
       throw new RuntimeException( "Error initializing from reader: " + reader, e );
     }
   }


   /**
    * Reads a property list from the specified input stream. The input stream
    * is expected to be UTF-8 encoded.
    *
    * @param ins the input stream to read the properties from.
    * @throws java.io.IOException if an error occurred when reading from the input stream.
    * @see #load(java.io.Reader)
    */
   private void load( InputStream ins )
       throws IOException
   {
     load( new InputStreamReader( ins, CommonConst.ENCODING_UTF8 ) );
   }


   /**
    * Reads a property list from the specified reader. The implementation of this method
    * has been "borrowed" from {@link java.util.Properties#load(java.io.InputStream)}.
    *
    * @param reader the reader to read the properties from.
    * @throws IOException if an error occurred when reading from the reader.
    * @see java.util.Properties#load
    */
   private void load( Reader reader )
       throws IOException
   {
     clear();  // clear existing values

     BufferedReader in = new BufferedReader( reader );

     while ( true )
     {
       // Get next line
       String line = in.readLine();
       if ( line == null )
         return;

       if ( line.length() > 0 )
       {
         // Continue lines that end in slashes if they are not comments
         char firstChar = line.charAt( 0 );
         if ( firstChar != '#' && firstChar != '!' )
         {
           while ( continueLine( line ) )
           {
             String nextLine = in.readLine();
             if ( nextLine == null )
               nextLine = "";
             String loppedLine = line.substring( 0, line.length() - 1 );
             // Advance beyond whitespace on new line
             int startIndex = 0;
             for ( ; startIndex < nextLine.length(); startIndex++ )
               if ( WHITE_SPACE_CHARS.indexOf( nextLine.charAt( startIndex ) ) == -1 )
                 break;
             nextLine = nextLine.substring( startIndex, nextLine.length() );
             line = loppedLine + nextLine;
           }

           // Find start of key
           int len = line.length();
           int keyStart;
           for ( keyStart = 0; keyStart < len; keyStart++ )
           {
             if ( WHITE_SPACE_CHARS.indexOf( line.charAt( keyStart ) ) == -1 )
               break;
           }

           // Blank lines are ignored
           if ( keyStart == len )
             continue;

           // Find separation between key and value
           int separatorIndex;
           for ( separatorIndex = keyStart; separatorIndex < len; separatorIndex++ )
           {
             char currentChar = line.charAt( separatorIndex );
             if ( currentChar == '\\' )
               separatorIndex++;
             else if ( KEY_VALUE_SEPARATORS.indexOf( currentChar ) != -1 )
               break;
           }

           // Skip over whitespace after key if any
           int valueIndex;
           for ( valueIndex = separatorIndex; valueIndex < len; valueIndex++ )
             if ( WHITE_SPACE_CHARS.indexOf( line.charAt( valueIndex ) ) == -1 )
               break;

           // Skip over one non whitespace key value separators if any
           if ( valueIndex < len )
             if ( STRICT_KEY_VALUE_SEPARATORS.indexOf( line.charAt( valueIndex ) ) != -1 )
               valueIndex++;

           // Skip over white space after other separators if any
           while ( valueIndex < len )
           {
             if ( WHITE_SPACE_CHARS.indexOf( line.charAt( valueIndex ) ) == -1 )
               break;
             valueIndex++;
           }
           String key = line.substring( keyStart, separatorIndex ).trim();
           String value = separatorIndex < len ? line.substring( valueIndex, len ).trim() : "";

           // Convert then store key and value
           key = loadConvert( key );
           value = loadConvert( value );
           put( key, value );
         }
       }
     }
   }


   /**
    * Returns true if the given line is a line that must
    * be appended to the next line.
    *
    * @param line a line.
    * @return true if the given line is a line that must
    * be appended to the next line, false otherwise.
    */
   private boolean continueLine( String line )
   {
     int slashCount = 0;
     int index = line.length() - 1;
     while ( index >= 0 && line.charAt( index-- ) == '\\' )
       slashCount++;
     return ( slashCount & 1 ) == 1;
   }


   /**
    * Converts encoded &#92;uxxxx to unicode chars
    * and changes special saved chars to their original forms.
    *
    * @param theString the string to convert.
    * @return the converted value.
    */
   private String loadConvert( String theString )
   {
     int len = theString.length();
     StringBuilder outBuffer = new StringBuilder( len );

     for ( int x = 0; x < len; )
     {
       char aChar = theString.charAt( x++ );
       if ( aChar == '\\' )
       {
         aChar = theString.charAt( x++ );
         if ( aChar == 'u' )
         {
           // Read the xxxx
           int value = 0;
           for ( int i = 0; i < 4; i++ )
           {
             aChar = theString.charAt( x++ );
             switch ( aChar )
             {
               case '0':
               case '1':
               case '2':
               case '3':
               case '4':
               case '5':
               case '6':
               case '7':
               case '8':
               case '9':
                 value = ( value << 4 ) + aChar - '0';
                 break;
               case 'a':
               case 'b':
               case 'c':
               case 'd':
               case 'e':
               case 'f':
                 value = ( value << 4 ) + 10 + aChar - 'a';
                 break;
               case 'A':
               case 'B':
               case 'C':
               case 'D':
               case 'E':
               case 'F':
                 value = ( value << 4 ) + 10 + aChar - 'A';
                 break;
               default:
                 throw new IllegalArgumentException( "Malformed \\uxxxx encoding." );
             }
           }
           outBuffer.append( (char) value );
         }
         else
         {
           if ( aChar == 't' )
             aChar = '\t';
           else if ( aChar == 'r' )
             aChar = '\r';
           else if ( aChar == 'n' )
             aChar = '\n';
           else if ( aChar == 'f' )
             aChar = '\f';

           outBuffer.append( aChar );
         }
       }
       else
         outBuffer.append( aChar );
     }
     return outBuffer.toString();
   }
 }

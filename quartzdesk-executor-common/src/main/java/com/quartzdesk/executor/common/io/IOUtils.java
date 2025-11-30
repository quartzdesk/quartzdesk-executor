 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.common.io;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.io.Reader;
 import java.io.Writer;

 /**
  * Various I/O utility methods.
  */
 public final class IOUtils
 {
   /**
    * Size of internal buffers used by various methods in this class.
    */
   private static final int BUFFER_SIZE = 2048;

   private static final Logger log = LoggerFactory.getLogger( IOUtils.class );


   /**
    * Private constructor of a utility class.
    */
   private IOUtils()
   {
   }


   /**
    * Closes the specified input stream if it is not null and logs
    * an error with the exception if there was a problem
    * closing the stream. If the input stream is null, this method
    * simply returns.
    *
    * @param ins an input stream.
    */
   public static void close( InputStream ins )
   {
     if ( ins != null )
     {
       try
       {
         ins.close();
       }
       catch ( IOException e )
       {
         log.error( "Error closing input stream.", e );
       }
     }
   }


   /**
    * Closes the specified output stream if it is not null and logs
    * an error with the exception if there was a problem
    * closing the stream. If the output stream is null, this method
    * simply returns.
    *
    * @param outs an output stream.
    */
   public static void close( OutputStream outs )
   {
     if ( outs != null )
     {
       try
       {
         outs.close();
       }
       catch ( IOException e )
       {
         log.error( "Error closing output stream: " + outs, e );
       }
     }
   }


   /**
    * Closes the specified reader if it is not null and logs
    * an error with the exception if there was a problem
    * closing the reader. If the reader is null, this method
    * simply returns.
    *
    * @param reader a reader.
    */
   public static void close( Reader reader )
   {
     if ( reader != null )
     {
       try
       {
         reader.close();
       }
       catch ( IOException e )
       {
         log.error( "Error closing reader: " + reader, e );
       }
     }
   }


   /**
    * Closes the specified reader if it is not null and logs
    * an error with the exception if there was a problem
    * closing the writer. If the writer is null, this method
    * simply returns.
    *
    * @param writer a writer.
    */
   public static void close( Writer writer )
   {
     if ( writer != null )
     {
       try
       {
         writer.close();
       }
       catch ( IOException e )
       {
         log.error( "Error closing writer: " + writer, e );
       }
     }
   }


   /**
    * Closes the specified random access file if it is not null
    * and logs an error with the exception if there was a problem
    * closing the file. If the file is null, this method simply
    * returns.
    *
    * @param file a a random access file.
    */
   public static void close( RandomAccessFile file )
   {
     if ( file != null )
     {
       try
       {
         file.close();
       }
       catch ( IOException e )
       {
         log.error( "Error closing random access file: " + file, e );
       }
     }
   }


   /**
    * Returns true if the specified path represents a readable file, false otherwise.
    *
    * @param path a file path.
    * @return true if the specified path represents a readable file, false otherwise.
    */
   public static boolean isReadableFile( File path )
   {
     return path.isFile() && path.canRead();
   }
 }

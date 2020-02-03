/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Various commonly used methods.
 */
public final class CommonUtils
{
  /**
   * Private constructor of a utility class.
   */
  private CommonUtils()
  {
  }

  /**
   * Compares the two Objects for equality. This method returns true if both Objects are null,
   * or they are equal according to the {@link Object#equals(Object)} method, false otherwise.
   * In case the specified objects are arrays, this method performs deep equality check of all
   * array elements.
   *
   * @param o1 the first object.
   * @param o2 the second object.
   * @return true if both Objects are equal, false otherwise.
   */
  public static boolean safeEquals( Object o1, Object o2 )
  {
    if ( o1 == o2 )
      return true;

    if ( o1 != null && o2 == null )
      return false;

    if ( o1 == null )  // o2 != null
      return false;

    if ( o1.getClass().isArray() && o2.getClass().isArray() )
    {
      int length1 = Array.getLength( o1 );
      int length2 = Array.getLength( o2 );

      // array lengths must be equal
      if ( length1 != length2 )
        return false;

      // array component types must be equal
      if ( !o1.getClass().getComponentType().equals( o2.getClass().getComponentType() ) )
        return false;

      // individual elements must be equal
      for ( int i = 0; i < length1; i++ )
      {
        if ( !safeEquals( Array.get( o1, i ), Array.get( o2, i ) ) )
          return false;
      }
      return true;
    }

    return o1.equals( o2 );
  }


  /**
   * Returns the hash-code of the specified object. If the specified object
   * is null, then this method returns 1, rather then throwing NullPointerException.
   *
   * @param o the object.
   * @return the object's hash-code, or 1 if the object is null.
   */
  public static int safeHashCode( Object o )
  {
    int hash = 1;

    if ( o != null )
    {
      if ( o.getClass().isArray() )
        return Arrays.deepHashCode( (Object[]) o );
      else
        hash = o.hashCode();
    }

    return hash;
  }


  /**
   * Returns 0 if both objects are null, -1 if c1 is null and c2 is not,
   * 1 if c1 is not null and c2 is null, otherwise the result of normal
   * comparison.
   *
   * @param o1 the first object.
   * @param o2 the second object.
   * @return the comparison result.
   */
  @SuppressWarnings( "unchecked" )
  public static <T extends Comparable<T>> int safeCompare( T o1, T o2 )
  {
    if ( o1 == o2 )
      return 0;

    if ( o1 != null && o2 == null )
      return 1;

    if ( o1 == null )  // o2 != null
      return -1;

    return o1.compareTo( o2 );
  }


  /**
   * Simply prepends the package name of the specified class to the
   * specified name and replaces all '.' with '/'.
   *
   * @param name  a resource name.
   * @param clazz a class to be used to resolve the name if it
   *              is a relative name.
   * @return the absolute resource name.
   */
  public static String getAbsoluteResourceName( String name, Class<?> clazz )
  {
    StringBuilder absName = new StringBuilder( clazz.getPackage().getName().replace( '.', '/' ) );
    absName.append( '/' );
    absName.append( name );
    return absName.toString();
  }


  /**
   * Returns the input stream for the resource with the specified absolute name.
   * The resource is loaded through the current thread's context class loader.
   *
   * @param absoluteName a resource name.
   * @return the resource input stream.
   * @throws IOException if the resource was not found.
   */
  public static InputStream getResourceAsStream( String absoluteName )
      throws IOException
  {
    String resourceName = absoluteName.startsWith( "/" ) ? absoluteName.substring( 1 ) : absoluteName;
    InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream( resourceName );

    if ( ins == null )
      throw new IOException( "Cannot find resource: " + absoluteName );

    return new BufferedInputStream( ins );
  }


  /**
   * Returns the input stream for the resource with the specified relative name.
   * The name is treated as relative and the package name of the specified
   * class is prepended to that name to make the name absolute. The resource
   * is loaded through the current thread's context class loader.
   *
   * @param relativeName a resource name (relative, or absolute).
   * @param clazz        a class used to make the resource name absolute and load the resource.
   * @return the resource input stream.
   * @throws IOException if the resource was not found.
   */
  public static InputStream getResourceAsStream( String relativeName, Class<?> clazz )
      throws IOException
  {
    String newName = getAbsoluteResourceName( relativeName, clazz );
    return getResourceAsStream( newName );
  }


  /**
   * Returns the reader for the resource with the specified absolute name.
   * The resource is loaded through the current thread's context class loader.
   *
   * @param absoluteName a resource name.
   * @param enc          the resource encoding.
   * @return the resource reader.
   * @throws IOException if the resource was not found.
   */
  public static Reader getResourceAsReader( String absoluteName, String enc )
      throws IOException
  {
    InputStream ins = getResourceAsStream( absoluteName );
    return new InputStreamReader( ins, enc );
  }


  /**
   * Returns the string representation of the stack trace of the specified exception including
   * all of its causes. This method supports dumping of SQLException chains which do not use the
   * standard chaining mechanism using the {@link Throwable#getCause()} method.
   *
   * @param t an exception.
   * @return the stack trace as a string.
   */
  public static String getStackTrace( Throwable t )
  {
    StringWriter sw = new StringWriter();
    PrintWriter w = new PrintWriter( sw );
    w.println( t );

    StackTraceElement[] traceElements = t.getStackTrace();
    for ( StackTraceElement traceElement : traceElements )
      w.println( "\tat " + traceElement );

    Throwable cause = t.getCause();

    // if the processed exception does not have a standard cause, try to extract
    // cause from "getNextException" in case the processed exception is an SQLException
    if ( cause == null && t instanceof SQLException )
      cause = ( (SQLException) t ).getNextException();

    if ( cause != null )
      printStackTraceAsCause( cause, w, traceElements );

    w.flush();
    w.close();

    return sw.toString();
  }


  /**
   * Recursively dumps the stack trace of the specified cause exception including
   * and all of its cause exceptions to the specified writer. This method handles
   * the SQLException chaining which does not use the standard Java cause chaining.
   *
   * @param t           a cause exception.
   * @param w           a print writer.
   * @param causedTrace the stack trace of the caused exception (i.e. of the
   *                    exception caused by t).
   */
  private static void printStackTraceAsCause( Throwable t, PrintWriter w,
      StackTraceElement[] causedTrace )
  {
    // Compute number of frames in common between this and caused
    StackTraceElement[] trace = t.getStackTrace();

    int m = trace.length - 1;
    int n = causedTrace.length - 1;

    while ( m >= 0 && n >= 0 && trace[m].equals( causedTrace[n] ) )
    {
      m--;
      n--;
    }
    int framesInCommon = trace.length - 1 - m;

    w.println( "Caused by: " + t );
    for ( int i = 0; i <= m; i++ )
      w.println( "\tat " + trace[i] );
    if ( framesInCommon != 0 )
      w.println( "\t... " + framesInCommon + " more" );

    // recursion if we have a cause
    Throwable cause = t.getCause();

    // if the processed exception does not have a standard cause, try to extract
    // cause from "getNextException" in case the processed exception is an SQLException
    if ( cause == null && t instanceof SQLException )
      cause = ( (SQLException) t ).getNextException();

    if ( cause != null )
      printStackTraceAsCause( cause, w, trace );
  }


  /**
   * Returns the cause of the specified exception.
   *
   * @param t an exception.
   * @return the cause.
   */
  public static Throwable getCause( Throwable t )
  {
    // SQLException does not use "standard" cause chaining...grrr
    if ( t instanceof SQLException )
    {
      return ( (SQLException) t ).getNextException();
    }
    else
    {
      return t.getCause();
    }
  }


  /**
   * Returns the root cause of the specified exception.
   *
   * @param t an exception.
   * @return the root cause.
   */
  public static Throwable getRootCause( Throwable t )
  {
    Throwable cause = t;

    while ( getCause( cause ) != null )
      cause = getCause( cause );

    return cause;
  }
}

/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.debug;

import com.quartzdesk.executor.common.CommonConst;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Various debugging utility methods.
 */
public final class Debug
{
  /**
   * Private constructor of a utility class.
   */
  private Debug()
  {
  }

  /**
   * Returns detailed info about the given class such as its name, interfaces,
   * code source, and class loader.
   *
   * @param clazz the class.
   * @return the class info as a string.
   */
  public static String getClassInfo( Class<?> clazz )
  {
    StringBuilder sb = new StringBuilder();

    ClassLoader cl = clazz.getClassLoader();
    sb.append( clazz.getName() );

    CodeSource clazzCS = clazz.getProtectionDomain().getCodeSource();
    sb.append( ", code source: " ).append( clazzCS );

    sb.append( CommonConst.NL ).append( "class loader:" );
    sb.append( getClassLoaderInfo( cl ) );

    sb.append( CommonConst.NL ).append( "interfaces:" );
    Class<?>[] ifaces = clazz.getInterfaces();
    for ( Class<?> iface : ifaces )
    {
      sb.append( CommonConst.NL ).append( "  " ).append( iface );
      ClassLoader loader = iface.getClassLoader();
      sb.append( CommonConst.NL ).append( "    class loader: " ).append( loader );
      ProtectionDomain pd = iface.getProtectionDomain();
      CodeSource cs = pd.getCodeSource();
      sb.append( CommonConst.NL ).append( "    code source: " ).append( cs );
    }

    sb.append( CommonConst.NL ).append( "ctx class loader:" );
    sb.append( getClassLoaderInfo( Thread.currentThread().getContextClassLoader() ) );

    return sb.toString();
  }


  /**
   * Dumps out info on the specified class loader. The dump describes the
   * entire class loader hierarchy starting at the specified class loader.
   *
   * @param cl a class loader.
   * @return the class loader info in a string.
   */
  public static String getClassLoaderInfo( ClassLoader cl )
  {
    StringBuilder sb = new StringBuilder();

    ClassLoader parent = cl;

    while ( parent != null )
    {
      sb.append( CommonConst.NL ).append( "  " ).append( parent );
      URL[] urls = getClassLoaderURLs( parent );

      if ( urls != null )
      {
        if ( urls.length == 0 )
          sb.append( CommonConst.NL ).append( "    empty URLs" );

        for ( URL url : urls )
          sb.append( CommonConst.NL ).append( "    " ).append( url );
      }
      else
      {
        sb.append( CommonConst.NL ).append( "    no URLs" );
      }

      parent = parent.getParent();
    }

    return sb.toString();
  }


  /**
   * Use reflection to access a URL[] getURLs or ULR[] getAllURLs method
   * so that non-URLClassLoader class loaders, or class loaders that override
   * getURLs to return null or empty, can provide the true classpath info.
   *
   * @param cl a class loader.
   * @return the classloader URLs.
   */
  public static URL[] getClassLoaderURLs( ClassLoader cl )
  {
    URL[] urls = {};
    try
    {
      Class<?> returnType = urls.getClass();
      Class<?>[] parameterTypes = {};
      Method getURLs = cl.getClass().getMethod( "getURLs", parameterTypes );
      if ( returnType.isAssignableFrom( getURLs.getReturnType() ) )
      {
        Object[] args = {};
        urls = (URL[]) getURLs.invoke( cl, args );
      }
    }
    catch ( NoSuchMethodException ignore )
    {
      // can happen for class loaders not derived from java.net.URLClassLoader
    }
    catch ( IllegalAccessException ignore )
    {
      // should not happen
    }
    catch ( InvocationTargetException ignore )
    {
      // should not happen
    }
    return urls;
  }
}

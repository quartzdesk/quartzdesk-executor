/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Simple wrapper around the Spring application context that
 * provides easy access to the Spring application context from
 * all parts of the application. It also provides generified
 * {@code getBean} methods to avoid casts in the application code.
 */
public class SpringContext
{
  private static final Logger log = LoggerFactory.getLogger( SpringContext.class );

  private ApplicationContext appCtx;


  /**
   * Creates a new {@link SpringContext} instance wrapping the specified
   * application context.
   *
   * @param appCtx an application context instance to wrap.
   */
  protected SpringContext( ApplicationContext appCtx )
  {
    this.appCtx = appCtx;
  }


  /**
   * Returns the {@link SpringContext} instance wrapping the specified
   * application context.
   *
   * @param appCtx an application context.
   * @return the {@link SpringContext} instance wrapping the specified
   *         application context.
   */
  public static SpringContext forContext( ApplicationContext appCtx )
  {
    return new SpringContext( appCtx );
  }


  /**
   * Returns the {@link SpringContext} instance wrapping a new application
   * context whose configuration is read from the specified classpath resource.
   *
   * @param resource an application context configuration classpath resource.
   * @return the {@link SpringContext} instance wrapping the created
   *         application context.
   */
  public static SpringContext forClasspathResource( String resource )
  {
    return new SpringContext( new ClassPathXmlApplicationContext( resource ) );
  }


  /**
   * Returns the {@link SpringContext} instance wrapping a new application
   * context whose configuration is read from the specified file path.
   *
   * @param filePath an application context configuration file path.
   * @return the {@link SpringContext} instance wrapping the created
   *         application context.
   */
  public static SpringContext forFileSystemResource( String filePath )
  {
    return new SpringContext( new FileSystemXmlApplicationContext( filePath ) );
  }


  /**
   * Returns the {@link SpringContext} instance wrapping the current
   * application context. This method is typically used to access
   * the application context from application components that are not
   * registered Spring beans.
   *
   * @return the {@link SpringContext} instance wrapping the current
   *         application context.
   */
  public static SpringContext getCurrent()
  {
    return new SpringContext( ApplicationContextProvider.getApplicationContext() );
  }


  /**
   * Returns the bean with the specified name.
   *
   * @param name a bean name.
   * @return the bean instance.
   */
  @SuppressWarnings("unchecked")
  public <T> T getBean( String name )
  {
    return (T) appCtx.getBean( name );
  }


  /**
   * Returns the bean with the specified name and type (can be an interface, or superclass).
   *
   * @param name a bean name.
   * @param beanType a required bean type.
   * @return the bean instance.
   */
  @SuppressWarnings("unchecked")
  public <T> T getBean( String name, Class<T> beanType )
  {
    return appCtx.getBean( name, beanType );
  }


  /**
   * Returns the bean with the specified type (can be an interface, or superclass).
   *
   * @param beanType a bean type.
   * @return the bean instance.
   */
  @SuppressWarnings( "unchecked" )
  public <T> T getBean( Class<T> beanType )
  {
    return (T) appCtx.getBean( beanType );
  }
}

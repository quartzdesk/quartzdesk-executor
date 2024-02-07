/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.web.listener.log;

import com.quartzdesk.executor.common.debug.Debug;
import com.quartzdesk.executor.common.text.MacroExpander;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This servlet context listener initializes the Logback logging framework
 * using the logback XML configuration file read from the specified location.
 * <p>
 * The location of the logback configuration file is passed to this
 * listener using the {@code configLocation} init parameter. The value of this
 * parameter can optionally refer to Servlet Context parameters and or JVM system
 * properties using the {@code ${parameter_name}} syntax where {@code parameter_name}
 * is either a name of a servlet context init parameter, or a name of of JVM system
 * property.
 * </p>
 */
public class LogbackInitContextListener
    implements ServletContextListener
{
  private static final Logger log = LoggerFactory.getLogger( LogbackInitContextListener.class );

  public static final String INIT_PARAM_CONFIG_LOCATION = "logbackConfigLocation";


  @Override
  public void contextInitialized( ServletContextEvent sce )
  {
    ServletContext servletCtx = sce.getServletContext();
    maybeInitializeLogback( servletCtx );
  }


  @Override
  public void contextDestroyed( ServletContextEvent sce )
  {
  }


  /**
   * Initializes Logback framework using the specified configuration file.
   * If the configuration file does not exist, or cannot be read, then this method logs
   * the problem and returns.
   *
   * @param servletCtx a servlet context.
   */
  private void maybeInitializeLogback( ServletContext servletCtx )
  {
    // non-expanded logback config file location
    String configLocation = servletCtx.getInitParameter( INIT_PARAM_CONFIG_LOCATION );

    String expandedConfigLocation = expandProperties( configLocation, servletCtx );
    File configLocationFile = new File( expandedConfigLocation );

    if ( configLocationFile.exists() || configLocationFile.isFile() )
    {
      if ( configLocationFile.canRead() )
      {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if ( loggerFactory instanceof LoggerContext )
        {
          log.info( "About to initialize Logback using config file: {}", configLocationFile );

          LoggerContext context = (LoggerContext) loggerFactory;
          JoranConfigurator jc = new JoranConfigurator();
          jc.setContext( context );
          context.reset();

          context.putProperty( "logback.config.dir", configLocationFile.getParentFile().getAbsolutePath() );

          try
          {
            jc.doConfigure( configLocationFile );

            log.info( "Successfully initialized Logback using config file: {}", configLocationFile );
          }
          catch ( JoranException e )
          {
            log.error( "Error initializing Logback from: " + configLocationFile, e );
          }
        }
        else
        {
          if ( loggerFactory instanceof NOPLoggerFactory )
          {
            // need to use System.out, otherwise the message would have been silently swallowed by the NOPLogger
            System.out.println(
                "Logback cannot be initialized as SLF4J is not statically bound to any logging framework implementation. See system error output for possible SLF4J errors." );

            // print SLF4J LoggerFactory class info
            System.out.println(
                "SLF4J LoggerFactory class info: " + Debug.getClassInfo( LoggerFactory.class ) );
          }
          else
          {
            log.warn(
                "Logback cannot be initialized as SLF4J is not statically bound to Logback implementation. Bound SLF4J logger factory: {}",
                loggerFactory.getClass().getName() );

            // log SLF4J LoggerFactory class info
            log.warn( "SLF4J LoggerFactory class info: {}", Debug.getClassInfo( LoggerFactory.class ) );
          }
        }
      }
      else
      {
        log.info( "Logback configuration file: {} exists, but cannot be read. Logback configuration not changed.",
            configLocationFile );
      }
    }
    else
    {
      log.info( "Logback configuration file: {} not found. Logback configuration not changed.", configLocationFile );
    }
  }


  /**
   * Expands all servlet context init parameters and/or JVM system properties referenced
   * in the specified string and returns the result. The reference is expected to use the
   * {@code ${parameter_name}} syntax. Servlet context init parameters override JVM system
   * properties.
   *
   * @param value a value.
   * @return the value with expanded property references.
   */
  @SuppressWarnings( "unchecked" )
  private String expandProperties( String value, ServletContext servletCtx )
  {
    Map<String, String> propertyMap = new HashMap<String, String>();

    // 1. JVM system properties - lowest priority
    for ( Map.Entry<Object, Object> systemProperty : System.getProperties().entrySet() )
    {
      propertyMap.put( systemProperty.getKey().toString(), systemProperty.getValue().toString() );
    }

    // 2. servlet context init parameters - highest priority (override JVM system properties)
    for ( Enumeration<String> paramNames =
        (Enumeration<String>) servletCtx.getInitParameterNames(); paramNames.hasMoreElements(); )
    {
      String paramName = paramNames.nextElement();
      propertyMap.put( paramName, servletCtx.getInitParameter( paramName ) );
    }

    MacroExpander expander = new MacroExpander( propertyMap );
    return expander.expandMacros( value );
  }
}

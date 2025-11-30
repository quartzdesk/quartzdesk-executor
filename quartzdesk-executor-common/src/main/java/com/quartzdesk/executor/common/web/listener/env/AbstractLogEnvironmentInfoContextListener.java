 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.common.web.listener.env;

 import com.quartzdesk.executor.common.CommonConst;

 import jakarta.servlet.ServletContext;
 import jakarta.servlet.ServletContextEvent;
 import jakarta.servlet.ServletContextListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import java.util.Collections;
 import java.util.Map;

 /**
  * Common base class for all servlet context listeners that print application
  * environment info to the log upon application startup.
  */
 public abstract class AbstractLogEnvironmentInfoContextListener
     implements ServletContextListener
 {
   @Override
   public void contextInitialized( ServletContextEvent sce )
   {
     ServletContext servletContext = sce.getServletContext();

     String applicationName = getApplicationName( servletContext );

     Logger log = LoggerFactory.getLogger( getClass() );

     if ( log.isInfoEnabled() )
     {
       StringBuilder sb = new StringBuilder()
           .append( "****************** Start " )
           .append( applicationName )
           .append( " Application Environment *******************" )

           .append( CommonConst.NL )
           .append( "Java Vendor: " ).append( System.getProperty( "java.vendor" ) )
           .append( CommonConst.NL )
           .append( "Java Version: " ).append( System.getProperty( "java.version" ) )
           .append( CommonConst.NL )
           .append( "Java Runtime Version: " ).append( System.getProperty( "java.runtime.version" ) )
           .append( CommonConst.NL )

           .append( "Java VM Name: " ).append( System.getProperty( "java.vm.name" ) )
           .append( CommonConst.NL )
           .append( "Java VM Vendor: " ).append( System.getProperty( "java.vm.vendor" ) )
           .append( CommonConst.NL )
           .append( "Java VM Version: " ).append( System.getProperty( "java.vm.version" ) )
           .append( CommonConst.NL )

           .append( "OS Name: " ).append( System.getProperty( "os.name" ) )
           .append( CommonConst.NL )
           .append( "OS Version: " ).append( System.getProperty( "os.version" ) )
           .append( CommonConst.NL )
           .append( "OS Architecture: " ).append( System.getProperty( "os.arch" ) )
           .append( CommonConst.NL )

           .append( "Java Home: " ).append( System.getProperty( "java.home" ) )
           .append( CommonConst.NL )
           .append( "Java Classpath: " ).append( System.getProperty( "java.class.path" ) )
           .append( CommonConst.NL );

       SecurityManager secMgr = System.getSecurityManager();
       sb.append( "Security Manager: " ).append( secMgr == null ? CommonConst.NOT_AVAIL : secMgr )
           .append( CommonConst.NL );

       String secPolicy = System.getProperty( "java.security.policy" );
       sb.append( "Java Security Policy: " ).append( secPolicy == null ? CommonConst.NOT_AVAIL : secPolicy )
           .append( CommonConst.NL );

       // print extended info (application specific info)
       for ( Map.Entry<String, String> entry : getExtendedInfo( servletContext ).entrySet() )
       {
         sb.append( entry.getKey() ).append( ": " ).append( entry.getValue() ).append( CommonConst.NL );
       }

       sb.append( "******************* End " )
           .append( applicationName )
           .append( " Application Environment ********************" );

       log.info( "Environment info:{}{}", CommonConst.NL, sb.toString() );
     }
   }


   @Override
   public void contextDestroyed( ServletContextEvent sce )
   {
   }


   /**
    * Returns the map with extended application info that is to be logged. This
    * implementation returns an empty map.
    *
    * @param servletContext a servlet context.
    * @return the map with extended application info.
    */
   protected Map<String, String> getExtendedInfo( ServletContext servletContext )
   {
     return Collections.emptyMap();
   }


   /**
    * Returns the application name.
    *
    * @param servletContext a servlet context.
    * @return the application name.
    */
   protected abstract String getApplicationName( ServletContext servletContext );
 }

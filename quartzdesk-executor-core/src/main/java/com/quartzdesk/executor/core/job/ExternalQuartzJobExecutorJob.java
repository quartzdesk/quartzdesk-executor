/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.core.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A Quartz job implementation that instantiates and executes the specified external
 * Quartz job implementation class. This job expects the following two job data map
 * parameters:
 *
 * <dl>
 * <dt>jobHomeDir</dt>
 * <dd>The job home directory containing {@code classes} and/or {@code lib} directories.
 * {@code classes} directory is expected to contain classes and resources required by the external Quartz job.
 * {@code lib} directory is expected to contain libraries (JAR files) required by the external Quartz job.
 * <br/>
 * <strong>WARNING: Do not include the Quartz scheduler JAR file in the lib directory.</strong>
 * </dd>
 *
 * <dt>jobClassName</dt>
 * <dd>The fully qualified Quartz job implementation class name. The job class must extend the Quartz {@code
 * org.quartz.Job} interface.</dd>
 * </dl>
 */
@DisallowConcurrentExecution
public class ExternalQuartzJobExecutorJob
    extends AbstractJob
{
  private static final Logger log = LoggerFactory.getLogger( ExternalQuartzJobExecutorJob.class );

  private static final String JDM_KEY_JOB_HOME_DIR = "jobHomeDir";
  private static final String JDM_KEY_JOB_CLASS_NAME = "jobClassName";


  @Override
  protected void executeJob( final JobExecutionContext context )
      throws JobExecutionException
  {
    log.debug( "Inside job: {}", context.getJobDetail().getKey() );
    JobDataMap jobDataMap = context.getMergedJobDataMap();

    // jobHomeDir (required)
    String jobHomeDirStr = jobDataMap.getString( JDM_KEY_JOB_HOME_DIR );
    if ( jobHomeDirStr == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_JOB_HOME_DIR + "' job data map parameter." );
    }
    File jobHomeDir = new File( jobHomeDirStr ).getAbsoluteFile();

    // jobClassName (required)
    final String jobClassName = jobDataMap.getString( JDM_KEY_JOB_CLASS_NAME );
    if ( jobClassName == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_JOB_CLASS_NAME + "' job data map parameter." );
    }

    ClassLoader origThreadContextClassLoader = Thread.currentThread().getContextClassLoader();
    try
    {
      ClassLoader externalJobClassLoader = getExternalJobClassLoader( jobHomeDir );

      Class<?> externalJobClass = externalJobClassLoader.loadClass( jobClassName );

      // check the specified job implementation class extends the Quartz Job interface
      if ( Job.class.isAssignableFrom( externalJobClass ) )
      {
        // create instance of the specified job class and invoke its execute method
        Job externalJob = (Job) externalJobClass.newInstance();

        // We must set the current thread's context class loader to the externalJobClassLoader just in case
        // the external job makes use of that class loader to load some of its classes/resources using
        // Java reflection APIs.
        Thread.currentThread().setContextClassLoader( externalJobClassLoader );

        externalJob.execute( context );
      }
      else
      {
        throw new JobExecutionException(
            "Specified job implementation class: " + externalJobClass + " does not extend " + Job.class.getName() +
                " class. Please make sure job home directory (" + jobHomeDir +
                ") does not contain the Quartz scheduler JAR (quartz-*.jar)." );
      }
    }
    catch ( JobExecutionException e )
    {
      throw e;
    }
    catch ( Exception e )
    {
      throw new JobExecutionException( "Error executing external Quartz job " + jobClassName, e );
    }
    finally
    {
      // restore the original thread context class loader
      Thread.currentThread().setContextClassLoader( origThreadContextClassLoader );
    }
  }


  /**
   * Returns the {@link ClassLoader} implementation to be used to load the external Quartz job implementation
   * class and its dependencies.
   *
   * @param jobHomeDir the external job's home directory.
   * @return the {@link ClassLoader} instance.
   */
  private ClassLoader getExternalJobClassLoader( File jobHomeDir )
  {
    URL[] classLoaderUrls = getExternalJobClassLoaderUrls( jobHomeDir );

    URLClassLoader externalJobClassLoader =
        new URLClassLoader( classLoaderUrls, Thread.currentThread().getContextClassLoader() );

    return externalJobClassLoader;
  }


  /**
   * Returns the list of URLs to be used to load the external job's class and its dependencies.
   *
   * @param jobHomeDir the external job's home directory.
   * @return the list of URLs.
   */
  private URL[] getExternalJobClassLoaderUrls( File jobHomeDir )
  {
    List<URL> urlList = new ArrayList<URL>();

    // 1. add the classes directory if it exists
    File classesDir = new File( jobHomeDir, "classes" );
    if ( classesDir.exists() && classesDir.isDirectory() )
    {
      try
      {
        urlList.add( classesDir.toURI().toURL() );
      }
      catch ( MalformedURLException e )
      {
        // should never happen
        log.warn( "Error converting file path: " + classesDir + " to URL.", e );
      }
    }

    // 2. add all JARs in the lib directory if it exists
    File libDir = new File( jobHomeDir, "lib" );
    if ( libDir.exists() && libDir.isDirectory() )
    {
      File[] jarFiles = libDir.listFiles( new FilenameFilter()
      {
        @Override
        public boolean accept( File dir, String name )
        {
          return ( name.toLowerCase().endsWith( ".jar" ) );
        }
      } );

      for ( File jarFile : jarFiles )
      {
        try
        {
          urlList.add( jarFile.toURI().toURL() );
        }
        catch ( MalformedURLException e )
        {
          // should never happen
          log.warn( "Error converting file path: " + jarFile + " to URL.", e );
        }
      }
    }

    return urlList.toArray( new URL[urlList.size()] );
  }
}

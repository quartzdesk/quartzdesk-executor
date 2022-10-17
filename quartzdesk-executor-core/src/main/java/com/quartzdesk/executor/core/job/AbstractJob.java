/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.core.job;

import com.quartzdesk.executor.core.CommonConst;
import com.quartzdesk.executor.core.CommonUtils;
import com.quartzdesk.executor.core.JobDataMapBuilder;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.StopWatch;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Common auxiliary base class for all Quartz jobs.
 *
 * @see Job
 */
public abstract class AbstractJob
    implements Job
{
  private static final Logger log = LoggerFactory.getLogger( AbstractJob.class );

  /**
   * Key of the scheduler context property that holds the {@link ApplicationContext}
   * instance. Please check refer to the {@code applicationContextSchedulerContextKey}
   * property of the {@link SchedulerFactoryBean}.
   */
  private static final String SCHEDULER_CONTEXT_APPLICATION_CONTEXT_KEY = "applicationContext";


  /**
   * The method invoked by the Quartz scheduler.
   *
   * @param context a {@link JobExecutionContext} instance.
   * @throws JobExecutionException if an error occurs while executing the job.
   */
  @Override
  public final void execute( JobExecutionContext context )
      throws JobExecutionException
  {
    String jobFullName = context.getJobDetail().getKey().toString();

    StopWatch sw = new StopWatch();
    sw.start();

    ClassLoader origContextClassLoader = Thread.currentThread().getContextClassLoader();

    try
    {
      if ( log.isInfoEnabled() )
        log.info( "Started scheduled job: {}", jobFullName );

      JobDataMap jobDataMap = context.getMergedJobDataMap();

      if ( log.isDebugEnabled() )
      {
        log.debug( "Job data map dump (BEFORE macros expansion):{}{}", CommonConst.NL,
            getMapDump( jobDataMap ) );
      }

      JobDataMapBuilder jobDataMapBuilder = new JobDataMapBuilder( context );

//      if ( log.isDebugEnabled() )
//      {
//        log.debug( "Job data map expansion macros:{}{}", CommonConst.NL, getMapDump( jobDataMapBuilder.getMacros() ) );
//      }

      JobDataMap expandedJobDataMap = jobDataMapBuilder.build();

      // replace all values in the original job data map with expanded values
      jobDataMap.putAll( expandedJobDataMap );

      if ( log.isDebugEnabled() )
      {
        log.debug( "Job data map dump (AFTER macros expansion):{}{}", CommonConst.NL, getMapDump( jobDataMap ) );
      }

      // Set the context class loader to be the class loader of the job class.
      // This is a workaround/fix for the WebSpere Application Server where
      // WebSphere work manager threads are typically used to execute jobs.
      Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

      executeJob( context );

      sw.stop();

      if ( log.isInfoEnabled() )
        log.info( "Finished scheduled job: {}. Time taken: {}s.", jobFullName, sw.getTotalTimeSeconds() );
    }
    catch ( JobExecutionException e )
    {
      if ( log.isErrorEnabled() )
        log.error( "Error executing scheduled job: " + jobFullName, e );
      throw e;
    }
    finally
    {
      // restore the original thread context class loader
      Thread.currentThread().setContextClassLoader( origContextClassLoader );
    }
  }


  /**
   * Method that must be implemented by all jobs.
   *
   * @param context a {@link JobExecutionContext} instance.
   * @throws JobExecutionException if an error occurs.
   */
  protected abstract void executeJob( JobExecutionContext context )
      throws JobExecutionException;


  /**
   * Returns the {@link ApplicationContext} instance extracted from the scheduler context, or
   * null if not found.
   *
   * @param context the {@link JobExecutionContext} instance.
   * @return the {@link ApplicationContext} instance.
   */
  protected ApplicationContext getApplicationContext( JobExecutionContext context )
  {
    try
    {
      return (ApplicationContext) context.getScheduler().getContext().get( SCHEDULER_CONTEXT_APPLICATION_CONTEXT_KEY );
    }
    catch ( SchedulerException e )
    {
      if ( log.isErrorEnabled() )
        log.error( "Error obtaining the application context.", e );

      return null;
    }
  }


  private String getMapDump( Map<String, ?> map )
  {
    Map<String, Object> sortedMap = new TreeMap<>();
    sortedMap.putAll( map );

    StringBuilder dump = new StringBuilder();

    for ( Iterator<String> keys = sortedMap.keySet().iterator(); keys.hasNext(); )
    {
      String key = keys.next();
      String value = CommonUtils.safeToString( sortedMap.get( key ) );

      dump.append( "  " )
          .append( key )
          .append( '=' )
          .append( value );

      if ( keys.hasNext() )
        dump.append( CommonConst.NL );
    }

    return dump.toString();
  }
}

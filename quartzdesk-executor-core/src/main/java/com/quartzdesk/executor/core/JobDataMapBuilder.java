package com.quartzdesk.executor.core;

import com.quartzdesk.executor.common.DateTimeUtils;
import com.quartzdesk.executor.common.text.MacroExpander;
import com.quartzdesk.executor.domain.convert.VersionConverter;
import com.quartzdesk.executor.domain.model.common.Version;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.StdScheduler;

import javax.management.ObjectName;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Expands job data map macros of an executed job instance.
 */
public class JobDataMapBuilder
{
  private static final String MACRO_CURRENT_TIMESTAMP = "CURRENT_TIMESTAMP";

  private static final String MACRO_SCHEDULER_OBJECT_NAME = "SCHEDULER_OBJECT_NAME";

  private static final String MACRO_SCHEDULER_VERSION = "SCHEDULER_VERSION";

  private static final String MACRO_SCHEDULER_NAME = "SCHEDULER_NAME";

  private static final String MACRO_SCHEDULER_INSTANCE_ID = "SCHEDULER_INSTANCE_ID";

  private static final String MACRO_JOB_GROUP_NAME = "JOB_GROUP_NAME";

  private static final String MACRO_JOB_NAME = "JOB_NAME";

  private static final String MACRO_JOB_DESCRIPTION = "JOB_DESCRIPTION";

  private static final String MACRO_JOB_CLASS_NAME = "JOB_CLASS_NAME";

  private static final String MACRO_JOB_CLASS_LOCATION = "JOB_CLASS_LOCATION";

  private static final String MACRO_TRIGGER_GROUP_NAME = "TRIGGER_GROUP_NAME";

  private static final String MACRO_TRIGGER_NAME = "TRIGGER_NAME";

  private static final String MACRO_TRIGGER_TYPE = "TRIGGER_TYPE";

  private static final String MACRO_TRIGGER_DESCRIPTION = "TRIGGER_DESCRIPTION";

  private static final String MACRO_CALENDAR_NAME = "CALENDAR_NAME";

  private static final String MACRO_EXEC_TYPE = "EXEC_TYPE";

  private static final String MACRO_JOB_EXECUTION_ID = "JOB_EXECUTION_ID";

  private static final String MACRO_FIRE_INSTANCE_ID = "FIRE_INSTANCE_ID";

  private static final String MACRO_FIRE_TIME = "FIRE_TIME";

  private static final String MACRO_SCHEDULED_FIRE_TIME = "SCHEDULED_FIRE_TIME";

  private static final String MACRO_PREVIOUS_FIRE_TIME = "PREVIOUS_FIRE_TIME";

  private static final String MACRO_NEXT_FIRE_TIME = "NEXT_FIRE_TIME";

  private static final String MACRO_RECOVERING = "RECOVERING";

  private static final String MACRO_JOB_DATA_MAP = "JOB_DATA_MAP";

  private static final String EXEC_TYPE_NORMAL = "NORMAL";

  private static final String EXEC_TYPE_QD_SYSTEM = "QD_SYSTEM";

  private static final String EXEC_TYPE_QD_MANUAL = "QD_MANUAL";

  private static final String EXEC_TYPE_QD_JOB_CHAIN = "QD_JOB_CHAIN";

  private static final String EXEC_TYPE_QD_WEB_SERVICE = "QD_WEB_SERVICE";

  private JobExecutionContext context;

  private Map<String, ?> macros;


  public JobDataMapBuilder( JobExecutionContext context )
  {
    this.context = context;
    macros = createMacros( context );
  }


  /**
   * Builds a new JobDataMap based on the job data map returned by @link {@link JobExecutionContext#getMergedJobDataMap()})
   * and returns a new JobDataMap with expanded macro values.
   *
   * @return the expanded job data map.
   */
  public JobDataMap build()
  {
    JobDataMap origJobDataMap = context.getMergedJobDataMap();
    JobDataMap newJobDataMap = new JobDataMap();

    MacroExpander macroExpander = new MacroExpander();

    for ( Map.Entry<String, Object> entry : origJobDataMap.entrySet() )
    {
      String key = entry.getKey();
      Object value = entry.getValue();

      // only String-based job data map parameters are expanded
      if ( value instanceof String )
      {
        Set<String> expandedValues = new HashSet<>();
        String expandedValue = macroExpander.expandMacros( (String) value, macros );
        /*
         * Keep expanding macros while the expanded value keeps changing.
         */
        while ( !expandedValues.contains( expandedValue ) )
        {
          expandedValues.add( expandedValue );
          expandedValue = macroExpander.expandMacros( expandedValue, macros );
        }
        newJobDataMap.put( key, expandedValue );
      }
      else
      {
        // store the original value as is (non-String values cannot contain macros...)
        newJobDataMap.put( key, value );
      }
    }

    return newJobDataMap;
  }


  public Map<String, ?> getMacros()
  {
    return macros;
  }


  /**
   * Creates and returns a map of macros used for the expansion of macros in the job data map.
   *
   * @param context a Quartz job execution context.
   * @return the map of expansion macros.
   */
  private Map<String, ?> createMacros( JobExecutionContext context )
  {
    Map<String, Object> macros = new HashMap<>();

    /*
     * General macros.
     */
    macros.put( MACRO_CURRENT_TIMESTAMP, Calendar.getInstance() );

    /*
     * Scheduler-related macros.
     */
    Scheduler scheduler = context.getScheduler();
    ObjectName schedulerObjectName = getSchedulerObjectName( scheduler );
    if ( schedulerObjectName != null )
    {
      macros.put( MACRO_SCHEDULER_OBJECT_NAME, schedulerObjectName );
    }

    try
    {
      SchedulerMetaData schedMetaData = scheduler.getMetaData();

      Version schedVersion = VersionConverter.INSTANCE.fromString( schedMetaData.getVersion() );
      macros.put( MACRO_SCHEDULER_VERSION, VersionConverter.INSTANCE.toString( schedVersion ) );

      macros.put( MACRO_SCHEDULER_NAME, schedMetaData.getSchedulerName() );
      macros.put( MACRO_SCHEDULER_INSTANCE_ID, schedMetaData.getSchedulerInstanceId() );
    }
    catch ( SchedulerException e )
    {
      // should never happen...
    }

    /*
     * JobDetail-related macros.
     */
    JobDetail jobDetail = context.getJobDetail();
    macros.put( MACRO_JOB_GROUP_NAME, jobDetail.getKey().getGroup() );
    macros.put( MACRO_JOB_NAME, jobDetail.getKey().getName() );
    macros.put( MACRO_JOB_DESCRIPTION, jobDetail.getDescription() );
    macros.put( MACRO_JOB_CLASS_NAME, jobDetail.getJobClass().getName() );
    macros.put( MACRO_JOB_CLASS_LOCATION, jobDetail.getJobClass().getProtectionDomain().getCodeSource().getLocation() );

    /*
     * Trigger-related macros.
     */
    Trigger trigger = context.getTrigger();
    macros.put( MACRO_TRIGGER_GROUP_NAME, trigger.getKey().getGroup() );
    macros.put( MACRO_TRIGGER_NAME, trigger.getKey().getName() );
    macros.put( MACRO_TRIGGER_TYPE, trigger.getKey().getGroup() );
    macros.put( MACRO_TRIGGER_DESCRIPTION, trigger.getDescription() );

    /*
     * Calendar-related macros.
     */
    //org.quartz.Calendar calendar = context.getCalendar();
    macros.put( MACRO_CALENDAR_NAME, trigger.getCalendarName() );

    /*
     * Executing job-related macros.
     */
    macros.put( MACRO_EXEC_TYPE, getExecType( context ) );
    macros.put( MACRO_JOB_EXECUTION_ID, createJobExecutionId( context.getFireInstanceId(), schedulerObjectName ) );
    macros.put( MACRO_FIRE_INSTANCE_ID, context.getFireInstanceId() );
    macros.put( MACRO_FIRE_TIME, DateTimeUtils.date2Calendar( context.getFireTime() ) );
    macros.put( MACRO_SCHEDULED_FIRE_TIME, DateTimeUtils.date2Calendar( context.getScheduledFireTime() ) );
    macros.put( MACRO_PREVIOUS_FIRE_TIME, DateTimeUtils.date2Calendar( context.getPreviousFireTime() ) );
    macros.put( MACRO_NEXT_FIRE_TIME, DateTimeUtils.date2Calendar( context.getNextFireTime() ) );
    macros.put( MACRO_RECOVERING, context.isRecovering() );

    JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
    for ( Map.Entry<String, ?> entry : mergedJobDataMap.entrySet() )
    {
      String key = entry.getKey();
      Object value = entry.getValue();
      String macroName = MACRO_JOB_DATA_MAP + '[' + key + ']';
      macros.put( macroName, value );
    }

    return macros;
  }


  /**
   * Returns the execution type for the executed job described by the specified Quartz job execution context.
   * This method derives the execution type from the trigger group's name.
   *
   * @param context the Quartz job execution context.
   * @return the execution type.
   */
  private String getExecType( JobExecutionContext context )
  {
    String triggerGroup = context.getTrigger().getKey().getGroup();

    switch ( triggerGroup )
    {
      case "QD_MANUAL":
        return EXEC_TYPE_QD_MANUAL;

      case "QD_JOB_CHAIN":
        return EXEC_TYPE_QD_JOB_CHAIN;

      case "QD_SYSTEM":
        return EXEC_TYPE_QD_SYSTEM;

      case "QD_WEB_SERVICE":
        return EXEC_TYPE_QD_WEB_SERVICE;

      default:
        return EXEC_TYPE_NORMAL;
    }
  }


  /**
   * Returns the Quartz scheduler object name of the specified Quartz scheduler.
   *
   * @return the object name.
   */
  private ObjectName getSchedulerObjectName( Scheduler scheduler )
  {
    if ( scheduler instanceof StdScheduler )
    {
      try
      {
        // 1. get hold of the QuartzScheduler instance wrapped in the StdScheduler instance
        Class<?> schedulerClass = scheduler.getClass();
        Field schedField = schedulerClass.getDeclaredField( "sched" );
        schedField.setAccessible( true );
        QuartzScheduler quartzScheduler = (QuartzScheduler) schedField.get( scheduler );

        // 2. get hold of the QuartzSchedulerResources instance wrapped in the QuartzScheduler instance
        Class<?> quartzSchedulerClass = quartzScheduler.getClass();
        Field resourcesField = quartzSchedulerClass.getDeclaredField( "resources" );
        resourcesField.setAccessible( true );

        // 3. obtain the JMX object name
        QuartzSchedulerResources resources = (QuartzSchedulerResources) resourcesField.get( quartzScheduler );
        return new ObjectName( resources.getJMXObjectName() );
      }
      catch ( Exception e )  // NoSuchFieldException, IllegalAccessException, MalformedObjectNameException
      {
        // do nothing
      }
    }

    return null;
  }


  /**
   * Creates and returns the job execution ID from the specified Quartz job fire instance ID and scheduler object name.
   *
   * @param fireInstanceId  a job fire instance ID.
   * @param schedObjectName a scheduler object name.
   * @return the job execution ID from the specified Quartz job fire instance ID and scheduler object name.
   */
  private String createJobExecutionId( String fireInstanceId, ObjectName schedObjectName )
  {
    if ( fireInstanceId == null )
      return null;

    return fireInstanceId + ( schedObjectName == null ? "" : '@' + schedObjectName.toString() );
  }
}

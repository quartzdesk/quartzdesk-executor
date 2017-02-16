/*
 * Copyright (c) 2015-2017 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.web.spring;

import com.quartzdesk.executor.web.WorkDir;

import com.quartzdesk.executor.common.CommonUtils;
import com.quartzdesk.executor.common.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * Activate either of the following Spring profiles based on the installed
 * QuartzDesk Executor DB profile:
 * <ul>
 * <li>db2</li>
 * <li>h2</li>
 * <li>mssql</li>
 * <li>mysql</li>
 * <li>mysql_innodb</li>
 * <li>oracle8</li>
 * <li>oracle9</li>
 * <li>postgres81</li>
 * <li>postgres82</li>
 * </ul>
 */
public class SpringProfilesActivator
    implements ApplicationContextInitializer<XmlWebApplicationContext>
{
  private static final Logger log = LoggerFactory.getLogger( SpringProfilesActivator.class );

  /**
   * Name of the default QuartzDesk Executor configuration resource.
   */
  private static final String DEFAULT_QUARTZDESK_EXECUTOR_CFG = "default-quartzdesk-executor.properties";

  /**
   * Name of the QuartzDesk Executor configuration property specifying the database profile.
   */
  private static final String CONFIG_KEY_DB_PROFILE = "db.profile";


  @Override
  public void initialize( XmlWebApplicationContext applicationContext )
  {
    ConfigurableEnvironment env = applicationContext.getEnvironment();

    WorkDir workDir;
    try
    {
      workDir = new WorkDir( applicationContext.getServletContext() );
    }
    catch ( IOException e )
    {
      throw new ApplicationContextException( "Error obtaining QuartzDesk Executor work directory.", e );
    }

    String databaseProfile = getDatabaseProfile( workDir );

    String[] activeProfiles = new String[] { databaseProfile };

    log.info( "Activating Spring profiles: {}", Arrays.toString( activeProfiles ) );

    env.setActiveProfiles( activeProfiles );
  }


  /**
   * Merges properties from the {@code default-quartzdesk-executor.properties} and
   * <code>${quartzdesk-executor.work.dir}/quartzdesk-executor.properties</code> (if it exists) and returns the value
   * of the {@code db.profile.name} configuration property.
   *
   * @param workDir the QuartzDesk Executor work directory.
   * @return the database profile to activate.
   */
  private String getDatabaseProfile( WorkDir workDir )
  {
    Properties mergedCfg = new Properties();

    /*
     * 1. read default-quartzdesk-executor.properties
     */
    InputStream defaultCfgIns = null;
    try
    {
      defaultCfgIns = CommonUtils.getResourceAsStream( DEFAULT_QUARTZDESK_EXECUTOR_CFG );
      if ( defaultCfgIns == null )
        throw new ApplicationContextException(
            "Default QuartzDesk Executor configuration: " + DEFAULT_QUARTZDESK_EXECUTOR_CFG + " not found." );

      Properties defaultCfg = new Properties();
      defaultCfg.load( defaultCfgIns );

      mergedCfg.putAll( defaultCfg );
    }
    catch ( IOException e )
    {
      throw new ApplicationContextException(
          "Error reading default QuartzDesk Executor configuration: " + DEFAULT_QUARTZDESK_EXECUTOR_CFG, e );
    }
    finally
    {
      IOUtils.close( defaultCfgIns );
    }

    /*
     * 2. read ${quartzdesk-executor.work.dir}/quartzdesk-executor.properties
     */
    File cfgFile = new File( workDir.getRoot(), "quartzdesk-executor.properties" );
    if ( IOUtils.isReadableFile( cfgFile ) )
    {
      InputStream cfgIns = null;
      try
      {
        cfgIns = new FileInputStream( cfgFile );

        Properties cfg = new Properties();
        cfg.load( cfgIns );

        mergedCfg.putAll( cfg );
      }
      catch ( IOException e )
      {
        throw new ApplicationContextException(
            "Error reading QuartzDesk configuration: " + cfgFile.getAbsoluteFile(), e );
      }
      finally
      {
        IOUtils.close( cfgIns );
      }
    }

    String dbProfile = mergedCfg.getProperty( CONFIG_KEY_DB_PROFILE );
    if ( dbProfile == null )
    {
      throw new ApplicationContextException(
          "QuartzDesk Executor configuration property: " + CONFIG_KEY_DB_PROFILE + " not defined." );
    }

    return dbProfile.trim();
  }
}

/*
 * Copyright (c) 2015-2025 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.core.job;

import com.opencsv.CSVWriter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Quartz job implementation that executes an SQL query in the specified database and saves the result set in the CVS
 * format in the job execution result. This job accepts the following job data map parameters. Required parameters
 * marked with (*):
 * <dl>
 * <dt>jdbcDriver (*)</dt>
 * <dd>The fully-qualified class name of the JDBC driver to use. Make sure the JDBC driver is on the classpath!</dd>
 *
 * <dt>jdbcUrl (*)</dt>
 * <dd>The JDBC driver-specific URL that is used to connect to the database.</dd>
 *
 * <dt>jdbcUsername (*)</dt>
 * <dd>The username to authenticate with.</dd>
 *
 * <dt>jdbcPassword (*)</dt>
 * <dd>The password to authenticate with.</dd>
 *
 * <dt>sqlQuery (*)</dt>
 * <dd>The SQL query to execute.</dd>
 *
 * <dt>resultIncludeColumnNames</dt>
 * <dd>Boolean flag indicating if column names should be included in the CSV result data. True by default.</dd>
 *
 * <dt>resultTrimWhiteSpace</dt>
 * <dd>Boolean flag indicating if white-space should be trimmed in the CSV result data. True by default.</dd>
 * </dl>
 */
@DisallowConcurrentExecution
public class SqlQueryExecutorJob
    extends AbstractJob
{
  private static final Logger log = LoggerFactory.getLogger( SqlQueryExecutorJob.class );

  private static final String JDM_KEY_JDBC_DRIVER = "jdbcDriver";
  private static final String JDM_KEY_JDBC_URL = "jdbcUrl";
  private static final String JDM_KEY_JDBC_USERNAME = "jdbcUsername";
  private static final String JDM_KEY_JDBC_PASSWORD = "jdbcPassword";
  private static final String JDM_KEY_SQL_QUERY = "sqlQuery";

  private static final String JDM_KEY_RESULT_INCLUDE_COLUMN_NAMES = "resultIncludeColumnNames";
  private static final String JDM_KEY_RESULT_TRIM_WHITE_SPACE = "resultTrimWhiteSpace";


  @Override
  protected void executeJob( final JobExecutionContext context )
      throws JobExecutionException
  {
    log.debug( "Inside job: {}", context.getJobDetail().getKey() );
    JobDataMap jobDataMap = context.getMergedJobDataMap();

    // jdbcDriver (required)
    final String jdbcDriver = jobDataMap.getString( JDM_KEY_JDBC_DRIVER );
    if ( jdbcDriver == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_JDBC_DRIVER + "' job data map parameter." );
    }

    // jdbcUrl (required)
    final String jdbcUrl = jobDataMap.getString( JDM_KEY_JDBC_URL );
    if ( jdbcUrl == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_JDBC_URL + "' job data map parameter." );
    }

    // jdbcUsername (required)
    final String jdbcUsername = jobDataMap.getString( JDM_KEY_JDBC_USERNAME );
    if ( jdbcUsername == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_JDBC_USERNAME + "' job data map parameter." );
    }

    // jdbcPassword (required)
    final String jdbcPassword = jobDataMap.getString( JDM_KEY_JDBC_PASSWORD );
    if ( jdbcPassword == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_JDBC_PASSWORD + "' job data map parameter." );
    }

    // sqlQuery (required)
    final String sqlQuery = jobDataMap.getString( JDM_KEY_SQL_QUERY );
    if ( sqlQuery == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_SQL_QUERY + "' job data map parameter." );
    }

    // resultIncludeColumnNames (optional)
    final String resultIncludeColumnNamesStr = jobDataMap.getString( JDM_KEY_RESULT_INCLUDE_COLUMN_NAMES );
    boolean resultIncludeColumnNames = true;
    if ( resultIncludeColumnNamesStr != null )
    {
      resultIncludeColumnNames = Boolean.parseBoolean( resultIncludeColumnNamesStr );
    }

    // resultTrimWhiteSpace (optional)
    final String resultTrimWhiteSpaceStr = jobDataMap.getString( JDM_KEY_RESULT_TRIM_WHITE_SPACE );
    boolean resultTrimWhiteSpace = true;
    if ( resultTrimWhiteSpaceStr != null )
    {
      resultTrimWhiteSpace = Boolean.parseBoolean( resultTrimWhiteSpaceStr );
    }

    Connection con = null;
    PreparedStatement pstat = null;
    ResultSet res = null;
    try
    {
      Class<?> driverClazz = Class.forName( jdbcDriver );
      if ( Driver.class.isAssignableFrom( driverClazz ) )
      {
        log.info( "Opening JDBC connection to: {}", jdbcUrl );
        con = DriverManager.getConnection( jdbcUrl, jdbcUsername, jdbcPassword );

        pstat = con.prepareStatement( sqlQuery );

        log.info( "Executing SQL query: {}", sqlQuery );
        res = pstat.executeQuery();
        log.info( "SQL query successfully executed." );

        log.info( "Exporting JDBC result set to CSV format, resultIncludeColumnNames={}, resultTrimWhiteSpace={}.",
            resultIncludeColumnNames, resultTrimWhiteSpace );

        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter( writer );
        csvWriter.writeAll( res, resultIncludeColumnNames, resultTrimWhiteSpace );
        csvWriter.close();

        String resultTxt = writer.getBuffer().toString().trim();
        context.setResult( resultTxt );
      }
      else
      {
        throw new JobExecutionException(
            "JDBC driver class: " + jdbcDriver + " does not implement " + Driver.class.getName() + " interface." );
      }
    }
    catch ( ClassNotFoundException e )
    {
      throw new JobExecutionException( "JDBC driver class: " + jdbcDriver + " not found.", e );
    }

    catch ( SQLException e )
    {
      throw new JobExecutionException( "Error opening JDBC connection using URL: " + jdbcUrl, e );
    }
    catch ( IOException e )
    {
      throw new JobExecutionException( "Error exporting JDBC result set to CSV format.", e );
    }
    finally
    {
      close( con, pstat, res );
    }
  }


  /**
   * Closes the specified JDBC connection.
   *
   * @param con a JDBC connection.
   */
  private void close( Connection con )
  {
    if ( con != null )
    {
      try
      {
        con.close();
      }
      catch ( SQLException e )
      {
        log.error( "Error closing connection.", e );
      }
    }
  }


  /**
   * Closes the specified JDBC statement.
   *
   * @param stat a JDBC statement.
   */
  private void close( Statement stat )
  {
    if ( stat != null )
    {
      try
      {
        stat.close();
      }
      catch ( SQLException e )
      {
        log.error( "Error closing statement.", e );
      }
    }
  }


  /**
   * Closes the specified JDBC result set.
   *
   * @param res a JDBC result set.
   */
  private void close( ResultSet res )
  {
    if ( res != null )
    {
      try
      {
        res.close();
      }
      catch ( SQLException e )
      {
        log.error( "Error closing result set.", e );
      }
    }
  }


  /**
   * Closes the specified JDBC connection, statement and result set.
   *
   * @param con  a JDBC connection.
   * @param stat a JDBC statement.
   * @param res  a JDBC result set.
   */
  private void close( Connection con, Statement stat, ResultSet res )
  {
    close( res );
    close( stat );
    close( con );
  }
}

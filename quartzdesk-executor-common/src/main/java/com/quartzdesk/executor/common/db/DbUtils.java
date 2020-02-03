/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.db;

import com.quartzdesk.executor.common.DateTimeUtils;
import com.quartzdesk.executor.common.type.TimestampWithTZ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Various DB related utility methods.
 */
public final class DbUtils
{
  private static final Logger log = LoggerFactory.getLogger( DbUtils.class );

  /**
   * Maps sequence names onto selects used to obtain the next sequence value.
   */
  private static final Map<String, String> SEQ_QUERY_CACHE =
      Collections.synchronizedMap( new HashMap<String, String>() );


  /**
   * Private constructor of a utility class.
   */
  private DbUtils()
  {
  }


  /**
   * Prepares a statement from the specified SQL query and optionally sets its parameters.
   *
   * @param con    a JDBC connection.
   * @param query  an SQL query.
   * @param params parameters.
   * @return the prepared statement.
   * @throws java.sql.SQLException if an error occurs.
   */
  public static PreparedStatement prepareStatement( Connection con, String query, Object... params )
      throws SQLException
  {
    PreparedStatement pstat = con.prepareStatement( query );
    setStatementParams( pstat, params );
    return pstat;
  }


  /**
   * Sets the parameters in the specified statement according to their runtime types.
   *
   * @param pstat  a prepared statement.
   * @param params parameters.
   * @throws SQLException if an error occurs.
   */
  static void setStatementParams( PreparedStatement pstat, Object... params )
      throws SQLException
  {
    if ( params != null )
    {
      for ( int i = 0; i < params.length; )
      {
        Object param = params[i++];
        if ( param instanceof String )
        {
          pstat.setString( i, (String) param );
        }
        else if ( param instanceof Long )
        {
          pstat.setLong( i, (Long) param );
        }
        else if ( param instanceof Integer )
        {
          pstat.setInt( i, (Integer) param );
        }
        else if ( param instanceof Short )
        {
          pstat.setShort( i, (Short) param );
        }
        else if ( param instanceof Byte )
        {
          pstat.setByte( i, (Byte) param );
        }
        else if ( param instanceof BigDecimal )
        {
          pstat.setBigDecimal( i, (BigDecimal) param );
        }
        else if ( param instanceof Double )
        {
          pstat.setDouble( i, (Double) param );
        }
        else if ( param instanceof Float )
        {
          pstat.setFloat( i, (Float) param );
        }
        else if ( param instanceof Date )
        {
          pstat.setDate( i, new java.sql.Date( ( (Date) param ).getTime() ) );
        }
        else if ( param instanceof TimestampWithTZ )
        {
          TimestampWithTZ tz = (TimestampWithTZ) param;
          Calendar cal = DateTimeUtils.createResetCalendar( tz.getTimeZone() );
          pstat.setTimestamp( i, new Timestamp( tz.getMillis() ), cal );
        }
        else if ( param instanceof byte[] )
        {
          pstat.setBytes( i, (byte[]) param );
        }
        else if ( param instanceof Clob )
        {
          pstat.setClob( i, (Clob) param );
        }
        else if ( param instanceof Blob )
        {
          pstat.setBlob( i, (Blob) param );
        }
        else if ( param instanceof Boolean )
        {
          pstat.setBoolean( i, (Boolean) param );
        }
        else
        {
          throw new SQLException( "Unsupported type of statement parameter: " + param.getClass().getName() );
        }
      }
    }
  }


  /**
   * Returns the values of the specified column as a String. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static String getString( ResultSet res, String name )
      throws SQLException
  {
    String v = res.getString( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Long. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Long getLong( ResultSet res, String name )
      throws SQLException
  {
    long v = res.getLong( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as an Integer. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Integer getInt( ResultSet res, String name )
      throws SQLException
  {
    int v = res.getInt( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Short. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Short getShort( ResultSet res, String name )
      throws SQLException
  {
    short v = res.getShort( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Byte. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Byte getByte( ResultSet res, String name )
      throws SQLException
  {
    byte v = res.getByte( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a BigDecimal. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static BigDecimal getBigDecimal( ResultSet res, String name )
      throws SQLException
  {
    BigDecimal v = res.getBigDecimal( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Double. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Double getDouble( ResultSet res, String name )
      throws SQLException
  {
    double v = res.getDouble( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Float. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Float getFloat( ResultSet res, String name )
      throws SQLException
  {
    float v = res.getFloat( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Date. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Date getDate( ResultSet res, String name )
      throws SQLException
  {
    java.sql.Date v = res.getDate( name );
    return res.wasNull() ? null : new Date( v.getTime() );
  }


  /**
   * Returns the values of the specified column as a {@link TimestampWithTZ} instance.
   * If the column value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static TimestampWithTZ getTimestampWithTZ( ResultSet res, String name )
      throws SQLException
  {
    Calendar cal = DateTimeUtils.createResetCalendar();
    Timestamp v = res.getTimestamp( name, cal );
    return res.wasNull() ? null : new TimestampWithTZ( v.getTime(), cal.getTimeZone() );
  }


  /**
   * Returns the values of the specified column as a byte array. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static byte[] getBytes( ResultSet res, String name )
      throws SQLException
  {
    byte[] v = res.getBytes( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Clob. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Clob getClob( ResultSet res, String name )
      throws SQLException
  {
    Clob v = res.getClob( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Blob. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Blob getBlob( ResultSet res, String name )
      throws SQLException
  {
    Blob v = res.getBlob( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Returns the values of the specified column as a Boolean. If the column
   * value is null, this method returns null.
   *
   * @param res  a result set.
   * @param name a name of the column.
   * @return the value of the column.
   * @throws SQLException if an error occurs.
   */
  public static Boolean getBoolean( ResultSet res, String name )
      throws SQLException
  {
    boolean v = res.getBoolean( name );
    return res.wasNull() ? null : v;
  }


  /**
   * Closes the specified JDBC connection.
   *
   * @param con a JDBC connection.
   */
  public static void close( Connection con )
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
  public static void close( Statement stat )
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
  public static void close( ResultSet res )
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
  public static void close( Connection con, Statement stat, ResultSet res )
  {
    close( res );
    close( stat );
    close( con );
  }
}

/*
 * Copyright (c) 2013-2025 QuartzDesk.com. All Rights Reserved.
 * QuartzDesk.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.quartzdesk.executor.dao.hibernate.usertype.basic;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A user type that stores Java Calendar values in SQL TIMESTAMP_WITH_TIMEZONE columns.
 *
 * @version :$
 */
public class CalendarUT
    implements UserType<Calendar>
{
  private static final int SQL_TYPE = Types.TIMESTAMP_WITH_TIMEZONE;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<Calendar> returnedClass()
  {
    return Calendar.class;
  }


  @Override
  public boolean equals( Calendar value1, Calendar value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( Calendar value )
  {
    return value.hashCode();
  }


  @Override
  public Calendar nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    Timestamp value = res.getTimestamp( index );

    if ( res.wasNull() )
      return null;

    Calendar cal = new GregorianCalendar( TimeZone.getDefault() );
    cal.setTime( value );

    return cal;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, Calendar value, int index,
      SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      Timestamp timestamp = new Timestamp( value.getTimeInMillis() );
      pstat.setTimestamp( index, timestamp, value );
    }
  }


  @Override
  public Calendar deepCopy( Calendar value )
  {
    return value == null ? null : (Calendar) value.clone();
  }


  @Override
  public boolean isMutable()
  {
    return true;
  }


  @Override
  public Serializable disassemble( Calendar value )
  {
    return value;
  }


  @Override
  public Calendar assemble( Serializable cached, Object owner )
  {
    return (Calendar) cached;
  }
}

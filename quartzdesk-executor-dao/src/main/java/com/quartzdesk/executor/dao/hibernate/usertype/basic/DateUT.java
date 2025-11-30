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
import java.sql.Types;
import java.util.Date;

/**
 * A user type that stores Java Date values in SQL DATE columns.
 *
 * @version :$
 */
public class DateUT
    implements UserType<Date>
{
  private static final int SQL_TYPE = Types.DATE;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<Date> returnedClass()
  {
    return Date.class;
  }


  @Override
  public boolean equals( Date value1, Date value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( Date value )
  {
    return value.hashCode();
  }


  @Override
  public Date nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    java.sql.Date value = res.getDate( index );

    if ( res.wasNull() )
      return null;

    return value;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, Date value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setDate( index, new java.sql.Date( value.getTime() ) );
    }
  }


  @Override
  public Date deepCopy( Date value )
  {
    return value == null ? null : (Date) value.clone();
  }


  @Override
  public boolean isMutable()
  {
    return true;
  }


  @Override
  public Serializable disassemble( Date value )
  {
    return value;
  }


  @Override
  public Date assemble( Serializable cached, Object owner )
  {
    return (Date) cached;
  }
}

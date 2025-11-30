/*
 * Copyright (c) 2013-2025 QuartzDesk.com. All Rights Reserved.
 * QuartzDesk.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.quartzdesk.executor.dao.hibernate.usertype.basic;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.EnhancedUserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * A user type that stores Java Long values in SQL BIGINT columns.
 *
 * @version :$
 */
public class LongUT
    implements EnhancedUserType<Long>
{
  private static final int SQL_TYPE = Types.BIGINT;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<Long> returnedClass()
  {
    return Long.class;
  }


  @Override
  public boolean equals( Long value1, Long value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( Long value )
  {
    return value.hashCode();
  }


  @Override
  public Long nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    long value = res.getLong( index );

    if ( res.wasNull() )
      return null;

    return value;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, Long value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setLong( index, value );
    }
  }


  @Override
  public Long deepCopy( Long value )
  {
    return value;
  }


  @Override
  public boolean isMutable()
  {
    return false;
  }


  @Override
  public Serializable disassemble( Long value )
  {
    return value;
  }


  @Override
  public Long assemble( Serializable cached, Object owner )
  {
    return (Long) cached;
  }


  /* Start of EnhancedUserType methods */


  @Override
  public String toSqlLiteral( Long value )
  {
    return Long.toString( value );
  }


  @Override
  public String toString( Long value )
  {
    return Long.toString( value );
  }


  @Override
  public Long fromStringValue( CharSequence charSequence )
      throws HibernateException
  {
    try
    {
      return Long.parseLong( charSequence.toString() );
    }
    catch ( NumberFormatException e )
    {
      throw new HibernateException( "Error converting: " + charSequence + " to Long.", e );
    }
  }

  /* End of EnhancedUserType methods */
}

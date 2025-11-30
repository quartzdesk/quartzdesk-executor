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
 * A user type that stores Java Integer values in SQL INTEGER columns.
 *
 * @version :$
 */
public class IntegerUT
    implements EnhancedUserType<Integer>
{
  private static final int SQL_TYPE = Types.INTEGER;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<Integer> returnedClass()
  {
    return Integer.class;
  }


  @Override
  public boolean equals( Integer value1, Integer value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( Integer value )
  {
    return value.hashCode();
  }


  @Override
  public Integer nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    int value = res.getInt( index );

    if ( res.wasNull() ) return null;

    return value;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, Integer value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setInt( index, value );
    }
  }


  @Override
  public Integer deepCopy( Integer value )
  {
    return value;
  }


  @Override
  public boolean isMutable()
  {
    return false;
  }


  @Override
  public Serializable disassemble( Integer value )
  {
    return value;
  }


  @Override
  public Integer assemble( Serializable cached, Object owner )
  {
    return (Integer) cached;
  }


  /* Start of EnhancedUserType methods */


  @Override
  public String toSqlLiteral( Integer value )
  {
    return Integer.toString( value );
  }


  @Override
  public String toString( Integer value )
  {
    return Integer.toString( value );
  }


  @Override
  public Integer fromStringValue( CharSequence charSequence )
      throws HibernateException
  {
    try
    {
      return Integer.parseInt( charSequence.toString() );
    }
    catch ( NumberFormatException e )
    {
      throw new HibernateException( "Error converting: " + charSequence + " to Integer.", e );
    }

  }

  /* End of EnhancedUserType methods */
}

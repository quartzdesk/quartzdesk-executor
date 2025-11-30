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
 * A user type that stores Java Short values in SQL SMALLINT columns.
 *
 * @version :$
 */
public class ShortUT
    implements EnhancedUserType<Short>
{
  private static final int SQL_TYPE = Types.SMALLINT;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<Short> returnedClass()
  {
    return Short.class;
  }


  @Override
  public boolean equals( Short value1, Short value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( Short value )
  {
    return value.hashCode();
  }


  @Override
  public Short nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    short value = res.getShort( index );

    if ( res.wasNull() )
      return null;

    return value;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, Short value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setShort( index, value );
    }
  }


  @Override
  public Short deepCopy( Short value )
  {
    return value;
  }


  @Override
  public boolean isMutable()
  {
    return false;
  }


  @Override
  public Serializable disassemble( Short value )
  {
    return value;
  }


  @Override
  public Short assemble( Serializable cached, Object owner )
  {
    return (Short) cached;
  }


  /* Start of EnhancedUserType methods */


  @Override
  public String toSqlLiteral( Short value )
  {
    return Short.toString( value );
  }


  @Override
  public String toString( Short value )
  {
    return Short.toString( value );
  }


  @Override
  public Short fromStringValue( CharSequence charSequence )
      throws HibernateException
  {
    try
    {
      return Short.parseShort( charSequence.toString() );
    }
    catch ( NumberFormatException e )
    {
      throw new HibernateException( "Error converting: " + charSequence + " to Short.", e );
    }
  }

  /* End of EnhancedUserType methods */
}

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

/**
 * A user type that stores Java Double values in SQL DOUBLE columns.
 *
 * @version :$
 */
public class DoubleUT
    implements UserType<Double>
{
  private static final int SQL_TYPE = Types.DOUBLE;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<Double> returnedClass()
  {
    return Double.class;
  }


  @Override
  public boolean equals( Double value1, Double value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( Double value )
  {
    return value.hashCode();
  }


  @Override
  public Double nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    double value = res.getDouble( index );

    if ( res.wasNull() )
      return null;

    return value;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, Double value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setDouble( index, value );
    }
  }


  @Override
  public Double deepCopy( Double value )
  {
    return value;
  }


  @Override
  public boolean isMutable()
  {
    return false;
  }


  @Override
  public Serializable disassemble( Double value )
  {
    return value;
  }


  @Override
  public Double assemble( Serializable cached, Object owner )
  {
    return (Double) cached;
  }
}

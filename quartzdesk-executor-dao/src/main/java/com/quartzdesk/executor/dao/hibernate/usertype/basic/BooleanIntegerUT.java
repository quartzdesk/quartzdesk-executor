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
 * A user type that stores Java Boolean values in SQL INTEGER columns.
 *
 * @version :$
 */
public class BooleanIntegerUT
    implements UserType<Boolean>
{
  private static final int SQL_TYPE = Types.INTEGER;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<Boolean> returnedClass()
  {
    return Boolean.class;
  }


  @Override
  public boolean equals( Boolean value1, Boolean value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( Boolean value )
  {
    return value.hashCode();
  }


  @Override
  public Boolean nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    int value = res.getInt( index );

    if ( res.wasNull() )
      return null;

    return switch ( value )
    {
      case 0 -> false;
      case 1 -> true;
      default -> throw new SQLException( "Illegal numeric boolean value: " + value + ". Expected either 0 or 1." );
    };
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, Boolean value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setInt( index, value ? 1 : 0 );
    }
  }


  @Override
  public Boolean deepCopy( Boolean value )
  {
    return value;
  }


  @Override
  public boolean isMutable()
  {
    return false;
  }


  @Override
  public Serializable disassemble( Boolean value )
  {
    return value;
  }


  @Override
  public Boolean assemble( Serializable cached, Object owner )
  {
    return (Boolean) cached;
  }
}

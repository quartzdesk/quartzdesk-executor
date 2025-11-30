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
 * A user type that stores Java String values in SQL VARCHAR columns.
 *
 * @version :$
 */
public class StringUT
    implements UserType<String>
{
  private static final int SQL_TYPE = Types.VARCHAR;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<String> returnedClass()
  {
    return String.class;
  }


  @Override
  public boolean equals( String value1, String value2 )
  {
    return value1 != null && value1.equals( value2 );
  }


  @Override
  public int hashCode( String value )
  {
    return value.hashCode();
  }


  @Override
  public String nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    String value = res.getString( index );

    if ( res.wasNull() )
      return null;

    return value;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, String value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setString( index, value );
    }
  }


  @Override
  public String deepCopy( String value )
  {
    return value;
  }


  @Override
  public boolean isMutable()
  {
    return false;
  }


  @Override
  public Serializable disassemble( String value )
  {
    return value;
  }


  @Override
  public String assemble( Serializable cached, Object owner )
  {
    return (String) cached;
  }
}

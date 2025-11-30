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
import java.util.Arrays;

/**
 * A user type that stores Java byte arrays values in SQL LONGVARBINARY columns.
 *
 * @version :$
 */
public class BinaryUT
    implements UserType<byte[]>
{
  private static final int SQL_TYPE = Types.LONGVARBINARY;


  @Override
  public int getSqlType()
  {
    return SQL_TYPE;
  }


  @Override
  public Class<byte[]> returnedClass()
  {
    return byte[].class;
  }


  @Override
  public boolean equals( byte[] value1, byte[] value2 )
  {
    return value1 == null && value2 == null || Arrays.equals( value1, value2 );
  }


  @Override
  public int hashCode( byte[] value )
  {
    return Arrays.hashCode( value );
  }


  @Override
  public byte[] nullSafeGet( ResultSet res, int index, SharedSessionContractImplementor session, Object owner )
      throws SQLException
  {
    byte[] value = res.getBytes( index );

    if ( res.wasNull() )
      return null;

    return value;
  }


  @Override
  public void nullSafeSet( PreparedStatement pstat, byte[] value, int index, SharedSessionContractImplementor session )
      throws SQLException
  {
    if ( value == null )
    {
      pstat.setNull( index, SQL_TYPE );
    }
    else
    {
      pstat.setBytes( index, value );
    }
  }


  @Override
  public byte[] deepCopy( byte[] value )
  {
    return value == null ? null : Arrays.copyOf( value, value.length );
  }


  @Override
  public boolean isMutable()
  {
    return true;
  }


  @Override
  public Serializable disassemble( byte[] value )
  {
    return deepCopy( value );
  }


  @Override
  public byte[] assemble( Serializable cached, Object owner )
  {
    return deepCopy( (byte[]) cached );
  }
}

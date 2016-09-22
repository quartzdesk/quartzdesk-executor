/*
 * Copyright (c) 2015-2016 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.domain.convert;

import com.quartzdesk.executor.domain.model.common.Version;

/**
 * Converter for {@link Version} instances.
 */
public class VersionConverter
{
  public static final VersionConverter INSTANCE = new VersionConverter();

  /**
   * Private constructor to prevent external instantiation.
   */
  private VersionConverter()
  {
  }

  public Version fromString( String value )
  {
    if ( value == null )
      return null;

    Version version = new Version();

    String[] values = value.split( "[\\.-]" );

    // expected at least 1 component
    if ( values.length < 1 )
      throw new IllegalArgumentException( "Illegal format of " + Version.class.getSimpleName() + " value: " + value );

    try
    {
      if ( values.length >= 1 )
        version.setMajor( Integer.parseInt( values[0] ) );

      if ( values.length >= 2 )
        version.setMinor( Integer.parseInt( values[1] ) );

      if ( values.length >= 3 )
        version.setMaintenance( Integer.parseInt( values[2] ) );

      if ( values.length == 4 )
        version.setQualifier( values[3] );
    }
    catch ( NumberFormatException e )
    {
      throw new IllegalArgumentException( "Illegal format of " + Version.class.getSimpleName() + " value: " + value );
    }

    return version;
  }


  public String toString( Version version )
  {
    if ( version == null )
      return null;

    StringBuilder sb = new StringBuilder()
        .append( version.getMajor() );

    if ( version.getMinor() != null )
      sb.append( '.' ).append( version.getMinor() );

    if ( version.getMaintenance() != null )
      sb.append( '.' ).append( version.getMaintenance() );

    if ( version.getQualifier() != null )
      sb.append( '-' ).append( version.getQualifier() );

    return sb.toString();
  }
}

/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.type;

import com.quartzdesk.executor.common.CommonUtils;

import java.io.Serializable;

/**
 * Represents a version number with the following format:
 * <pre>
 *   &lt;major version&gt;.&lt;minor version&gt;.&lt;maintenance version&gt;[-qualifier]
 * </pre>
 */
public class Version
    implements Comparable<Version>, Serializable
{
  private Integer major = 0;
  private Integer minor = 0;
  private Integer maintenance = 0;
  private String qualifier;

  /**
   * Creates a new {@link Version} instance with all version numbers set to 0.
   */
  public Version()
  {
  }

  /**
   * Creates a new {@link Version} instance with the specified version numbers.
   *
   * @param major       a major version number.
   * @param minor       a minor version number.
   * @param maintenance a maintenance version number.
   */
  public Version( int major, int minor, int maintenance )
  {
    this( major, minor, maintenance, null );
  }

  /**
   * Creates a new {@link Version} instance with the specified version numbers and qualifier.
   *
   * @param major       a major version number.
   * @param minor       a minor version number.
   * @param maintenance a maintenance version number.
   * @param qualifier   an optional qualifier.
   * @throws IllegalArgumentException if either of the specified numerical components is
   * not a positive number.
   */
  public Version( int major, int minor, int maintenance, String qualifier )
  {
    if ( major < 0 )
      throw new IllegalArgumentException( "Major number must be >= 0" );
    this.major = major;

    if ( minor < 0 )
      throw new IllegalArgumentException( "Minor number must be >= 0." );
    this.minor = minor;

    if ( maintenance < 0 )
      throw new IllegalArgumentException( "Maintenance number must be >= 0" );
    this.maintenance = maintenance;

    this.qualifier = qualifier;
  }

  /**
   * Creates a new {@link Version} instance by parsing the string in the following format:
   * &lt;major version&gt;.&lt;minor version&gt;.&lt;maintenance version&gt;[-qualifier]
   *
   * @param versionStr a version string.
   * @return the created {@link Version} instance.
   * @throws IllegalArgumentException if the version string does not conform to the
   * specified version format.
   */
  public static Version parseVersion( String versionStr )
  {
    Version version = new Version();

    String[] values = versionStr.split( "[\\.-]" );

    // expected 3, or 4 components
    if ( values.length != 3 && values.length != 4 )
      throw new IllegalArgumentException(
          "Illegal format of version number: " + versionStr +
              ". Expected version number in the <major>.<minor>.<maintenance>[-qualifier] format." );

    try
    {
      if ( values.length >= 1 )
        version.major = Integer.parseInt( values[0] );

      if ( values.length >= 2 )
        version.minor = Integer.parseInt( values[1] );

      if ( values.length >= 3 )
        version.maintenance = Integer.parseInt( values[2] );

      if ( values.length >= 4 )
        version.qualifier = values[3];
    }
    catch ( NumberFormatException e )
    {
      throw new IllegalArgumentException(
          "Illegal format of version number: " + versionStr +
              ". Expected version number in the <major>.<minor>.<maintenance>[-qualifier] format.", e );
    }

    return version;
  }

  public Integer getMajor()
  {
    return major;
  }

  public void setMajor( Integer major )
  {
    this.major = major;
  }

  public Integer getMinor()
  {
    return minor;
  }

  public void setMinor( Integer minor )
  {
    this.minor = minor;
  }

  public Integer getMaintenance()
  {
    return maintenance;
  }

  public void setMaintenance( Integer maintenance )
  {
    this.maintenance = maintenance;
  }

  public String getQualifier()
  {
    return qualifier;
  }

  public void setQualifier( String qualifier )
  {
    this.qualifier = qualifier;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals( Object obj )
  {
    if ( obj != null && obj instanceof Version )
    {
      Version other = (Version) obj;

      return CommonUtils.safeEquals( major, other.major )
          && CommonUtils.safeEquals( minor, other.minor )
          && CommonUtils.safeEquals( maintenance, other.maintenance )
          && CommonUtils.safeEquals( qualifier, other.qualifier );
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return CommonUtils.safeHashCode( major ) * 31
        + CommonUtils.safeHashCode( minor ) * 13
        + CommonUtils.safeHashCode( maintenance ) * 7
        + CommonUtils.safeHashCode( qualifier );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo( Version o )
  {
    if ( major < o.major )
      return -1;
    if ( major > o.major )
      return 1;

    // majors are equal
    if ( minor < o.minor )
      return -1;
    if ( minor > o.minor )
      return 1;

    // majors and minors are equal
    if ( maintenance < o.maintenance )
      return -1;
    if ( maintenance > o.maintenance )
      return 1;

    return CommonUtils.safeCompare( qualifier, o.qualifier );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder()
        .append( major )
        .append( '.' )
        .append( minor )
        .append( '.' )
        .append( maintenance );

    if ( qualifier != null )
      sb.append( '-' ).append( qualifier );

    return sb.toString();
  }
}

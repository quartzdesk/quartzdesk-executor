/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.type;

import com.quartzdesk.executor.common.DateTimeUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Simple type to represent a timestamp with time zone information. It also
 * exists to avoid using {@link java.sql.Timestamp} in higher level APIs.
 */
public class TimestampWithTZ
    implements Cloneable, Comparable<TimestampWithTZ>, Serializable
{
  private long millis;
  private TimeZone timeZone;

  /**
   * Creates a new {@link TimestampWithTZ} instance initialized with
   * the current timestamp and the default time zone.
   */
  public TimestampWithTZ()
  {
    this( System.currentTimeMillis() );
  }


  /**
   * Creates a new {@link TimestampWithTZ} instance initialized with
   * the specified timestamp and the default time zone.
   *
   * @param millis a timestamp value in milliseconds.
   */
  public TimestampWithTZ( long millis )
  {
    this( millis, TimeZone.getDefault() );
  }


  /**
   * Creates a new {@link TimestampWithTZ} instance initialized with
   * the current timestamp and the specified time zone.
   *
   * @param timeZone a time zone.
   */
  public TimestampWithTZ( TimeZone timeZone )
  {
    this( System.currentTimeMillis(), timeZone );
  }


  /**
   * Creates a new {@link TimestampWithTZ} instance initialized with
   * the specified timestamp and time zone.
   *
   * @param millis   a timestamp value in milliseconds.
   * @param timeZone a time zone.
   */
  public TimestampWithTZ( long millis, TimeZone timeZone )
  {
    this.millis = millis;
    this.timeZone = timeZone;
  }


  /**
   * Returns this timestamp's value in milliseconds.
   *
   * @return this timestamp's value in milliseconds.
   */
  public long getMillis()
  {
    return millis;
  }

  /**
   * Sets this timestamp's value in milliseconds.
   *
   * @param millis this timestamp's value in milliseconds.
   */
  public void setMillis( long millis )
  {
    this.millis = millis;
  }


  /**
   * Returns this timestamp's time zone.
   *
   * @return this timestamp's time zone.
   */
  public TimeZone getTimeZone()
  {
    return timeZone;
  }

  /**
   * Sets this timestamp's time zone.
   *
   * @param timeZone the time zone.
   */
  public void setTimeZone( TimeZone timeZone )
  {
    this.timeZone = timeZone;
  }


  /**
   * Returns the {@link Calendar} representation of this timestamp.
   * The returned calendar is lenient and its first day of week is
   * set to {@link Calendar#MONDAY} and minimal days in first week
   * is set to 4 to support ISO week numbering.
   *
   * @return the {@link Calendar} representation of this timestamp.
   */
  public Calendar toCalendar()
  {
    Calendar cal = Calendar.getInstance( timeZone );
    cal.setLenient( true );
    cal.setTimeInMillis( millis );
    cal.setFirstDayOfWeek( Calendar.MONDAY );  // default is ISO standard
    cal.setMinimalDaysInFirstWeek( 4 );  // default is ISO standard
    return cal;
  }


  /**
   * Returns the {@link Date} representation of this timestamp.
   *
   * @return the {@link Date} representation of this timestamp.
   */
  public Date toDate()
  {
    return toCalendar().getTime();
  }


  /**
   * Returns the hash code of this timestamp.
   *
   * @return the hash code of this timestamp.
   */
  @Override
  public int hashCode()
  {
    // CSOFF: MagicNumber
    return (int) ( millis ^ ( millis >>> 32 ) ) * timeZone.hashCode();
    // CSON: MagicNumber
  }


  /**
   * Returns true if the specified object represents an equal
   * timestamp as this timestamp, false otherwise.
   *
   * @param obj an object.
   * @return true if the specified object represents an equal
   * timestamp as this timestamp, false otherwise.
   */
  @Override
  public boolean equals( Object obj )
  {
    return this == obj || ( obj instanceof TimestampWithTZ &&
        millis == ( (TimestampWithTZ) obj ).millis &&
        timeZone.equals( ( (TimestampWithTZ) obj ).timeZone ) );

  }


  /**
   * Returns a clone of this timestamp.
   *
   * @return a clone of this timestamp.
   */
  @Override
  public TimestampWithTZ clone()
  {
    try
    {
      TimestampWithTZ t = (TimestampWithTZ) super.clone();
      t.timeZone = (TimeZone) timeZone.clone();
      return t;
    }
    catch ( CloneNotSupportedException e )
    {
      throw new IllegalStateException( "Error cloning " + TimestampWithTZ.class.getName(), e );
    }
  }


  /**
   * Compares this timestamp to the specified timestamp and
   * returns 0 if both timestamps represent the same time, -1
   * if this timestamp precedes the specified timestamp, 1 otherwise.
   *
   * @param o a timestamp.
   * @return 0, -1, or 1.
   */
  public int compareTo( TimestampWithTZ o )
  {
    Calendar thisCal = DateTimeUtils.createResetCalendar( timeZone );
    thisCal.setTimeInMillis( millis );

    Calendar otherCal = DateTimeUtils.createResetCalendar( o.timeZone );
    otherCal.setTimeInMillis( o.millis );

    return thisCal.compareTo( otherCal );
  }


  /**
   * Adds the specified number of milliseconds to this timestamp.
   *
   * @param millis the number of milliseconds to add.
   * @return this timestamp.
   */
  public TimestampWithTZ addMilliseconds( int millis )
  {
    this.millis += millis;
    return this;
  }


  /**
   * Adds the specified number of seconds to this timestamp.
   *
   * @param seconds the number of seconds to add.
   * @return this timestamp.
   */
  public TimestampWithTZ addSeconds( int seconds )
  {
    millis += seconds * 1000L;
    return this;
  }


  /**
   * Adds the specified number of minutes to this timestamp.
   *
   * @param minutes the number of minutes to add.
   * @return this timestamp.
   */
  public TimestampWithTZ addMinutes( int minutes )
  {
    return addSeconds( minutes * 60 );
  }


  /**
   * Adds the specified number of hours to this timestamp.
   *
   * @param hours the number of hours to add.
   * @return this timestamp.
   */
  public TimestampWithTZ addHours( int hours )
  {
    return addMinutes( hours * 60 );
  }


  /**
   * Adds the specified number of days to this timestamp.
   *
   * @param days the number of days to add.
   * @return this timestamp.
   */
  public TimestampWithTZ addDays( int days )
  {
    return addHours( days * 24 );
  }


  /**
   * Adds the specified number of months to this timestamp.
   *
   * @param months the number of months to add.
   * @return this timestamp.
   */
  public TimestampWithTZ addMonths( int months )
  {
    Calendar cal = toCalendar();
    cal.add( Calendar.MONTH, months );
    millis = cal.getTimeInMillis();
    return this;
  }


  /**
   * Adds the specified number of years to this timestamp.
   *
   * @param years the number of years to add.
   * @return this timestamp.
   */
  public TimestampWithTZ addYears( int years )
  {
    Calendar cal = toCalendar();
    cal.add( Calendar.YEAR, years );
    millis = cal.getTimeInMillis();
    return this;
  }


  /**
   * Returns true if this timestamp represents a time after the time
   * represented by the specified timestamp, false otherwise.
   *
   * @param t the other timestamp.
   * @return true if this timestamp represents a time after the time
   * represented by the specified timestamp, false otherwise.
   */
  public boolean isAfter( TimestampWithTZ t )
  {
    Calendar thisCal = toCalendar();
    Calendar otherCal = t.toCalendar();
    return thisCal.after( otherCal );
  }


  /**
   * Returns true if this timestamp represents a time before the time
   * represented by the specified timestamp, false otherwise.
   *
   * @param t the other timestamp.
   * @return true if this timestamp represents a time before the time
   * represented by the specified timestamp, false otherwise.
   */
  public boolean isBefore( TimestampWithTZ t )
  {
    Calendar thisCal = toCalendar();
    Calendar otherCal = t.toCalendar();
    return thisCal.before( otherCal );
  }


  /**
   * Returns a string representation of this timestamp
   * in this timestamp's timezone.
   *
   * @return a string representation of this timestamp
   * in this timestamp's timezone.
   */
  @Override
  public String toString()
  {
    return toString( timeZone );
  }


  /**
   * Returns a string representation of this timestamp
   * in the specified time zone.
   *
   * @param timeZone a time zone.
   * @return a string representation of this timestamp
   * in the specified time zone.
   */
  public String toString( TimeZone timeZone )
  {
    return DateTimeUtils.formatTimestamp( millis, timeZone );
  }
}

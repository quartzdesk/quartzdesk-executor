/*
 * Copyright (c) 2015-2017 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common;

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Defines various useful time and date formatting methods.
 */
public final class DateTimeUtils
{
  private static final String TIMESTAMP_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

  /**
   * Format for RFC 1036 date string -- "Sunday, 06-Nov-94 08:49:37 GMT".
   */
  private static final String TIMESTAMP_FORMAT_RFC1036 = "EEEEEEEEE, dd-MMM-yy HH:mm:ss z";

  /**
   * Format for RFC 1123 date string -- "Sun, 06 Nov 1994 08:49:37 GMT".
   */
  private static final String TIMESTAMP_FORMAT_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss z";

  /**
   * Format for ISO 8601 (RFC 3339) date string -- "2012-06-27T12:31:00.003+0000".
   */
  private static final String TIMESTAMP_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";


  private static final String TIME_FORMAT_HH_MM_SS_SSS = "HH:mm:ss.SSS";

  private static final String DATE_FORMAT_DASHED_YYYY_MM_DD = "yyyy-MM-dd";
  private static final String DATE_FORMAT_DOTTED_YYYY_MM_DD = "yyyy.MM.dd";

  private static final String DATE_FORMAT_DASHED_DD_MM_YYYY = "dd-MM-yyyy";
  private static final String DATE_FORMAT_DOTTED_DD_MM_YYYY = "dd.MM.yyyy";

  private static final String WITH_TIME_ZONE = " z";

  private static final String DURATION_MSG_FORMAT = "{0}{1}:{2}:{3}.{4}";
  private static final double[] DURATION_DAY_LIMITS = { 0, 1, 2 };
  private static final String[] DURATION_DAY_CHOICES = { CommonConst.EMPTY_STRING, "1 day ", "{0,number} days " };

  private static final String DURATION_MM_SS_MSG_FORMAT = "{0}.{1}";

  /**
   * Set of all Java time zone IDs available on this JVM. This set is used
   * to speed up the time zone availability checks. Unfortunately the Java
   * {@link TimeZone} class provides no methods to check if a time zone
   * with a given ID is available.
   */
  private static final Set<String> AVAILABLE_TIME_ZONES;

  static
  {
    AVAILABLE_TIME_ZONES = new HashSet<String>();
    AVAILABLE_TIME_ZONES.addAll( Arrays.asList( TimeZone.getAvailableIDs() ) );
  }


  /**
   * Private constructor of a utility class.
   */
  private DateTimeUtils()
  {
  }

  /**
   * Returns a string representation of the specified duration in SS.mm format.
   *
   * @param duration a duration value in milliseconds.
   * @return the formated duration.
   */
  public static String formatDurationSSmm( long duration )
  {
    long dur = duration;

    long seconds = (int) dur / CommonConst.MILLS_IN_SECOND;
    dur -= seconds * CommonConst.MILLS_IN_SECOND;

    int millis = (int) dur;

    Object[] durationArgs = new Object[2];

    // CSOFF: MagicNumber
    durationArgs[0] =
        seconds < 10 ? "0" + seconds : Long.toString( seconds );

    durationArgs[1] =
        millis < 100 ? ( millis < 10 ? "00" + millis : "0" + millis ) : Integer.toString( millis );
    // CSON: MagicNumber

    MessageFormat format = new MessageFormat( DURATION_MM_SS_MSG_FORMAT );

    return format.format( durationArgs );
  }

  /**
   * Returns a string representation of the specified duration in HH:MM:SS.mm format.
   *
   * @param duration a duration value in milliseconds.
   * @return the formated duration.
   */
  public static String formatDuration( long duration )
  {
    long dur = duration;

    long days = (int) dur / CommonConst.MILLS_IN_DAY;
    dur -= days * CommonConst.MILLS_IN_DAY;

    long hours = (int) dur / CommonConst.MILLS_IN_HOUR;
    dur -= hours * CommonConst.MILLS_IN_HOUR;

    long minutes = (int) dur / CommonConst.MILLS_IN_MINUTE;
    dur -= minutes * CommonConst.MILLS_IN_MINUTE;

    long seconds = (int) dur / CommonConst.MILLS_IN_SECOND;
    dur -= seconds * CommonConst.MILLS_IN_SECOND;

    long millis = dur;

    Object[] durationArgs = new Object[5];
    // CSOFF: MagicNumber
    durationArgs[0] = days;

    durationArgs[1] =
        hours < 10 ? "0" + hours : Long.toString( hours );

    durationArgs[2] =
        minutes < 10 ? "0" + minutes : Long.toString( minutes );

    durationArgs[3] =
        seconds < 10 ? "0" + seconds : Long.toString( seconds );

    durationArgs[4] =
        millis < 100 ? ( millis < 10 ? "00" + millis : "0" + millis ) : Long.toString( millis );
    // CSON: MagicNumber

    MessageFormat format = new MessageFormat( DURATION_MSG_FORMAT );

    ChoiceFormat dayForm = new ChoiceFormat( DURATION_DAY_LIMITS, DURATION_DAY_CHOICES );
    format.setFormat( 0, dayForm );

    return format.format( durationArgs );
  }


  /**
   * Returns a string representation of the specified time value
   * in hh:mm:ss.SSS format in the local time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @return the string representation of the specified time.
   */
  public static String formatTime( long millis )
  {
    SimpleDateFormat f = new SimpleDateFormat( TIME_FORMAT_HH_MM_SS_SSS );
    return f.format( new Date( millis ) );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in yyyy-mm-dd hh:mm:ss.SSS format in the local time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestamp( long millis )
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS );
    return f.format( new Date( millis ) );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in yyyy-mm-dd hh:mm:ss.SSS format in the specified time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @param zone   a time zone. If null the local timezone is assumed.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestamp( long millis, TimeZone zone )
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_YYYY_MM_DD_HH_MM_SS_SSS + WITH_TIME_ZONE );
    f.setTimeZone( zone != null ? zone : TimeZone.getDefault() );
    return f.format( new Date( millis ) );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in the RFC1036 format (e.g. &quot;Sunday, 06-Nov-94 08:49:37 GMT&quot;
   * in the US locale) in the local time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @param locale a locale.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestampRfc1036( long millis, Locale locale )
  {
    return formatTimestampRfc1036( millis, locale, null );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in the RFC1036 format (e.g. &quot;Sunday, 06-Nov-94 08:49:37 GMT&quot;
   * in the US locale) in the specified time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @param locale a locale.
   * @param zone   a time zone. If null the local timezone is assumed.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestampRfc1036( long millis, Locale locale, TimeZone zone )
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_RFC1036, locale );
    f.setTimeZone( zone != null ? zone : TimeZone.getDefault() );
    return f.format( new Date( millis ) );
  }


  /**
   * Parses the specified timestamp in the RFC1036 format (e.g. &quot;Sunday, 06-Nov-94 08:49:37 GMT&quot;
   * in the US locale).
   *
   * @param timestamp a timestamp as a string.
   * @param locale    a locale.
   * @return the date representation of the specified timestamp.
   * @throws ParseException if the timestamp cannot be parsed.
   */
  public static Date parseTimestampRfc1036( String timestamp, Locale locale )
      throws ParseException
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_RFC1036, locale );
    f.setTimeZone( TimeZone.getDefault() );
    return f.parse( timestamp );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in the RFC1123 format (e.g. &quot;Sun, 06 Nov 1994 08:49:37 GMT&quot;
   * in the US locale) in the local time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @param locale a locale.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestampRfc1123( long millis, Locale locale )
  {
    return formatTimestampRfc1123( millis, locale, null );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in the RFC1123 format (e.g. &quot;Sun, 06 Nov 1994 08:49:37 GMT&quot;
   * in the US locale) in the specified time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @param locale locale.
   * @param zone   a time zone. If null the local timezone is assumed.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestampRfc1123( long millis, Locale locale, TimeZone zone )
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_RFC1123, locale );
    f.setTimeZone( zone != null ? zone : TimeZone.getDefault() );
    return f.format( new Date( millis ) );
  }


  /**
   * Parses the specified timestamp in the RFC1123 format (e.g. &quot;Sun, 06 Nov 1994 08:49:37 GMT&quot;
   * in the US locale).
   *
   * @param timestamp a timestamp as a string.
   * @param locale    a locale.
   * @return the calendar representation of the specified timestamp.
   * @throws ParseException if the timestamp cannot be parsed.
   */
  public static Date parseTimestampRfc1123( String timestamp, Locale locale )
      throws ParseException
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_RFC1123, locale );
    f.setTimeZone( TimeZone.getDefault() );
    return f.parse( timestamp );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in the ISO 8601 (RFC 3339) format (e.g. &quot;2012-06-27T12:31:00.003+0000&quot;)
   * in the local time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestampIso8601( long millis )
  {
    return formatTimestampIso8601( millis, null );
  }


  /**
   * Returns a string representation of the specified timestamp value
   * in the ISO 8601 (RFC 3339) format (e.g. &quot;2012-06-27T12:31:00.003+0000&quot;)
   * in the specified time zone.
   *
   * @param millis a timestamp in milliseconds.
   * @param zone   a time zone. If null the local timezone is assumed.
   * @return the string representation of the specified timestamp.
   */
  public static String formatTimestampIso8601( long millis, TimeZone zone )
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_ISO8601, Locale.US );
    f.setTimeZone( zone != null ? zone : TimeZone.getDefault() );
    return f.format( new Date( millis ) );
  }


  /**
   * Parses the specified timestamp in the ISO 8601 (RFC 3339) format
   * (e.g. &quot;2012-06-27T12:31:00.003+0000&quot;).
   *
   * @param timestamp a timestamp as a string.
   * @return the calendar representation of the specified timestamp.
   * @throws ParseException if the timestamp cannot be parsed.
   */
  public static Date parseTimestampIso8601( String timestamp )
      throws ParseException
  {
    SimpleDateFormat f = new SimpleDateFormat( TIMESTAMP_FORMAT_ISO8601, Locale.US );
    f.setTimeZone( TimeZone.getDefault() );
    return f.parse( timestamp );
  }


  /**
   * Formats the specified date as dd-MM-yyyy.
   *
   * @param date a date to format.
   * @return the formatted date.
   */
  public static String formatDashedDateDDMMYYYY( Date date )
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DASHED_DD_MM_YYYY );
    f.setTimeZone( TimeZone.getDefault() );
    return f.format( date );
  }


  /**
   * Parses the specified date in dd-MM-yyyy format and returns the Date
   * instance using the system default time zone.
   *
   * @param date a date in dd-MM-yyyy format.
   * @return the Date instance.
   * @throws ParseException if the date cannot be parsed.
   */
  public static Date parseDashedDateDDMMYYYY( String date )
      throws ParseException
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DASHED_DD_MM_YYYY );
    f.setTimeZone( TimeZone.getDefault() );
    return f.parse( date );
  }


  /**
   * Formats the specified date as dd.MM.yyyy.
   *
   * @param date a date to format.
   * @return the formatted date.
   */
  public static String formatDottedDateDDMMYYYY( Date date )
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DOTTED_DD_MM_YYYY );
    f.setTimeZone( TimeZone.getDefault() );
    return f.format( date );
  }


  /**
   * Parses the specified date in dd.MM.yyyy and returns the Date
   * instance using the system default time zone.
   *
   * @param date a date in dd.MM.yyyy format.
   * @return the Date instance.
   * @throws ParseException if the date cannot be parsed.
   */
  public static Date parseDottedDateDDMMYYYY( String date )
      throws ParseException
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DOTTED_DD_MM_YYYY );
    f.setTimeZone( TimeZone.getDefault() );
    return f.parse( date );
  }


  /**
   * Formats the specified date as yyyy-MM-dd.
   *
   * @param date a date to format.
   * @return the formatted date.
   */
  public static String formatDashedDateYYYYMMDD( Date date )
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DASHED_YYYY_MM_DD );
    f.setTimeZone( TimeZone.getDefault() );
    return f.format( date );
  }


  /**
   * Parses the specified date in yyyy-MM-dd format and returns the Date
   * instance using the system default time zone.
   *
   * @param date a date in yyyy-MM-dd format.
   * @return the Date instance.
   * @throws ParseException if the date cannot be parsed.
   */
  public static Date parseDashedDateYYYYMMDD( String date )
      throws ParseException
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DASHED_YYYY_MM_DD );
    f.setTimeZone( TimeZone.getDefault() );
    return f.parse( date );
  }


  /**
   * Formats the specified date as yyyy.MM.dd.
   *
   * @param date a date to format.
   * @return the formatted date.
   */
  public static String formatDottedDateYYYYMMDD( Date date )
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DOTTED_YYYY_MM_DD );
    f.setTimeZone( TimeZone.getDefault() );
    return f.format( date );
  }


  /**
   * Parses the specified date in yyyy.MM.dd format and returns the Date
   * instance using the system default time zone.
   *
   * @param date a date in yyyy.MM.dd format.
   * @return the Date instance.
   * @throws ParseException if the date cannot be parsed.
   */
  public static Date parseDottedDateYYYYMMDD( String date )
      throws ParseException
  {
    SimpleDateFormat f = new SimpleDateFormat( DATE_FORMAT_DOTTED_YYYY_MM_DD );
    f.setTimeZone( TimeZone.getDefault() );
    return f.parse( date );
  }


  /**
   * Formats the specified date using the {@link DateFormat#LONG} format and the specified locale.
   *
   * @param date   a date to format.
   * @param locale a locale.
   * @return the formatted date.
   */
  public static String formatDateLong( Date date, Locale locale )
  {
    return DateFormat.getDateInstance( DateFormat.LONG, locale ).format( date );
  }


  /**
   * Parses the specified date in the {@link DateFormat#LONG} format and locale
   * and returns the Date instance using the system default time zone.
   *
   * @param date   a date in the {@link DateFormat#LONG} format.
   * @param locale a locale.
   * @return the Date instance.
   * @throws ParseException if the date cannot be parsed.
   */
  public static Date parseDateLong( String date, Locale locale )
      throws ParseException
  {
    return DateFormat.getDateInstance( DateFormat.LONG, locale ).parse( date );
  }


  /**
   * Creates a new reset (non-lenient) calendar with the UTC time zone.
   *
   * @return the reset UTC calendar.
   */
  public static Calendar createResetUTCCalendar()
  {
    return createResetCalendar( CommonConst.TIME_ZONE_UTC );
  }


  /**
   * Creates a new reset (non-lenient) calendar with the default time zone.
   *
   * @return the reset calendar for the default time zone.
   */
  public static Calendar createResetCalendar()
  {
    return createResetCalendar( TimeZone.getDefault() );
  }

  /**
   * Creates a new reset (non-lenient) calendar with the given time zone.
   *
   * @param timeZone a time zone.
   * @return the reset calendar.
   */
  public static Calendar createResetCalendar( TimeZone timeZone )
  {
    Calendar cal = new GregorianCalendar();
    cal.setTimeZone( timeZone );
    cal.clear();
    cal.setFirstDayOfWeek( Calendar.MONDAY );  // ISO standard
    cal.setMinimalDaysInFirstWeek( 4 );  // ISO standard
    return cal;
  }


  /**
   * Adds the specified values to the values to the specified Calendar instance and returns
   * the result.
   *
   * @param cal     a Calendar instance to modify.
   * @param years   the number of years to add.
   * @param months  the number of months to add.
   * @param days    the number of days to add.
   * @param hours   the number of hours to add.
   * @param minutes the number of minutes to add.
   * @param seconds the number of seconds to add.
   * @param millis  the number of milliseconds to add.
   * @return the updated Calendar instance.
   */
  // CSOFF: ParameterNumber
  public static Calendar add2Calendar( Calendar cal, int years, int months, int days,
      int hours, int minutes, int seconds, int millis )
  // CSON: ParameterNumber
  {
    if ( years != 0 )
      cal.add( Calendar.YEAR, years );

    if ( months != 0 )
      cal.add( Calendar.MONTH, months );

    if ( days != 0 )
      cal.add( Calendar.DAY_OF_MONTH, days );

    if ( hours != 0 )
      cal.add( Calendar.HOUR_OF_DAY, hours );

    if ( minutes != 0 )
      cal.add( Calendar.MINUTE, minutes );

    if ( seconds != 0 )
      cal.add( Calendar.SECOND, seconds );

    if ( millis != 0 )
      cal.add( Calendar.MILLISECOND, millis );

    return cal;
  }


  /**
   * Converts the specified {@link Calendar} to {@link Date}.
   *
   * @param cal a {@link Calendar} instance, or null.
   * @return the date, or null.
   */
  public static Date calendar2Date( Calendar cal )
  {
    return cal == null ? null : cal.getTime();
  }


  /**
   * Converts the specified {@link Date} to {@link Calendar} using the
   * system default time zone.
   *
   * @param date a date.
   * @return the calendar.
   */
  public static Calendar date2Calendar( Date date )
  {
    return date2Calendar( date, TimeZone.getDefault() );
  }


  /**
   * Converts the specified {@link Date} to {@link Calendar} using the
   * specified time zone.
   *
   * @param date     a date.
   * @param timeZone a time zone.
   * @return the calendar.
   */
  public static Calendar date2Calendar( Date date, TimeZone timeZone )
  {
    if ( date == null )
      return null;

    Calendar cal = new GregorianCalendar( timeZone );
    cal.setTime( date );
    return cal;
  }


  /**
   * Creates a new date as a clone of the specified date and adds the specified number of
   * seconds to it.
   *
   * @param date    a date.
   * @param seconds a number of seconds to add.
   * @return the new date.
   */
  public static Date addSeconds( Date date, int seconds )
  {
    Date newDate = (Date) date.clone();

    Calendar cal = Calendar.getInstance();
    cal.setLenient( true );
    cal.setTime( newDate );
    cal.add( Calendar.SECOND, seconds );

    return cal.getTime();
  }


  /**
   * Returns the number of days between the specified start date and end date.
   * If the end date precedes the start date, then this method returns 0.
   * Inspired by the <a href="http://tripoverit.blogspot.com/2007/07/java-calculate-difference-between-two.html">
   * Java - calculate the difference between two dates</a> article.
   *
   * @param startDate a start date.
   * @param endDate   an end date.
   * @return the number of days between the specified start date and end date.
   */
  public static long diffDays( Calendar startDate, Calendar endDate )
  {
    Calendar sDate = (Calendar) startDate.clone();
    long daysBetween = 0;

    int y1 = sDate.get( Calendar.YEAR );
    int y2 = endDate.get( Calendar.YEAR );
    int m1 = sDate.get( Calendar.MONTH );
    int m2 = endDate.get( Calendar.MONTH );

    //**year optimization**
    while ( ( ( y2 - y1 ) * 12 + ( m2 - m1 ) ) > 12 )
    {
      //move to Jan 01
      if ( sDate.get( Calendar.MONTH ) == Calendar.JANUARY
          && sDate.get( Calendar.DAY_OF_MONTH ) == sDate.getActualMinimum( Calendar.DAY_OF_MONTH ) )
      {

        daysBetween += sDate.getActualMaximum( Calendar.DAY_OF_YEAR );
        sDate.add( Calendar.YEAR, 1 );
      }
      else
      {
        int diff = 1 + sDate.getActualMaximum( Calendar.DAY_OF_YEAR ) - sDate.get( Calendar.DAY_OF_YEAR );
        sDate.add( Calendar.DAY_OF_YEAR, diff );
        daysBetween += diff;
      }
      y1 = sDate.get( Calendar.YEAR );
    }

    //** optimize for month **
    //while the difference is more than a month, add a month to start month
    while ( ( m2 - m1 ) % 12 > 1 )
    {
      daysBetween += sDate.getActualMaximum( Calendar.DAY_OF_MONTH );
      sDate.add( Calendar.MONTH, 1 );
      m1 = sDate.get( Calendar.MONTH );
    }

    // process remainder date
    while ( sDate.before( endDate ) )
    {
      sDate.add( Calendar.DAY_OF_MONTH, 1 );
      daysBetween++;
    }

    return daysBetween;
  }


  /**
   * Trims the specified calendar to the day by resetting the following attributes:
   * <ul>
   * <li>hour of day</li>
   * <li>minutes</li>
   * <li>seconds</li>
   * <li>milliseconds</li>
   * </ul>
   * The passed calendar is not modified by this method.
   *
   * @param calendar a calendar to trim.
   * @return the trimmed calendar.
   */
  public static Calendar trimToDay( Calendar calendar )
  {
    Calendar cloneCal = (Calendar) calendar.clone();
    cloneCal.set( Calendar.HOUR_OF_DAY, 0 );
    cloneCal.set( Calendar.MINUTE, 0 );
    cloneCal.set( Calendar.SECOND, 0 );
    cloneCal.set( Calendar.MILLISECOND, 0 );

    return cloneCal;
  }


  /**
   * Returns true, if the time zone with the specified ID is available on
   * this JVM, false otherwise.
   *
   * @param timeZoneId a Java time zone ID.
   * @return true if available, false otherwise.
   */
  public static boolean isTimeZoneAvailable( String timeZoneId )
  {
    return AVAILABLE_TIME_ZONES.contains( timeZoneId );
  }


  /**
   * Returns the formatted offset of the specified time zone at the current local time.
   *
   * @param timeZone a time zone.
   * @return the formatted offset (e.g. UTC+01:00).
   */
  public static String formatTimeZoneOffset( TimeZone timeZone )
  {
    int offsetSeconds = timeZone.getOffset( System.currentTimeMillis() ) / 1000;

    int offsetHours = Math.abs( offsetSeconds / 3600 );
    int offsetMinutes = ( Math.abs( offsetSeconds ) - ( offsetHours * 3600 ) ) / 60;

    StringBuilder offset = new StringBuilder( "UTC" );
    if ( offsetSeconds >= 0 )
    {
      offset.append( '+' );
    }
    else
    {
      offset.append( '-' );
    }

    if ( offsetHours < 10 )
      offset.append( '0' );

    offset.append( offsetHours );

    offset.append( ':' );

    if ( offsetMinutes < 10 )
      offset.append( '0' );

    offset.append( offsetMinutes );

    return offset.toString();
  }


  /**
   * Returns the formatted representation of the time zone with the specified
   * Java ID. If the time zone ID is not recognized, then the ID is returned.
   *
   * @param timeZoneId a time zone ID.
   * @return the formatted representation of the time zone.
   */
  public static String formatTimeZone( String timeZoneId, Locale locale )
  {
    if ( isTimeZoneAvailable( timeZoneId ) )
    {
      TimeZone timeZone = TimeZone.getTimeZone( timeZoneId );
      return formatTimeZone( timeZone, locale );
    }
    else
    {
      return timeZoneId;
    }
  }


  /**
   * Returns the formatted representation of the specified time zone.
   *
   * @param timeZone a time zone.
   * @return the formatted representation of the time zone.
   */
  public static String formatTimeZone( TimeZone timeZone, Locale locale )
  {
    String displayId = timeZone.getID().replace( '_', ' ' );
    //return '(' + formatTimeZoneOffset( timeZone ) + ") " + displayId + " (" + timeZone.getDisplayName( locale ) + ')';
    return '(' + formatTimeZoneOffset( timeZone ) + ") " + displayId;
  }
}

/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common;

import java.io.File;
import java.util.TimeZone;

/**
 * Various commonly used constants applicable to any Java application.
 */
public interface CommonConst
{
  /**
   * XML preambule.
   */
  String XML_PREAMBULE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

  /**
   * UTF-8 encoding.
   */
  String ENCODING_UTF8 = "UTF-8";

  /**
   * Latin 1 (ISO 8859-1) encoding.
   */
  String ENCODING_ISO_8859_1 = "ISO-8859-1";

  /**
   * Latin 2 (ISO 8859-2) encoding.
   */
  String ENCODING_ISO_8859_2 = "ISO-8859-2";

  /**
   * CP1250 encoding (Windows-1250 encoding that slightly deviates from
   * ISO-8859-2, see http://en.wikipedia.org/wiki/Windows-1250).
   */
  String ENCODING_CP_1250 = "Cp1250";

  /**
   * CP1252 encoding (Windows-1252 encoding that slightly deviates from
   * ISO-8859-1, see http://en.wikipedia.org/wiki/Windows-1252).
   */
  String ENCODING_CP_1252 = "Cp1252";

  /**
   * CP852 encoding.
   */
  String ENCODING_CP_852 = "Cp852";

  /**
   * UTC time-zone.
   */
  TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone( "UTC" );

  /**
   * GMT time-zone.
   */
  TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone( "GMT" );

  /**
   * Platform dependent new line separator.
   */
  String NL = System.getProperty( "line.separator" );

  /**
   * UNIx line separator.
   */
  String NL_UNIX = "\n";

  /**
   * DOS line separator.
   */
  String NL_DOS = "\r\n";

  /**
   * Platform dependent path separator (':' in Unix, ';' in DOS).
   */
  String PATH_SEPARATOR = File.pathSeparator;

  /**
   * Platform dependent file separator ('/' in Unix, '\' in DOS).
   */
  String FILE_SEPARATOR = File.separator;

  /**
   * Not-available shortcut as a String.
   */
  String NOT_AVAIL = "n/a";

  /**
   * Empty string.
   */
  String EMPTY_STRING = "";

  /**
   * Single space.
   */
  String SINGLE_SPACE = " ";

  /**
   * Comma.
   */
  String COMMA = ",";

  /**
   * Single quot.
   */
  String QUOT = "'";

  /**
   * Double-quot.
   */
  String DQUOT = "\"";

  /**
   * Boolean true expressed as an integer value.
   */
  int INT_TRUE = 1;

  /**
   * Boolean false expressed as an integer value.
   */
  int INT_FALSE = 0;

  /**
   * Empty byte array.
   */
  byte[] EMPTY_BYTE_ARRAY = new byte[0];

  /**
   * Empty string array.
   */
  String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * Number of milliseconds in a second.
   */
  long MILLS_IN_SECOND = 1000L;

  /**
   * Number of seconds in a minute.
   */
  int SECONDS_IN_MINUTE = 60;

  /**
   * Number of minutes in an hour.
   */
  int MINUTES_IN_HOUR = 60;

  /**
   * Number of hours in a day.
   */
  int HOURS_IN_DAY = 24;

  /**
   * Number of milliseconds in a minute.
   */
  long MILLS_IN_MINUTE = SECONDS_IN_MINUTE * MILLS_IN_SECOND;


  /**
   * Number of milliseconds in an hour.
   */
  long MILLS_IN_HOUR = MINUTES_IN_HOUR * MILLS_IN_MINUTE;


  /**
   * Number of milliseconds in a day.
   */
  long MILLS_IN_DAY = HOURS_IN_DAY * MILLS_IN_HOUR;

  /**
   * Number of seconds in a day.
   */
  long SECONDS_IN_HOUR = MINUTES_IN_HOUR * SECONDS_IN_MINUTE;

  /**
   * Number of seconds in a day.
   */
  long SECONDS_IN_DAY = HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE;
}

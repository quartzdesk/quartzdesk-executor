/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.text;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Expands macros in the specified string. Macro starts with a 'start character' ($ by default) followed
 * by a left curly brace and ends with a right curly brace.
 *
 * <p>
 * Macros can have either of the following two formats:
 * <dl>
 *  <dt>${MACRO_NAME}</dt>
 *  <dd>Simple macro without a formatting pattern.</dd>
 *  <dt>${MACRO_NAME,PATTERN}</dt>
 *  <dd>Macro with a formatting pattern. Currently only {@link Calendar}, {@link Date} and {@link Number}
 *  macro values support formatting patterns.</dd>
 * </dl>
 * </p>
 */
public final class MacroExpander
{
  private static final char DEFAULT_START_CHAR = '$';

  private static final char PATTERN_SEPARATOR_CHAR = ',';

  /**
   * Default format for {@link Calendar} objects - ISO 8601 (RFC 3339), e.g. "2012-06-27T12:31:00.003+0000".
   */
  private static final String DEFAULT_PATTERN_CALENDAR = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  /**
   * Default format for {@link Date} objects - ISO 8601 (RFC 3339), e.g. "2012-06-27T12:31:00.003+0000".
   */
  private static final String DEFAULT_PATTERN_DATE = "yyyy-MM-dd";

  private char startChar;

  private Locale locale;

  private Map<String, ?> macros;


  /**
   * Creates a new {@link MacroExpander} using the default macro start character and locale.
   *
   * @param macros the macros.
   */
  public MacroExpander( Map<String, ?> macros )
  {
    this( DEFAULT_START_CHAR, Locale.getDefault(), macros );
  }


  /**
   * Creates a new {@link MacroExpander} using the specified macro start character and the default locale.
   *
   * @param startChar a macro start character.
   * @param macros    the macros.
   */
  public MacroExpander( char startChar, Map<String, ?> macros )
  {
    this( startChar, Locale.getDefault(), macros );
  }


  /**
   * Creates a new {@link MacroExpander} using the specified macro start character and locale.
   *
   * @param startChar a macro start character.
   * @param locale    a locale used for formatting macros that support formatting patterns.
   * @param macros    the macros.
   */
  public MacroExpander( char startChar, Locale locale, Map<String, ?> macros )
  {
    if ( Character.isWhitespace( startChar ) )
      throw new IllegalArgumentException( "Macro start character cannot be whitespace." );

    this.startChar = startChar;
    this.locale = locale;
    this.macros = macros;
  }


  /**
   * Returns the macros used by this instance.
   *
   * @return the macros used by this instance.
   */
  public Map<String, ?> getMacros()
  {
    return macros;
  }


  /**
   * Expands macros in the specified value and returns the expanded value.
   *
   * @param value a value with macros to be expanded.
   * @return the expanded value.
   */
  public String expandMacros( String value )
  {
    return expandMacros( value, 0 );
  }


  /**
   * Expands macros in the specified value starting at the specified index
   * and returns the expanded value.
   *
   * @param value      a value with macros to be expanded.
   * @param startIndex an index to start at.
   * @return the expanded value.
   */
  private String expandMacros( String value, int startIndex )
  {
    String val = value;

    if ( val != null )
    {
      int i = val.indexOf( startChar + "{", startIndex );
      if ( i == -1 )
        return val;
      else
      {
        int j = val.indexOf( '}', i );
        if ( j == -1 )
          return val;

        String insideBracesStr = val.substring( i + 2, j );

        String macroName;
        String macroPattern = null;

        // check if the string that is inside curly braces contains an optional format string
        int formatSeparatorAt = insideBracesStr.indexOf( PATTERN_SEPARATOR_CHAR );
        if ( formatSeparatorAt == -1 )
        {
          // curly braces contain only MACRO_NAME
          macroName = insideBracesStr.trim();
        }
        else
        {
          // curly braces contain MACRO_NAME and FORMAT_PATTERN
          macroName = insideBracesStr.substring( 0, formatSeparatorAt ).trim();
          macroPattern = insideBracesStr.substring( formatSeparatorAt + 1 ).trim();
        }

        Object macroValue = macros.get( macroName );

        if ( macroValue == null )
        {
          if ( macros.containsKey( macroName ) )
          {
            // the macro is present in the macros map - i.e. it is a known macro => expand the macro value to ''
            val = val.substring( 0, i ) + val.substring( j + 1 );
            val = expandMacros( val, i );
          }
          else
          {
            // the macro is not present in the macros map - i.e. it is an unknown macro => do not expand the macro
            val = expandMacros( val, j );
          }
        }
        else
        {
          String sMacroValue = format( macroValue, macroPattern );

          val = val.substring( 0, i ) + sMacroValue + val.substring( j + 1 );
          val = expandMacros( val, i + sMacroValue.length() );
        }
      }
    }
    return val;
  }


  /**
   * Returns true if the specified macro value type supports a format pattern, false otherwise.
   *
   * @param macroValue a macro value to be formatted.
   * @return true if the specified macro value type supports a format pattern, false otherwise.
   */
  private boolean isFormatPatternSupported( Object macroValue )
  {
    return macroValue instanceof Calendar
        || macroValue instanceof Date
        || macroValue instanceof Number;
  }


  /**
   * Formats the specified macro value according to the specified (optional) pattern.
   *
   * @param macroValue   a macro value.
   * @param macroPattern an optional formatting pattern.
   * @return the formatted value.
   */
  private String format( Object macroValue, String macroPattern )
  {
    if ( isFormatPatternSupported( macroValue ) )
    {
      if ( macroValue instanceof Calendar )
      {
        if ( StringUtils.isBlank( macroPattern ) )
        {
          // use default formatting pattern for Calendar objects
          macroPattern = DEFAULT_PATTERN_CALENDAR;
        }

        Format format = new SimpleDateFormat( macroPattern, locale );
        return format.format( ( (Calendar) macroValue ).getTime() );
      }
      else if ( macroValue instanceof Date )
      {
        if ( StringUtils.isBlank( macroPattern ) )
        {
          // use default formatting pattern for Date objects
          macroPattern = DEFAULT_PATTERN_DATE;
        }

        Format format = new SimpleDateFormat( macroPattern, locale );
        return format.format( macroValue );
      }
      else if ( macroValue instanceof Number )
      {
        if ( StringUtils.isBlank( macroPattern ) )
        {
          // use default formatting pattern for Number objects
          return macroValue.toString();
        }
        else
        {
          DecimalFormat format = new DecimalFormat( macroPattern );
          return format.format( macroValue );
        }
      }
      else
      {
        throw new IllegalArgumentException( "Formatting not supported for type: " + macroValue.getClass().getName() );
      }
    }
    else
    {
      return macroValue.toString();
    }
  }
}

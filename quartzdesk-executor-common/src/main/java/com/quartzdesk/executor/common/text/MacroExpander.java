/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.text;

import java.util.Map;

/**
 * Expands macros in the specified string. Macro starts with a 'start character' followed
 * by a left curly brace and ends with a right curly brace. The default value of the
 * 'start character' is '$'.
 */
public final class MacroExpander
{
  private static final char DEFALT_START_CHAR = '$';

  private char startChar;


  /**
   * Creates a new {@link MacroExpander} using the default
   * macro start character.
   */
  public MacroExpander()
  {
    this( DEFALT_START_CHAR );
  }


  /**
   * Creates a new {@link MacroExpander} using the specified
   * macro start character.
   *
   * @param startChar a macro start character.
   */
  public MacroExpander( char startChar )
  {
    if ( Character.isWhitespace( startChar ) )
      throw new IllegalArgumentException( "Macro start character cannot be whitespace." );

    this.startChar = startChar;
  }


  /**
   * Expands macros in the specified value and returns the expanded value.
   *
   * @param value a value with macros to be expanded.
   * @param macros macros.
   * @return the expanded value.
   */
  public String expandMacros( String value, Map<String, ?> macros )
  {
    return expandMacros( value, 0, macros );
  }


  /**
   * Expands macros in the specified value starting at the specified index
   * and returns the expanded value.
   *
   * @param value a value with macros to be expanded.
   * @param startIndex an index to start at.
   * @param macros a map with macro name-value pairs.
   * @return the expanded value.
   */
  private String expandMacros( String value, int startIndex, Map<String, ?> macros )
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

        String macroName = val.substring( i + 2, j );
        Object macroValue = macros.get( macroName );
        if ( macroValue != null )
        {
          String sMacroValue = macroValue.toString();
          val = val.substring( 0, i ) + sMacroValue + val.substring( j + 1 );
          val = expandMacros( val, i + sMacroValue.length(), macros );
        }
        else
        {
          val = expandMacros( val, j, macros );
        }
      }
    }
    return val;
  }
}

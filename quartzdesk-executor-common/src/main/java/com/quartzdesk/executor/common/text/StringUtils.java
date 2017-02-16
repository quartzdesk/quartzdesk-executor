/*
 * Copyright (c) 2015-2017 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.text;

import com.quartzdesk.executor.common.CommonConst;

/**
 * Various commonly used string manipulation methods.
 */
public final class StringUtils
{
  /**
   * Private constructor of a utility class.
   */
  private StringUtils()
  {
  }


  /**
   * Returns true if the input string is null, empty or contains only spaces,
   * false otherwise.
   *
   * @param input a string.
   * @return true if the input string is null, empty or contains only spaces,
   * false otherwise.
   */
  public static boolean isBlank( String input )
  {
    return input == null || CommonConst.EMPTY_STRING.equals( input ) || CommonConst.EMPTY_STRING.equals( input.trim() );
  }


  /**
   * Returns true, if the input string is not null, or is not empty and  does not contain
   * only whitespace characters, false otherwise.
   *
   * @param input a string.
   * @return true if the input string is not null, or empty, or contains only
   * spaces, false otherwise.
   */
  public static boolean isNotBlank( String input )
  {
    return !isBlank( input );
  }
}

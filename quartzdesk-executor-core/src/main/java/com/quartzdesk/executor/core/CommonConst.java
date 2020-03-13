/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.core;

import java.io.File;

/**
 * Various commonly used constants applicable to any Java application.
 */
public interface CommonConst
{
  /**
   * Empty string.
   */
  String EMPTY_STRING = "";

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
}

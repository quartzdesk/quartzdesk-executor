/*
 * Copyright (c) 2015-2016 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.debug;

import com.quartzdesk.executor.common.DateTimeUtils;

/**
 * A simple stop-watch implementation. The typical usage sequence is:
 * <ol>
 * <li>Create StopWatch instance</li>
 * <li>Invoke start()</li>
 * <li>Invoke stop()</li>
 * <li>Invoke getElapsedTime(), or getFormattedElapsedTime()</li>
 * <li>Invoke reset()</li>
 * <li>. . .</li>
 * </ol>
 */
public class StopWatch
{
  private long startedAt;
  private long finishedAt;

  /**
   * Creates a stop-watch. The stop-watch is initially
   * in a stopped state and must be started first.
   */
  public StopWatch()
  {
    reset();
  }

  /**
   * Starts this stop-watch.
   *
   * @return this stop-watch.
   */
  public StopWatch start()
  {
    reset();
    startedAt = System.currentTimeMillis();
    return this;
  }

  /**
   * Stops this stop-watch.
   *
   * @return this stop-watch.
   */
  public StopWatch stop()
  {
    if ( startedAt == 0 )
      throw new IllegalStateException( "Not started" );

    finishedAt = System.currentTimeMillis();
    return this;
  }

  /**
   * Returns the elapsed time in milliseconds.
   *
   * @return the elapsed time in milliseconds.
   */
  public long getElapsedTime()
  {
    if ( startedAt == 0 )
      throw new IllegalStateException( "Not started" );

    if ( finishedAt == 0 )
      throw new IllegalStateException( "Not stopped" );

    return finishedAt - startedAt;
  }

  /**
   * Returns the elapsed time as a formatted string.
   *
   * @return the elapsed time as a formatted string.
   */
  public String getFormattedElapsedTime()
  {
    long duration = getElapsedTime();
    return DateTimeUtils.formatDuration( duration );
  }

  /**
   * Resets this stop-watch.
   *
   * @return this stop-watch.
   */
  private StopWatch reset()
  {
    startedAt = 0;
    finishedAt = 0;
    return this;
  }
}

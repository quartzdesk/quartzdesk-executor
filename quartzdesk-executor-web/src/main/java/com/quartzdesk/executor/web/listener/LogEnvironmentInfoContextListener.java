/*
 * Copyright (c) 2015-2025 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.web.listener;

import com.quartzdesk.executor.web.WorkDir;

import com.quartzdesk.executor.common.web.listener.env.AbstractLogEnvironmentInfoContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Prints description of the QuartzDesk application environment into the log
 * upon application startup.
 */
public class LogEnvironmentInfoContextListener
    extends AbstractLogEnvironmentInfoContextListener
{
  private static final Logger log = LoggerFactory.getLogger( LogEnvironmentInfoContextListener.class );

  @Override
  protected String getApplicationName( ServletContext servletContext )
  {
    return "QuartzDesk Executor";
  }

  @Override
  protected Map<String, String> getExtendedInfo( ServletContext servletContext )
  {
    Map<String, String> info = new LinkedHashMap<String, String>();

    try
    {
      WorkDir workDir = new WorkDir( servletContext );
      info.put( "quartzdesk-executor.work.dir", workDir.getRoot().getAbsolutePath() );
    }
    catch ( IOException e )
    {
      // should not happen
    }

    return info;
  }
}

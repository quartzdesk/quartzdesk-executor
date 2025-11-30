/*
 * Copyright (c) 2015-2025 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.web;

import com.quartzdesk.executor.common.text.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

/**
 * Provides access to the QuartzDesk Executor work directory.
 */
public final class WorkDir
{
  private static final Logger log = LoggerFactory.getLogger( WorkDir.class );

  /**
   * Name of the servlet context init parameter pointing to the QuartzDesk Executor work directory.
   */
  public static final String SERVLET_CTX_INIT_PARAM_WORK_DIR = "quartzdesk-executor.work.dir";

  /**
   * Name of the JVM system property pointing to the QuartzDesk Executor work directory.
   */
  public static final String JVM_SYSTEM_PROPERTY_WORK_DIR = "quartzdesk-executor.work.dir";

  private File root;

  /**
   * Private constructor of a utility class.
   */
  public WorkDir( ServletContext servletCtx )
      throws IOException
  {
    String workDirJvm = System.getProperty( JVM_SYSTEM_PROPERTY_WORK_DIR );
    String workDirCtx = servletCtx.getInitParameter( SERVLET_CTX_INIT_PARAM_WORK_DIR );

    // 1. servlet context init parameters - highest priority (overrides JVM system properties)
    if ( StringUtils.isNotBlank( workDirCtx ) )
    {
      File workDir = new File( workDirCtx );
      if ( workDir.isDirectory() )
      {
        root = workDir.getAbsoluteFile();
        return;
      }
      else
      {
        throw new IOException(
            "QuartzDesk Executor work directory: " + workDir +
                " specified in servlet context init parameter does not exist." );
      }
    }

    // 2. JVM system properties - lowest priority
    if ( StringUtils.isNotBlank( workDirJvm ) )
    {
      File workDir = new File( workDirJvm );
      if ( workDir.isDirectory() )
      {
        root = workDir.getAbsoluteFile();
        return;
      }
      else
      {
        throw new IOException(
            "QuartzDesk Executor work directory: " + workDir + " specified in JVM system property does not exist." );
      }
    }

    throw new IOException(
        "QuartzDesk Executor work directory not specified in servlet context init parameter (" +
            SERVLET_CTX_INIT_PARAM_WORK_DIR + "), nor in JVM system property (" + JVM_SYSTEM_PROPERTY_WORK_DIR + ")." );
  }


  /**
   * Returns the file path of the QuartzDesk Executor work directory.
   *
   * @return the file path of the QuartzDesk Executor work directory.
   */
  public File getRoot()
  {
    return root;
  }
}

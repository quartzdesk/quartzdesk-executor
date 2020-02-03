/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.dao;

/**
 * Exception indicating an error in the DAO API.
 */
public class DaoException
    extends RuntimeException
{
  public DaoException( String message )
  {
    super( message );
  }

  public DaoException( String message, Throwable cause )
  {
    super( message, cause );
  }

  public DaoException( Throwable cause )
  {
    super( cause );
  }
}

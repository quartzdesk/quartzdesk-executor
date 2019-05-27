/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.dao;

import com.quartzdesk.executor.common.db.DbUtils;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common base-class for all DAO classes.
 */
public abstract class AbstractDao
    extends HibernateDaoSupport
{
  private static final Logger log = LoggerFactory.getLogger( AbstractDao.class );

  private JdbcTemplate jdbcTemplate;


  /**
   * Set the Hibernate SessionFactory to be used by this DAO.
   * Will automatically create a HibernateTemplate for the given SessionFactory.
   *
   * @see #createHibernateTemplate
   * @see #setHibernateTemplate
   */
  @Required
  public final void setDataSource( DataSource dataSource )
  {
    jdbcTemplate = new JdbcTemplate( dataSource );
  }


  /**
   * Returns the JDBC template for the configured data source.
   *
   * @return the JDBC template.
   */
  public JdbcTemplate getJdbcTemplate()
  {
    return jdbcTemplate;
  }


  /**
   * Initializes the specified lazily loaded Hibernate proxy, or persistent collection.
   *
   * @param proxy a Hibernate proxy, or persistent collection.
   */
  public void initialize( Object proxy )
  {
    getHibernateTemplate().initialize( proxy );
  }


  /**
   * Checks if the specified table exists in the specified schema and returns true if
   * it exists, false otherwise. This method tries to look up the table using both
   * lower-case and upper-case schema and table names because some databases seems to
   * require the names to be in upper case (DB2, Oracle), whereas other databases require
   * the names to be in lower-case.
   *
   * @param session    a Hibernate session.
   * @param schemaName an optional schema name where to look for the table name.
   * @param tableName  a table name.
   * @return true if the table exists, false otherwise.
   */
  public boolean tableExists( Session session, final String schemaName, final String tableName )
  {
    final AtomicBoolean tableExists = new AtomicBoolean( false );

    session.doWork( new Work()
    {
      @Override
      public void execute( Connection connection )
          throws SQLException
      {
        log.debug( "Checking if table '{}' exists.", tableName );

        DatabaseMetaData metaData = connection.getMetaData();

        // 1. attempt - try schema and table name in lower-case (does not work in DB2 and Oracle)
        ResultSet res =
            metaData.getTables( null,
                schemaName == null ? null : schemaName.toLowerCase( Locale.US ),
                tableName.toLowerCase( Locale.US ), new String[] { "TABLE" } );

        tableExists.set( res.next() );
        DbUtils.close( res );

        if ( tableExists.get() )
        {
          log.debug( "Table '{}' exists.", tableName );
        }
        else
        {
          // 2. attempt - try schema and table name in upper-case (required for DB2 and Oracle)
          res = metaData.getTables( null,
              schemaName == null ? null : schemaName.toUpperCase( Locale.US ),
              tableName.toUpperCase( Locale.US ), new String[] { "TABLE" } );

          tableExists.set( res.next() );
          DbUtils.close( res );

          if ( tableExists.get() )
          {
            log.debug( "Table '{}' exists.", tableName );
          }
          else
          {
            log.debug( "Table '{}' does not exist.", tableName );
          }
        }
      }
    } );

    return tableExists.get();
  }
}

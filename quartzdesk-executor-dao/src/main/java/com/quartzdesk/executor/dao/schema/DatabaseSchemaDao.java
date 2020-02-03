/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.dao.schema;

import com.quartzdesk.executor.dao.AbstractDao;
import com.quartzdesk.executor.domain.model.common.Version;
import com.quartzdesk.executor.domain.model.db.SchemaUpdate;

import com.quartzdesk.executor.common.db.DatabaseScriptExecutor;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

/**
 * DAO for the management of database schema initialization and updates.
 */
public class DatabaseSchemaDao
    extends AbstractDao
{
  private static final Logger log = LoggerFactory.getLogger( DatabaseSchemaDao.class );

  /**
   * Returns the {@link SchemaUpdate} instance with the specified ID, null if
   * no such instance exists.
   *
   * @param schemaUpdateId a schema update ID.
   * @return the {@link SchemaUpdate} instance with the specified ID, null if
   * no such instance exists.
   */
  public SchemaUpdate getSchemaUpdate( Long schemaUpdateId )
  {
    Session session = getSessionFactory().getCurrentSession();
    return (SchemaUpdate) session.get( SchemaUpdate.class, schemaUpdateId );
  }


  /**
   * Returns the latest {@link SchemaUpdate} instance, null if
   * the database does not contain the {@code qd_schema_update} table,
   * or if the table is empty.
   *
   * @return the latest {@link SchemaUpdate} instance, null if
   * the database does not contain the {@code qd_schema_update} table,
   * or if the table is empty.
   */
  public SchemaUpdate getLatestSchemaUpdate()
  {
    Session session = getSessionFactory().getCurrentSession();

    if ( tableExists( session, null, "qd_schema_update" ) )
    {
      // table qd_schema_update exists; get the latest schema update record from the qd_schema_update table
      log.debug( "Schema update history table 'qd_schema_update' exists." );

      Query query = session.createQuery( "from SchemaUpdate u order by u.id desc" );
      query.setMaxResults( 1 );  // only the most recent record

      SchemaUpdate schemaUpdate = (SchemaUpdate) query.uniqueResult();

      if ( schemaUpdate == null )
      {
        // pre 1.1.2 QuartzDesk versions did not populate the qd_schema_update table so we initialize it with 0.0.0
        schemaUpdate = new SchemaUpdate()
            .withMajor( 0 )
            .withMinor( 0 )
            .withMaintenance( 0 )
            .withAppliedAt( Calendar.getInstance() );

        insertSchemaUpdate( schemaUpdate );

        return schemaUpdate;
      }
      else
      {
        return schemaUpdate;
      }
    }
    else
    {
      log.debug( "Schema update history table 'qd_schema_update' does not exist." );
      return null;
    }
  }


  /**
   * Inserts the specified {@link SchemaUpdate} instance into the database.
   *
   * @param schemaUpdate a {@link SchemaUpdate} instance.
   * @return the ID of the inserted {@link SchemaUpdate} instance.
   */
  public Long insertSchemaUpdate( SchemaUpdate schemaUpdate )
  {
    Session session = getSessionFactory().getCurrentSession();
    return (Long) session.save( schemaUpdate );
  }


  /**
   * Initializes, or upgrades the current QuartzDesk database schema by
   * executing the specified list of SQL scripts.
   *
   * @param scriptUrls    the list of SQL scripts to execute.
   * @param schemaVersion the version of the schema after the specified SQL scripts have been applied.
   */
  public void initializeOrUpgradeSchema( final List<URL> scriptUrls, final Version schemaVersion )
  {
    Session session = getSessionFactory().getCurrentSession();
    session.doWork( new Work()
    {
      @Override
      public void execute( Connection connection )
          throws SQLException
      {
        DatabaseScriptExecutor scriptExecutor = new DatabaseScriptExecutor();
        scriptExecutor.addScriptUrls( scriptUrls );

        scriptExecutor.executeScripts( connection );

        SchemaUpdate schemaUpdate = new SchemaUpdate()
            .withMajor( schemaVersion.getMajor() )
            .withMinor( schemaVersion.getMinor() )
            .withMaintenance( schemaVersion.getMaintenance() )
            .withAppliedAt( Calendar.getInstance() );

        insertSchemaUpdate( schemaUpdate );
      }
    } );
  }
}

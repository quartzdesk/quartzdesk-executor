 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.dao.schema;

 import com.quartzdesk.executor.common.db.DatabaseScriptExecutor;
 import com.quartzdesk.executor.common.db.DbUtils;
 import com.quartzdesk.executor.dao.AbstractDao;
 import com.quartzdesk.executor.domain.model.common.Version;
 import com.quartzdesk.executor.domain.model.db.SchemaUpdate;

 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import java.net.URL;
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
    * SQL query to return the list of schema updates sorted by their IDs in the descending order.
    */
   private static final String SQL_SELECT_SCHEMA_UPDATES =
       "SELECT * FROM qd_schema_update u ORDER BY u.schema_update_id DESC";


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
     return session.get( SchemaUpdate.class, schemaUpdateId );
   }


   /**
    * Returns the list of {@link SchemaUpdate} instances representing all schema updates. The list is sorted in
    * descending order by the schema update ID. I.e. the most recent update is first.
    *
    * @return the list of {@link SchemaUpdate} instances representing all schema updates.
    */
   public List<SchemaUpdate> getSchemaUpdates()
   {
     // WE NEED TO USE AN SQL QUERY RATHER THAN AN HQL QUERY BECAUSE SOME OF THE TABLE COLUMNS (LICENSE_SN AND
     // LICENSE_TYPE) MAY NOT EXIST (PRIOR TO 3.0.0) AND THAT WOULD TRIGGER AN ERROR IN HIBERNATE

     return getJdbcTemplate().query( SQL_SELECT_SCHEMA_UPDATES,
         ( rs, rowNum ) -> {
           SchemaUpdate schemaUpdate = new SchemaUpdate()
               .withId( rs.getLong( "schema_update_id" ) )
               .withMajor( rs.getInt( "major" ) )
               .withMinor( rs.getInt( "minor" ) )
               .withMaintenance( rs.getInt( "maintenance" ) )
               .withAppliedAt( DbUtils.getTimestamp( rs, "applied_at" ) );

           return schemaUpdate;
         } );
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

       List<SchemaUpdate> schemaUpdates = getSchemaUpdates();

       // null if no records found, otherwise the first record (the most recent record)
       return schemaUpdates.isEmpty() ? null : schemaUpdates.get( 0 );
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
     session.doWork( connection -> {
       DatabaseScriptExecutor scriptExecutor = new DatabaseScriptExecutor();
       scriptExecutor.addScriptUrls( scriptUrls );

       scriptExecutor.executeScripts( connection );

       SchemaUpdate schemaUpdate = new SchemaUpdate()
           .withMajor( schemaVersion.getMajor() )
           .withMinor( schemaVersion.getMinor() )
           .withMaintenance( schemaVersion.getMaintenance() )
           .withAppliedAt( Calendar.getInstance() );

       insertSchemaUpdate( schemaUpdate );
     } );
   }
 }

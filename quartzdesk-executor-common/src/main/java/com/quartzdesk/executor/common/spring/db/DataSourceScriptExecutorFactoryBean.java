 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.common.spring.db;

 import com.quartzdesk.executor.common.db.DatabaseScriptExecutor;
 import com.quartzdesk.executor.common.db.DbUtils;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.config.AbstractFactoryBean;
 import org.springframework.core.io.Resource;
 import org.springframework.lang.NonNull;

 import javax.sql.DataSource;
 import java.io.IOException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.SQLException;

 /**
  * Spring factory bean that executes the specified set of SQL scripts on the
  * specified data source.
  */
 public class DataSourceScriptExecutorFactoryBean
     extends AbstractFactoryBean<DataSource>
 {
   private static final Logger log = LoggerFactory.getLogger( DataSourceScriptExecutorFactoryBean.class );

   private DataSource dataSource;

   private Resource[] scripts;


   public DataSource getDataSource()
   {
     return dataSource;
   }


   public void setDataSource( @NonNull DataSource dataSource )
   {
     this.dataSource = dataSource;
   }


   public Resource[] getScripts()
   {
     return scripts;
   }


   public void setScripts( @NonNull Resource[] scripts )
   {
     this.scripts = scripts;
   }


   @Override
   public Class<DataSource> getObjectType()
   {
     return DataSource.class;
   }


   @Override
   protected DataSource createInstance()
       throws Exception
   {
     if ( scripts.length > 0 )
     {
       Connection con = dataSource.getConnection();
       try
       {
         applyScripts( con );
       }
       finally
       {
         DbUtils.close( con );
       }
     }
     return dataSource;
   }


   /**
    * Applies the SQL init scripts specified in the {@link #scripts} to the database.
    *
    * @param con a JDBC connection.
    */
   protected void applyScripts( Connection con )
   {
     for ( Resource initScript : scripts )
     {
       applyInitScript( con, initScript );
     }
   }


   /**
    * Applies the specified SQL init script to the database.
    *
    * @param con        a JDBC connection.
    * @param initScript the SQL init script to apply.
    */
   private void applyInitScript( Connection con, Resource initScript )
   {
     URL initScriptUrl;
     try
     {
       initScriptUrl = initScript.getURL();
     }
     catch ( IOException e )
     {
       throw new RuntimeException( "Error obtaining URL of SQL script resource: " + initScript );
     }

     boolean autoCommit = true;
     try
     {
       autoCommit = con.getAutoCommit();

       DatabaseScriptExecutor scriptExecutor = new DatabaseScriptExecutor();
       scriptExecutor.addScriptUrl( initScriptUrl );
       scriptExecutor.executeScripts( con );

       // if auto-commit disabled, we need to commit the changes
       if ( !autoCommit )
         con.commit();

       log.info( "Successfully applied SQL script: {} to database.", initScriptUrl );
     }
     catch ( SQLException e )
     {
       try
       {
         if ( !autoCommit )
           con.rollback();
       }
       catch ( SQLException se )
       {
         log.warn( "Error rolling-back connection: " + con, se );
       }

       throw new RuntimeException( "Error applying SQL script: " + initScriptUrl, e );
     }
   }
 }

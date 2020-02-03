/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.dao.schema;

import com.quartzdesk.executor.dao.DaoException;
import com.quartzdesk.executor.domain.convert.VersionComparator;
import com.quartzdesk.executor.domain.convert.VersionConverter;
import com.quartzdesk.executor.domain.model.common.Version;
import com.quartzdesk.executor.domain.model.db.SchemaUpdate;

import com.quartzdesk.executor.common.CommonConst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages QuartzDesk database schema by applying the database initialization
 * and upgrade scripts.
 */
public class DatabaseSchemaManager
{
  private static final Logger log = LoggerFactory.getLogger( DatabaseSchemaManager.class );

  private static final Pattern UPGRADE_SCRIPT_PATTERN = Pattern.compile( "/(\\d+_\\d+_\\d+)/.+\\.sql$" );

  private PlatformTransactionManager transactionManager;

  private Version productVersion;

  private DatabaseSchemaDao databaseSchemaDao;
  private String initScriptsRoot;
  private String upgradeScriptsRoot;

  @Required
  public void setTransactionManager( PlatformTransactionManager transactionManager )
  {
    this.transactionManager = transactionManager;
  }

  @Required
  public void setProductVersion( Version productVersion )
  {
    this.productVersion = productVersion;
  }

  @Required
  public void setDatabaseSchemaDao( DatabaseSchemaDao databaseSchemaDao )
  {
    this.databaseSchemaDao = databaseSchemaDao;
  }

  @Required
  public void setInitScriptsRoot( String initScriptsRoot )
  {
    this.initScriptsRoot = initScriptsRoot;
  }

  @Required
  public void setUpgradeScriptsRoot( String upgradeScriptsRoot )
  {
    this.upgradeScriptsRoot = upgradeScriptsRoot;
  }


  /**
   * Attempts to initialize, or upgrade the QuartzDesk database schema to the
   * current application schema version.
   */
  @PostConstruct
  protected void maybeInitializeOrUpgradeDatabaseSchema()
  {
    final Version desiredSchemaVersion = new Version()
        .withMajor( productVersion.getMajor() )
        .withMinor( productVersion.getMinor() )
        .withMaintenance( productVersion.getMaintenance() );

    // we must enclose the logic in a transaction so that the Hibernate session is available
    // in the invoked DatabaseSchemaDao
    TransactionTemplate txTemplate = new TransactionTemplate( transactionManager );
    txTemplate.execute( new TransactionCallbackWithoutResult()
    {
      @Override
      protected void doInTransactionWithoutResult( TransactionStatus status )
      {
        SchemaUpdate latestSchemaUpdate = databaseSchemaDao.getLatestSchemaUpdate();

        if ( latestSchemaUpdate == null )
        {
          /*
           * Schema is empty, apply DB initialization scripts.
           */
          List<URL> scriptUrls = getInitScriptUrls();

          log.info( "Initializing empty database schema to {} by applying SQL scripts: {}{}",
              new Object[] {
                  VersionConverter.INSTANCE.toString( desiredSchemaVersion ),
                  CommonConst.NL,
                  dumpScriptList( scriptUrls )
              } );

          databaseSchemaDao.initializeOrUpgradeSchema( scriptUrls, desiredSchemaVersion );
        }
        else
        {
          Version currentSchemaVersion = new Version()
              .withMajor( latestSchemaUpdate.getMajor() )
              .withMinor( latestSchemaUpdate.getMinor() )
              .withMaintenance( latestSchemaUpdate.getMaintenance() );

          log.info( "Detected database schema version: {}",
              VersionConverter.INSTANCE.toString( currentSchemaVersion ) );

          /*
           * Check the current schema version is not > then the desired schema
           * version. Downgrades are not supported.
           */
          checkSchemaVersion( currentSchemaVersion, desiredSchemaVersion );

          if ( currentSchemaVersion.equals( desiredSchemaVersion ) )
          {
            log.info( "Database schema is up-to-date." );
          }
          else
          {
            List<URL> scriptUrls = getUpgradeScriptUrls( currentSchemaVersion, desiredSchemaVersion );

            if ( scriptUrls.isEmpty() )
            {
              log.info(
                  "Formally upgrading database schema {} -> {} because no SQL upgrade scripts are available for {} -> {}",
                  new Object[] {
                      VersionConverter.INSTANCE.toString( currentSchemaVersion ),
                      VersionConverter.INSTANCE.toString( desiredSchemaVersion ),
                      VersionConverter.INSTANCE.toString( currentSchemaVersion ),
                      VersionConverter.INSTANCE.toString( desiredSchemaVersion )
                  } );

              // formal upgrade of the QuartzDesk schema version to the current QuartzDesk version
              SchemaUpdate schemaUpdate = new SchemaUpdate()
                  .withMajor( desiredSchemaVersion.getMajor() )
                  .withMinor( desiredSchemaVersion.getMinor() )
                  .withMaintenance( desiredSchemaVersion.getMaintenance() )
                  .withAppliedAt( Calendar.getInstance() );

              databaseSchemaDao.insertSchemaUpdate( schemaUpdate );
            }
            else
            {
              log.info( "Upgrading database schema {} -> {} by applying SQL upgrade scripts: {}{}",
                  new Object[] {
                      VersionConverter.INSTANCE.toString( currentSchemaVersion ),
                      VersionConverter.INSTANCE.toString( desiredSchemaVersion ),
                      CommonConst.NL,
                      dumpScriptList( scriptUrls )} );

              // applies scripts and inserts a new schema update record
              databaseSchemaDao.initializeOrUpgradeSchema( scriptUrls, desiredSchemaVersion );
            }
          }
        }
      }
    } );
  }


  /**
   * Checks the current database schema is upgradeable to the desired version.
   *
   * @param currentSchemaVersion the current database schema version.
   * @param desiredSchemaVersion the desired database schema version.
   */
  private void checkSchemaVersion( Version currentSchemaVersion, Version desiredSchemaVersion )
  {
    // check that the current schema version <= desired schema version
    if ( VersionComparator.INSTANCE.compare( currentSchemaVersion, desiredSchemaVersion ) <= 0 )
    {
      // ok
    }
    else
    {
      // the current database version is higher then the agent version (someone must have upgraded
      // the database without upgrading this agent)
      throw new DaoException(
          "Detected QuartzDesk schema version (" +
              VersionConverter.INSTANCE.toString( currentSchemaVersion ) +
              ") that is higher then the maximum expected schema version (" +
              VersionConverter.INSTANCE.toString( desiredSchemaVersion ) +
              "). This happens if you are running multiple versions of the QuartzDesk application that share the same database." );
    }
  }


  /**
   * Returns the list of QuartzDesk database schema SQL init scripts.
   *
   * @return the list of SQL init scripts.
   */
  public List<URL> getInitScriptUrls()
  {
    try
    {
      // find all .sql scripts
      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] scriptResources = resolver.getResources( initScriptsRoot + "/*.sql" );

      List<URL> scriptUrls = new ArrayList<URL>();
      for ( Resource scriptResource : scriptResources )
      {
        scriptUrls.add( scriptResource.getURL() );
      }

      // sort the scripts by their name
      Collections.sort( scriptUrls, new Comparator<URL>()
      {
        @Override
        public int compare( URL o1, URL o2 )
        {
          return o1.getPath().compareTo( o2.getPath() );
        }
      } );

      return scriptUrls;
    }
    catch ( IOException e )
    {
      throw new DaoException( "Error getting database schema SQL init script URLs.", e );
    }
  }


  /**
   * Returns the list of QuartzDesk database schema SQL upgrade scripts to be
   * applied to the current schema version to upgrade it to the desired schema version.
   *
   * @param currentSchemaVersion the current database schema version.
   * @param desiredSchemaVersion the desired database schema version.
   * @return the list of SQL upgrade scripts.
   */
  private List<URL> getUpgradeScriptUrls( Version currentSchemaVersion, Version desiredSchemaVersion )
  {
    List<URL> result = new ArrayList<URL>();
    try
    {
      // sort the directories by version number
      Set<Version> dirVersions = new TreeSet<Version>( VersionComparator.INSTANCE );

      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] scriptResources = resolver.getResources( upgradeScriptsRoot + "/*/*.sql" );

      for ( Resource scriptResource : scriptResources )
      {
        Matcher matcher = UPGRADE_SCRIPT_PATTERN.matcher( scriptResource.getURL().getPath() );
        if ( matcher.find() )
        {
          String dirVersionStr = matcher.group( 1 );
          String[] dirVersionParts = dirVersionStr.split( "_" );  // 1_0_0

          Version dirVersion = new Version()
              .withMajor( Integer.parseInt( dirVersionParts[0] ) )
              .withMinor( Integer.parseInt( dirVersionParts[1] ) )
              .withMaintenance( Integer.parseInt( dirVersionParts[2] ) );

          dirVersions.add( dirVersion );
        }
      }

      for ( Version dirVersion : dirVersions )
      {
        // currentSchemaVersion < dirVersion <= desiredSchemaVersion
        if ( VersionComparator.INSTANCE.compare( dirVersion, currentSchemaVersion ) > 0
            && VersionComparator.INSTANCE.compare( dirVersion, desiredSchemaVersion ) <= 0 )
        {
          String dirVersionStr =
              dirVersion.getMajor().toString() + '_' + dirVersion.getMinor() + '_' + dirVersion.getMaintenance();

          scriptResources = resolver.getResources( upgradeScriptsRoot + '/' + dirVersionStr + "/*.sql" );
          List<URL> scriptUrls = new ArrayList<URL>();

          for ( Resource scriptResource : scriptResources )
          {
            scriptUrls.add( scriptResource.getURL() );
          }

          // sort the scripts inside the dir by their name
          Collections.sort( scriptUrls, new Comparator<URL>()
          {
            @Override
            public int compare( URL o1, URL o2 )
            {
              return o1.getPath().compareTo( o2.getPath() );
            }
          } );

          result.addAll( scriptUrls );
        }
      }
    }
    catch ( IOException e )
    {
      throw new DaoException( "Error getting database schema SQL upgrade script URLs.", e );
    }

    return result;
  }


  private String dumpScriptList( List<URL> scriptUrls )
  {
    StringBuilder sb = new StringBuilder();
    for ( Iterator<URL> i = scriptUrls.iterator(); i.hasNext(); )
    {
      sb.append( i.next() );
      if ( i.hasNext() )
        sb.append( CommonConst.NL );
    }

    return sb.toString();
  }
}

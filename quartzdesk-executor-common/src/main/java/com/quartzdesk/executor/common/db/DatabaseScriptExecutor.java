/*
 * Copyright (c) 2015-2016 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.db;

import com.quartzdesk.executor.common.CommonConst;
import com.quartzdesk.executor.common.debug.StopWatch;
import com.quartzdesk.executor.common.io.IOUtils;
import com.quartzdesk.executor.common.text.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Populates a database from SQL scripts defined in external resources.
 *
 * <p>Call {@link #addScriptUrl(URL)} to add a SQL script location.
 * Call {@link #setSqlScriptEncoding(String)} to set the encoding for all added scripts.
 *
 * This class has been inspired by the {@code org.springframework.jdbc.datasource.init.ResourceDatabasePopulator}
 * from the Spring Framework.
 */
public class DatabaseScriptExecutor
{
  private static final Logger log = LoggerFactory.getLogger( DatabaseScriptExecutor.class );

  private static final String DEFAULT_COMMENT_PREFIX = "--";

  private static final String DEFAULT_STATEMENT_SEPARATOR = ";";

  /**
   * Special "start statement" comment that allows us to escape an SQL statement containing
   * statement separators (e.g. ;) inside. For example, PostgreSQL function declarations.
   */
  private static final String STATEMENT_SEPARATOR_START = "[statement]";

  /**
   * Special "end statement" comment that allows us to escape an SQL statement containing
   * statement separators (e.g. ;) inside. For example, PostgreSQL function declarations.
   */
  private static final String STATEMENT_SEPARATOR_END = "[/statement]";

  private List<URL> scriptUrls = new ArrayList<URL>();

  private String sqlScriptEncoding = CommonConst.ENCODING_UTF8;

  private String separator;

  private String commentPrefix = DEFAULT_COMMENT_PREFIX;

  private boolean continueOnError;

  private boolean ignoreFailedDrops;

  private boolean commitAfterScript;


  /**
   * Adds the specified SQL script URL to the list of scripts to execute.
   *
   * @param scriptUrl the path to a SQL script
   */
  public void addScriptUrl( URL scriptUrl )
  {
    scriptUrls.add( scriptUrl );
  }


  /**
   * Adds the specified list of SQL script URLs to the list of scripts to execute.
   *
   * @param scriptUrls the SQL scripts to execute.
   */
  public void addScriptUrls( Collection<URL> scriptUrls )
  {
    this.scriptUrls.addAll( scriptUrls );
  }


  /**
   * Specify the encoding for SQL scripts. The default encoding is UTF-8.
   *
   * @see #addScriptUrl(URL)
   */
  public void setSqlScriptEncoding( String sqlScriptEncoding )
  {
    this.sqlScriptEncoding = sqlScriptEncoding;
  }


  /**
   * Specify the statement separator, if a custom one. Default is ";".
   */
  public void setSeparator( String separator )
  {
    this.separator = separator;
  }


  /**
   * Set the line prefix that identifies comments in the SQL script.
   * Default is "--".
   */
  public void setCommentPrefix( String commentPrefix )
  {
    this.commentPrefix = commentPrefix;
  }


  /**
   * Flag to indicate that all failures in SQL should be logged but not cause a failure.
   * Defaults to false.
   *
   * @param continueOnError the flag value.
   */
  public void setContinueOnError( boolean continueOnError )
  {
    this.continueOnError = continueOnError;
  }


  /**
   * Flag to indicate that a failed SQL {@code DROP} statement can be ignored.
   * <p>This is useful for non-embedded databases whose SQL dialect does not support an
   * {@code IF EXISTS} clause in a {@code DROP}. The default is false so that if the
   * populator runs accidentally, it will fail fast when the script starts with a {@code DROP}.
   *
   * @param ignoreFailedDrops the flag value.
   */
  public void setIgnoreFailedDrops( boolean ignoreFailedDrops )
  {
    this.ignoreFailedDrops = ignoreFailedDrops;
  }


  /**
   * Flag to indicate that a commit should be issued after each of the executed SQL script.
   * If set to false (default), then no commit is performed once an SQL script has been
   * executed. If set to true, then after each executed SQL script a commit is issued.
   *
   * @param commitAfterScript the flag value.
   */
  public void setCommitAfterScript( boolean commitAfterScript )
  {
    this.commitAfterScript = commitAfterScript;
  }


  /**
   * Executes the configured SQL scripts using the specified JDBC connection.
   *
   * @param connection a JDBC connection.
   * @throws SQLException if an SQL error occurs.
   */
  public void executeScripts( Connection connection )
      throws SQLException
  {
    for ( URL scriptUrl : scriptUrls )
    {
      executeScript( connection, scriptUrl );
    }
  }


  /**
   * Execute the given SQL script.
   * <p>There should be one statement per line. Any {@link #setSeparator(String) statement separators}
   * will be removed.
   * <p><b>Do not use this method to execute DDL if you expect rollback.</b>
   *
   * @param connection the JDBC Connection with which to perform JDBC operations.
   * @param scriptUrl the URL of the SQL script to execute.
   */
  private void executeScript( Connection connection, URL scriptUrl )
      throws SQLException
  {
    log.info( "Executing SQL script: {}", scriptUrl );

    StopWatch sw = new StopWatch().start();

    List<String> statements = new LinkedList<String>();
    String script;
    try
    {
      script = readScript( scriptUrl );
    }
    catch ( IOException ex )
    {
      throw new SQLException( "Error reading SQL script: " + scriptUrl, ex );
    }
    String delimiter = separator;
    if ( delimiter == null )
    {
      delimiter = DEFAULT_STATEMENT_SEPARATOR;
      if ( !containsSqlScriptDelimiters( script, delimiter ) )
      {
        delimiter = "\n";
      }
    }

    splitSqlScript( script, delimiter, commentPrefix, statements );

    Statement stat = null;
    try
    {
      stat = connection.createStatement();

      int statNumber = 0;
      for ( String statStr : statements )
      {
        statNumber++;
        try
        {
          stat.execute( statStr );
          int updateCount = stat.getUpdateCount();  // rows affected
          log.debug( "Update count: {} returned for SQL statement: {}", updateCount, statStr );
        }
        catch ( SQLException ex )
        {
          boolean dropStatement = statStr.trim().toUpperCase().startsWith( "drop" );
          if ( continueOnError || ( dropStatement && ignoreFailedDrops ) )
          {
            log.debug(
                "Failed to execute SQL statement #" + statNumber + " of SQL script " + scriptUrl + ": " +
                    statStr, ex );
          }
          else
          {
            throw new SQLException(
                "Failed to execute SQL statement #" + statNumber + " of SQL script " + scriptUrl + ": " +
                    statStr, ex );
          }
        }
      }
    }
    finally
    {
      if ( stat != null )
      {
        DbUtils.close( stat );
      }

      if ( commitAfterScript )
      {
        connection.commit();
      }
    }

    sw.stop();

    log.info( "Finished executing SQL script {}. Time taken: {}", scriptUrl, sw.getFormattedElapsedTime() );
  }


  /**
   * Read a script from the given resource and build a String containing the lines.
   *
   * @param scriptUrl the SQL script URL to be read from.
   * @return {@code String} containing the script lines.
   * @throws IOException in case of I/O errors.
   */
  private String readScript( URL scriptUrl )
      throws IOException
  {
    String statementSeparatorStart = commentPrefix + ' ' + STATEMENT_SEPARATOR_START;
    String statementSeparatorEnd = commentPrefix + ' ' + STATEMENT_SEPARATOR_END;

    LineNumberReader lnr = new LineNumberReader( new InputStreamReader( scriptUrl.openStream(), sqlScriptEncoding ) );
    try
    {
      String currentStatement = lnr.readLine();
      StringBuilder scriptBuilder = new StringBuilder();
      while ( currentStatement != null )
      {
        if ( StringUtils.isNotBlank( currentStatement ) )
        {
          if ( commentPrefix != null && !currentStatement.startsWith( commentPrefix ) )
          {
            if ( scriptBuilder.length() > 0 )
            {
              scriptBuilder.append( '\n' );
            }
            scriptBuilder.append( currentStatement );
          }
          if ( commentPrefix != null && ( currentStatement.startsWith( statementSeparatorStart ) ||
              currentStatement.startsWith( statementSeparatorEnd ) ) )
          {
            if ( scriptBuilder.length() > 0 )
            {
              scriptBuilder.append( '\n' );
            }
            scriptBuilder.append( currentStatement );
          }
        }

        currentStatement = lnr.readLine();
      }
      maybeAddSeparatorToScript( scriptBuilder );
      return scriptBuilder.toString();
    }
    finally
    {
      IOUtils.close( lnr );
    }
  }


  private void maybeAddSeparatorToScript( StringBuilder scriptBuilder )
  {
    if ( separator == null )
    {
      return;
    }
    String trimmed = separator.trim();
    if ( trimmed.length() == separator.length() )
    {
      return;
    }
    // separator ends in whitespace, so we might want to see if the script is trying
    // to end the same way
    if ( scriptBuilder.lastIndexOf( trimmed ) == scriptBuilder.length() - trimmed.length() )
    {
      scriptBuilder.append( separator.substring( trimmed.length() ) );
    }
  }


  /**
   * Does the provided SQL script contain the specified delimiter?
   *
   * @param script the SQL script
   * @param delim character delimiting each statement - typically a ';' character
   */
  private boolean containsSqlScriptDelimiters( String script, String delim )
  {
    boolean inLiteral = false;
    char[] content = script.toCharArray();
    for ( int i = 0; i < script.length(); i++ )
    {
      if ( content[i] == '\'' )
      {
        inLiteral = !inLiteral;
      }
      if ( !inLiteral && script.startsWith( delim, i ) )
      {
        return true;
      }
    }
    return false;
  }


  /**
   * Split an SQL script into separate statements delimited by the provided delimiter
   * string. Each individual statement will be added to the provided {@code List}.
   * <p>Within a statement, the provided {@code commentPrefix} will be honored;
   * any text beginning with the comment prefix and extending to the end of the
   * line will be omitted from the statement. In addition, multiple adjacent
   * whitespace characters will be collapsed into a single space.
   *
   * @param script the SQL script
   * @param delim character delimiting each statement (typically a ';' character)
   * @param commentPrefix the prefix that identifies line comments in the SQL script &mdash; typically "--"
   * @param statements the List that will contain the individual statements
   */
  private void splitSqlScript( String script, String delim, String commentPrefix, List<String> statements )
  {
    String statementSeparatorStart = commentPrefix + ' ' + STATEMENT_SEPARATOR_START;
    String statementSeparatorEnd = commentPrefix + ' ' + STATEMENT_SEPARATOR_END;

    StringBuilder sb = new StringBuilder();
    boolean inLiteral = false;
    boolean inEscape = false;
    boolean inStatementEscape = false;

    char[] content = script.toCharArray();

    for ( int i = 0; i < script.length(); i++ )
    {
      char c = content[i];

      if ( inEscape )
      {
        inEscape = false;
        sb.append( c );
        continue;
      }

      // MySQL style escapes
      if ( c == '\\' )
      {
        inEscape = true;
        sb.append( c );
        continue;
      }

      if ( c == '\'' )
      {
        inLiteral = !inLiteral;
      }

      if ( !inLiteral )
      {
        // normal statements ending with ;
        if ( script.startsWith( delim, i ) && !inStatementEscape )
        {
          // we've reached the end of the current statement
          if ( sb.length() > 0 )
          {
            statements.add( sb.toString() );
            sb = new StringBuilder();
          }
          i += delim.length() - 1;
          continue;
        }
        else if ( script.startsWith( commentPrefix, i ) )
        {
          // -- [statement]
          if ( script.startsWith( statementSeparatorStart, i ) )
          {
            inStatementEscape = true;
          }

          // -- [/statement]
          if ( script.startsWith( statementSeparatorEnd, i ) )
          {
            inStatementEscape = false;

            // we've reached the end of the escaped statement
            if ( sb.length() > 0 )
            {
              statements.add( sb.toString() );
              sb = new StringBuilder();
            }
          }

          // skip over any content from the start of the comment to the EOL
          int indexOfNextNewline = script.indexOf( "\n", i );
          if ( indexOfNextNewline > i )
          {
            i = indexOfNextNewline;
            continue;
          }
          else
          {
            // if there's no newline after the comment, we must be at the end
            // of the script, so stop here.
            break;
          }
        }
        else if ( c == ' ' || c == '\n' || c == '\t' )
        {
          // avoid multiple adjacent whitespace characters
          if ( sb.length() > 0 && sb.charAt( sb.length() - 1 ) != ' ' )
          {
            c = ' ';
          }
          else
          {
            continue;
          }
        }
      }
      sb.append( c );
    }

    String statement = sb.toString();
    if ( StringUtils.isNotBlank( statement ) )
    {
      statements.add( statement );
    }
  }
}

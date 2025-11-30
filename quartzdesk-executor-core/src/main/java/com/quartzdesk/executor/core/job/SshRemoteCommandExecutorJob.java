/*
 * Copyright (c) 2015-2025 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.core.job;

import com.quartzdesk.executor.common.CommonConst;
import com.quartzdesk.executor.common.text.StringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A Quartz job implementation that executes an arbitrary remote command/script specified in a job data map parameter.
 * The following job data map parameters are supported:
 *
 * <dl>
 * <dt>sshHost</dt>
 * <dd>The remote host to connect to.</dd>
 *
 * <dt>sshPort</dt>
 * <dd>The remote SSH port to connect to. If omitted, then port 22 is used.</dd>
 *
 * <dt>sshUser</dt>
 * <dd>The SSH auth username.</dd>
 *
 * <dt>sshPassword</dt>
 * <dd>The SSH auth password.</dd>
 *
 * <dt>command</dt>
 * <dd>The command to execute.</dd>
 *
 * <dt>commandArgs</dt>
 * <dd>Optional command line arguments to pass to the command.</dd>
 * </dl>
 */
@DisallowConcurrentExecution
public class SshRemoteCommandExecutorJob
    extends AbstractJob
{
  private static final Logger log = LoggerFactory.getLogger( SshRemoteCommandExecutorJob.class );

  private static final int DEFAULT_SSH_PORT = 22;

  private static final String JDM_KEY_SSH_HOST = "sshHost";
  private static final String JDM_KEY_SSH_PORT = "sshPort";
  private static final String JDM_KEY_SSH_USER = "sshUser";

  private static final String JDM_KEY_SSH_PASSWORD = "sshPassword";
  private static final String JDM_KEY_SSH_PRIV_KEY_FILE = "sshPrivKeyFile";

  private static final String JDM_KEY_COMMAND = "command";
  private static final String JDM_KEY_COMMAND_ARGS = "commandArgs";


  @Override
  protected void executeJob( JobExecutionContext context )
      throws JobExecutionException
  {
    log.debug( "Inside job: {}", context.getJobDetail().getKey() );

    JobDataMap jobDataMap = context.getMergedJobDataMap();

    // SSH host
    String sshHost = jobDataMap.getString( JDM_KEY_SSH_HOST );
    if ( sshHost == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_COMMAND + "' job data map parameter." );
    }

    Integer sshPort = jobDataMap.getIntegerFromString( JDM_KEY_SSH_PORT );
    if ( sshPort == null )
    {
      sshPort = DEFAULT_SSH_PORT;
    }

    // SSH user
    String sshUser = jobDataMap.getString( JDM_KEY_SSH_USER );
    if ( sshUser == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_SSH_USER + "' job data map parameter." );
    }

    // SSH password
    String sshPassword = jobDataMap.getString( JDM_KEY_SSH_PASSWORD );

    // SSH private key file
    String sshPrivKeyFile = jobDataMap.getString( JDM_KEY_SSH_PRIV_KEY_FILE );

    if ( sshPrivKeyFile != null )
    {
      File sshPrivKeyFilePath = new File( sshPrivKeyFile );
      if ( sshPrivKeyFilePath.exists() && sshPrivKeyFilePath.isFile() && sshPrivKeyFilePath.canRead() )
      {
        // ok - private key exists and is accessible
      }
      else
      {
        throw new JobExecutionException( "Private key " + sshPrivKeyFile + " does not exist or is not readable." );
      }
    }

    if ( sshPassword == null && sshPrivKeyFile == null )
    {
      throw new JobExecutionException( "Either '" + JDM_KEY_SSH_PASSWORD + "' or '" + JDM_KEY_SSH_PRIV_KEY_FILE +
          "' job data map parameter must be specified." );
    }

    // command
    String command = jobDataMap.getString( JDM_KEY_COMMAND );
    if ( command == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_COMMAND + "' job data map parameter." );
    }

    // command arguments (optional)
    String commandArgs = jobDataMap.getString( JDM_KEY_COMMAND_ARGS );

    JSch jsch = new JSch();

    Session session = null;
    try
    {
      if ( sshPrivKeyFile != null )
      {
        jsch.addIdentity( sshPrivKeyFile );
      }

      session = jsch.getSession( sshUser, sshHost, sshPort );
      SSHUserInfo userInfo = new SSHUserInfo( sshUser, sshPassword );
      session.setUserInfo( userInfo );
      session.setConfig( "PreferredAuthentications", "publickey,password" );
      session.setConfig( "StrictHostKeyChecking", "no" );
      log.info( "Connecting to {}:{}", sshHost, sshPort );
      session.connect();

      ChannelExec channel = (ChannelExec) session.openChannel( "exec" );

      String commandLine = command;
      if ( commandArgs != null )
        commandLine += CommonConst.SINGLE_SPACE + commandArgs;

      channel.setCommand( commandLine );
      channel.setInputStream( null );

      InputStream ins = channel.getInputStream();

      log.info( "Executing remote command using command line: {}", commandLine );

      channel.connect();

      String output = readCommandOutput( ins, channel );

      int exitCode = channel.getExitStatus();

      log.debug( "Remote command finished with exit code: {}", exitCode );
      context.setResult( exitCode );  // exit code is used as the job's execution result (visible in the QuartzDesk GUI)

      if ( StringUtils.isBlank( output ) )
      {
        log.info( "Remote command produced no output." );
      }
      else
      {
        log.info( "Remote command produced the following output:{}{}", CommonConst.NL, output );
      }

      if ( exitCode != 0 )
      {
        throw new JobExecutionException( "Remote command finished with non-zero exit code: " + exitCode );
      }
    }
    catch ( Exception e )  // JSchException, IOException
    {
      throw new JobExecutionException( "Error running remote command.", e );
    }
    finally
    {
      // clean up the SSH session
      if ( session != null && session.isConnected() )
      {
        session.disconnect();
      }
    }
  }


  /**
   * Reads the output of the executed command and returns it as a string.
   *
   * @param ins     an input stream to read the data from.
   * @param channel an SSH channel.
   * @return the command output data.
   * @throws IOException if an I/O error occurs.
   */
  private String readCommandOutput( InputStream ins, Channel channel )
      throws IOException
  {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] bytesBuffer = new byte[1024];
    while ( true )
    {
      while ( ins.available() > 0 )
      {
        int readCount = ins.read( bytesBuffer, 0, bytesBuffer.length );
        if ( readCount < 0 )
          break;

        outputStream.write( bytesBuffer, 0, readCount );
      }

      if ( channel.isClosed() )
      {
        if ( ins.available() > 0 )
        {
          continue;
        }
        else
        {
          // channel closed and no more bytes available
          break;
        }
      }

      try
      {
        // wait for data to be available
        Thread.sleep( 100 );
      }
      catch ( InterruptedException e )
      {
        // ignore
      }
    }

    // we assume Latin1 output
    return outputStream.size() == 0 ? null : outputStream.toString( CommonConst.ENCODING_ISO_8859_1 );
  }


  private static class SSHUserInfo
      implements UserInfo
  {
    private String sshUser;
    private String sshPassword;

    protected SSHUserInfo( String sshUser, String sshPassword )
    {
      this.sshUser = sshUser;
      this.sshPassword = sshPassword;
    }

    @Override
    public String getPassphrase()
    {
      return null;
    }

    @Override
    public String getPassword()
    {
      return sshPassword;
    }

    @Override
    public boolean promptPassword( String s )
    {
      return true;
    }

    @Override
    public boolean promptPassphrase( String s )
    {
      return false;
    }

    @Override
    public boolean promptYesNo( String s )
    {
      return false;
    }

    @Override
    public void showMessage( String s )
    {
    }
  }
}

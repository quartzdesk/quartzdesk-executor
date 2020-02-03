/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.core.job;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Quartz job implementation that performs an HTTP POST request to the URL specified in the
 * {@code url} job data map parameter. The POST request optionally uses the HTTP Basic authentication
 * in case the {@code username} and {@code password} job data map parameters are set.
 */
@DisallowConcurrentExecution
public class UrlInvokerJob
    extends AbstractJob
{
  private static final Logger log = LoggerFactory.getLogger( UrlInvokerJob.class );

  private static final String JDM_KEY_URL = "url";
  private static final String JDM_KEY_USERNAME = "username";
  private static final String JDM_KEY_PASSWORD = "password";

  @Override
  protected void executeJob( final JobExecutionContext context )
      throws JobExecutionException
  {
    log.debug( "Inside job: {}", context.getJobDetail().getKey() );
    JobDataMap jobDataMap = context.getMergedJobDataMap();

    // url (required)
    final String url = jobDataMap.getString( JDM_KEY_URL );
    if ( url == null )
    {
      throw new JobExecutionException( "Missing required '" + JDM_KEY_URL + "' job data map parameter." );
    }

    // username (optional)
    String username = jobDataMap.getString( JDM_KEY_USERNAME );
    // password (optional)
    String password = jobDataMap.getString( JDM_KEY_PASSWORD );


    CloseableHttpClient httpClient;
    if ( username != null && password != null )
    {
      // use HTTP basic authentication
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
          AuthScope.ANY,
          new UsernamePasswordCredentials( username, password ) );

      httpClient = HttpClients.custom()
          .setDefaultCredentialsProvider( credsProvider )
          .build();
    }
    else
    {
      // use no HTTP authentication
      httpClient = HttpClients.custom()
          .build();
    }

    try
    {
      HttpPost httpPost = new HttpPost( url );

      ResponseHandler<String> responseHandler = new ResponseHandler<String>()
      {
        @Override
        public String handleResponse( HttpResponse httpResponse )
            throws IOException
        {
          int status = httpResponse.getStatusLine().getStatusCode();

          //context.setResult( Integer.toString( status ) );

          if ( status >= 200 && status < 300 )
          {
            HttpEntity entity = httpResponse.getEntity();
            return entity == null ? null : EntityUtils.toString( entity );
          }
          else
          {
            throw new ClientProtocolException( "URL: " + url + " returned unexpected response status code: " + status );
          }
        }
      };

      log.debug( "HTTP request line: {}", httpPost.getRequestLine() );

      log.info( "Invoking target URL: {}", url );
      String responseText = httpClient.execute( httpPost, responseHandler );

      log.debug( "Response text: {}", responseText );

      if ( !responseText.trim().isEmpty() )
      {
      /*
       * We use the HTTP response text as the Quartz job execution result. This code can then be easily
       * viewed in the Execution History in the QuartzDesk GUI and it can be, for example, used to trigger
       * execution notifications.
       */
        context.setResult( responseText );
      }


    }
    catch ( IOException e )
    {
      throw new JobExecutionException( "Error invoking URL: " + url, e );
    }
    finally
    {
      try
      {
        httpClient.close();
      }
      catch ( IOException e )
      {
        log.error( "Error closing HTTP client.", e );
      }
    }
  }
}

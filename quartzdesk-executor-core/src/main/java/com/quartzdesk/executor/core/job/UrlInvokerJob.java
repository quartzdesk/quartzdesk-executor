/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

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

  private static final String JDM_KEY_PASSWORD = "password";

  private static final String JDM_KEY_URL = "url";

  private static final String JDM_KEY_USERNAME = "username";


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

    final URI uri;
    try
    {
      uri = new URI( url );
    }
    catch ( URISyntaxException e )
    {
      throw new JobExecutionException( "Invalid URL syntax.", e );
    }

    HttpClientBuilder httpClientBuilder = HttpClients.custom();
    if ( username != null && password != null )
    {
      // use HTTP basic authentication
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(
          AuthScope.ANY,
          new UsernamePasswordCredentials( username, password ) );

      httpClientBuilder.setDefaultCredentialsProvider( credsProvider );
    }
    else
    {
      // use no HTTP authentication
    }

    if ( "https".equals( uri.getScheme() ) )
    {
      TrustManager[] trustAllCerts = new TrustManager[] {
          new X509TrustManager()
          {
            public void checkClientTrusted( X509Certificate[] certs, String authType )
            {
            }


            public void checkServerTrusted( X509Certificate[] certs, String authType )
            {
            }


            public X509Certificate[] getAcceptedIssuers()
            {
              return null;
            }
          }
      };

      try
      {
        SSLContext sc = SSLContext.getInstance( "SSL" );
        sc.init( null, trustAllCerts, new SecureRandom() );

        httpClientBuilder.setSSLHostnameVerifier( NoopHostnameVerifier.INSTANCE )
            .setSSLContext( sc );
      }
      catch ( GeneralSecurityException e )
      {
        throw new JobExecutionException( "Error creating SSL context.", e );
      }
    }

    CloseableHttpClient httpClient = httpClientBuilder.build();

    try
    {
      HttpPost httpPost = new HttpPost( uri );

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
            throw new ClientProtocolException(
                "URL: " + uri + " returned unexpected response status code: " + status );
          }
        }
      };

      log.debug( "HTTP request line: {}", httpPost.getRequestLine() );

      log.info( "Invoking target URL: {}", uri );
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
      throw new JobExecutionException( "Error invoking URL: " + uri, e );
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

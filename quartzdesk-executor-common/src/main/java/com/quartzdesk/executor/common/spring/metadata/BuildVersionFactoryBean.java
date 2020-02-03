/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.spring.metadata;

import com.quartzdesk.executor.common.type.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.util.Map;

/**
 * Spring factory bean that extracts the build version from the specified
 * build properties.
 *
 * @see BuildPropertiesFactoryBean
 */
public class BuildVersionFactoryBean
    extends AbstractFactoryBean<Version>
{
  private static final Logger log = LoggerFactory.getLogger( BuildVersionFactoryBean.class );

  private Map<String, String> buildProperties;
  private static final String BUILD_VERSION_KEY = "build.version";

  public Map<String, String> getBuildProperties()
  {
    return buildProperties;
  }

  @Required
  public void setBuildProperties( Map<String, String> buildProperties )
  {
    this.buildProperties = buildProperties;
  }

  @Override
  public Class<Version> getObjectType()
  {
    return Version.class;
  }

  @Override
  protected Version createInstance()
      throws Exception
  {
    String buildVersionStr = buildProperties.get( BUILD_VERSION_KEY );

    if ( buildVersionStr == null )
      throw new IllegalStateException(
          "Missing " + BUILD_VERSION_KEY + " property in build properties: " + buildProperties );

    return Version.parseVersion( buildVersionStr );
  }
}

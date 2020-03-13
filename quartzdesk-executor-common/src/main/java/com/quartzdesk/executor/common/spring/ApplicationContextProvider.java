/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.spring;


import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Simple bean that exposes the current Spring application context
 * in all parts of the application even those parts are not registered
 * Spring beans.
 * <p/>
 * This bean should be registered in the Spring application context
 * configuration as a singleton.
 * <p/>
 * Inspired by http://blog.jdevelop.eu/2008/07/06/access-the-spring-applicationcontext-from-everywhere-in-your-application/.
 */
public class ApplicationContextProvider
    implements ApplicationContextAware
{
  private static ApplicationContext appCtx;

  /**
   * Sets the application context. This method is invoked automatically
   * by Spring because this bean implements the {@link ApplicationContextAware}
   * interface.
   *
   * @param appCtx an application context.
   */
  @Override
  public void setApplicationContext( ApplicationContext appCtx )
  {
    /*
     * Little bit ugly assignment to a static field from a non-static context,
     * but there is no other way...
     */
    ApplicationContextProvider.appCtx = appCtx;
  }


  /**
   * Returns the Spring application context. To access the current
   * application context, please use {@link com.quartzdesk.executor.common.spring.SpringContext#getCurrent}.
   *
   * @return the Spring application context.
   */
  protected static ApplicationContext getApplicationContext()
  {
    assert appCtx != null : "Spring application context is not available. " +
        ApplicationContextProvider.class.getName() + " bean must be registered with Spring.";

    return appCtx;
  }
}

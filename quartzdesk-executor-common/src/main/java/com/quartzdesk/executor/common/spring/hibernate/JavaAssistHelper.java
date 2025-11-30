/*
 * Copyright (c) 2015-2025 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.common.spring.hibernate;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyFactory.ClassLoaderProvider;

/**
 * A helper class to avoid the nasty exception when creating a session factory.
 *
 * <pre>
 * org.hibernate.HibernateException: HHH000142: Javassist Enhancement failed: com.quartzdesk.domain.model.message.channel.MessageChannelProfileFolder
 *	at org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer.getProxyFactory(JavassistLazyInitializer.java:167) ~[hibernate-core-4.1.9.Final.jar:4.1.9.Final]
 *	at org.hibernate.proxy.pojo.javassist.JavassistProxyFactory.postInstantiate(JavassistProxyFactory.java:66) ~[hibernate-core-4.1.9.Final.jar:4.1.9.Final]
 *  ...
 * Caused by: java.lang.NoClassDefFoundError: org/hibernate/proxy/HibernateProxy
 *	at java.lang.ClassLoader.defineClass1(Native Method) ~[na:1.6.0_35]
 *	at java.lang.ClassLoader.defineClassCond(ClassLoader.java:631) ~[na:1.6.0_35]
 *	at java.lang.ClassLoader.defineClass(ClassLoader.java:615) ~[na:1.6.0_35]
 *	...
 * Caused by: java.lang.ClassNotFoundException: org.hibernate.proxy.HibernateProxy
 *	at java.net.URLClassLoader$1.run(URLClassLoader.java:202) ~[na:1.6.0_35]
 *	at java.security.AccessController.doPrivileged(Native Method) ~[na:1.6.0_35]
 *	at java.net.URLClassLoader.findClass(URLClassLoader.java:190) ~[na:1.6.0_35]
 *	...
 * </pre>
 *
 * Inspired by <a href="https://hibernate.onjira.com/browse/HHH-3826">https://hibernate.onjira.com/browse/HHH-3826</a>.
 *
 */
public class JavaAssistHelper
{
  public static ClassLoaderProvider createJavaAssistClassLoader()
  {
    ProxyFactory.classLoaderProvider = new ProxyFactory.ClassLoaderProvider()
    {
      @Override
      public ClassLoader get( ProxyFactory pf )
      {
        return Thread.currentThread().getContextClassLoader();
      }
    };
    return ProxyFactory.classLoaderProvider;
  }
}
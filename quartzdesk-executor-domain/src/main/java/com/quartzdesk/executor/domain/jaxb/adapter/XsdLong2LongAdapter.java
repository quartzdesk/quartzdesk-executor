/*
 * Copyright (c) 2015-2025 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.domain.jaxb.adapter;

import com.quartzdesk.executor.domain.jaxb.JaxbDatatypeConverter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Implementation of the JAXB {@link XmlAdapter} that maps
 * the xsd:long onto the {@link Long} type and vice-versa.
 */
public class XsdLong2LongAdapter
    extends XmlAdapter<String, Long>
{
  @Override
  public Long unmarshal( String value )
  {
    return ( JaxbDatatypeConverter.parseLong( value ) );
  }

  @Override
  public String marshal( Long value )
  {
    return ( JaxbDatatypeConverter.printLong( value ) );
  }
}

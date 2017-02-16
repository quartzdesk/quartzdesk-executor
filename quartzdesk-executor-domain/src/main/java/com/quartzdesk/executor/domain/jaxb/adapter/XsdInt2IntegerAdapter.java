/*
 * Copyright (c) 2015-2017 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.domain.jaxb.adapter;

import com.quartzdesk.executor.domain.jaxb.JaxbDatatypeConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Implementation of the JAXB {@link XmlAdapter} that maps
 * the xsd:int onto the {@link Integer} type and vice-versa.
 */
public class XsdInt2IntegerAdapter
    extends XmlAdapter<String, Integer>
{
  @Override
  public Integer unmarshal( String value )
  {
    return ( JaxbDatatypeConverter.parseInteger( value ) );
  }

  @Override
  public String marshal( Integer value )
  {
    return ( JaxbDatatypeConverter.printInteger( value ) );
  }
}

/*
 * Copyright (c) 2015-2020 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.domain.jaxb.adapter;

import com.quartzdesk.executor.domain.jaxb.JaxbDatatypeConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Implementation of the JAXB {@link XmlAdapter} that maps
 * the xsd:date onto the {@link Boolean} type and vice-versa.
 */
public class XsdBoolean2BooleanAdapter
    extends XmlAdapter<String, Boolean>
{
  @Override
  public Boolean unmarshal( String value )
  {
    return ( JaxbDatatypeConverter.parseBoolean( value ) );
  }

  @Override
  public String marshal( Boolean value )
  {
    return ( JaxbDatatypeConverter.printBoolean( value ) );
  }
}

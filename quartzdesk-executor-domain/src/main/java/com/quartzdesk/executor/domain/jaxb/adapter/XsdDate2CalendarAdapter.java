/*
 * Copyright (c) 2015-2025 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.domain.jaxb.adapter;

import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Calendar;

/**
 * Implementation of the JAXB {@link XmlAdapter} that maps
 * the xsd:date onto the {@link Calendar} type and vice-versa.
 */
public class XsdDate2CalendarAdapter
    extends XmlAdapter<String, Calendar>
{
  @Override
  public Calendar unmarshal( String value )
  {
    return ( DatatypeConverter.parseDate( value ) );
  }

  @Override
  public String marshal( Calendar value )
  {
    if ( value == null )
    {
      return null;
    }
    return ( DatatypeConverter.printDate( value ) );
  }
}

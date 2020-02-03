/*
 * Copyright (c) 2015-2019 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.domain.jaxb.adapter;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Calendar;

/**
 * Implementation of the JAXB {@link XmlAdapter} that maps
 * the xsd:time onto the {@link Calendar} type and vice-versa.
 */
public class XsdTime2CalendarAdapter
    extends XmlAdapter<String, Calendar>
{
  @Override
  public Calendar unmarshal( String value )
  {
    return ( DatatypeConverter.parseTime( value ) );
  }

  @Override
  public String marshal( Calendar value )
  {
    if ( value == null )
    {
      return null;
    }
    return ( DatatypeConverter.printTime( value ) );
  }
}

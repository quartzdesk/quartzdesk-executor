 /*
  * Copyright (c) 2013-2025 QuartzDesk.com.
  * Licensed under the MIT license (https://opensource.org/licenses/MIT).
  */

 package com.quartzdesk.executor.domain.convert;

 import com.quartzdesk.executor.domain.model.common.Version;

 import java.io.Serializable;
 import java.util.Comparator;

 /**
  * Comparator of {@link Version} instances.
  */
 public class VersionComparator
     implements Comparator<Version>, Serializable
 {
   public static final VersionComparator INSTANCE = new VersionComparator();


   /**
    * Private constructor to prevent external instantiation.
    */
   private VersionComparator()
   {
   }


   @Override
   public int compare( Version v1, Version v2 )
   {
     int r = safeCompare( v1.getMajor(), v2.getMajor() );
     if ( r < 0 )
       return -1;
     if ( r > 0 )
       return 1;

     // majors are equal
     r = safeCompare( v1.getMinor(), v2.getMinor() );
     if ( r < 0 )
       return -1;
     if ( r > 0 )
       return 1;

     // majors and minors are equal
     r = safeCompare( v1.getMaintenance(), v2.getMaintenance() );
     if ( r < 0 )
       return -1;
     if ( r > 0 )
       return 1;

     return safeCompare( v1.getQualifier(), v2.getQualifier() );
   }


   /**
    * Returns 0 if both objects are null, -1 if c1 is null and c2 is not,
    * 1 if c1 is not null and c2 is null, otherwise the result of normal
    * comparison.
    * <p/>
    * <strong>
    * Please note that this method performs a lexicographic comparison
    * on string arguments using the standard {@link String#compareTo(String)}
    * method.
    * </strong>
    *
    * @param o1 the first object.
    * @param o2 the second object.
    * @return the comparison result.
    */
   private <T extends Comparable<T>> int safeCompare( T o1, T o2 )
   {
     if ( o1 == o2 )
       return 0;

     if ( o1 != null && o2 == null )
       return 1;

     if ( o1 == null )  // o2 != null
       return -1;

      /*
      Comparable c1 = (Comparable) o1;
      Comparable c2 = (Comparable) o2;
      */

     return o1.compareTo( o2 );
   }
 }

/*
 * Copyright (c) 2015-2017 QuartzDesk.com.
 * Licensed under the MIT license (https://opensource.org/licenses/MIT).
 */

package com.quartzdesk.executor.domain.model;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Common base class for all generated domain model classes.
 * <p/>
 * This base class must be annotated with the {@link XmlTransient}
 * annotation as explained <a href="https://sites.google.com/site/codingkb/java-2/jaxb/jaxb-4">here</a>.
 */
@XmlTransient
public abstract class DomainModel
    implements Serializable
{
}

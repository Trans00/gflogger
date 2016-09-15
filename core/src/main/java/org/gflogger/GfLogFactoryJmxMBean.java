package org.gflogger;

import javax.management.MXBean;
import java.util.Collection;

/**
 * Created by Denis Gburg on 11.09.2016.
 */
@MXBean
public interface GfLogFactoryJmxMBean {

    void setLevel(String logger, String level);
}

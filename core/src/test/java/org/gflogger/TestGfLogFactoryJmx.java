package org.gflogger;

import org.gflogger.config.xml.StreamAppenderFactory;
import org.gflogger.config.xml.XmlConfiguration;
import org.gflogger.config.xml.XmlLogFactoryConfigurator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Denis Gburg on 11.09.2016.
 */
public class TestGfLogFactoryJmx {
    private final StringBuffer buffer = new StringBuffer();

    @Before
    public void setUp(){
        buffer.setLength(0);
        System.setProperty("TZ", "GMT");
        System.setProperty("gflogger.configuration", "gflogger.xml");
        StreamAppenderFactory.outputStream = buffer;
    }

    @Test
    public void testFile() throws Throwable {
        XmlLogFactoryConfigurator.configure("file:/gflogger2.xml");

        final GFLog changedLog = GFLogFactory.getLog("changed.this");
        final GFLog unchangedLog = GFLogFactory.getLog("changed");
        GFLogFactory.GfLogFactoryJmx jmx = GFLogFactory.getJmx();
        changedLog.trace("beforeTrace");
        changedLog.debug("beforeDebug");
        changedLog.info("beforeInfo");
        changedLog.warn("beforeWarn");
        changedLog.error("beforeError");
        changedLog.fatal("beforeFatal");

        unchangedLog.trace("beforeTrace");
        unchangedLog.debug("beforeDebug");
        unchangedLog.info("beforeInfo");
        unchangedLog.warn("beforeWarn");
        unchangedLog.error("beforeError");
        unchangedLog.fatal("beforeFatal");

        jmx.setLevel("changed.this", "INFO");

        changedLog.trace("afterTrace");
        changedLog.debug("afterDebug");
        changedLog.info("afterInfo");
        changedLog.warn("afterWarn");
        changedLog.error("afterError");
        changedLog.fatal("afterFatal");

        unchangedLog.trace("afterTrace");
        unchangedLog.debug("afterDebug");
        unchangedLog.info("afterInfo");
        unchangedLog.warn("afterWarn");
        unchangedLog.error("afterError");
        unchangedLog.fatal("afterFatal");

        GFLogFactory.stop();

        final String output = buffer.toString();
        String[] result = output.split(System.getProperty("line.separator"));
        assertEquals(result[0],"FATAL - beforeFatal [changed.this] [main]");
        assertEquals(result[1],"FATAL - beforeFatal [changed] [main]");
        assertEquals(result[2],"INFO - afterInfo [changed.this] [main]");
        assertEquals(result[3],"WARN - afterWarn [changed.this] [main]");
        assertEquals(result[4],"ERROR - afterError [changed.this] [main]");
        assertEquals(result[5],"FATAL - afterFatal [changed.this] [main]");
        assertEquals(result[6],"FATAL - afterFatal [changed] [main]");
        assertEquals(result.length,7);
    }

}

package org.desy.logbook.settings;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Johannes Strampe
 */
public class SettingsTest {

    /**
     * Method should return 0 for first call and a
     * higher value for the next call.
     */
    @Test
    public void testGetSleepTime() {
        assertEquals(0, Settings.getSleepTime());
        assertTrue(0<Settings.getSleepTime());
    }

}
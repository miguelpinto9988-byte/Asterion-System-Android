package pe.asterionsystem.app;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class SplashTimeWindowTest {
    @Test
    public void midnightToElevenFiftyNineIsMorning() {
        for (int hour = 0; hour < 12; hour++) {
            assertTrue("hour=" + hour, SplashTimeWindow.isMorning(hour));
        }
    }

    @Test
    public void noonToElevenFiftyNinePmIsNight() {
        for (int hour = 12; hour < 24; hour++) {
            assertFalse("hour=" + hour, SplashTimeWindow.isMorning(hour));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeHourIsRejected() {
        SplashTimeWindow.isMorning(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hourTwentyFourIsRejected() {
        SplashTimeWindow.isMorning(24);
    }
}

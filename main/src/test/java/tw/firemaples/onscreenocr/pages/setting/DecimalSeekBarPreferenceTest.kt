package tw.firemaples.onscreenocr.pages.setting

import org.junit.Assert.assertEquals
import org.junit.Test

class DecimalSeekBarPreferenceTest {

    @Test
    fun testFormatValue_standardDeciSeconds() {
        assertEquals("2.0 s", DecimalSeekBarPreference.formatValue(20))
        assertEquals("1.5 s", DecimalSeekBarPreference.formatValue(15))
        assertEquals("0.5 s", DecimalSeekBarPreference.formatValue(5))
        assertEquals("30.0 s", DecimalSeekBarPreference.formatValue(300))
    }

    @Test
    fun testFormatValue_legacyValuesMigration() {
        assertEquals("1.0 s", DecimalSeekBarPreference.formatValue(1))
        assertEquals("2.0 s", DecimalSeekBarPreference.formatValue(2))
        assertEquals("3.0 s", DecimalSeekBarPreference.formatValue(3))
        assertEquals("4.0 s", DecimalSeekBarPreference.formatValue(4))
    }
}

package com.odorik.odorikbuddy.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionUtilsTest {

    @Test
    fun `equal versions are not newer`() {
        assertFalse(VersionUtils.isNewer("1.4.4", "1.4.4"))
        assertEquals(0, VersionUtils.compare("1.4.4", "1.4.4"))
    }

    @Test
    fun `higher patch is newer`() {
        assertTrue(VersionUtils.isNewer("1.4.5", "1.4.4"))
        assertFalse(VersionUtils.isNewer("1.4.3", "1.4.4"))
    }

    @Test
    fun `higher minor beats higher patch`() {
        assertTrue(VersionUtils.isNewer("1.5.0", "1.4.9"))
    }

    @Test
    fun `numeric comparison not lexicographic`() {
        assertTrue(VersionUtils.isNewer("1.10", "1.9"))
        assertFalse(VersionUtils.isNewer("1.9", "1.10"))
    }

    @Test
    fun `missing segments count as zero`() {
        assertFalse(VersionUtils.isNewer("1.4", "1.4.0"))
        assertTrue(VersionUtils.isNewer("1.4.1", "1.4"))
    }

    @Test
    fun `leading v prefix is ignored`() {
        assertTrue(VersionUtils.isNewer("v1.5.1", "1.5.0"))
        assertFalse(VersionUtils.isNewer("v1.5.0", "1.5.0"))
        assertEquals(0, VersionUtils.compare("v1.4.4", "1.4.4"))
        assertEquals(0, VersionUtils.compare("V1.4.4", "v1.4.4"))
    }

    @Test
    fun `pre-release and build suffixes are ignored`() {
        assertEquals(0, VersionUtils.compare("1.5.0-beta1", "1.5.0"))
        assertTrue(VersionUtils.isNewer("v1.5.1-rc1", "1.5.0"))
    }

    @Test
    fun `non-numeric segments count as zero`() {
        assertFalse(VersionUtils.isNewer("abc", "0.0.0"))
        assertTrue(VersionUtils.isNewer("1.0", "abc"))
    }
}

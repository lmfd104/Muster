package com.lmfd.warboss.bsdata

import com.lmfd.warboss.data.bsdata.ImportStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportStatusTest {

    @Test
    fun downloading_knownSize_progressCorrect() {
        val s = ImportStatus.Downloading(50L, 100L)
        assertEquals(0.5f, s.progress, 0.001f)
    }

    @Test
    fun downloading_unknownSize_progressNegative() {
        val s = ImportStatus.Downloading(50L, -1L)
        assertEquals(-1f, s.progress, 0.001f)
    }

    @Test
    fun downloading_zeroTotal_progressNegative() {
        val s = ImportStatus.Downloading(0L, 0L)
        assertEquals(-1f, s.progress, 0.001f)
    }

    @Test
    fun parsing_progressFraction() {
        val s = ImportStatus.Parsing("Orks", 3, 10)
        assertEquals(0.3f, s.progress, 0.001f)
    }

    @Test
    fun parsing_zeroTotal_progressZero() {
        val s = ImportStatus.Parsing("Loading…", 0, 0)
        assertEquals(0f, s.progress, 0.001f)
    }

    @Test
    fun idle_distinctFromComplete() {
        val idle: ImportStatus = ImportStatus.Idle
        assertFalse(idle is ImportStatus.Complete)
    }

    @Test
    fun error_holdsMessage() {
        val e = ImportStatus.Error("boom")
        assertEquals("boom", e.message)
    }
}

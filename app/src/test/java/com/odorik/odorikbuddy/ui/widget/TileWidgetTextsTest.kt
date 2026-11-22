package com.odorik.odorikbuddy.ui.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class TileWidgetTextsTest {

    @Test
    fun `label is the title and the contact name goes below it`() {
        assertEquals("Mum", TileWidgetTexts.title("Mum", "Jana Novakova"))
        assertEquals("Jana Novakova", TileWidgetTexts.subtitle("Mum", "Jana Novakova", "+420123456789"))
    }

    @Test
    fun `without a label the contact name is the title and the number goes below it`() {
        assertEquals("Jana Novakova", TileWidgetTexts.title("", "Jana Novakova"))
        assertEquals("+420123456789", TileWidgetTexts.subtitle("", "Jana Novakova", "+420123456789"))
    }

    @Test
    fun `unknown number is not shown twice`() {
        assertEquals("+420123456789", TileWidgetTexts.title("", "+420123456789"))
        assertEquals("", TileWidgetTexts.subtitle("", "+420123456789", "+420123456789"))
    }

    @Test
    fun `initials use the first two words`() {
        assertEquals("JN", TileWidgetTexts.initials("jana novakova"))
        assertEquals("JN", TileWidgetTexts.initials("  Jana   Novakova Stara "))
        assertEquals("M", TileWidgetTexts.initials("mum"))
    }

    @Test
    fun `blank name gives a question mark`() {
        assertEquals("?", TileWidgetTexts.initials(""))
        assertEquals("?", TileWidgetTexts.initials("   "))
    }
}

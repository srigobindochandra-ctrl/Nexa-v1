package com.example

import com.example.model.ChatMessage
import com.example.model.NexaLanguage
import com.example.model.NexaMode
import com.example.model.WriteCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testNexaLanguages() {
        assertEquals(NexaLanguage.EN, NexaLanguage.fromCode("en-US"))
        assertEquals(NexaLanguage.BN, NexaLanguage.fromCode("bn-BD"))
        assertEquals(NexaLanguage.HI, NexaLanguage.fromCode("hi-IN"))
        assertEquals(NexaLanguage.EN, NexaLanguage.fromCode("unknown"))
    }

    @Test
    fun testNexaModes() {
        assertEquals(6, NexaMode.entries.size)
        assertEquals(NexaMode.GIRLFRIEND, NexaMode.fromId("girlfriend"))
        assertEquals(NexaMode.DEVELOPER, NexaMode.fromId("developer"))
        assertEquals(NexaMode.TEACHER, NexaMode.fromId("teacher"))
        assertEquals(NexaMode.FRIEND, NexaMode.fromId("friend"))
        assertEquals(NexaMode.NATURAL, NexaMode.fromId("natural"))
        assertEquals(NexaMode.CUSTOM, NexaMode.fromId("custom"))
    }

    @Test
    fun testChatMessageCreation() {
        val msg = ChatMessage(text = "Hello NEXA", isUser = true)
        assertTrue(msg.isUser)
        assertEquals("Hello NEXA", msg.text)
        assertNotNull(msg.formattedTime)
    }

    @Test
    fun testWriteCategories() {
        assertEquals(4, WriteCategory.entries.size)
        assertTrue(WriteCategory.CODE.prefix.contains("code"))
    }

    @Test
    fun testAppTabs() {
        assertTrue(com.example.model.AppTab.entries.contains(com.example.model.AppTab.HOME))
        assertTrue(com.example.model.AppTab.entries.contains(com.example.model.AppTab.MODES))
        assertTrue(com.example.model.AppTab.entries.contains(com.example.model.AppTab.PLUGINS))
        assertTrue(com.example.model.AppTab.entries.contains(com.example.model.AppTab.CHAT))
        assertTrue(com.example.model.AppTab.entries.contains(com.example.model.AppTab.CONNECTIONS))
        assertEquals("Home", com.example.model.AppTab.HOME.label)
        assertEquals("Modes", com.example.model.AppTab.MODES.label)
        assertEquals("Plugins", com.example.model.AppTab.PLUGINS.label)
    }
}

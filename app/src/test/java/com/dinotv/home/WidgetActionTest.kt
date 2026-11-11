package com.dinotv.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetActionTest {
    @Test fun knownIdsRoundTrip() {
        for (action in WidgetAction.entries) assertEquals(action, WidgetAction.from(action.id))
    }

    @Test fun unknownIdsAreIgnored() {
        assertNull(WidgetAction.from(null))
        assertNull(WidgetAction.from(""))
        assertNull(WidgetAction.from("music; alert(1)"))
    }

    @Test fun scriptsBalanceTheirBrackets() {
        for (action in WidgetAction.entries) {
            val s = action.script
            assertEquals(action.id, s.count { it == '(' }, s.count { it == ')' })
            assertEquals(action.id, s.count { it == '{' }, s.count { it == '}' })
            assertTrue(action.id, s.contains("pane-signin"))
        }
    }

    @Test fun nightTogglePressesTheUnlitButton() {
        assertTrue(WidgetAction.NIGHT_TOGGLE.script.contains("[data-sleep-enabled]:not(.active)"))
    }
}

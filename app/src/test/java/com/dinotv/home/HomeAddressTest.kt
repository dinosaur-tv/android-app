package com.dinotv.home
import org.junit.Assert.*
import org.junit.Test
class HomeAddressTest {
    @Test fun normalizesSelfHostedAddress() {
        assertEquals("https://home.example.org/console/", HomeAddress.normalize("https://home.example.org"))
        assertEquals("https://home.example.org:8443/console/", HomeAddress.normalize("https://home.example.org:8443/"))
    }
    @Test fun rejectsUntrustedSchemesAndCredentials() {
        for (url in listOf("http://example.org", "javascript:alert(1)", "file:///etc/passwd",
            "https://user:pass@example.org", "https://example.org/?token=secret", "https://example.org/#token", "https://example.org:0"))
            assertNull(url, HomeAddress.normalize(url))
    }
    @Test fun navigationUsesExactOrigin() {
        val home = "https://home.example.org/console/"
        assertTrue(HomeAddress.sameOrigin(home, "https://home.example.org:443/console/settings"))
        assertFalse(HomeAddress.sameOrigin(home, "https://home.example.org.evil.org/"))
        assertFalse(HomeAddress.sameOrigin(home, "https://home.example.org:8443/"))
        assertFalse(HomeAddress.sameOrigin(home, "https://user@home.example.org/"))
    }
}

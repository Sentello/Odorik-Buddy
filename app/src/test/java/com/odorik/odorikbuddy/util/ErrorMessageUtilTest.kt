package com.odorik.odorikbuddy.util

import com.odorik.odorikbuddy.R
import com.odorik.odorikbuddy.data.repository.AuthenticationException
import com.odorik.odorikbuddy.data.repository.CredentialsNotSetException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMessageUtilTest {

    @Test
    fun `credentials not set maps to auth resource`() {
        assertEquals(R.string.auth_credentials_not_set, ErrorMessageUtil.errorResFor(CredentialsNotSetException()))
    }

    @Test
    fun `authentication failure maps to invalid credentials`() {
        assertEquals(R.string.invalid_credentials, ErrorMessageUtil.errorResFor(AuthenticationException("x")))
    }

    @Test
    fun `dns failure maps to host unresolvable`() {
        assertEquals(R.string.error_host_unresolvable, ErrorMessageUtil.errorResFor(UnknownHostException("odorik.cz")))
    }

    @Test
    fun `timeouts and connect failures map to network unreachable`() {
        assertEquals(R.string.error_network_unreachable, ErrorMessageUtil.errorResFor(SocketTimeoutException()))
        assertEquals(R.string.error_network_unreachable, ErrorMessageUtil.errorResFor(ConnectException()))
        assertEquals(R.string.error_network_unreachable, ErrorMessageUtil.errorResFor(IOException("tls")))
    }

    @Test
    fun `unknown exceptions fall back to null`() {
        assertNull(ErrorMessageUtil.errorResFor(IllegalStateException("boom")))
        assertNull(ErrorMessageUtil.errorResFor(null))
    }
}

package com.m57.hermescontrol.data.remote

import android.content.SharedPreferences
import com.m57.hermescontrol.data.config.ConnectionProfile
import com.m57.hermescontrol.data.config.ServerStore
import com.m57.hermescontrol.data.config.ServerStoreState
import com.m57.hermescontrol.data.local.AuthManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NativeAuthStorageTest {
    private val values = mutableMapOf<String, String?>()
    private var profile = "first"
    private var endpoint = ServerEndpoint.parse("https://first.example/prefix/")
    private var storeState = ServerStoreState(baseUrl = "https://first.example/prefix/")
    private val session = NativeAuthSession("access", "refresh", 2000000000, "nous", "https://first.example/prefix/")

    @Before
    fun setup() {
        AuthManager.resetAuthStateForTest()
        val prefs = mockk<SharedPreferences>(relaxed = true)
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { prefs.getString(any(), any()) } answers { values[firstArg()] }
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers {
            values[firstArg()] = secondArg()
            editor
        }
        every { editor.remove(any()) } answers {
            values.remove(firstArg<String>())
            editor
        }
        AuthManager::class.java.getDeclaredField("prefsDeferred").apply { isAccessible = true }
            .set(AuthManager, CompletableDeferred(prefs))
        mockkObject(AuthManager)
        val store = mockk<ServerStore>(relaxed = true)
        every { store.getLatestState() } answers { storeState }
        every { store.update(any()) } answers {
            storeState = firstArg<(ServerStoreState) -> ServerStoreState>().invoke(storeState)
        }
        every { AuthManager.serverStore } returns store
        every { AuthManager.getBaseUrl() } answers { endpoint.baseUrl.toString() }
        every { AuthManager.getSelectedProfileId() } answers { profile }
        every { AuthManager.endpointForBuild() } answers { endpoint }
    }

    @After
    fun teardown() {
        unmockkAll()
        AuthManager.resetAuthStateForTest()
    }

    @Test
    fun persistedSessionsAreIsolatedByProfileAndEndpoint() {
        AuthManager.setNativeSession(session)
        assertTrue(AuthManager.getNativeSession()!!.accessToken == "access")
        profile = "second"
        assertNull(AuthManager.getNativeSession())
        profile = "first"
        endpoint = ServerEndpoint.parse("https://different.example/")
        assertNull(AuthManager.getNativeSession())
    }

    @Test
    fun logoutRejectsLateRefreshEvenIfSameCredentialsAreInstalledAgain() {
        AuthManager.setNativeSession(session)
        val revision = AuthManager.getNativeAuthRevision()
        AuthManager.setProfileToken("first", null)
        assertNull(AuthManager.getNativeSession())
        assertFalse(AuthManager.replaceNativeSession("first", session, session, revision))
        AuthManager.setNativeSession(session)
        assertFalse(AuthManager.replaceNativeSession("first", session, session, revision))
    }

    @Test
    fun profileSwitchRejectsLateRefresh() {
        AuthManager.setNativeSession(session)
        val revision = AuthManager.getNativeAuthRevision()
        profile = "second"
        assertFalse(AuthManager.replaceNativeSession("first", session, null, revision))
        profile = "first"
        assertTrue(AuthManager.getNativeSession()!!.refreshToken == "refresh")
    }

    @Test
    fun endpointAndProfileRoundTripsInvalidatePendingSignInRevision() {
        val start = AuthManager.getNativeAuthRevision()
        AuthManager.setBaseUrl("https://different.example/")
        assertTrue(AuthManager.getNativeAuthRevision() > start)
        assertFalse(AuthManager.completeNativeSignIn("first", start, session))
        val beforeSwitch = AuthManager.getNativeAuthRevision()
        AuthManager.setSelectedProfileId("second")
        profile = "second"
        AuthManager.setSelectedProfileId("first")
        profile = "first"
        assertTrue(AuthManager.getNativeAuthRevision() > beforeSwitch)
        assertFalse(AuthManager.completeNativeSignIn("first", beforeSwitch, session))
    }

    @Test
    fun hostedAndLoopbackProfilesRestoreTheirOwnWebSocketModes() {
        AuthManager.setWsAuthParam("ticket")
        profile = "second"
        AuthManager.setWsAuthParam("token")
        AuthManager.setSelectedProfileId("first")
        profile = "first"
        assertTrue(AuthManager.isGatedMode())
        assertTrue(storeState.wsAuthParam == "ticket")
        AuthManager.setSelectedProfileId("second")
        profile = "second"
        assertFalse(AuthManager.isGatedMode())
        assertTrue(storeState.wsAuthParam == "token")
    }

    @Test
    fun legacyModeFallsBackAndDeletedProfilesLoseStoredMode() {
        storeState = storeState.copy(wsAuthParam = "ticket")
        assertTrue(AuthManager.isGatedMode())
        AuthManager.setWsAuthParam("ticket")
        storeState = storeState.copy(connectionProfiles = listOf(ConnectionProfile("first", "Hosted")))
        assertTrue(values.containsKey("ws_auth_first"))
        AuthManager.saveConnectionProfiles(emptyList())
        assertFalse(values.containsKey("ws_auth_first"))
    }
}

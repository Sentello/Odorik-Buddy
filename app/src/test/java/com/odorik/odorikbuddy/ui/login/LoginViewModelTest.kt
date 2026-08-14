package com.odorik.odorikbuddy.ui.login

import android.content.Context
import com.odorik.odorikbuddy.data.repository.AuthenticationException
import com.odorik.odorikbuddy.data.repository.UserRepository
import com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val context: Context = mock {
        on { getString(any()) } doReturn "error-message"
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(userRepository: UserRepository, credit: GetCreditUseCase) =
        LoginViewModel(userRepository, credit, context)

    @Test
    fun `blank credentials error immediately without touching the repository`() = runTest(dispatcher) {
        val userRepository: UserRepository = mock()
        val credit: GetCreditUseCase = mock()
        val vm = viewModel(userRepository, credit)

        vm.onLoginClick("", "secret", remember = true)
        advanceUntilIdle()

        assertTrue(vm.loginUiState.value is LoginUiState.Error)
        verify(userRepository, never()).setSessionCredentials(any(), any())
    }

    @Test
    fun `successful login with remember persists credentials`() = runTest(dispatcher) {
        val userRepository: UserRepository = mock()
        val credit: GetCreditUseCase = mock {
            onBlocking { execute() } doReturn Result.success(100.0)
        }
        val vm = viewModel(userRepository, credit)

        vm.onLoginClick("user", "secret", remember = true)
        advanceUntilIdle()

        assertTrue(vm.loginUiState.value is LoginUiState.Success)
        verify(userRepository).setSessionCredentials("user", "secret")
        verify(userRepository).persistCredentials("user", "secret")
    }

    @Test
    fun `successful login without remember clears persisted credentials`() = runTest(dispatcher) {
        val userRepository: UserRepository = mock()
        val credit: GetCreditUseCase = mock {
            onBlocking { execute() } doReturn Result.success(100.0)
        }
        val vm = viewModel(userRepository, credit)

        vm.onLoginClick("user", "secret", remember = false)
        advanceUntilIdle()

        assertTrue(vm.loginUiState.value is LoginUiState.Success)
        verify(userRepository).clearPersistedCredentials()
        verify(userRepository, never()).persistCredentials(any(), any())
    }

    @Test
    fun `authentication failure clears all credentials and shows error`() = runTest(dispatcher) {
        val userRepository: UserRepository = mock()
        val credit: GetCreditUseCase = mock {
            onBlocking { execute() } doReturn Result.failure(AuthenticationException("bad"))
        }
        val vm = viewModel(userRepository, credit)

        vm.onLoginClick("user", "wrong", remember = true)
        advanceUntilIdle()

        assertTrue(vm.loginUiState.value is LoginUiState.Error)
        verify(userRepository).clearCredentials()
        verify(userRepository, never()).persistCredentials(any(), any())
    }

    @Test
    fun `editing a field resets an error state to idle`() = runTest(dispatcher) {
        val userRepository: UserRepository = mock()
        val credit: GetCreditUseCase = mock()
        val vm = viewModel(userRepository, credit)

        vm.onLoginClick("", "", remember = true)
        advanceUntilIdle()
        assertTrue(vm.loginUiState.value is LoginUiState.Error)

        vm.onUserIdChanged()
        assertTrue(vm.loginUiState.value is LoginUiState.Idle)
    }
}

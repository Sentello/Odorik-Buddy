package com.odorik.odorikbuddy.ui.login;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B!\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0001\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u001e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u0016R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/odorik/odorikbuddy/ui/login/LoginViewModel;", "Landroidx/lifecycle/ViewModel;", "userRepository", "Lcom/odorik/odorikbuddy/data/repository/UserRepository;", "getCreditUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetCreditUseCase;", "context", "Landroid/content/Context;", "(Lcom/odorik/odorikbuddy/data/repository/UserRepository;Lcom/odorik/odorikbuddy/domain/usecase/GetCreditUseCase;Landroid/content/Context;)V", "_loginUiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/odorik/odorikbuddy/ui/login/LoginUiState;", "loginUiState", "Lkotlinx/coroutines/flow/StateFlow;", "getLoginUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "onLoginClick", "", "userId", "", "password", "remember", "", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LoginViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase getCreditUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.odorik.odorikbuddy.ui.login.LoginUiState> _loginUiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.odorik.odorikbuddy.ui.login.LoginUiState> loginUiState = null;
    
    @javax.inject.Inject()
    public LoginViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.repository.UserRepository userRepository, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase getCreditUseCase, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.odorik.odorikbuddy.ui.login.LoginUiState> getLoginUiState() {
        return null;
    }
    
    public final void onLoginClick(@org.jetbrains.annotations.NotNull()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String password, boolean remember) {
    }
}
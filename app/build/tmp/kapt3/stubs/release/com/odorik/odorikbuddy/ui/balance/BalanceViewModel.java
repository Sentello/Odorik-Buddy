package com.odorik.odorikbuddy.ui.balance;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u0011\u001a\u00020\u0012R\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0019\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/odorik/odorikbuddy/ui/balance/BalanceViewModel;", "Landroidx/lifecycle/ViewModel;", "odorikApi", "Lcom/odorik/odorikbuddy/data/remote/OdorikApi;", "secureStorage", "Lcom/odorik/odorikbuddy/data/local/SecureStorage;", "(Lcom/odorik/odorikbuddy/data/remote/OdorikApi;Lcom/odorik/odorikbuddy/data/local/SecureStorage;)V", "_balance", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_error", "balance", "Lkotlinx/coroutines/flow/StateFlow;", "getBalance", "()Lkotlinx/coroutines/flow/StateFlow;", "error", "getError", "fetchBalance", "Lkotlinx/coroutines/Job;", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BalanceViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.remote.OdorikApi odorikApi = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.local.SecureStorage secureStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _balance = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> balance = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> error = null;
    
    @javax.inject.Inject()
    public BalanceViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.remote.OdorikApi odorikApi, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.SecureStorage secureStorage) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getBalance() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job fetchBalance() {
        return null;
    }
}
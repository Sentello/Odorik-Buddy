package com.odorik.odorikbuddy.ui.dashboard;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u000e\u001a\u00020\u0012H\u0002J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\u0006\u0010\u0013\u001a\u00020\u0012R\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000f\u00a8\u0006\u0014"}, d2 = {"Lcom/odorik/odorikbuddy/ui/dashboard/DashboardViewModel;", "Landroidx/lifecycle/ViewModel;", "getCreditUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetCreditUseCase;", "getUserInfoUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetUserInfoUseCase;", "(Lcom/odorik/odorikbuddy/domain/usecase/GetCreditUseCase;Lcom/odorik/odorikbuddy/domain/usecase/GetUserInfoUseCase;)V", "_credit", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_userInfo", "Lcom/odorik/odorikbuddy/data/model/UserInfo;", "credit", "Lkotlinx/coroutines/flow/StateFlow;", "getCredit", "()Lkotlinx/coroutines/flow/StateFlow;", "userInfo", "getUserInfo", "", "loadData", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class DashboardViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase getCreditUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase getUserInfoUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Double> _credit = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Double> credit = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.odorik.odorikbuddy.data.model.UserInfo> _userInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.odorik.odorikbuddy.data.model.UserInfo> userInfo = null;
    
    @javax.inject.Inject()
    public DashboardViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase getCreditUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase getUserInfoUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Double> getCredit() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.odorik.odorikbuddy.data.model.UserInfo> getUserInfo() {
        return null;
    }
    
    public final void loadData() {
    }
    
    private final void getCredit() {
    }
    
    private final void getUserInfo() {
    }
}
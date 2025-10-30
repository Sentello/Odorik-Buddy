package com.odorik.odorikbuddy.ui.dashboard;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B1\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0001\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0016\u0010&\u001a\u00020\'2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020)0\u0017H\u0002J\u0016\u0010*\u001a\u00020\'2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020)0\u0017H\u0002J\u0016\u0010+\u001a\u00020\'2\f\u0010(\u001a\b\u0012\u0004\u0012\u00020)0\u0017H\u0002J\b\u0010,\u001a\u00020\'H\u0002J\b\u0010\u001a\u001a\u00020\'H\u0002J\b\u0010#\u001a\u00020\'H\u0002J\u0018\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\u00112\u0006\u00100\u001a\u000201H\u0002J\u0018\u00102\u001a\u00020.2\u0006\u0010/\u001a\u00020\u00112\u0006\u00100\u001a\u000201H\u0002J\u0006\u00103\u001a\u00020\'J\u0010\u00104\u001a\u0002052\u0006\u0010/\u001a\u00020\u0011H\u0002R\u0016\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00150\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00170\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0019\u0010\u001c\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001bR\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u000f0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001bR\u0019\u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00150\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001bR\u001d\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00170\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001b\u00a8\u00066"}, d2 = {"Lcom/odorik/odorikbuddy/ui/dashboard/DashboardViewModel;", "Landroidx/lifecycle/ViewModel;", "getCreditUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetCreditUseCase;", "getUserInfoUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetUserInfoUseCase;", "historyRepository", "Lcom/odorik/odorikbuddy/data/repository/HistoryRepository;", "securePreferences", "Lcom/odorik/odorikbuddy/data/local/SecurePreferences;", "context", "Landroid/content/Context;", "(Lcom/odorik/odorikbuddy/domain/usecase/GetCreditUseCase;Lcom/odorik/odorikbuddy/domain/usecase/GetUserInfoUseCase;Lcom/odorik/odorikbuddy/data/repository/HistoryRepository;Lcom/odorik/odorikbuddy/data/local/SecurePreferences;Landroid/content/Context;)V", "_credit", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_error", "", "_thisMonthsSpending", "_todaysSpending", "_userInfo", "Lcom/odorik/odorikbuddy/data/model/UserInfo;", "_weeklySpending", "", "credit", "Lkotlinx/coroutines/flow/StateFlow;", "getCredit", "()Lkotlinx/coroutines/flow/StateFlow;", "error", "getError", "thisMonthsSpending", "getThisMonthsSpending", "todaysSpending", "getTodaysSpending", "userInfo", "getUserInfo", "weeklySpending", "getWeeklySpending", "calculateThisMonthsSpending", "", "history", "Lcom/odorik/odorikbuddy/model/HistoryItem;", "calculateTodaysSpending", "calculateWeeklySpending", "fetchSpendingData", "isSameDay", "", "isoDate", "calendar", "Ljava/util/Calendar;", "isSameMonth", "loadData", "parseIsoDate", "Ljava/util/Date;", "app_release"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class DashboardViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase getCreditUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase getUserInfoUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.repository.HistoryRepository historyRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Double> _credit = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Double> credit = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.odorik.odorikbuddy.data.model.UserInfo> _userInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.odorik.odorikbuddy.data.model.UserInfo> userInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Double> _todaysSpending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Double> todaysSpending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Double> _thisMonthsSpending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Double> thisMonthsSpending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<java.lang.Double>> _weeklySpending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Double>> weeklySpending = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> error = null;
    
    @javax.inject.Inject()
    public DashboardViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetCreditUseCase getCreditUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetUserInfoUseCase getUserInfoUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.repository.HistoryRepository historyRepository, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
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
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Double> getTodaysSpending() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Double> getThisMonthsSpending() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.Double>> getWeeklySpending() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getError() {
        return null;
    }
    
    public final void loadData() {
    }
    
    private final void getCredit() {
    }
    
    private final void getUserInfo() {
    }
    
    private final void fetchSpendingData() {
    }
    
    private final void calculateTodaysSpending(java.util.List<com.odorik.odorikbuddy.model.HistoryItem> history) {
    }
    
    private final void calculateThisMonthsSpending(java.util.List<com.odorik.odorikbuddy.model.HistoryItem> history) {
    }
    
    private final void calculateWeeklySpending(java.util.List<com.odorik.odorikbuddy.model.HistoryItem> history) {
    }
    
    private final boolean isSameDay(java.lang.String isoDate, java.util.Calendar calendar) {
        return false;
    }
    
    private final boolean isSameMonth(java.lang.String isoDate, java.util.Calendar calendar) {
        return false;
    }
    
    private final java.util.Date parseIsoDate(java.lang.String isoDate) {
        return null;
    }
}
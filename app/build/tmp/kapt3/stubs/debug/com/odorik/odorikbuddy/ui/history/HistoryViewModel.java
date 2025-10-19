package com.odorik.odorikbuddy.ui.history;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u000f\u001a\u00020\u0010H\u0002R\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/odorik/odorikbuddy/ui/history/HistoryViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/odorik/odorikbuddy/data/repository/HistoryRepository;", "securePreferences", "Lcom/odorik/odorikbuddy/data/local/SecurePreferences;", "(Lcom/odorik/odorikbuddy/data/repository/HistoryRepository;Lcom/odorik/odorikbuddy/data/local/SecurePreferences;)V", "_history", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/odorik/odorikbuddy/model/HistoryItem;", "history", "Lkotlinx/coroutines/flow/StateFlow;", "getHistory", "()Lkotlinx/coroutines/flow/StateFlow;", "fetchHistory", "", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HistoryViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.repository.HistoryRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.odorik.odorikbuddy.model.HistoryItem>> _history = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.model.HistoryItem>> history = null;
    
    @javax.inject.Inject()
    public HistoryViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.repository.HistoryRepository repository, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.SecurePreferences securePreferences) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.model.HistoryItem>> getHistory() {
        return null;
    }
    
    private final void fetchHistory() {
    }
}
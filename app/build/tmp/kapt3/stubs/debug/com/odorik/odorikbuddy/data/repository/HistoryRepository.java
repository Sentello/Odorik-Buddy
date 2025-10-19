package com.odorik.odorikbuddy.data.repository;

/**
 * Repository for fetching call and SMS history from the Odorik API.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J4\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\t2\u0006\u0010\f\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/odorik/odorikbuddy/data/repository/HistoryRepository;", "", "apiService", "Lcom/odorik/odorikbuddy/data/remote/OdorikApi;", "(Lcom/odorik/odorikbuddy/data/remote/OdorikApi;)V", "getCombinedHistory", "", "Lcom/odorik/odorikbuddy/model/HistoryItem;", "user", "", "pass", "from", "to", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class HistoryRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.remote.OdorikApi apiService = null;
    
    @javax.inject.Inject()
    public HistoryRepository(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.remote.OdorikApi apiService) {
        super();
    }
    
    /**
     * Fetches both call and SMS history, combines them, and sorts them by date.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCombinedHistory(@org.jetbrains.annotations.NotNull()
    java.lang.String user, @org.jetbrains.annotations.NotNull()
    java.lang.String pass, @org.jetbrains.annotations.NotNull()
    java.lang.String from, @org.jetbrains.annotations.NotNull()
    java.lang.String to, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.odorik.odorikbuddy.model.HistoryItem>> $completion) {
        return null;
    }
}
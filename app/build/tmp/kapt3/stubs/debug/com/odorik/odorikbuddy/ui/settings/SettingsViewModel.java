package com.odorik.odorikbuddy.ui.settings;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u0018\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 J\u0006\u0010\u001b\u001a\u00020\u001eJ\u0006\u0010!\u001a\u00020\u001eJ\u000e\u0010\"\u001a\u00020\u001e2\u0006\u0010#\u001a\u00020\u0012R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00120\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0015R\u0019\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u001d\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u000f0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00120\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0019R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/odorik/odorikbuddy/ui/settings/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "getLinesUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetLinesUseCase;", "getLineInfoUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetLineInfoUseCase;", "userRepository", "Lcom/odorik/odorikbuddy/data/repository/UserRepository;", "themeManager", "Lcom/odorik/odorikbuddy/data/local/ThemeManager;", "(Lcom/odorik/odorikbuddy/domain/usecase/GetLinesUseCase;Lcom/odorik/odorikbuddy/domain/usecase/GetLineInfoUseCase;Lcom/odorik/odorikbuddy/data/repository/UserRepository;Lcom/odorik/odorikbuddy/data/local/ThemeManager;)V", "_lineInfo", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/odorik/odorikbuddy/data/model/LineInfo;", "_lines", "", "Lcom/odorik/odorikbuddy/data/model/Line;", "_logoutEvent", "", "isDarkMode", "Landroidx/compose/runtime/State;", "()Landroidx/compose/runtime/State;", "lineInfo", "Lkotlinx/coroutines/flow/StateFlow;", "getLineInfo", "()Lkotlinx/coroutines/flow/StateFlow;", "lines", "getLines", "logoutEvent", "getLogoutEvent", "", "lineId", "", "logout", "setDarkMode", "enabled", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase getLinesUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetLineInfoUseCase getLineInfoUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.repository.UserRepository userRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.data.local.ThemeManager themeManager = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.State<java.lang.Boolean> isDarkMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.odorik.odorikbuddy.data.model.Line>> _lines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.data.model.Line>> lines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.odorik.odorikbuddy.data.model.LineInfo> _lineInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.odorik.odorikbuddy.data.model.LineInfo> lineInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _logoutEvent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> logoutEvent = null;
    
    @javax.inject.Inject()
    public SettingsViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase getLinesUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetLineInfoUseCase getLineInfoUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.repository.UserRepository userRepository, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.data.local.ThemeManager themeManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.compose.runtime.State<java.lang.Boolean> isDarkMode() {
        return null;
    }
    
    public final void setDarkMode(boolean enabled) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.data.model.Line>> getLines() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.odorik.odorikbuddy.data.model.LineInfo> getLineInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getLogoutEvent() {
        return null;
    }
    
    public final void getLines() {
    }
    
    public final void getLineInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String lineId) {
    }
    
    public final void logout() {
    }
}
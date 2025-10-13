package com.odorik.odorikbuddy.ui.calls;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0006\u0010\u0014\u001a\u00020\u001cJ\u0006\u0010\u001b\u001a\u00020\u001cJ*\u0010\u001d\u001a\u00020\u001c2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\u0012\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u001c0#J\u001e\u0010$\u001a\u00020\u001c2\u0006\u0010%\u001a\u00020\u000e2\u0006\u0010&\u001a\u00020\u000e2\u0006\u0010\'\u001a\u00020\u000eR\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0015R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0015R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u000b0\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0015\u00a8\u0006("}, d2 = {"Lcom/odorik/odorikbuddy/ui/calls/CallViewModel;", "Landroidx/lifecycle/ViewModel;", "getCallListUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetCallListUseCase;", "getLinesUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/GetLinesUseCase;", "callUseCase", "Lcom/odorik/odorikbuddy/domain/usecase/CallUseCase;", "(Lcom/odorik/odorikbuddy/domain/usecase/GetCallListUseCase;Lcom/odorik/odorikbuddy/domain/usecase/GetLinesUseCase;Lcom/odorik/odorikbuddy/domain/usecase/CallUseCase;)V", "_callList", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/odorik/odorikbuddy/data/model/CallInfo;", "_callResult", "", "_error", "_lines", "Lcom/odorik/odorikbuddy/data/model/Line;", "callList", "Lkotlinx/coroutines/flow/StateFlow;", "getCallList", "()Lkotlinx/coroutines/flow/StateFlow;", "callResult", "getCallResult", "error", "getError", "lines", "getLines", "", "handleContactSelection", "contentResolver", "Landroid/content/ContentResolver;", "contactUri", "Landroid/net/Uri;", "onPhoneNumberSelected", "Lkotlin/Function1;", "makeCall", "callerId", "recipient", "line", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class CallViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetCallListUseCase getCallListUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase getLinesUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.odorik.odorikbuddy.domain.usecase.CallUseCase callUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.odorik.odorikbuddy.data.model.CallInfo>> _callList = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.data.model.CallInfo>> callList = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _callResult = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> callResult = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.odorik.odorikbuddy.data.model.Line>> _lines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.data.model.Line>> lines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> error = null;
    
    @javax.inject.Inject()
    public CallViewModel(@org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetCallListUseCase getCallListUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.GetLinesUseCase getLinesUseCase, @org.jetbrains.annotations.NotNull()
    com.odorik.odorikbuddy.domain.usecase.CallUseCase callUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.data.model.CallInfo>> getCallList() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getCallResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.odorik.odorikbuddy.data.model.Line>> getLines() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getError() {
        return null;
    }
    
    public final void getCallList() {
    }
    
    public final void getLines() {
    }
    
    public final void makeCall(@org.jetbrains.annotations.NotNull()
    java.lang.String callerId, @org.jetbrains.annotations.NotNull()
    java.lang.String recipient, @org.jetbrains.annotations.NotNull()
    java.lang.String line) {
    }
    
    public final void handleContactSelection(@org.jetbrains.annotations.NotNull()
    android.content.ContentResolver contentResolver, @org.jetbrains.annotations.NotNull()
    android.net.Uri contactUri, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onPhoneNumberSelected) {
    }
}
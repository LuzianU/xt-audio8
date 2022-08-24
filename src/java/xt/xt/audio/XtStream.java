package xt.audio;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import static xt.audio.Utility.handleAssert;
import static xt.audio.Utility.handleError;

public final class XtStream implements AutoCloseable {

    static { Native.register(Utility.LIBRARY); }
    private static native void XtStreamStop(Pointer s);
    private static native long XtStreamStart(Pointer s);
    private static native void XtStreamDestroy(Pointer s);
    private static native Pointer XtStreamGetHandle(Pointer s);
    private static native boolean XtStreamIsRunning(Pointer s);
    private static native Structs.XtFormat XtStreamGetFormat(Pointer s);
    private static native boolean XtStreamIsInterleaved(Pointer s);
    private static native long XtStreamGetLatency(Pointer s, Structs.XtLatency latency);
    private static native long XtStreamGetFrames(Pointer s, IntByReference frames);

    private Pointer _s;
    private Structs.XtFormat _format;

    private final Object _user;
    private final Structs.XtStreamParams _params;
    private final NativeCallbacks.NativeOnXRun _onNativeXRun;
    private final NativeCallbacks.NativeOnBuffer _onNativeBuffer;
    private final NativeCallbacks.NativeOnRunning _onNativeRunning;
    private final Structs.XtBuffer _buffer = new Structs.XtBuffer();
    private final Structs.XtLatency _latency = new Structs.XtLatency();
    private final IntByReference _frames = new IntByReference();

    public Structs.XtFormat getFormat() { return _format; }
    public void start() { Utility.handleError(XtStreamStart(_s)); }
    public void stop() { Utility.handleAssert(() -> XtStreamStop(_s));}
    public Pointer getHandle() { return Utility.handleAssert(XtStreamGetHandle(_s)); }
    public boolean isRunning() { return Utility.handleAssert(XtStreamIsRunning(_s)); }
    public boolean isInterleaved() { return Utility.handleAssert(XtStreamIsInterleaved(_s)); }
    @Override public void close() { Utility.handleAssert(() -> XtStreamDestroy(_s));_s = Pointer.NULL; }

    NativeCallbacks.NativeOnXRun onNativeXRun() { return _onNativeXRun; }
    NativeCallbacks.NativeOnBuffer onNativeBuffer() { return _onNativeBuffer; }
    NativeCallbacks.NativeOnRunning onNativeRunning() { return _onNativeRunning; }

    XtStream(Structs.XtStreamParams params, Object user) {
        _user = user;
        _params = params;
        boolean stdcall = Platform.isWindows() && !Platform.is64Bit();
        _onNativeXRun = stdcall? (NativeCallbacks.WinX86NativeOnXRun) this::onXRun: (NativeCallbacks.NativeOnXRun)this::onXRun;
        _onNativeBuffer = stdcall? (NativeCallbacks.WinX86NativeOnBuffer) this::onBuffer: (NativeCallbacks.NativeOnBuffer)this::onBuffer;
        _onNativeRunning = stdcall? (NativeCallbacks.WinX86NativeOnRunning) this::onRunning: (NativeCallbacks.NativeOnRunning)this::onRunning;
    }

    void init(Pointer s) {
        _s = s;
        _format = Utility.handleAssert(XtStreamGetFormat(_s));
    }

    public int getFrames() {
        Utility.handleError(XtStreamGetFrames(_s, _frames));
        return _frames.getValue();
    }

    public Structs.XtLatency getLatency() {
        Utility.handleError(XtStreamGetLatency(_s, _latency));
        return _latency;
    }

    private void onXRun(Pointer stream, int index, Pointer user) throws Exception {
        _params.onXRun.callback(this, index, _user);
    }

    private int onBuffer(Pointer stream, Pointer buffer, Pointer user) throws Exception {
        for(int i = 0; i < Native.getNativeSize(Structs.XtBuffer.ByValue.class); i++)
            _buffer.getPointer().setByte(i, buffer.getByte(i));
        _buffer.read();
        return _params.onBuffer.callback(this, _buffer, _user);
    }

    private void onRunning(Pointer stream, boolean running, long error, Object user) throws Exception {
        _params.onRunning.callback(this, running, error, user);
    }
}
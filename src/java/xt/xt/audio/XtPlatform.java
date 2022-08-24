package xt.audio;

import com.sun.jna.FromNativeConverter;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import static xt.audio.Utility.handleAssert;

public final class XtPlatform implements AutoCloseable {

    static { Native.register(Utility.LIBRARY); }
    private static native void XtPlatformDestroy(Pointer p);
    private static native Pointer XtPlatformGetService(Pointer p, Enums.XtSystem system);
    private static native Enums.XtSystem XtPlatformSetupToSystem(Pointer p, Enums.XtSetup setup);
    private static native void XtPlatformGetSystems(Pointer p, int[] buffer, IntByReference size);

    Pointer _p;
    XtPlatform(Pointer p) { _p = p; }

    @Override public void close() { Utility.handleAssert(() -> XtPlatformDestroy(_p));_p = Pointer.NULL; }
    public Enums.XtSystem setupToSystem(Enums.XtSetup setup) { return Utility.handleAssert(XtPlatformSetupToSystem(_p, setup)); }

    public XtService getService(Enums.XtSystem system) {
        Pointer s = Utility.handleAssert(XtPlatformGetService(_p, system));
        return s == Pointer.NULL? null: new XtService(s);
    }

    public Enums.XtSystem[] getSystems() {
        XtTypeMapper mapper = new XtTypeMapper();
        IntByReference size = new IntByReference();
        Utility.handleAssert(() -> XtPlatformGetSystems(_p, null, size));
        int[] result = new int[size.getValue()];
        Utility.handleAssert(() -> XtPlatformGetSystems(_p, result, size));
        FromNativeConverter converter = mapper.getFromNativeConverter(Enums.XtSystem.class);
        return Arrays.stream(result).mapToObj(s -> converter.fromNative(s, null)).toArray(Enums.XtSystem[]::new);
    }
}
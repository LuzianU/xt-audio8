package xt.audio;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.nio.charset.Charset;
import java.util.EnumSet;

import static xt.audio.Utility.handleAssert;
import static xt.audio.Utility.handleError;

public final class XtDeviceList implements AutoCloseable {

    static { Native.register(Utility.LIBRARY); }
    private static native void XtDeviceListDestroy(Pointer l);
    private static native long XtDeviceListGetCount(Pointer l, IntByReference count);
    private static native long XtDeviceListGetId(Pointer l, int index, byte[] buffer, IntByReference size);
    private static native long XtDeviceListGetName(Pointer l, String id, byte[] buffer, IntByReference size);
    private static native long XtDeviceListGetCapabilities(Pointer l, String id, IntByReference capabilities);

    private Pointer _l;
    XtDeviceList(Pointer l) { _l = l; }
    @Override public void close() { Utility.handleAssert(() -> XtDeviceListDestroy(_l));_l = Pointer.NULL; }

    public int getCount() {
        IntByReference count = new IntByReference();
        Utility.handleError(XtDeviceListGetCount(_l, count));
        return count.getValue();
    }

    public EnumSet<Enums.XtDeviceCaps> getCapabilities(String id) {
        IntByReference flags = new IntByReference();
        EnumSet<Enums.XtDeviceCaps> result = EnumSet.noneOf(Enums.XtDeviceCaps.class);
        Utility.handleError(XtDeviceListGetCapabilities(_l, id, flags));
        for(Enums.XtDeviceCaps caps: Enums.XtDeviceCaps.values())
            if((flags.getValue() & caps._flag) != 0)
                result.add(caps);
        return result;
    }

    public String getId(int index) {
        IntByReference size = new IntByReference();
        Utility.handleError(XtDeviceListGetId(_l, index, null, size));
        byte[] buffer = new byte[size.getValue()];
        Utility.handleError(XtDeviceListGetId(_l, index, buffer, size));
        return new String(buffer, 0, size.getValue() - 1, Charset.forName("UTF-8"));
    }

    public String getName(String id) {
        IntByReference size = new IntByReference();
        Utility.handleError(XtDeviceListGetName(_l, id, null, size));
        byte[] buffer = new byte[size.getValue()];
        Utility.handleError(XtDeviceListGetName(_l, id, buffer, size));
        return new String(buffer, 0, size.getValue() - 1, Charset.forName("UTF-8"));
    }
}
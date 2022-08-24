package xt.audio;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.nio.charset.Charset;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import static xt.audio.Utility.handleAssert;
import static xt.audio.Utility.handleError;
import java.util.EnumSet;

public final class XtService {

    static { Native.register(Utility.LIBRARY); }
    private static native int XtServiceGetCapabilities(Pointer s);
    private static native long XtServiceOpenDevice(Pointer s, String id, PointerByReference device);
    private static native long XtServiceOpenDeviceList(Pointer s, int flags, PointerByReference list);
    private static native long XtServiceAggregateStream(Pointer s, NativeStructs.AggregateStreamParams params, Pointer user, PointerByReference stream);
    private static native long XtServiceGetDefaultDeviceId(Pointer s, boolean output, IntByReference valid, byte[] buffer, IntByReference size);

    static byte[] toNative(Structs.XtAggregateDeviceParams params) {
        NativeStructs.AggregateDeviceParams result = new NativeStructs.AggregateDeviceParams();
        result.channels = params.channels;
        result.bufferSize = params.bufferSize;
        result.device = params.device.handle();
        result.write();
        return result.getPointer().getByteArray(0, result.size());
    }

    private final Pointer _s;
    XtService(Pointer s) { _s = s; }

    public XtDevice openDevice(String id) {
        PointerByReference d = new PointerByReference();
        Utility.handleError(XtServiceOpenDevice(_s, id, d));
        return new XtDevice(d.getValue());
    }

    public String getDefaultDeviceId(boolean output) {
        IntByReference size = new IntByReference();
        IntByReference valid = new IntByReference();
        Utility.handleError(XtServiceGetDefaultDeviceId(_s, output, valid, null, size));
        if(valid.getValue() == 0) return null;
        byte[] buffer = new byte[size.getValue()];
        Utility.handleError(XtServiceGetDefaultDeviceId(_s, output, valid, buffer, size));
        if(valid.getValue() == 0) return null;
        return new String(buffer, 0, size.getValue() - 1, Charset.forName("UTF-8"));
    }

    public XtDeviceList openDeviceList(EnumSet<Enums.XtEnumFlags> flags) {
        int flag = 0;
        for(Enums.XtEnumFlags f: flags) flag |= f._flag;
        PointerByReference list = new PointerByReference();
        return Utility.handleError(XtServiceOpenDeviceList(_s, flag, list), new XtDeviceList(list.getValue()));
    }

    public EnumSet<Enums.XtServiceCaps> getCapabilities() {
        EnumSet<Enums.XtServiceCaps> result = EnumSet.noneOf(Enums.XtServiceCaps.class);
        Integer flags = Utility.handleAssert(XtServiceGetCapabilities(_s));
        for(Enums.XtServiceCaps caps: Enums.XtServiceCaps.values())
            if((flags & caps._flag) != 0)
                result.add(caps);
        return result;
    }

    public XtStream aggregateStream(Structs.XtAggregateStreamParams params, Object user) {
        PointerByReference stream = new PointerByReference();
        NativeStructs.AggregateStreamParams native_ = new NativeStructs.AggregateStreamParams();
        int size = Native.getNativeSize(NativeStructs.AggregateDeviceParams.ByValue.class);
        XtStream result = new XtStream(params.stream, user);
        Memory devices = new Memory(params.count * size);
        for(int i = 0; i < params.count; i++)
            devices.write(i * size, toNative(params.devices[i]), 0, size);
        native_.mix = params.mix;
        native_.devices = devices;
        native_.count = params.count;
        native_.stream = new NativeStructs.StreamParams();
        native_.master = params.master.handle();
        native_.stream.onBuffer = result.onNativeBuffer();
        native_.stream.interleaved = params.stream.interleaved;
        native_.stream.onXRun = params.stream.onXRun == null? null: result.onNativeXRun();
        native_.stream.onRunning = params.stream.onRunning == null? null: result.onNativeRunning();
        Utility.handleError(XtServiceAggregateStream(_s, native_, Pointer.NULL, stream));
        result.init(stream.getValue());
        return result;
    }
}
package xt.audio;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

public final class XtSafeBuffer implements AutoCloseable {

    static final Map<XtStream, XtSafeBuffer> _map = new HashMap<>();
    public static XtSafeBuffer register(XtStream stream) {
        XtSafeBuffer result = new XtSafeBuffer(stream);
        _map.put(stream, result);
        return result;
    }

    private final int _inputs;
    private final int _outputs;
    private final Object _input;
    private final Object _output;
    private final XtStream _stream;
    private final Structs.XtFormat _format;
    private final Structs.XtAttributes _attrs;
    private final boolean _interleaved;

    public Object getInput() { return _input; }
    public Object getOutput() { return _output; }
    public void close() { _map.remove(_stream); }
    public static XtSafeBuffer get(XtStream stream) { return _map.get(stream); }

    XtSafeBuffer(XtStream stream) {
        _stream = stream;
        _format = stream.getFormat();
        _inputs = _format.channels.inputs;
        _outputs = _format.channels.outputs;
        _interleaved = stream.isInterleaved();
        _attrs = XtAudio.getSampleAttributes(_format.mix.sample);
        _input = createBuffer(_inputs);
        _output = createBuffer(_outputs);
    }

    Object createBuffer(int channels) {
        /*
            XtSample.UINT8, byte.class,
            XtSample.INT16, short.class,
            XtSample.INT24, byte.class,
            XtSample.INT32, int.class,
            XtSample.FLOAT32, float.class
         */
        //Class<?> type = _types.get(_format.mix.sample);
        Class<?> type = float.class;
        if (_format.mix.sample == Enums.XtSample.UINT8) {
            type = byte.class;
        } else if (_format.mix.sample == Enums.XtSample.INT16) {
            type = short.class;
        } else if (_format.mix.sample == Enums.XtSample.INT24) {
            type = byte.class;
        } else if (_format.mix.sample == Enums.XtSample.INT32) {
            type = int.class;
        } else if (_format.mix.sample == Enums.XtSample.FLOAT32) {
            type = float.class;
        } else
            System.err.println("@createBuffer");
        int elems = _stream.getFrames() * _attrs.count;
        if(_interleaved) return Array.newInstance(type, channels * elems);
        Class<?> channelType = Array.newInstance(type, 0).getClass();
        Object result = Array.newInstance(channelType, channels);
        for(int i = 0; i < channels; i++) Array.set(result, i, Array.newInstance(type, elems));
        return result;
    }

    public void lock(Structs.XtBuffer buffer) {
        if(buffer.input == Pointer.NULL) return;
        if(_interleaved) lockInterleaved(buffer);
        else for(int i = 0; i < _inputs; i++) lockChannel(buffer, i);
    }

    public void unlock(Structs.XtBuffer buffer) {
        if(buffer.output == Pointer.NULL) return;
        if(_interleaved) unlockInterleaved(buffer);
        else for(int i = 0; i < _outputs; i++) unlockChannel(buffer, i);
    }

    void lockInterleaved(Structs.XtBuffer buffer) {
        int elems = _inputs * buffer.frames * _attrs.count;
        fromNative(buffer.input, _input, elems);
    }

    void unlockInterleaved(Structs.XtBuffer buffer) {
        int elems = _outputs * buffer.frames * _attrs.count;
        toNative(_output, buffer.output, elems);
    }

    void lockChannel(Structs.XtBuffer buffer, int channel) {
        int elems = buffer.frames * _attrs.count;
        Pointer channelBuffer = buffer.input.getPointer(channel * Native.POINTER_SIZE);
        fromNative(channelBuffer, Array.get(_input, channel), elems);
    }

    void unlockChannel(Structs.XtBuffer buffer, int channel) {
        int elems = buffer.frames * _attrs.count;
        Pointer channelBuffer = buffer.output.getPointer(channel * Native.POINTER_SIZE);
        toNative(Array.get(_output, channel), channelBuffer, elems);
    }

    void toNative(Object source, Pointer dest, int count) {
        switch(_format.mix.sample) {
        case UINT8: dest.write(0, (byte[])source, 0, count); break;
        case INT16: dest.write(0, (short[])source, 0, count); break;
        case INT24: dest.write(0, (byte[])source, 0, count); break;
        case INT32: dest.write(0, (int[])source, 0, count); break;
        case FLOAT32: dest.write(0, (float[])source, 0, count); break;
        default: throw new IllegalArgumentException();
        }
    }

    void fromNative(Pointer source, Object dest, int count) {
        switch(_format.mix.sample) {
        case UINT8: source.read(0, (byte[])dest, 0, count); break;
        case INT16: source.read(0, (short[])dest, 0, count); break;
        case INT24: source.read(0, (byte[])dest, 0, count); break;
        case INT32: source.read(0, (int[])dest, 0, count); break;
        case FLOAT32: source.read(0, (float[])dest, 0, count); break;
        default: throw new IllegalArgumentException();
        }
    }
}
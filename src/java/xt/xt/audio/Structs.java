package xt.audio;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.TypeMapper;
import xt.audio.Callbacks.XtOnBuffer;
import xt.audio.Callbacks.XtOnRunning;
import xt.audio.Callbacks.XtOnXRun;
import xt.audio.Enums.XtCause;
import xt.audio.Enums.XtSample;
import xt.audio.Enums.XtSystem;
import static xt.audio.Utility.XtPrintErrorInfo;
import java.util.Arrays;
import java.util.List;

public interface Structs {

    public static class XtMix extends Structure {
        public XtMix() { }
        public int rate;
        public XtSample sample;
        public static final TypeMapper TYPE_MAPPER = new XtTypeMapper();
        public XtMix(int rate, XtSample sample) { this.rate = rate; this.sample = sample; }
        @Override protected List getFieldOrder() { return Arrays.asList("rate", "sample"); }
    }

    public static class XtStreamParams {
        public boolean interleaved;
        public XtOnBuffer onBuffer;
        public XtOnXRun onXRun;
        public XtOnRunning onRunning;
        public XtStreamParams() {}
        public XtStreamParams(boolean interleaved, XtOnBuffer onBuffer, XtOnXRun onXRun, XtOnRunning onRunning) {
            this.interleaved = interleaved; this.onBuffer = onBuffer; this.onXRun = onXRun; this.onRunning = onRunning;
        }
    }

    public static class XtVersion extends Structure {
        public int major;
        public int minor;
        public static class ByValue extends XtVersion implements Structure.ByValue {}
        @Override protected List getFieldOrder() { return Arrays.asList("major", "minor"); }
    }

    public static class XtLatency extends Structure {
        public double input;
        public double output;
        @Override protected List getFieldOrder() { return Arrays.asList("input", "output"); }
    }

    public static class XtDeviceStreamParams {
        public XtStreamParams stream;
        public XtFormat format;
        public double bufferSize;
        public XtDeviceStreamParams() {}
        public XtDeviceStreamParams(XtStreamParams stream, XtFormat format, double bufferSize) {
            this.stream = stream; this.format = format; this.bufferSize = bufferSize;
        }
    }

    public static class XtAggregateDeviceParams {
        public XtDevice device;
        public XtChannels channels;
        public double bufferSize;
        public XtAggregateDeviceParams() {}
        public XtAggregateDeviceParams(XtDevice device, XtChannels channels, double bufferSize) {
            this.device = device; this.channels = channels; this.bufferSize = bufferSize;
        }
    }

    public static class XtBufferSize extends Structure {
        public double min;
        public double max;
        public double current;
        @Override protected List getFieldOrder() { return Arrays.asList("min", "max", "current"); }
    }

    public static class XtFormat extends Structure {
        public XtFormat() { }
        public XtMix mix = new XtMix();
        public XtChannels channels = new XtChannels();
        @Override protected List getFieldOrder() { return Arrays.asList("mix", "channels"); }
        public XtFormat(XtMix mix, XtChannels channels) { this.mix = mix; this.channels = channels; }
    }

    public static class XtBuffer extends Structure {
        public Pointer input;
        public Pointer output;
        public double time;
        public long position;
        public int frames;
        public boolean timeValid;
        public static class ByValue extends XtBuffer implements Structure.ByValue {}
        @Override protected List getFieldOrder() {
            return Arrays.asList("input", "output", "time", "position", "frames", "timeValid");
        }
    }

    public static class XtServiceError extends Structure {
        public XtCause cause;
        public String text;
        public static final TypeMapper TYPE_MAPPER = new XtTypeMapper();
        @Override protected List getFieldOrder() { return Arrays.asList("cause", "text"); }
    }

    public static class XtErrorInfo extends Structure {
        public int fault;
        public XtSystem system;
        public XtServiceError service;
        public static final TypeMapper TYPE_MAPPER = new XtTypeMapper();
        @Override public String toString() { return XtPrintErrorInfo(this); }
        public static class ByValue extends XtErrorInfo implements Structure.ByValue {}
        @Override protected List getFieldOrder() { return Arrays.asList("fault", "system", "service"); }
    }

    public static class XtAttributes extends Structure {
        public int size;
        public int count;
        public boolean isFloat;
        public boolean isSigned;
        public static class ByValue extends XtAttributes implements Structure.ByValue {}
        @Override protected List getFieldOrder() { return Arrays.asList("size", "count", "isFloat", "isSigned"); }
    }

    public static class XtChannels extends Structure {
        public int inputs;
        public long inMask;
        public int outputs;
        public long outMask;
        public XtChannels() { }
        public static class ByValue extends XtChannels implements Structure.ByValue {}
        @Override protected List getFieldOrder() { return Arrays.asList("inputs", "inMask", "outputs", "outMask"); }
        public XtChannels(int inputs, long inMask, int outputs, long outMask) {
            this.inputs = inputs; this.inMask = inMask; this.outputs = outputs; this.outMask = outMask;
        }
    }

    public static class XtAggregateStreamParams {
        public XtStreamParams stream;
        public XtAggregateDeviceParams[] devices;
        public int count;
        public XtMix mix;
        public XtDevice master;
        public XtAggregateStreamParams() {}
        public XtAggregateStreamParams(XtStreamParams stream, XtAggregateDeviceParams[] devices, int count, XtMix mix, XtDevice master) {
            this.stream = stream; this.devices = devices; this.count = count; this.mix = mix; this.master = master;
        }
    }
}
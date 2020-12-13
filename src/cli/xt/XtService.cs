using System;
using System.Linq;
using System.Runtime.InteropServices;
using System.Security;
using System.Text;
using static Xt.Utility;

namespace Xt
{
    [SuppressUnmanagedCodeSecurity]
    public sealed class XtService
    {
        [DllImport("xt-core")] static extern XtCapabilities XtServiceGetCapabilities(IntPtr s);
        [DllImport("xt-core")] static extern ulong XtServiceOpenDevice(IntPtr s, int index, out IntPtr device);
        [DllImport("xt-core")] static extern ulong XtServiceOpenDeviceList(IntPtr s, XtEnumFlags flags, out IntPtr list);
        [DllImport("xt-core")] static extern ulong XtServiceAggregateStream(IntPtr s, in AggregateStreamParams @params, IntPtr user, out IntPtr stream);
        [DllImport("xt-core")] static extern ulong XtServiceGetDefaultDeviceId(IntPtr s, bool output, out bool valid, [Out] byte[] buffer, ref int size);

        readonly IntPtr _s;
        internal XtService(IntPtr s) => _s = s;

        public XtCapabilities GetCapabilities() => XtServiceGetCapabilities(_s);
        public XtDevice OpenDevice(int index) => HandleError(XtServiceOpenDevice(_s, index, out var r), new XtDevice(r));
        public XtDeviceList OpenDeviceList(XtEnumFlags flags) => HandleError(XtServiceOpenDeviceList(_s, flags, out var r), new XtDeviceList(r));

        static AggregateDeviceParams ToNative(XtAggregateDeviceParams managed)
        {
            var result = new AggregateDeviceParams();
            result.channels = managed.channels;
            result.bufferSize = managed.bufferSize;
            result.device = managed.device.Handle();
            return result;
        }

        public string GetDefaultDeviceId(bool output)
        {
            bool valid;
            int size = 0;
            HandleError(XtServiceGetDefaultDeviceId(_s, output, out valid, null, ref size));
            if(!valid) return null;
            var buffer = new byte[size];
            HandleError(XtServiceGetDefaultDeviceId(_s, output, out valid, buffer, ref size));
            if(!valid) return null;
            return Encoding.UTF8.GetString(buffer, 0, size - 1);
        }

        public unsafe XtStream AggregateStream(in XtAggregateStreamParams @params, object user)
        {
            var result = new XtStream(@params.stream.onBuffer, @params.stream.onXRun, user);
            var native = new AggregateStreamParams();
            var devices = @params.devices.Select(ToNative).ToArray();
            fixed (AggregateDeviceParams* devs = devices)
            {
                native.mix = @params.mix;
                native.count = @params.count;
                native.devices = new IntPtr(devs);
                native.master = @params.master.Handle();
                native.stream.onBuffer = result.OnNativeBuffer();
                native.stream.interleaved = @params.stream.interleaved ? 1 : 0;
                native.stream.onXRun = @params.stream.onXRun == null ? null : result.OnNativeXRun();
                result.Init(HandleError(XtServiceAggregateStream(_s, in native, IntPtr.Zero, out var r), r));
                return result;
            }
        }
    }
}
using System;
using System.Threading;

namespace Xt
{
    public class Aggregate
    {
        static void XRun(int index, object user)
        => Console.WriteLine("XRun on device " + index + ".");

        static void OnAggregate(XtStream stream, in XtBuffer buffer, object user)
        {
            XtSafeBuffer safe = XtSafeBuffer.Get(stream);
            safe.Lock(buffer);
            XtFormat format = stream.GetFormat();
            XtAttributes attrs = XtAudio.GetSampleAttributes(format.mix.sample);
            int bytes = buffer.frames * stream.GetFormat().channels.inputs * attrs.size;
            Buffer.BlockCopy(safe.GetInput(), 0, safe.GetOutput(), 0, bytes);
            safe.Unlock(buffer);
        }

        public static void Main()
        {
            XtMix mix = new XtMix(48000, XtSample.Int16);
            XtFormat inputFormat = new XtFormat(mix, new XtChannels(2, 0, 0, 0));
            XtFormat outputFormat = new XtFormat(mix, new XtChannels(0, 0, 2, 0));

            using XtAudio audio = new XtAudio(null, IntPtr.Zero, null);
            XtSystem system = XtAudio.SetupToSystem(XtSetup.SystemAudio);
            XtService service = XtAudio.GetService(system);
            if (service == null) return;

            using XtDevice input = service.OpenDefaultDevice(false);
            using XtDevice output = service.OpenDefaultDevice(true);
            if (input?.SupportsFormat(inputFormat) != true) return;
            if (output?.SupportsFormat(outputFormat) != true) return;

            using XtStream stream = service.AggregateStream(
                    new XtDevice[] { input, output },
                    new XtChannels[] { inputFormat.channels, outputFormat.channels },
                    new double[] { 30.0, 30.0 },
                    2, mix, true, output, OnAggregate, XRun, null);
            using XtSafeBuffer safe = XtSafeBuffer.Register(stream, true);
            stream.Start();
            Thread.Sleep(2000);
            stream.Stop();
        }
    }
}
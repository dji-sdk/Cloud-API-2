using System;
using FlightHubSdk;
using Sdk = FlightHubSdk.FlightHubSdk;

if (!OperatingSystem.IsLinux() || System.Runtime.InteropServices.RuntimeInformation.ProcessArchitecture
    != System.Runtime.InteropServices.Architecture.X64)
    throw new PlatformNotSupportedException("flight_hub_sdk requires Linux x64.");

Console.WriteLine($"Native SDK: {Sdk.getVersion()}");
if (string.IsNullOrWhiteSpace(Sdk.getVersion()))
    throw new Exception("Native version is empty.");
// Exercise non-ASCII roundtrips and LP64 long across the real PInvoke boundary.
using var config = new FlightHubSdkConfig();
config.address = "https://测试.example";
if (config.address != "https://测试.example") throw new Exception("UTF-8 roundtrip failed.");
using var values = new LongLongVector();
values.Add(5_000_000_000L);
if (values[0] != 5_000_000_000L) throw new Exception("64-bit integer roundtrip failed.");
using var task = new FlightTask();
task.run_at = 5_000_000_000L;
if (task.run_at != 5_000_000_000L) throw new Exception("Linux C++ long was truncated.");
using var callback = new DeviceCallbackProbe();
using var devices = new DeviceTopoVector();
using var error = new FlightHubError();
error.error_code = FlightHubSdkErrorCode.SUCCESS;
GC.Collect();
GC.WaitForPendingFinalizers();
// Calling the generated base method crosses PInvoke and invokes the C++ virtual
// method, which returns through the director delegate to the managed override.
callback.InvokeThroughNative(devices, error);
if (callback.Calls != 1) throw new Exception("Native callback director failed.");
GC.KeepAlive(callback);
Console.WriteLine("Native loading, UTF-8, 64-bit values and callback director passed.");

sealed class DeviceCallbackProbe : DeviceListCallback
{
    public int Calls { get; private set; }

    public void InvokeThroughNative(DeviceTopoVector devices, FlightHubError error)
        => base.onDeviceList(devices, error);

    public override void onDeviceList(DeviceTopoVector devices, FlightHubError error)
    {
        // Never let an exception escape into C++; report state after returning.
        using (error)
        using (devices)
            if (devices.Count == 0 && error.error_code == FlightHubSdkErrorCode.SUCCESS)
                Calls++;
    }
}

using System;
using System.Runtime.CompilerServices;
using FlightHubSdk;

static void Check(bool condition, string message)
{
    if (!condition) throw new InvalidOperationException(message);
}

static void Collect()
{
    GC.Collect();
    GC.WaitForPendingFinalizers();
    GC.Collect();
}

[MethodImpl(MethodImplOptions.NoInlining)]
static WeakReference Register(Action action)
{
    using var probe = new Probe(action);
    Check(callback_probe.registerProbe("accept", probe) == 1, "Registration failed.");
    Check(probe.Disposals == 0, "Registration invoked the consumer's Dispose override.");
    return new WeakReference(probe);
}

int calls = 0;
var retained = Register(() => calls++);
Collect();
Check(retained.IsAlive && callback_probe.destroyedProbes() == 0, "Disposed proxy lost its native callback or delegate.");
callback_probe.fireProbe();
callback_probe.fireProbe();
Check(calls == 2, "Retained callback failed to run repeatedly.");
callback_probe.cancelProbe();
callback_probe.cancelProbe();
Collect();
Check(!retained.IsAlive && callback_probe.destroyedProbes() == 1, "Cancellation did not release exactly one callback.");

using (var rejected = new Probe(() => { }))
    Check(callback_probe.registerProbe("reject", rejected) == -1, "Expected native registration rejection.");
Check(callback_probe.destroyedProbes() == 2, "Rejected callback was not released exactly once.");

using (var invalid = new Probe(() => { }))
{
    try
    {
        callback_probe.registerProbe(null, invalid);
        throw new InvalidOperationException("Expected native argument validation failure.");
    }
    catch (ArgumentNullException) { }
}
Check(callback_probe.destroyedProbes() == 3, "Unadopted callback was not released exactly once.");

var completed = Register(() => calls++);
callback_probe.completeProbe();
callback_probe.completeProbe();
Collect();
Check(calls == 3 && !completed.IsAlive && callback_probe.destroyedProbes() == 4,
    "Completion did not release exactly one callback.");

bool survivedCancellation = false;
Register(() =>
{
    callback_probe.cancelProbe();
    survivedCancellation = callback_probe.destroyedProbes() == 4;
});
callback_probe.fireProbeOnWorker();
Check(survivedCancellation && callback_probe.destroyedProbes() == 5,
    "Cancellation freed an in-flight callback.");

callback_probe.registerProbe("accept", null);
callback_probe.completeProbe();
Check(callback_probe.destroyedProbes() == 5, "Null callback changed ownership.");

using (var reused = new Probe(() => calls++))
{
    callback_probe.registerProbe("accept", reused);
    bool refused = false;
    try { callback_probe.registerProbe("accept", reused); }
    catch (InvalidOperationException) { refused = true; }
    Check(refused, "The same callback was accepted twice.");
    callback_probe.completeProbe();
}
Check(calls == 4 && callback_probe.destroyedProbes() == 6, "Reusing a callback disturbed its first registration.");
using (var synchronous = new Probe(() => calls++))
    Check(callback_probe.registerProbe("sync", synchronous) == 1, "Synchronous registration failed.");
Check(calls == 5 && callback_probe.destroyedProbes() == 7, "Synchronous callback was not released exactly once.");
Console.WriteLine("Callback ownership passed: cancellation, rejection, invalid arguments, completion, synchronous and worker callbacks, duplicate registration and GC.");

sealed class Probe : LifetimeProbeCallback
{
    private readonly Action _action;
    public Probe(Action action) => _action = action;
    public int Disposals { get; private set; }
    public override void onSignal() => _action();
    protected override void Dispose(bool disposing)
    {
        if (disposing) Disposals++;
        base.Dispose(disposing);
    }
}

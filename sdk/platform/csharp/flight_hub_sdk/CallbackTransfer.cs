using System;
using System.Runtime.InteropServices;

namespace FlightHubSdk;

// A transfer keeps the director's managed target alive from the time the proxy
// gives up deletion rights until the last native copy of the callback is gone.
internal sealed class CallbackTransfer : IDisposable
{
    [StructLayout(LayoutKind.Sequential)]
    internal struct Argument
    {
        internal IntPtr Callback;
        internal IntPtr Root;
        internal IntPtr ReleaseRoot;
        internal IntPtr Accepted;
    }

    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    private delegate void ReleaseRootCallback(IntPtr root);

    private static readonly ReleaseRootCallback ReleaseRoot = root => GCHandle.FromIntPtr(root).Free();
    private static readonly IntPtr ReleaseRootPointer = Marshal.GetFunctionPointerForDelegate(ReleaseRoot);

    private readonly Action<HandleRef> _delete;
    private Argument _argument;

    private CallbackTransfer(Action<HandleRef> delete) => _delete = delete;

    internal Argument Value => _argument;

    internal static CallbackTransfer Create<T>(T callback, Func<T, HandleRef> release, Action<HandleRef> delete)
        where T : class
    {
        var transfer = new CallbackTransfer(delete);
        if (callback is null) return transfer;

        try
        {
            transfer._argument.Accepted = Marshal.AllocHGlobal(sizeof(int));
            Marshal.WriteInt32(transfer._argument.Accepted, 0);
            transfer._argument.Root = GCHandle.ToIntPtr(GCHandle.Alloc(callback));
            transfer._argument.ReleaseRoot = ReleaseRootPointer;
            // SWIG locks this proxy during disposal too. Detach the pointer
            // without invoking a user override of Dispose on registration.
            lock (callback)
                transfer._argument.Callback = release(callback).Handle;
            return transfer;
        }
        catch
        {
            transfer.Dispose();
            throw;
        }
    }

    public void Dispose()
    {
        var argument = _argument;
        _argument = default;
        if (argument.Accepted == IntPtr.Zero) return;
        try
        {
            // P/Invoke or an earlier native argument check can fail before adoption.
            if (Marshal.ReadInt32(argument.Accepted) != 0) return;
            try
            {
                if (argument.Callback != IntPtr.Zero)
                    _delete(new HandleRef(null, argument.Callback));
            }
            finally
            {
                if (argument.Root != IntPtr.Zero) ReleaseRoot(argument.Root);
            }
        }
        finally
        {
            Marshal.FreeHGlobal(argument.Accepted);
        }
    }
}

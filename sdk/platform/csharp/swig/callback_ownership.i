%{
#include "callback_ownership.h"
%}

template <class T> class FhManagedCallback;

%define FHSDK_CALLBACK_TYPE(Type, Deleter)
%typemap(cscode) Type %{
  internal static global::System.Runtime.InteropServices.HandleRef ReleaseToSdk(Type callback) {
    if (!callback.swigCMemOwn || callback.swigCPtr.Handle == global::System.IntPtr.Zero)
      throw new global::System.InvalidOperationException("This SDK callback was disposed or already submitted.");
    var pointer = callback.swigCPtr;
    callback.swigCMemOwn = false;
    callback.swigCPtr = default;
    return pointer;
  }
%}
%typemap(ctype) FhManagedCallback<Type> "FhCallbackArgument"
%typemap(imtype) FhManagedCallback<Type> "CallbackTransfer.Argument"
%typemap(cstype) FhManagedCallback<Type> "Type"
%typemap(csin, pre="    using var callbackTransfer = CallbackTransfer.Create($csinput, Type.ReleaseToSdk, $imclassname.Deleter);") FhManagedCallback<Type> "callbackTransfer.Value"
%typemap(in, canthrow=1) FhManagedCallback<Type> {
    try {
        $1 = FhManagedCallback<Type>($input);
    } catch (const std::bad_alloc&) {
        SWIG_CSharpSetPendingException(SWIG_CSharpOutOfMemoryException, "Could not retain SDK callback");
        return $null;
    }
}
%enddef

%define FHSDK_CALLBACK_DIRECTOR(Type)
FHSDK_CALLBACK_TYPE(Type, delete_ ## Type)
%feature("director") Type;
%enddef

#define FHSDK_CALLBACK_PARAMETER(Type) FhManagedCallback<Type> callback
#define FHSDK_SHARE_CALLBACK(Type) auto cb = callback
#define FHSDK_DELETE_CALLBACK() ((void)0)

// The Java interface enables these directors globally rather than naming them.
FHSDK_CALLBACK_DIRECTOR(CloseChargeCallback)
FHSDK_CALLBACK_DIRECTOR(CloseDebugModeCallback)
FHSDK_CALLBACK_DIRECTOR(CloseDockCoverCallback)
FHSDK_CALLBACK_DIRECTOR(CloseDockCoverProgressCallback)
FHSDK_CALLBACK_DIRECTOR(CloseDroneCallback)
FHSDK_CALLBACK_DIRECTOR(ControlAuthResultCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceFormatCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceFormatProgressCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceRebootCallback)
FHSDK_CALLBACK_DIRECTOR(DeviceRebootProgressCallback)
FHSDK_CALLBACK_DIRECTOR(DockDeviceStatusListCallback)
FHSDK_CALLBACK_DIRECTOR(DroneDeviceStatusListCallback)
FHSDK_CALLBACK_DIRECTOR(DroneFormatCallback)
FHSDK_CALLBACK_DIRECTOR(GetInFlightTaskToposCallback)
FHSDK_CALLBACK_DIRECTOR(LiveStreamStartCallback)
FHSDK_CALLBACK_DIRECTOR(OpenChargeCallback)
FHSDK_CALLBACK_DIRECTOR(OpenDebugModeCallback)
FHSDK_CALLBACK_DIRECTOR(OpenDockCoverCallback)
FHSDK_CALLBACK_DIRECTOR(OpenDockCoverProgressCallback)
FHSDK_CALLBACK_DIRECTOR(OpenDroneCallback)
FHSDK_CALLBACK_DIRECTOR(SetRoamingStationSnCallback)
FHSDK_CALLBACK_DIRECTOR(StartRoamingCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDeviceEsimOperatorSwitchProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDeviceFormatProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDeviceRebootProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDeviceSimSlotSwitchProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDockCoverCloseProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDockCoverOpenProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDroneChargeCloseProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDroneChargeOpenProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDroneCloseProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDroneFormatProgressCallback)
FHSDK_CALLBACK_DIRECTOR(SubscribeDroneOpenProgressCallback)
FHSDK_CALLBACK_DIRECTOR(TaskTopoChangedCallback)

%module(directors="1") callback_probe
%include "std_string.i"
%include "callback_ownership.i"
FHSDK_CALLBACK_DIRECTOR(LifetimeProbeCallback)

%{
#include <functional>
#include <thread>
#include <utility>
static std::function<void()> pending_probe;
static int destroyed_probes = 0;
%}

%inline %{
class LifetimeProbeCallback {
public:
    virtual ~LifetimeProbeCallback() { ++destroyed_probes; }
    virtual void onSignal() = 0;
};

int destroyedProbes() { return destroyed_probes; }

// SWIG validates the required string before adopting the last argument.
int registerProbe(const std::string& required, FhManagedCallback<LifetimeProbeCallback> callback) {
    if (required == "reject") return -1;
    if (required == "sync") {
        if (callback) callback->onSignal();
        return 1;
    }
    pending_probe = [callback] { if (callback) callback->onSignal(); };
    return 1;
}

void fireProbe() {
    auto callback = pending_probe;
    if (callback) callback();
}

void fireProbeOnWorker() {
    std::thread([callback = pending_probe] { if (callback) callback(); }).join();
}

void cancelProbe() { pending_probe = {}; }

void completeProbe() {
    auto callback = std::exchange(pending_probe, {});
    if (callback) callback();
}
%}

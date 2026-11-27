#pragma once

#include <memory>

// Keep this layout in sync with CallbackTransfer.Argument.
struct FhCallbackArgument {
    void* callback;
    void* root;
    void (*release_root)(void*);
    int* accepted;
};

template <class T>
class FhManagedCallback {
public:
    FhManagedCallback() = default;

    explicit FhManagedCallback(FhCallbackArgument argument) {
        if (!argument.callback) return;
        *argument.accepted = 1;
        // shared_ptr invokes the deleter even if allocating its control block fails.
        owner_ = std::shared_ptr<T>(static_cast<T*>(argument.callback),
            [root = argument.root, release = argument.release_root](T* callback) {
                delete callback;
                release(root);
            });
    }

    explicit operator bool() const noexcept { return static_cast<bool>(owner_); }
    T* operator->() const noexcept { return owner_.get(); }

private:
    std::shared_ptr<T> owner_;
};

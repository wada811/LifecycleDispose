package com.wada811.lifecycledispose.test

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.wada811.lifecycledispose.disposeOnDestroy
import com.wada811.lifecycledispose.disposeOnLifecycle
import com.wada811.lifecycledispose.disposeOnPause
import com.wada811.lifecycledispose.disposeOnStop
import io.reactivex.disposables.Disposable

enum class DisposeStrategy {
    OnLifecycle {
        override fun dispose(activity: FragmentActivity, disposable: Disposable) {
            disposable.disposeOnLifecycle(activity)
        }

        override fun dispose(fragment: Fragment, disposable: Disposable) {
            disposable.disposeOnLifecycle(fragment)
        }
    },
    OnPause {
        override fun dispose(activity: FragmentActivity, disposable: Disposable) {
            disposable.disposeOnPause(activity)
        }

        override fun dispose(fragment: Fragment, disposable: Disposable) {
            disposable.disposeOnPause(fragment)
        }
    },
    OnStop {
        override fun dispose(activity: FragmentActivity, disposable: Disposable) {
            disposable.disposeOnStop(activity)
        }

        override fun dispose(fragment: Fragment, disposable: Disposable) {
            disposable.disposeOnStop(fragment)
        }
    },
    OnDestroy {
        override fun dispose(activity: FragmentActivity, disposable: Disposable) {
            disposable.disposeOnDestroy(activity)
        }

        override fun dispose(fragment: Fragment, disposable: Disposable) {
            disposable.disposeOnDestroy(fragment)
        }
    },
    ;

    abstract fun dispose(activity: FragmentActivity, disposable: Disposable)
    abstract fun dispose(fragment: Fragment, disposable: Disposable)
}
@file:Suppress("unused")

package com.wada811.lifecycledispose

import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.disposables.Disposable

/**
 * Dispose on corresponding lifecycle event.
 *
 * ```
 * | Subscribe | Lifecycle.State | Dispose   |
 * | --------- | --------------- | --------- |
 * | onCreate  | INITIALIZED     | onDestroy |
 * | onStart   | CREATED         | onStop    |
 * | onResume  | STARTED         | onPause   |
 * | onPause   | STARTED         | onDestroy |
 * | onStop    | CREATED         | onDestroy |
 * | onDestroy | DESTROYED       | onDestroy |
 * ```
 */
@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnLifecycle(activity: FragmentActivity): TDisposable =
    this.also {
        disposeOnLifecycleEvent(activity, when (activity.lifecycle.currentState) {
            INITIALIZED -> ON_DESTROY // onCreate
            CREATED -> ON_STOP // onStart, onStop
            STARTED, RESUMED -> ON_PAUSE // onResume, onPause
            DESTROYED -> ON_DESTROY // onDestroy
        })
    }

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnStop(activity: FragmentActivity): TDisposable = disposeOnLifecycleEvent(activity, ON_STOP)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnPause(activity: FragmentActivity): TDisposable = disposeOnLifecycleEvent(activity, ON_PAUSE)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnDestroy(activity: FragmentActivity): TDisposable = disposeOnLifecycleEvent(activity, ON_DESTROY)


@Keep
private fun <TDisposable : Disposable> TDisposable.disposeOnLifecycleEvent(activity: FragmentActivity, lifecycleEvent: Lifecycle.Event): TDisposable =
    this.also {
        when (activity.lifecycle.currentState) {
            DESTROYED -> dispose()
            else -> disposeOnLifecycleEvents(activity.lifecycle, listOf(lifecycleEvent, ON_DESTROY))
        }
    }

/**
 * Dispose on corresponding lifecycle event.
 *
 * ```
 * | Subscribe     | Lifecycle.State | Dispose       |
 * | ------------- | --------------- | ------------- |
 * | onCreate      | INITIALIZED     | onDestroy     |
 * | onCreateView  | INITIALIZED     | onDestroyView |
 * | onStart       | CREATED         | onStop        |
 * | onResume      | STARTED         | onPause       |
 * | onPause       | STARTED         | onDestroyView |
 * | onStop        | CREATED         | onDestroyView |
 * | onDestroyView | DESTROYED       | onDestroyView |
 * | onDestroy     | DESTROYED       | onDestroy     |
 * ```
 */
@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnLifecycle(fragment: Fragment): TDisposable =
    this.also {
        disposeOnLifecycleEvent(fragment)
    }

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnStop(fragment: Fragment): TDisposable = disposeOnLifecycleEvent(fragment, ON_STOP)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnPause(fragment: Fragment): TDisposable = disposeOnLifecycleEvent(fragment, ON_PAUSE)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnDestroy(fragment: Fragment): TDisposable = disposeOnLifecycleEvent(fragment, ON_DESTROY)

private fun <TDisposable : Disposable> TDisposable.disposeOnLifecycleEvent(fragment: Fragment, lifecycleEvent: Lifecycle.Event? = null): TDisposable =
    this.also {
        if (fragment.view != null) {
            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            when (lifecycle.currentState) {
                // Called from onViewCreated, onActivityCreated, onViewStateRestored
                INITIALIZED -> disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_DESTROY))
                // Called from onStart, onStop
                CREATED -> disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_STOP, ON_DESTROY))
                // Called from onResume, onPause
                STARTED, RESUMED -> disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_PAUSE, ON_DESTROY))
                // Called from onDestroyView
                else -> safeDispose()
            }
        } else {
            val lifecycle = fragment.lifecycle
            when (lifecycle.currentState) {
                INITIALIZED -> doOnLifecycleEvents(lifecycle, {
                    // Called from onAttach, onCreate
                    if (fragment.view != null) {
                        disposeOnLifecycleEvents(fragment.viewLifecycleOwner.lifecycle, listOfNotNull(lifecycleEvent, ON_DESTROY))
                    } else {
                        disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_DESTROY))
                    }
                }, listOf(ON_START))
                CREATED -> doOnLifecycleEvents(lifecycle, { event ->
                    // Evaluate which Fragment has view after onCreateView.
                    if (event == ON_START) {
                        if (fragment.view != null) {
                            // Called from onCreateView
                            disposeOnLifecycleEvents(fragment.viewLifecycleOwner.lifecycle, listOfNotNull(lifecycleEvent, ON_DESTROY))
                        } else {
                            // Called from onActivityCreated, onStart
                            disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_STOP, ON_DESTROY))
                        }
                    } else if (event == ON_DESTROY) {
                        // Called from onStop
                        safeDispose()
                    }
                }, listOf(ON_START, ON_DESTROY))
                // Called from onResume, onPause
                STARTED, RESUMED -> disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_PAUSE, ON_DESTROY))
                // Called from onDestroy
                else -> safeDispose()
            }
        }
    }

private fun Disposable.disposeOnLifecycleEvents(lifecycle: Lifecycle, lifecycleEvents: List<Lifecycle.Event>) {
    doOnLifecycleEvents(lifecycle, { safeDispose() }, lifecycleEvents)
}

private fun Disposable.doOnLifecycleEvents(
    lifecycle: Lifecycle,
    onEvent: Disposable.(Lifecycle.Event) -> Unit,
    lifecycleEvents: List<Lifecycle.Event>
) {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event in lifecycleEvents) {
                lifecycle.removeObserver(this)
                onEvent(event)
            }
        }
    })
}

private fun Disposable.safeDispose() {
    if (!isDisposed) {
        dispose()
    }
}


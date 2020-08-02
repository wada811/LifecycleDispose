@file:Suppress("unused")

package com.wada811.lifecycledispose

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
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
import io.reactivex.rxjava3.disposables.Disposable

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
fun <T : Disposable> T.disposeOnLifecycle(activity: FragmentActivity): T =
    disposeOnLifecycleEvent(activity, when (activity.lifecycle.currentState) {
        INITIALIZED -> ON_DESTROY // onCreate
        CREATED -> ON_STOP // onStart, onStop
        STARTED, RESUMED -> ON_PAUSE // onResume, onPause
        DESTROYED -> ON_DESTROY // onDestroy
    })

fun <T : Disposable> T.disposeOnStop(activity: FragmentActivity): T = disposeOnLifecycleEvent(activity, ON_STOP)
fun <T : Disposable> T.disposeOnPause(activity: FragmentActivity): T = disposeOnLifecycleEvent(activity, ON_PAUSE)
fun <T : Disposable> T.disposeOnDestroy(activity: FragmentActivity): T = disposeOnLifecycleEvent(activity, ON_DESTROY)

private fun <T : Disposable> T.disposeOnLifecycleEvent(activity: FragmentActivity, lifecycleEvent: Event): T =
    disposeOnLifecycleEvents({ activity.lifecycle }, { listOf(lifecycleEvent, ON_DESTROY) })

/**
 * Dispose on corresponding lifecycle event.
 *
 * ```
 * | Subscribe     | ViewLifecycle.State   | Dispose       | Lifecycle.State | Dispose       |
 * | ------------- | --------------------- | ------------- | --------------- | ------------- |
 * | onAttach      | IllegalStateException | onDestroy     | INITIALIZED     | onDestroy     |
 * | onCreate      | IllegalStateException | onDestroy     | INITIALIZED     | onDestroy     |
 * | onCreateView  | IllegalStateException | onDestroyView | CREATED         | onDestroy     |
 * | onViewCreated | INITIALIZED           | onDestroyView | not called      | not called    |
 * | onStart       | CREATED               | onStop        | CREATED         | onStop        |
 * | onResume      | STARTED               | onPause       | STARTED         | onPause       |
 * | onPause       | STARTED               | onDestroyView | STARTED         | onDestroy     |
 * | onStop        | CREATED               | onDestroyView | CREATED         | onDestroy     |
 * | onDestroyView | DESTROYED             | onDestroyView | not called      | not called    |
 * | onDestroy     | IllegalStateException | onDestroy     | DESTROYED       | onDestroy     |
 * ```
 */
fun <T : Disposable> T.disposeOnLifecycle(fragment: Fragment): T =
    disposeOnLifecycleEvent(fragment, when (fragment.viewLifecycleOrLifecycle.currentState) {
        INITIALIZED -> ON_DESTROY // onAttach, onCreate, onViewCreated
        CREATED -> if (fragment.viewLifecycleOwnerLiveData.value != null) ON_STOP else null  // onCreateView: ON_DESTROY, onStart, onStop: ON_STOP
        STARTED, RESUMED -> ON_PAUSE // onResume, onPause
        DESTROYED -> ON_DESTROY // onDestroyView, onDestroy
    })

fun <T : Disposable> T.disposeOnStop(fragment: Fragment): T = disposeOnLifecycleEvent(fragment, ON_STOP)
fun <T : Disposable> T.disposeOnPause(fragment: Fragment): T = disposeOnLifecycleEvent(fragment, ON_PAUSE)
fun <T : Disposable> T.disposeOnDestroy(fragment: Fragment): T = disposeOnLifecycleEvent(fragment, ON_DESTROY)

private fun <T : Disposable> T.disposeOnLifecycleEvent(fragment: Fragment, lifecycleEvent: Event?): T =
    disposeOnLifecycleEvents(
        { fragment.viewLifecycleOrLifecycle },
        { listOf(lifecycleEvent ?: if (fragment.viewLifecycleOwnerLiveData.value != null) ON_DESTROY else ON_STOP, ON_DESTROY) }
    )

private val Fragment.viewLifecycleOrLifecycle: Lifecycle
    get() = if (viewLifecycleOwnerLiveData.value != null) viewLifecycleOwner.lifecycle else lifecycle

private fun <T : Disposable> T.disposeOnLifecycleEvents(lifecycle: () -> Lifecycle, lifecycleEvents: () -> List<Event>): T =
    if (lifecycle().currentState == DESTROYED) safeDispose() else doOnLifecycleEvents(lifecycle, lifecycleEvents) { safeDispose() }

private fun <T : Disposable> T.doOnLifecycleEvents(
    lifecycle: () -> Lifecycle,
    lifecycleEvents: () -> List<Event>,
    onEvent: T.() -> Unit
): T = this.also {
    lifecycle().addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Event) {
            if (event == ON_START) {
                source.lifecycle.removeObserver(this)
                lifecycle().addObserver(object : LifecycleEventObserver {
                    override fun onStateChanged(source: LifecycleOwner, event: Event) {
                        if (event in lifecycleEvents()) {
                            source.lifecycle.removeObserver(this)
                            onEvent()
                        }
                    }
                })
            } else if (event in lifecycleEvents()) {
                source.lifecycle.removeObserver(this)
                onEvent()
            }
        }
    })
}

private fun <T : Disposable> T.safeDispose(): T = this.also {
    if (!isDisposed) {
        dispose()
    }
}

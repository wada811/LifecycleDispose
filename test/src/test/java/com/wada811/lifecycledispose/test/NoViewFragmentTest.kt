package com.wada811.lifecycledispose.test

import android.os.Build
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.STARTED
import com.google.common.truth.Truth
import com.wada811.lifecycledispose.test.FragmentLifecycleEvent.OnAttach
import com.wada811.lifecycledispose.test.FragmentLifecycleEvent.OnCreate
import com.wada811.lifecycledispose.test.FragmentLifecycleEvent.OnDestroy
import com.wada811.lifecycledispose.test.FragmentLifecycleEvent.OnPause
import com.wada811.lifecycledispose.test.FragmentLifecycleEvent.OnResume
import com.wada811.lifecycledispose.test.FragmentLifecycleEvent.OnStart
import com.wada811.lifecycledispose.test.FragmentLifecycleEvent.OnStop
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test disposing on corresponding lifecycle event.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/lifecycle#lc">Handling Lifecycles with Lifecycle-Aware Components | Android Developers</a>
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(ParameterizedRobolectricTestRunner::class)
class NoViewFragmentTest(
    private val disposeStrategy: DisposeStrategy,
    private val subscribeLifecycleEvent: FragmentLifecycleEvent,
    private val expectedLifecycleState: Lifecycle.State
) {
    @Test
    fun test() {
        var fragment: DisposeNoViewFragment? = null
        val scenario = FragmentScenario.launch(
            DisposeNoViewFragment::class.java,
            DisposeNoViewFragment.createBundle(
                disposeStrategy,
                subscribeLifecycleEvent
            )
        )
        scenario.onFragment { fragment = it }
        scenario.moveToState(DESTROYED)
        Truth.assertThat(fragment!!.disposedLifecycleState).isEqualTo(expectedLifecycleState)
        fragment = null
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Strategy: {0}, subscribe: {1}, dispose: {2}")
        fun parameters(): Iterable<Array<Any>> {
            val parameters = mutableListOf<Array<Any>>()
            // OnLifecycle
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, OnAttach, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, OnCreate, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, OnStart, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, OnResume, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, OnPause, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, OnStop, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, OnDestroy, DESTROYED))
            // OnPause
            parameters.add(arrayOf(DisposeStrategy.OnPause, OnAttach, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, OnCreate, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, OnStart, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, OnResume, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, OnPause, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, OnStop, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, OnDestroy, DESTROYED))
            // OnStop
            parameters.add(arrayOf(DisposeStrategy.OnStop, OnAttach, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, OnCreate, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, OnStart, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, OnResume, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, OnPause, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, OnStop, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, OnDestroy, DESTROYED))
            // OnDestroy
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, OnAttach, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, OnCreate, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, OnStart, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, OnResume, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, OnPause, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, OnStop, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, OnDestroy, DESTROYED))
            return parameters
        }
    }
}

package com.wada811.lifecycledispose

import android.os.Build
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wada811.lifecycledispose.infra.TestNoViewFragment
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Test disposing on corresponding lifecycle event.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/lifecycle#lc">Handling Lifecycles with Lifecycle-Aware Components | Android Developers</a>
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class NoViewFragmentTest {
    @Test
    fun onCreate() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onCreateDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.DESTROYED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun onCreateView() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onCreateViewDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.CREATED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun onStart() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onStartDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.CREATED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)

    }

    @Test
    fun onResume() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onResumeDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.STARTED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun onPause() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onPauseDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.DESTROYED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun onStop() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onStopDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.DESTROYED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun onDestroyView() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onDestroyViewDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.DESTROYED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun onDestroy() {
        val scenario = FragmentScenario.launch(TestNoViewFragment::class.java)
        scenario.onFragment {
            it.onDestroyDoOnDispose = {
                Assert.assertEquals(Lifecycle.State.DESTROYED, it.lifecycle.currentState)
            }
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
    }
}

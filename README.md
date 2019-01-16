LifecycleDisposable
=====

[![Build Status](https://app.bitrise.io/app/25a74c8a899d5c9a/status.svg?token=rSUoGqwaasQ6M5a7KKPTdA&branch=master)](https://app.bitrise.io/app/25a74c8a899d5c9a)

LifecycleDisposable dispose RxJava streams on lifecycle down event that corresponding to Activity/Fragment's lifecycle state when subscribe using [Android Architecture Components Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle).

![Lifecycle State](https://developer.android.com/images/topic/libraries/architecture/lifecycle-states.png)

## Usage
### Activity

```kotlin
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Observable.just("LifecycleDisposable")
            .subscribe()
            .disposeOnLifecycle(this) // dispose when onDestroy is called
    }
}
```

**Table 1** Corresponding between Activity's lifecycle state and Lifecycle down event.

| Subscribe | Lifecycle.State | Lifecycle.Event | Dispose   |
| --------- | --------------- | --------------- | --------- |
| onCreate  | INITIALIZED     | ON_DESTROY      | onDestroy |
| onStart   | CREATED         | ON_STOP         | onStop    |
| onResume  | STARTED         | ON_PAUSE        | onPause   |
| onPause   | STARTED         | ON_DESTROY      | onDestroy |
| onStop    | CREATED         | ON_DESTROY      | onDestroy |
| onDestroy | DESTROYED       | ON_DESTROY      | onDestroy |


### Fragment

```kotlin
class MainFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Observable.just("LifecycleDisposable")
            .subscribe()
            .disposeOnLifecycle(this) // dispose when onDestroy is called
    }
}
```

**Table 2** Corresponding between Fragment's lifecycle state and Lifecycle down event.

| Subscribe     | Lifecycle.State | Lifecycle.Event | Dispose       |
| ------------- | --------------- | --------------- | ------------- |
| onCreate      | INITIALIZED     | ON_DESTROY      | onDestroy     |
| onCreateView  | INITIALIZED     | ON_DESTROY      | onDestroyView |
| onStart       | CREATED         | ON_STOP         | onStop        |
| onResume      | STARTED         | ON_PAUSE        | onPause       |
| onPause       | STARTED         | ON_DESTROY      | onDestroyView |
| onStop        | CREATED         | ON_DESTROY      | onDestroyView |
| onDestroyView | DESTROYED       | ON_DESTROY      | onDestroyView |
| onDestroy     | DESTROYED       | ON_DESTROY      | onDestroy     |

 
## Gradle

[![](https://jitpack.io/v/wada811/LifecycleDisposable.svg)](https://jitpack.io/#wada811/LifecycleDisposable)

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

### AndroidX

```groovy
dependencies {
    implementation 'com.github.wada811.LifecycleDisposable:lifecycledisposable:x.y.z'
}
```

### Support Library

```groovy
dependencies {
    implementation 'com.github.wada811.LifecycleDisposable:lifecycledisposablesupport:x.y.z'
}
```

## License

Copyright (C) 2019 wada811

Licensed under the Apache License, Version 2.0

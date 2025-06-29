package org.example.project

import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import pro.respawn.flowmvi.api.*
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.dsl.updateState
import pro.respawn.flowmvi.plugins.reduce
import pro.respawn.flowmvi.savedstate.plugins.serializeState
import pro.respawn.flowmvi.savedstate.api.SaveBehavior
import pro.respawn.flowmvi.savedstate.api.NullRecover
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.whileSubscribed
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Serializable
data class CounterStore(val value: Int = 0)

@Serializable
data class ErrorState(val message: String? = null)

sealed interface CounterState : MVIState {
    val counter: CounterStore

    @Serializable
    data class Content(
        override val counter: CounterStore = CounterStore()
    ) : CounterState

    data class Loading(
        override val counter: CounterStore = CounterStore()
    ) : CounterState

    data class Error(
        val error: ErrorState,
        override val counter: CounterStore = CounterStore()
    ) : CounterState

}

@Serializable
sealed interface CounterIntent : MVIIntent {
    @Serializable
    data object ClickedIncrement : CounterIntent
    @Serializable
    data object ClickedReset : CounterIntent
}

@Serializable
sealed interface CounterAction : MVIAction {
    @Serializable
    data class ShowMessage(val message: String) : CounterAction
}

class CounterContainer : Container<CounterState, CounterIntent, CounterAction> {

    private val _actions = MutableSharedFlow<CounterAction>()
    val actions = _actions.asSharedFlow()

    override val store = store(initial = CounterState.Content()) {
        configure {
            name = "Counter"
            debuggable = true
        }
        recover {
            updateState { CounterState.Error(ErrorState(it.message), counter) }
            null
        }
        serializeState(
            path = ".cache",
            recover = NullRecover,
            serializer = CounterState.Content.serializer(),
            behaviors = setOf(
                SaveBehavior.OnUnsubscribe(),
                SaveBehavior.Periodic()
            )
        )
        whileSubscribed {
            while (isActive) {
                delay(1.seconds)
                updateState<CounterState.Content, _> {
                    copy(counter = counter.copy(value = counter.value + Random.nextInt(0, 100)))
                }
            }
        }
        reduce { intent ->
            when (intent) {
                is CounterIntent.ClickedIncrement -> updateState<CounterState.Content, _> {
                    val newValue = counter.value + 1
                    launchAction(CounterAction.ShowMessage("Counter increased: $newValue"))
                    copy(counter = CounterStore(newValue))
                }
                is CounterIntent.ClickedReset -> updateState<CounterState.Content, _> {
                    launchAction(CounterAction.ShowMessage("Counter has been reset."))
                    copy(counter = CounterStore(0))
                }
            }
        }
    }

    private suspend fun launchAction(action: CounterAction) {
        _actions.emit(action)
    }
}
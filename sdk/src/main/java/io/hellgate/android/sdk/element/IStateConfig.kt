package io.hellgate.android.sdk.element

internal interface IStateConfig<Input> {
    fun determineState(input: Input): TextFieldState
}

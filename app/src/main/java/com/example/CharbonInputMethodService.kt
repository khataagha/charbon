package com.example

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.ui.keyboard.CharbonKeyboardContent

class CharbonInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onCreateInputView(): View {
        window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this@CharbonInputMethodService)
            decorView.setViewTreeViewModelStoreOwner(this@CharbonInputMethodService)
            decorView.setViewTreeSavedStateRegistryOwner(this@CharbonInputMethodService)
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@CharbonInputMethodService)
            setViewTreeViewModelStoreOwner(this@CharbonInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@CharbonInputMethodService)

            setContent {
                CharbonKeyboardContent(
                    onInsertText = { text ->
                        currentInputConnection?.commitText(text, 1)
                    },
                    onBackspace = {
                        handleBackspace()
                    },
                    onEnter = {
                        handleEnter()
                    },
                    onSendKeyEvent = { keyCode ->
                        sendDownUpKeyEvents(keyCode)
                    },
                    onSelectAll = {
                        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                    },
                    onSwitchKeyboard = {
                        switchInputMethod()
                    },
                    onOpenSettings = {
                        val intent = Intent(this@CharbonInputMethodService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                )
            }
        }
        return composeView
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val editorInfo = currentInputEditorInfo
        val action = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun switchInputMethod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (!switchToNextInputMethod(false)) {
                    showPicker()
                }
            } catch (_: Exception) {
                showPicker()
            }
        } else {
            showPicker()
        }
    }

    private fun showPicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

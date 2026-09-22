package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.mutableStateListOf
import com.example.model.ActiveFormatState
import com.example.model.InlineFormat
import com.example.model.MarkdownFormattingEngine
import com.example.ui.markdown.MarkdownPreviewView
import com.example.ui.markdown.MarkdownToolbar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.model.CharbonThemeMode
import com.example.repository.CharbonPreferences
import com.example.ui.keyboard.CharbonKeyboardContent
import com.example.ui.theme.CharbonTheme
import com.example.ui.theme.LightCharbonColors
import com.example.ui.theme.LocalCharbonColors
import com.example.ui.theme.NotoSansSymbolsFamily

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CharbonSetupScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharbonSetupScreen() {
    val context = LocalContext.current
    val prefs = remember { CharbonPreferences(context) }
    var currentTheme by remember { mutableStateOf(prefs.themeMode) }
    var isHapticEnabled by remember { mutableStateOf(prefs.hapticFeedbackEnabled) }
    var keyboardHeightDp by remember { mutableStateOf(prefs.keyboardHeightDp.toFloat()) }
    var showQuickSymbols by remember { mutableStateOf(prefs.showQuickSymbolRow) }
    var customCollections by remember { mutableStateOf(prefs.getCustomCollections()) }
    var newCollectionName by remember { mutableStateOf("") }
    var newCollectionCodepoints by remember { mutableStateOf("") }
    var testTextFieldValue by remember {
        mutableStateOf(TextFieldValue("# Welcome to **Charbon**\n\nExperience rich *Markdown* formatting, `code`, and extended Unicode: ▭ ╔═╗ ∑(x) ➔"))
    }
    var showInAppKeyboard by remember { mutableStateOf(true) }
    var sandboxTab by remember { mutableStateOf("editor") } // "editor" or "preview"
    var pendingInlineStyles by remember { mutableStateOf(setOf<InlineFormat>()) }
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    fun pushUndo(value: TextFieldValue) {
        if (undoStack.size > 50) undoStack.removeAt(0)
        undoStack.add(value)
        redoStack.clear()
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(testTextFieldValue)
            testTextFieldValue = undoStack.removeAt(undoStack.lastIndex)
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(testTextFieldValue)
            testTextFieldValue = redoStack.removeAt(redoStack.lastIndex)
        }
    }

    fun toggleInlineFormat(format: InlineFormat) {
        pushUndo(testTextFieldValue)
        val (newVal, newPending) = MarkdownFormattingEngine.toggleInlineFormat(
            value = testTextFieldValue,
            format = format,
            pendingStyles = pendingInlineStyles
        )
        testTextFieldValue = newVal
        pendingInlineStyles = newPending
    }

    fun toggleHeading() {
        pushUndo(testTextFieldValue)
        testTextFieldValue = MarkdownFormattingEngine.toggleHeading(testTextFieldValue)
    }

    fun toggleBlockquote() {
        pushUndo(testTextFieldValue)
        testTextFieldValue = MarkdownFormattingEngine.toggleBlockquote(testTextFieldValue)
    }

    fun toggleBulletList() {
        pushUndo(testTextFieldValue)
        testTextFieldValue = MarkdownFormattingEngine.toggleBulletList(testTextFieldValue)
    }

    fun toggleNumberedList() {
        pushUndo(testTextFieldValue)
        testTextFieldValue = MarkdownFormattingEngine.toggleNumberedList(testTextFieldValue)
    }

    fun toggleCodeBlock() {
        pushUndo(testTextFieldValue)
        testTextFieldValue = MarkdownFormattingEngine.toggleCodeBlock(testTextFieldValue)
    }

    fun insertLink() {
        pushUndo(testTextFieldValue)
        testTextFieldValue = MarkdownFormattingEngine.insertLink(testTextFieldValue)
    }

    var isKeyboardEnabled by remember { mutableStateOf(checkIsKeyboardEnabled(context)) }
    var isKeyboardDefault by remember { mutableStateOf(checkIsKeyboardDefault(context)) }

    fun insertTextToSandbox(textToInsert: String) {
        pushUndo(testTextFieldValue)
        testTextFieldValue = MarkdownFormattingEngine.handleTextCommit(
            current = testTextFieldValue,
            textToInsert = textToInsert,
            pendingStyles = pendingInlineStyles
        )
    }

    fun handleBackspaceSandbox() {
        val currentText = testTextFieldValue.text
        val selection = testTextFieldValue.selection
        if (selection.min != selection.max) {
            val start = selection.min
            val end = selection.max
            val newText = currentText.substring(0, start) + currentText.substring(end)
            testTextFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(start)
            )
        } else if (selection.min > 0) {
            val start = selection.min
            val codePointBefore = Character.codePointBefore(currentText, start)
            val charCount = Character.charCount(codePointBefore)
            val deletePos = (start - charCount).coerceAtLeast(0)
            val newText = currentText.substring(0, deletePos) + currentText.substring(start)
            testTextFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(deletePos)
            )
        }
    }

    fun handleEnterSandbox() {
        insertTextToSandbox("\n")
    }

    fun handleKeyEventSandbox(keyCode: Int) {
        val currentText = testTextFieldValue.text
        val cursorPos = testTextFieldValue.selection.start
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                val newPos = (cursorPos - 1).coerceAtLeast(0)
                testTextFieldValue = testTextFieldValue.copy(selection = TextRange(newPos))
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                val newPos = (cursorPos + 1).coerceAtMost(currentText.length)
                testTextFieldValue = testTextFieldValue.copy(selection = TextRange(newPos))
            }
            KeyEvent.KEYCODE_MOVE_HOME -> {
                testTextFieldValue = testTextFieldValue.copy(selection = TextRange(0))
            }
            KeyEvent.KEYCODE_MOVE_END -> {
                testTextFieldValue = testTextFieldValue.copy(selection = TextRange(currentText.length))
            }
        }
    }

    fun handleSelectAllSandbox() {
        testTextFieldValue = testTextFieldValue.copy(selection = TextRange(0, testTextFieldValue.text.length))
    }

    // Recheck IME status when returning to app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isKeyboardEnabled = checkIsKeyboardEnabled(context)
                isKeyboardDefault = checkIsKeyboardDefault(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CharbonTheme(mode = currentTheme) {
        val colors = LocalCharbonColors.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_charbon_logo),
                                    contentDescription = "Charbon Logo",
                                    modifier = Modifier.size(32.dp),
                                    colorFilter = if (colors == LightCharbonColors) null else ColorFilter.tint(colors.textPrimary)
                                )
                                Text(
                                    text = "Charbon",
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    letterSpacing = (-0.3).sp
                                )
                            }

                            // Version Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.keySpecialBackground)
                                    .border(1.dp, colors.keyBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "v1.6",
                                    color = colors.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.toolbarBackground
                    )
                )
            },
            containerColor = colors.keyboardBackground
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Setup Status Banner
                    SetupStatusCard(
                        isKeyboardEnabled = isKeyboardEnabled,
                        isKeyboardDefault = isKeyboardDefault,
                        colors = colors
                    )
                }

                // Step 1: Enable Keyboard in Settings
                item {
                    StepCard(
                        stepNumber = "1",
                        title = "Enable Charbon Keyboard",
                        subtitle = if (isKeyboardEnabled) "Charbon is enabled in Android System Settings" else "Turn on Charbon in Settings > Manage Keyboards",
                        isCompleted = isKeyboardEnabled,
                        buttonLabel = if (isKeyboardEnabled) "Enabled" else "Open Settings",
                        colors = colors,
                        onClick = {
                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                            context.startActivity(intent)
                        },
                        tag = "enable_ime_button"
                    )
                }

                // Step 2: Choose as Default
                item {
                    StepCard(
                        stepNumber = "2",
                        title = "Select as Default Keyboard",
                        subtitle = if (isKeyboardDefault) "Charbon is active as your primary input method" else "Switch your active keyboard to Charbon",
                        isCompleted = isKeyboardDefault,
                        buttonLabel = if (isKeyboardDefault) "Active" else "Choose Keyboard",
                        colors = colors,
                        onClick = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        },
                        tag = "select_ime_button"
                    )
                }

                // Step 3: Interactive Sandbox
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_sandbox_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.toolbarBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Interactive Live Test Sandbox",
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.keySpecialBackground)
                                        .border(0.8.dp, colors.keyBorder, RoundedCornerShape(8.dp))
                                        .padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sandboxTab == "editor") colors.accent else androidx.compose.ui.graphics.Color.Transparent)
                                            .clickable { sandboxTab = "editor" }
                                            .padding(horizontal = 9.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Editor",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sandboxTab == "editor") androidx.compose.ui.graphics.Color.Black else colors.textPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sandboxTab == "preview") colors.accent else androidx.compose.ui.graphics.Color.Transparent)
                                            .clickable { sandboxTab = "preview" }
                                            .padding(horizontal = 9.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Preview",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sandboxTab == "preview") androidx.compose.ui.graphics.Color.Black else colors.textPrimary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Test typing extended Unicode characters (such as ▭ U+25AD, ╔, ∑, ➔) and rich Markdown formatting live below.",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            val activeMarkdownState = MarkdownFormattingEngine.detectActiveFormats(
                                value = testTextFieldValue,
                                pendingInlineStyles = pendingInlineStyles
                            )

                            if (sandboxTab == "editor") {
                                // Formatting Toolbar
                                MarkdownToolbar(
                                    activeState = activeMarkdownState,
                                    canUndo = undoStack.isNotEmpty(),
                                    canRedo = redoStack.isNotEmpty(),
                                    onToggleInline = { toggleInlineFormat(it) },
                                    onToggleHeading = { toggleHeading() },
                                    onToggleBlockquote = { toggleBlockquote() },
                                    onToggleBulletList = { toggleBulletList() },
                                    onToggleNumberedList = { toggleNumberedList() },
                                    onToggleCodeBlock = { toggleCodeBlock() },
                                    onInsertLink = { insertLink() },
                                    onUndo = { handleUndo() },
                                    onRedo = { handleRedo() },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = testTextFieldValue,
                                    onValueChange = { newVal ->
                                        if (newVal.text != testTextFieldValue.text) {
                                            pushUndo(testTextFieldValue)
                                        }
                                        testTextFieldValue = newVal
                                    },
                                    placeholder = {
                                        Text(
                                            "Tap keyboard keys or formatting buttons above...",
                                            color = colors.textSecondary.copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontFamily = NotoSansSymbolsFamily
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("sandbox_text_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.accent,
                                        unfocusedBorderColor = colors.keyBorder,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary,
                                        cursorColor = colors.accent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.keySpecialBackground)
                                        .border(1.dp, colors.keyBorder, RoundedCornerShape(12.dp))
                                        .padding(4.dp)
                                ) {
                                    MarkdownPreviewView(markdownText = testTextFieldValue.text)
                                }
                            }

                            // Quick actions row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("Charbon", testTextFieldValue.text)
                                        clipboard?.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.keyBorder)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = {
                                        testTextFieldValue = TextFieldValue("")
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.keyBorder)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                        imm?.showInputMethodPicker()
                                    },
                                    modifier = Modifier.weight(1.3f).height(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.keyBorder)
                                ) {
                                    Icon(Icons.Default.TouchApp, contentDescription = "Pick IME", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick System IME", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Embedded Live Keyboard View
                            if (showInAppKeyboard) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((keyboardHeightDp + 96).dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, colors.keyBorder, RoundedCornerShape(12.dp))
                                        .testTag("embedded_charbon_keyboard")
                                ) {
                                    CharbonKeyboardContent(
                                        onInsertText = { insertTextToSandbox(it) },
                                        onBackspace = { handleBackspaceSandbox() },
                                        onEnter = { handleEnterSandbox() },
                                        onSendKeyEvent = { handleKeyEventSandbox(it) },
                                        onSelectAll = { handleSelectAllSandbox() },
                                        onSwitchKeyboard = {
                                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                            imm?.showInputMethodPicker()
                                        },
                                        onOpenSettings = {
                                            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                            context.startActivity(intent)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Keyboard Preferences
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preferences_card"),
                        colors = CardDefaults.cardColors(containerColor = colors.toolbarBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Theme & Appearance",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemePill(
                                    label = "Light",
                                    isSelected = currentTheme == CharbonThemeMode.LIGHT,
                                    colors = colors,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        currentTheme = CharbonThemeMode.LIGHT
                                        prefs.themeMode = CharbonThemeMode.LIGHT
                                    }
                                )
                                ThemePill(
                                    label = "Dark",
                                    isSelected = currentTheme == CharbonThemeMode.DARK,
                                    colors = colors,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        currentTheme = CharbonThemeMode.DARK
                                        prefs.themeMode = CharbonThemeMode.DARK
                                    }
                                )
                                ThemePill(
                                    label = "AMOLED",
                                    isSelected = currentTheme == CharbonThemeMode.AMOLED,
                                    colors = colors,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        currentTheme = CharbonThemeMode.AMOLED
                                        prefs.themeMode = CharbonThemeMode.AMOLED
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Haptic Feedback toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Haptic Key Feedback",
                                            color = colors.textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Subtle vibration pulse on character tap",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isHapticEnabled,
                                    onCheckedChange = {
                                        isHapticEnabled = it
                                        prefs.hapticFeedbackEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = colors.accentText,
                                        checkedTrackColor = colors.accent
                                    ),
                                    modifier = Modifier.testTag("haptic_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Keyboard Height Slider
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Straighten,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Keyboard Height",
                                            color = colors.textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = "${keyboardHeightDp.toInt()} dp",
                                        color = colors.accent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = keyboardHeightDp,
                                    onValueChange = {
                                        keyboardHeightDp = it
                                        prefs.keyboardHeightDp = it.toInt()
                                    },
                                    valueRange = 170f..310f,
                                    steps = 13,
                                    colors = SliderDefaults.colors(
                                        thumbColor = colors.accent,
                                        activeTrackColor = colors.accent,
                                        inactiveTrackColor = colors.keyBorder
                                    ),
                                    modifier = Modifier.testTag("slider_keyboard_height")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Symbol Row toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ViewHeadline,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Quick Symbol Number Row",
                                            color = colors.textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Top row with ! @ # $ % ^ & * ( ) on ABC keyboard",
                                            color = colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = showQuickSymbols,
                                    onCheckedChange = {
                                        showQuickSymbols = it
                                        prefs.showQuickSymbolRow = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = colors.accentText,
                                        checkedTrackColor = colors.accent
                                    ),
                                    modifier = Modifier.testTag("switch_quick_symbols")
                                )
                            }
                        }
                    }
                }

                // Custom Collections Manager Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.toolbarBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tag,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Custom Symbol Collections",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Group your favorite characters into custom tabs shown on the Unicode keyboard.",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Existing custom collections list
                            if (customCollections.isEmpty()) {
                                Text(
                                    text = "No custom collections yet. Add one below!",
                                    color = colors.textSecondary.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for ((colName, cps) in customCollections) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.keyBackground)
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "📁 $colName",
                                                    color = colors.textPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = cps.joinToString(" ") { String(Character.toChars(it)) },
                                                    color = colors.textSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    prefs.deleteCustomCollection(colName)
                                                    customCollections = prefs.getCustomCollections()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Delete collection",
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Add new collection form
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newCollectionName,
                                    onValueChange = { newCollectionName = it },
                                    label = { Text("Name", fontSize = 11.sp) },
                                    placeholder = { Text("Math, Stars...", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = newCollectionCodepoints,
                                    onValueChange = { newCollectionCodepoints = it },
                                    label = { Text("Characters", fontSize = 11.sp) },
                                    placeholder = { Text("★ ☆ ✦ ✧", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val trimmedName = newCollectionName.trim()
                                    if (trimmedName.isNotEmpty() && newCollectionCodepoints.isNotEmpty()) {
                                        val cps = mutableListOf<Int>()
                                        var i = 0
                                        while (i < newCollectionCodepoints.length) {
                                            val cp = newCollectionCodepoints.codePointAt(i)
                                            if (!Character.isWhitespace(cp)) {
                                                cps.add(cp)
                                            }
                                            i += Character.charCount(cp)
                                        }
                                        if (cps.isNotEmpty()) {
                                            prefs.saveCustomCollection(trimmedName, cps)
                                            customCollections = prefs.getCustomCollections()
                                            newCollectionName = ""
                                            newCollectionCodepoints = ""
                                            Toast.makeText(context, "Saved collection '$trimmedName'", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = colors.accentText
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("button_add_collection")
                            ) {
                                Text("Add Custom Collection", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Extended Unicode Highlights Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colors.toolbarBackground),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FormatShapes,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Unicode Coverage & Character Map",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Charbon bundles high-density Noto Sans Symbols with complete coverage across core Unicode blocks:",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Visual Glyph Categories
                            val categories = listOf(
                                "Geometric Shapes" to listOf("▭", "■", "□", "▲", "▼", "◆", "◉", "◈"),
                                "Box & Framing" to listOf("╔", "╗", "╚", "╝", "║", "═", "╬", "╦"),
                                "Block Elements" to listOf("█", "▀", "▄", "▌", "░", "▒", "▓", "▐"),
                                "Math & Calculus" to listOf("∑", "∏", "√", "∞", "∫", "≈", "≠", "≤", "≥"),
                                "Arrows & Direction" to listOf("←", "↑", "→", "➔", "⇐", "⇒", "➜", "↺"),
                                "Global Currency" to listOf("€", "₽", "₹", "₿", "¥", "¢", "£", "₩")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for ((catName, glyphs) in categories) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.keyBackground)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = catName,
                                            color = colors.accent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            for (g in glyphs) {
                                                Text(
                                                    text = g,
                                                    fontFamily = NotoSansSymbolsFamily,
                                                    fontSize = 13.sp,
                                                    color = colors.textPrimary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Charbon • Elemental Unicode Keyboard • Version 1.6",
                            color = colors.textSecondary.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SetupStatusCard(
    isKeyboardEnabled: Boolean,
    isKeyboardDefault: Boolean,
    colors: com.example.ui.theme.CharbonColors
) {
    val isReady = isKeyboardEnabled && isKeyboardDefault
    val statusBg = if (isReady) colors.success.copy(alpha = 0.12f) else colors.accent.copy(alpha = 0.10f)
    val statusBorder = if (isReady) colors.success.copy(alpha = 0.35f) else colors.accent.copy(alpha = 0.25f)
    val statusIcon = if (isReady) Icons.Default.CheckCircle else Icons.Default.Settings
    val statusColor = if (isReady) colors.success else colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(statusBg)
            .border(1.dp, statusBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isReady) "Charbon Active & Ready" else "Setup Required",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.15).sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isReady) colors.success.copy(alpha = 0.2f) else colors.accent.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isReady) "ACTIVE" else "PENDING",
                            color = if (isReady) colors.success else colors.accent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isReady)
                        "You can now type extended Unicode in any app (browser, notes, chat, terminal)."
                    else
                        "Complete the two quick steps below to enable Charbon and set it as your default keyboard.",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: String,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    buttonLabel: String,
    colors: com.example.ui.theme.CharbonColors,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.keyBorder.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) colors.success else colors.keySpecialBackground)
                        .border(1.dp, if (isCompleted) colors.success else colors.keyBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = stepNumber,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) colors.keySpecialBackground else colors.accent,
                    contentColor = if (isCompleted) colors.textPrimary else colors.accentText
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag(tag)
            ) {
                Text(
                    text = buttonLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isCompleted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemePill(
    label: String,
    isSelected: Boolean,
    colors: com.example.ui.theme.CharbonColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val previewColor = when (label) {
        "Light" -> Color(0xFFF7F6F2)
        "Dark" -> Color(0xFF181A20)
        "AMOLED" -> Color(0xFF000000)
        else -> colors.accent
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.accent else colors.cardBackground)
            .border(
                1.dp,
                if (isSelected) colors.accent else colors.keyBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(1.dp, if (isSelected) colors.accentText.copy(alpha = 0.8f) else colors.keyBorder, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSelected) colors.accentText else colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

private fun checkIsKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
    val enabledImes = imm.enabledInputMethodList
    val myPackage = context.packageName
    return enabledImes.any { it.packageName == myPackage }
}

private fun checkIsKeyboardDefault(context: Context): Boolean {
    val currentIme = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    ) ?: return false
    return currentIme.contains(context.packageName)
}

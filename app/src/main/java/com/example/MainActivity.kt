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
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
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
    var testTextFieldValue by remember {
        mutableStateOf(TextFieldValue("▭ ╔═╗ ∑(x) ➔"))
    }
    var showInAppKeyboard by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current

    var isKeyboardEnabled by remember { mutableStateOf(checkIsKeyboardEnabled(context)) }
    var isKeyboardDefault by remember { mutableStateOf(checkIsKeyboardDefault(context)) }

    fun insertTextToSandbox(textToInsert: String) {
        val currentText = testTextFieldValue.text
        val selection = testTextFieldValue.selection
        val start = selection.min
        val end = selection.max
        val newText = currentText.substring(0, start) + textToInsert + currentText.substring(end)
        val newCursorPos = start + textToInsert.length
        testTextFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPos)
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_charbon_logo),
                                contentDescription = "Charbon Logo",
                                modifier = Modifier.size(32.dp),
                                colorFilter = if (colors == LightCharbonColors) null else ColorFilter.tint(colors.textPrimary)
                            )
                            Text(
                                text = "Charbon",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
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
                    Spacer(modifier = Modifier.height(6.dp))
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

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedButton(
                                        onClick = { showInAppKeyboard = !showInAppKeyboard },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = colors.accent
                                        ),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (showInAppKeyboard) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (showInAppKeyboard) "Hide Keyboard" else "Show Keyboard",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Test typing extended Unicode characters (such as ▭ U+25AD, ╔, ∑, ➔) live using the interactive Charbon keyboard below, or tap the text field for system input.",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                            )

                            OutlinedTextField(
                                value = testTextFieldValue,
                                onValueChange = { testTextFieldValue = it },
                                placeholder = {
                                    Text(
                                        "Tap keyboard keys below to test Charbon...",
                                        color = colors.textSecondary.copy(alpha = 0.6f),
                                        fontSize = 14.sp
                                    )
                                },
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

                            // Quick actions row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("Charbon", testTextFieldValue.text)
                                        clipboard?.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        testTextFieldValue = TextFieldValue("")
                                    },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                        imm?.showInputMethodPicker()
                                    },
                                    modifier = Modifier.weight(1.3f).height(32.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.TouchApp, contentDescription = "Pick IME", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick System IME", fontSize = 11.sp)
                                }
                            }

                            // Embedded Live Keyboard View
                            if (showInAppKeyboard) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(310.dp)
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

                            // Emulator physical keyboard advisory note
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.keySpecialBackground.copy(alpha = 0.5f))
                                    .border(0.8.dp, colors.keyBorder, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Emulator Tip: In streaming Android emulators, Android detects your computer keyboard as a physical keyboard, which causes Android to hide on-screen keyboards by default. You can test all Charbon features directly with the interactive keyboard above, or enable 'Show virtual keyboard' in Android Settings > Languages & input > Physical keyboard.",
                                        color = colors.textSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Charbon bundles high-density Noto Sans Symbols with complete coverage of:\n" +
                                        "• Geometric Shapes (▭ U+25AD White rectangle, ■, □, ▬, ▲, ▼, ◆, ◉)\n" +
                                        "• Box Drawing & Framing (╔, ╗, ╚, ╝, ║, ═, ╬)\n" +
                                        "• Block Elements (█, ▀, ▄, ▌, ░, ▒, ▓)\n" +
                                        "• Mathematical Operators & Calculus (∑, ∏, √, ∞, ∫, ≈, ≠, ≤, ≥)\n" +
                                        "• Arrows & Currency (←, ↑, →, ➔, ⇐, ⇒, €, ₽, ₹, ₿)\n" +
                                        "• Subscripts, Greek, Dingbats, Runic, Braille, and Enclosed numbers.",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
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
    val statusBg = if (isReady) Color(0xFF10B981).copy(alpha = 0.15f) else colors.accent.copy(alpha = 0.12f)
    val statusBorder = if (isReady) Color(0xFF10B981).copy(alpha = 0.4f) else colors.accent.copy(alpha = 0.3f)
    val statusIcon = if (isReady) Icons.Default.CheckCircle else Icons.Default.Settings
    val statusColor = if (isReady) Color(0xFF10B981) else colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(statusBg)
            .border(1.dp, statusBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isReady) "Charbon Keyboard is Active & Ready!" else "Setup Charbon as Your Keyboard",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isReady)
                        "You can now type extended Unicode in any app (browser, notes, chat, terminal)."
                    else
                        "Complete the two steps below to enable and set Charbon as default.",
                    color = colors.textSecondary,
                    fontSize = 12.sp
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
        colors = CardDefaults.cardColors(containerColor = colors.toolbarBackground),
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) Color(0xFF10B981) else colors.keySpecialBackground)
                        .border(1.dp, if (isCompleted) Color(0xFF10B981) else colors.keyBorder, CircleShape),
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
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.accent else colors.keyBackground)
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
            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) colors.accentText else colors.textSecondary,
                modifier = Modifier.size(14.dp)
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

package com.example.ui.keyboard

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCharbonColors

@Composable
fun CursorPadView(
    onSendKeyEvent: (Int) -> Unit,
    onSelectAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCharbonColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.keyboardBackground)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Jump Row (Home, Word Left, Word Right, End)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CursorActionButton(
                label = "Line Start",
                icon = Icons.Default.FirstPage,
                modifier = Modifier.weight(1f),
                onClick = { onSendKeyEvent(KeyEvent.KEYCODE_MOVE_HOME) }
            )
            CursorActionButton(
                label = "Select All",
                icon = Icons.Default.SelectAll,
                modifier = Modifier.weight(1f),
                onClick = onSelectAll
            )
            CursorActionButton(
                label = "Line End",
                icon = Icons.Default.LastPage,
                modifier = Modifier.weight(1f),
                onClick = { onSendKeyEvent(KeyEvent.KEYCODE_MOVE_END) }
            )
        }

        // D-Pad Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up Arrow
            Row(horizontalArrangement = Arrangement.Center) {
                DPadButton(
                    icon = Icons.Default.ArrowUpward,
                    contentDescription = "Cursor Up",
                    onClick = { onSendKeyEvent(KeyEvent.KEYCODE_DPAD_UP) },
                    modifier = Modifier.testTag("dpad_up")
                )
            }

            // Left, Down, Right
            Row(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DPadButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cursor Left",
                    onClick = { onSendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT) },
                    modifier = Modifier.testTag("dpad_left")
                )

                DPadButton(
                    icon = Icons.Default.ArrowDownward,
                    contentDescription = "Cursor Down",
                    onClick = { onSendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN) },
                    modifier = Modifier.testTag("dpad_down")
                )

                DPadButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Cursor Right",
                    onClick = { onSendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT) },
                    modifier = Modifier.testTag("dpad_right")
                )
            }
        }
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCharbonColors.current
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.keyBackground)
            .border(1.dp, colors.keyBorder, RoundedCornerShape(12.dp))
            .repeatingClickable(initialDelayMillis = 350L, repeatIntervalMillis = 65L) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.textPrimary,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun CursorActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalCharbonColors.current
    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(colors.keySpecialBackground)
            .border(0.8.dp, colors.keyBorder, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colors.accent,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            color = colors.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

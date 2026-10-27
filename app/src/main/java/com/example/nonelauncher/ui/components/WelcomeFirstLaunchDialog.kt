package com.example.nonelauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nonelauncher.ui.theme.Board
import com.example.nonelauncher.ui.theme.BoardActionButton
import com.example.nonelauncher.ui.theme.BoardBodyStyle
import com.example.nonelauncher.ui.theme.BoardLabelStyle
import com.example.nonelauncher.ui.theme.BoardPanel
import com.example.nonelauncher.ui.theme.FlapWordmark

@Composable
fun WelcomeFirstLaunchDialog(
    isDefaultLauncher: Boolean,
    onGetStarted: () -> Unit,
) {
    Dialog(onDismissRequest = onGetStarted) {
        BoardPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlapWordmark(text = "nONE", cellHeight = 30.dp, fontSize = 16.sp)

                Text(
                    text = "A DAY AS A DEPARTURE BOARD",
                    color = Board.Steel,
                    style = BoardLabelStyle
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isDefaultLauncher) {
                        "Tap a row to set the current step. Set your 1 Thing on the white band. Swipe the handle left for Structure, right for search; swipe up for the dock."
                    } else {
                        "Thanks for installing. Set nONE as your default Home app, then tap a row to set your step, swipe the handle left for Structure and right for search."
                    },
                    style = BoardBodyStyle.copy(fontSize = 13.sp),
                    color = Board.Paint.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoardActionButton(
                        text = "Get Started",
                        onClick = onGetStarted
                    )
                }
            }
        }
    }
}

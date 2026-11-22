package com.example.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ConceptImageSize
import com.example.ui.components.FrontierGlassCard
import com.example.ui.components.FrontierSheetScaffold
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CreamText
import com.example.ui.theme.LichenMuted
import com.example.ui.theme.ObsidianForest
import com.example.ui.theme.PanelBorder
import com.example.ui.theme.PanelCard
import com.example.ui.theme.PanelDark
import com.example.ui.theme.PanelElevated
import com.example.viewmodel.ImageStudioUiState

@Composable
fun ConceptStudioSheet(
    prompt: String,
    selectedSize: ConceptImageSize,
    studioState: ImageStudioUiState,
    isFirebaseEnabled: Boolean,
    onPromptChange: (String) -> Unit,
    onSelectSize: (ConceptImageSize) -> Unit,
    onGenerate: () -> Unit,
    onDismiss: () -> Unit
) {
    FrontierSheetScaffold(
        title = "Concept Art Studio",
        subtitle = "Design 1K, 2K & 4K frontier outposts",
        headerIcon = Icons.Filled.Brush,
        accentColor = AmberGold,
        closeTestTag = "btn_close_studio",
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            if (!isFirebaseEnabled) {
                FrontierGlassCard(
                    accentBorder = AmberGold,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = AmberBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.firebase_required_notice_studio),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CreamText
                        )
                    }
                }
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("Concept Art Prompt", color = LichenMuted) },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = PanelBorder,
                    focusedTextColor = CreamText,
                    unfocusedTextColor = CreamText,
                    focusedContainerColor = PanelCard,
                    unfocusedContainerColor = PanelCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("studio_prompt_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            val presets = listOf(
                "Volcanic magma smelter fortress with golden ore bridge",
                "Frozen aurora crystal drill outpost in low-poly 3D",
                "Ancient jungle temple relic synthesizer with glowing cyan gems"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(presets) { preset ->
                    AssistChip(
                        onClick = { onPromptChange(preset) },
                        label = {
                            Text(
                                text = preset.take(28) + "...",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = PanelCard,
                            labelColor = CreamText
                        ),
                        border = BorderStroke(1.dp, PanelBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Output Resolution",
                style = MaterialTheme.typography.labelLarge,
                color = CreamText
            )
            Spacer(modifier = Modifier.height(6.dp))

            val sizes = ConceptImageSize.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                sizes.forEachIndexed { index, size ->
                    SegmentedButton(
                        selected = selectedSize == size,
                        onClick = { onSelectSize(size) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = sizes.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = AmberGold,
                            activeContentColor = ObsidianForest,
                            inactiveContainerColor = PanelCard,
                            inactiveContentColor = LichenMuted,
                            activeBorderColor = AmberGold,
                            inactiveBorderColor = PanelBorder
                        ),
                        modifier = Modifier.testTag("btn_size_${size.label.lowercase()}")
                    ) {
                        Text("${size.label} (${size.pixelDim}px)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onGenerate,
                enabled = studioState !is ImageStudioUiState.Loading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberGold,
                    contentColor = ObsidianForest,
                    disabledContainerColor = PanelElevated
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("btn_generate_concept")
            ) {
                Icon(Icons.Filled.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate ${selectedSize.label} Concept",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            FrontierGlassCard(modifier = Modifier.height(220.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (studioState) {
                        is ImageStudioUiState.Idle -> {
                            Text(
                                text = "Choose 1K, 2K, or 4K and tap Generate Concept.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LichenMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        is ImageStudioUiState.Loading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = AmberGold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Rendering ${studioState.size.label} concept...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CreamText
                                )
                            }
                        }
                        is ImageStudioUiState.Success -> {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    bitmap = studioState.result.bitmap.asImageBitmap(),
                                    contentDescription = stringResource(R.string.cd_generated_concept),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = PanelDark.copy(alpha = 0.9f),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                ) {
                                    val prefix = if (studioState.result.isSamplePlaceholder) "Sample Preview" else "Gemini"
                                    Text(
                                        text = "$prefix • ${studioState.result.size.label} (${studioState.result.size.pixelDim}px)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AmberBright,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                        is ImageStudioUiState.Error -> {
                            Text(
                                text = "Error: ${studioState.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoralCrimson,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

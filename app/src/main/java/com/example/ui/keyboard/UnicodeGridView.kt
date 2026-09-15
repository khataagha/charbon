package com.example.ui.keyboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UnicodeBlock
import com.example.model.UnicodeCharacter
import com.example.repository.UnicodeRepository
import com.example.ui.theme.LocalCharbonColors
import com.example.ui.theme.NotoSansSymbolsFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnicodeGridView(
    activeBlock: UnicodeBlock?,
    isFavoritesActive: Boolean,
    isRecentsActive: Boolean,
    activeCustomCollection: String? = null,
    customCollections: Map<String, List<Int>> = emptyMap(),
    favoritesList: List<Int>,
    recentsList: List<Int>,
    selectedCharacter: UnicodeCharacter?,
    onSelectBlock: (UnicodeBlock) -> Unit,
    onSelectFavorites: () -> Unit,
    onSelectRecents: () -> Unit,
    onSelectCustomCollection: (String) -> Unit = {},
    onCharacterClick: (UnicodeCharacter) -> Unit,
    onCharacterLongClick: (UnicodeCharacter) -> Unit,
    onOpenInspector: (UnicodeCharacter) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCharbonColors.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    // Resolve characters to display based on category, search, favorites, recents, or custom collection
    val displayCharacters: List<UnicodeCharacter> = remember(
        searchQuery,
        activeBlock,
        isFavoritesActive,
        isRecentsActive,
        activeCustomCollection,
        customCollections,
        favoritesList,
        recentsList
    ) {
        if (searchQuery.isNotBlank()) {
            UnicodeRepository.search(searchQuery)
        } else if (isFavoritesActive) {
            favoritesList.map { UnicodeRepository.getCharacter(it) }
        } else if (isRecentsActive) {
            recentsList.map { UnicodeRepository.getCharacter(it) }
        } else if (activeCustomCollection != null && customCollections.containsKey(activeCustomCollection)) {
            val list = customCollections[activeCustomCollection] ?: emptyList()
            list.map { UnicodeRepository.getCharacter(it) }
        } else if (activeBlock != null) {
            UnicodeRepository.getCharactersForBlock(activeBlock)
        } else {
            UnicodeRepository.getCharactersForBlock(UnicodeRepository.BLOCKS.first())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.keyboardBackground)
    ) {
        // Search & Category Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.toolbarBackground)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchExpanded) {
                // Search Input Field
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(colors.keyboardBackground)
                        .border(1.dp, colors.accent.copy(alpha = 0.5f), RoundedCornerShape(17.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("unicode_search_input"),
                        singleLine = true,
                        cursorBrush = SolidColor(colors.accent),
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 13.sp
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search by char, hex (25AD), or name...",
                                    color = colors.textSecondary.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        isSearchExpanded = false
                        searchQuery = ""
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Close search",
                        tint = colors.textSecondary
                    )
                }
            } else {
                // Search Icon trigger
                IconButton(
                    onClick = { isSearchExpanded = true },
                    modifier = Modifier.size(34.dp).testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Open search",
                        tint = colors.textSecondary
                    )
                }

                // Scrollable category tabs
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorites Chip
                    FilterChip(
                        selected = isFavoritesActive,
                        onClick = onSelectFavorites,
                        label = {
                            Text("★ Favs", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = colors.accentText,
                            containerColor = colors.keyBackground,
                            labelColor = colors.textPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isFavoritesActive,
                            borderColor = colors.keyBorder,
                            selectedBorderColor = colors.accent
                        ),
                        modifier = Modifier.height(28.dp).testTag("chip_favorites")
                    )

                    // Recents Chip
                    FilterChip(
                        selected = isRecentsActive,
                        onClick = onSelectRecents,
                        label = {
                            Text("🕒 Recents", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.accent,
                            selectedLabelColor = colors.accentText,
                            containerColor = colors.keyBackground,
                            labelColor = colors.textPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isRecentsActive,
                            borderColor = colors.keyBorder,
                            selectedBorderColor = colors.accent
                        ),
                        modifier = Modifier.height(28.dp).testTag("chip_recents")
                    )

                    // Custom User Collections Chips
                    for ((collectionName, _) in customCollections) {
                        val isSelected = activeCustomCollection == collectionName
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCustomCollection(collectionName) },
                            label = {
                                Text("📁 $collectionName", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.accentText,
                                containerColor = colors.keyBackground,
                                labelColor = colors.textPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = colors.keyBorder,
                                selectedBorderColor = colors.accent
                            ),
                            modifier = Modifier.height(28.dp).testTag("chip_col_$collectionName")
                        )
                    }

                    // All Unicode Blocks
                    for (block in UnicodeRepository.BLOCKS) {
                        val isSelected = !isFavoritesActive && !isRecentsActive && activeCustomCollection == null && activeBlock?.id == block.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectBlock(block) },
                            label = {
                                Text(
                                    text = "${block.icon} ${block.name}",
                                    fontSize = 11.sp,
                                    fontFamily = NotoSansSymbolsFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.accentText,
                                containerColor = colors.keyBackground,
                                labelColor = colors.textPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = colors.keyBorder,
                                selectedBorderColor = colors.accent
                            ),
                            modifier = Modifier.height(28.dp).testTag("chip_${block.id}")
                        )
                    }
                }
            }
        }

        // Live Character Status & Quick Inspector Banner
        if (selectedCharacter != null) {
            val isCurrentFav = favoritesList.contains(selectedCharacter.codePoint)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.keySpecialBackground)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedCharacter.char,
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontFamily = NotoSansSymbolsFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "U+${selectedCharacter.hex} • ${selectedCharacter.name}",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleFavorite(selectedCharacter.codePoint) },
                        modifier = Modifier.size(28.dp).testTag("quick_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isCurrentFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isCurrentFav) "Remove from favorites" else "Add to favorites",
                            tint = if (isCurrentFav) Color(0xFFFFB300) else colors.textSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = { onOpenInspector(selectedCharacter) },
                        modifier = Modifier.size(28.dp).testTag("quick_inspector_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Inspect ${selectedCharacter.char}",
                            tint = colors.accent,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // Character Grid (8 columns for optimal key size and tap target)
        if (displayCharacters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isFavoritesActive) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = colors.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "No favorites yet",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap any character & tap the star (★), or hold a character to inspect & favorite.",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                } else {
                    Text(
                        text = "No characters found",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("unicode_characters_grid")
            ) {
                items(
                    items = displayCharacters,
                    key = { it.codePoint }
                ) { charItem ->
                    val isCurrent = selectedCharacter?.codePoint == charItem.codePoint
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrent) colors.keyBackgroundPressed else colors.keyBackground)
                            .border(
                                width = if (isCurrent) 1.5.dp else 0.8.dp,
                                color = if (isCurrent) colors.accent else colors.keyBorder,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .combinedClickable(
                                onClick = { onCharacterClick(charItem) },
                                onLongClick = { onCharacterLongClick(charItem) }
                            )
                            .testTag("key_char_${charItem.hex}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = charItem.char,
                                color = colors.textPrimary,
                                fontSize = 18.sp,
                                fontFamily = NotoSansSymbolsFamily,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = charItem.hex,
                                color = colors.textSecondary.copy(alpha = 0.8f),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

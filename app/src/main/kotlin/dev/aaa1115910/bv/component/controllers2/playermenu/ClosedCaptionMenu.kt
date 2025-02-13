package dev.aaa1115910.bv.component.controllers2.playermenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.itemsIndexed
import dev.aaa1115910.biliapi.entity.video.VideoMoreInfo
import dev.aaa1115910.bv.component.FocusGroup
import dev.aaa1115910.bv.component.controllers.LocalVideoPlayerControllerData
import dev.aaa1115910.bv.component.controllers2.LocalMenuFocusStateData
import dev.aaa1115910.bv.component.controllers2.MenuFocusState
import dev.aaa1115910.bv.component.controllers2.VideoPlayerClosedCaptionMenuItem
import dev.aaa1115910.bv.component.controllers2.playermenu.component.MenuListItem
import dev.aaa1115910.bv.component.controllers2.playermenu.component.RadioMenuList
import dev.aaa1115910.bv.component.controllers2.playermenu.component.StepLessMenuItem
import java.text.NumberFormat

@OptIn(ExperimentalTvFoundationApi::class)
@Composable
fun ClosedCaptionMenuList(
    modifier: Modifier = Modifier,
    onSubtitleChange: (VideoMoreInfo.SubtitleItem) -> Unit,
    onSubtitleSizeChange: (TextUnit) -> Unit,
    onSubtitleBackgroundOpacityChange: (Float) -> Unit,
    onSubtitleBottomPadding: (Dp) -> Unit,
    onFocusStateChange: (MenuFocusState) -> Unit
) {
    val context = LocalContext.current
    val data = LocalVideoPlayerControllerData.current
    val focusState = LocalMenuFocusStateData.current

    val focusRequester = remember { FocusRequester() }
    var selectedClosedCaptionMenuItem by remember { mutableStateOf(VideoPlayerClosedCaptionMenuItem.Switch) }

    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val menuItemsModifier = Modifier
            .width(200.dp)
            .padding(horizontal = 8.dp)
        AnimatedVisibility(visible = focusState.focusState != MenuFocusState.MenuNav) {
            when (selectedClosedCaptionMenuItem) {
                VideoPlayerClosedCaptionMenuItem.Switch -> RadioMenuList(
                    modifier = menuItemsModifier,
                    items = data.availableSubtitleTracks.map { it.lanDoc },
                    selected = data.availableSubtitleTracks.indexOfFirst { it.id == data.currentSubtitleId },
                    onSelectedChanged = { onSubtitleChange(data.availableSubtitleTracks[it]) },
                    onFocusBackToParent = {
                        onFocusStateChange(MenuFocusState.Menu)
                        focusRequester.requestFocus()
                    },
                )

                VideoPlayerClosedCaptionMenuItem.Size -> StepLessMenuItem(
                    modifier = menuItemsModifier,
                    value = data.currentSubtitleFontSize.value.toInt(),
                    step = 1,
                    range = 12..48,
                    text = "${data.currentSubtitleFontSize.value.toInt()} SP",
                    onValueChange = { onSubtitleSizeChange(it.sp) },
                    onFocusBackToParent = { onFocusStateChange(MenuFocusState.Menu) }
                )

                VideoPlayerClosedCaptionMenuItem.Opacity -> StepLessMenuItem(
                    modifier = menuItemsModifier,
                    value = data.currentSubtitleBackgroundOpacity,
                    step = 0.01f,
                    range = 0f..1f,
                    text = NumberFormat.getPercentInstance()
                        .apply { maximumFractionDigits = 0 }
                        .format(data.currentSubtitleBackgroundOpacity),
                    onValueChange = onSubtitleBackgroundOpacityChange,
                    onFocusBackToParent = { onFocusStateChange(MenuFocusState.Menu) }
                )

                VideoPlayerClosedCaptionMenuItem.Padding -> StepLessMenuItem(
                    modifier = menuItemsModifier,
                    value = data.currentSubtitleBottomPadding.value.toInt(),
                    step = 1,
                    range = 0..48,
                    text = "${data.currentSubtitleBottomPadding.value.toInt()} DP",
                    onValueChange = { onSubtitleBottomPadding(it.dp) },
                    onFocusBackToParent = { onFocusStateChange(MenuFocusState.Menu) }
                )
            }
        }
        FocusGroup(
            modifier = Modifier.focusRequester(focusRequester),
        ) {
            TvLazyColumn(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyUp) {
                            if (listOf(Key.Enter, Key.DirectionCenter).contains(it.key)) {
                                return@onPreviewKeyEvent false
                            }
                            return@onPreviewKeyEvent true
                        }
                        when (it.key) {
                            Key.DirectionRight -> onFocusStateChange(MenuFocusState.MenuNav)
                            Key.DirectionLeft -> onFocusStateChange(MenuFocusState.Items)
                            else -> {}
                        }
                        false
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(VideoPlayerClosedCaptionMenuItem.values()) { index, item ->
                    val buttonModifier =
                        (if (index == 0) Modifier.initiallyFocused() else Modifier.restorableFocus())
                            .width(200.dp)
                    MenuListItem(
                        modifier = buttonModifier,
                        text = item.getDisplayName(context),
                        selected = selectedClosedCaptionMenuItem == item,
                        onClick = {},
                        onFocus = { selectedClosedCaptionMenuItem = item },
                    )
                }
            }
        }
    }
}
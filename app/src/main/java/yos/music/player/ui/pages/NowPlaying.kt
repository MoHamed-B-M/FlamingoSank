@file:Suppress("DEPRECATION")

package yos.music.player.ui.pages

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderPositions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastMap
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.blankj.utilcode.util.TimeUtils
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yos.music.player.R
import yos.music.player.code.MediaController
import yos.music.player.code.MediaController.mediaControl
import yos.music.player.code.MediaController.musicPlaying
import yos.music.player.code.MediaController.playingMusicList
import yos.music.player.code.SystemMediaControlResolver
import yos.music.player.code.VolumeChangeReceiver
import yos.music.player.code.YosPlaybackService
import yos.music.player.code.utils.lrc.YosMediaEvent
import yos.music.player.code.utils.lrc.YosUIConfig
import yos.music.player.code.utils.others.Vibrator
import yos.music.player.code.utils.player.FadeExo.fadePause
import yos.music.player.code.utils.player.FadeExo.fadePlay
import yos.music.player.data.libraries.FavPlayListLibrary
import yos.music.player.data.libraries.SettingsLibrary
import yos.music.player.data.libraries.YosMediaItem
import yos.music.player.data.libraries.artistsName
import yos.music.player.data.libraries.defaultArtistsName
import yos.music.player.data.libraries.defaultTitle
import yos.music.player.data.models.MainViewModel
import yos.music.player.data.models.MediaViewModel
import yos.music.player.data.models.MediaViewModelObject
import yos.music.player.ui.pages.NowPlayingPage.Album
import yos.music.player.ui.pages.NowPlayingPage.Lyric
import yos.music.player.ui.pages.NowPlayingPage.PlayingList
import yos.music.player.ui.theme.YosRoundedCornerShape
import yos.music.player.ui.widgets.YosLyricView
import yos.music.player.ui.widgets.effects.YosFloatingLight
import yos.music.player.ui.widgets.audio.MusicQualityIndicator
import yos.music.player.ui.widgets.basic.ImageQuality
import yos.music.player.ui.widgets.basic.ShadowImageWithCache
import yos.music.player.ui.widgets.basic.YosWrapper
import yos.music.player.ui.widgets.effects.ShadowType
import yos.music.player.ui.widgets.effects.overlayEffect

@Stable
object NowPlayingPage {
    const val Album = "Album"
    const val PlayingList = "PlayingList"
    const val Lyric = "Lyric"
}

private const val ShareAlbumKey = "album"
private const val AnimDurationMillis = 300

@ExperimentalSharedTransitionApi
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun NowPlaying(
    mainViewModel: MainViewModel,
    mediaViewModel: MediaViewModel,
    navController: NavController,
    isPlayingStatusLambda: () -> Boolean,
    isPlayingOnChanged: (Boolean) -> Unit,
    nowPageLambda: () -> String,
    showMiniPlayer: () -> Boolean,
    nowPageOnChanged: (String) -> Unit
) = Surface(
    modifier = Modifier.fillMaxSize(),
    contentColor = Color.White,
    color = Color.Transparent
) {
    val context = LocalContext.current

    val lrcEntries by MediaViewModelObject.lrcEntries.collectAsState()
    val bitmap by MediaViewModelObject.bitmap.collectAsState()

    val thisMusicPlaying = remember { musicPlaying }

    val lastClickTime = rememberSaveable { mutableLongStateOf(0L) }
    val showControl = rememberSaveable { mutableStateOf(true) }
    val translation = rememberSaveable { 
        derivedStateOf { SettingsLibrary.NowPlayingTranslation } 
    }

    // Derived states for better performance
    val shuffleModeEnabled = remember { derivedStateOf { mediaControl?.shuffleModeEnabled ?: false } }
    val repeatMode = remember { derivedStateOf { mediaControl?.repeatMode ?: REPEAT_MODE_OFF } }

    // Touch timeout effect
    LaunchedEffect(showControl.value, nowPageLambda(), lastClickTime.longValue) {
        if (nowPageLambda() != Lyric && !showControl.value) {
            showControl.value = true
        }
        if (showControl.value) {
            val time = 2500L
            delay(time)
            if (TimeUtils.getNowMills() - lastClickTime.longValue >= time && nowPageLambda() == Lyric) {
                showControl.value = false
            }
        }
    }

    // Background floating lights
    YosFloatingLight(
        album = { bitmap },
        isPlaying = isPlayingStatusLambda,
        modifier = Modifier.fillMaxSize(),
        nowPage = { nowPageLambda() },
        showMiniPlayer = showMiniPlayer
    )

    // Main content area
    SharedTransitionLayout {
        Column(Modifier.fillMaxSize()) {
            // Handle bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .overlayEffect()
                        .size(width = 32.dp, height = 4.5.dp)
                        .background(Color(0x4DFFFFFF), RoundedCornerShape(2.25.dp))
                        .clip(RoundedCornerShape(2.25.dp))
                )
            }

            // Main content switcher
            Crossfade(
                targetState = nowPageLambda(),
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 22.dp)
            ) { page ->
                when (page) {
                    Album -> AlbumContent(thisMusicPlaying, isPlayingStatusLambda)
                    Lyric -> LyricContent(bitmap, thisMusicPlaying) { nowPageOnChanged(Album) }
                    PlayingList -> PlaylistContent(shuffleModeEnabled, repeatMode, thisMusicPlaying)
                }
            }

            // Player controls
            PlayerControls(
                showControl = showControl.value,
                isPlayingStatusLambda = isPlayingStatusLambda,
                isPlayingOnChanged = isPlayingOnChanged,
                translation = translation.value,
                translationEnabled = showControl.value,
                nowPage = nowPageLambda(),
                nowPageOnChanged = nowPageOnChanged,
                lastClickTime = { lastClickTime.longValue = TimeUtils.getNowMills() }
            )
        }
    }
}

@Composable
private fun AlbumContent(
    thisMusicPlaying: MutableState<YosMediaItem?>,
    isPlayingStatusLambda: () -> Boolean
) {
    Column(
        Modifier
            .fillMaxSize()
            .clickable(enabled = false, onClick = {})
    ) {
        Column(Modifier.fillMaxHeight(0.595f)) {
            Album(
                modifier = Modifier,
                albumUrl = { thisMusicPlaying.value?.thumb },
                isPlaying = isPlayingStatusLambda
            )
            
            AnimatedContent(
                targetState = thisMusicPlaying.value,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.padding(horizontal = 32.dp)
            ) { music ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(end = 15.dp)
                    ) {
                        Text(
                            text = music?.title ?: defaultTitle,
                            fontSize = 19.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = music?.artistsName ?: defaultArtistsName,
                            fontSize = 18.5.sp,
                            modifier = Modifier.overlayEffect(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White.copy(alpha = 0.35f)
                        )
                    }
                    ActionButtonsRow { music }
                }
            }
        }
    }
}

@Composable
private fun LyricContent(
    bitmap: Uri?,
    thisMusicPlaying: MutableState<YosMediaItem?>,
    onAlbumClick: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        PlayingBar(
            modifier = Modifier,
            albumUrlLambda = { bitmap },
            musicPlayingLambda = { thisMusicPlaying.value },
            onAlbumClick = onAlbumClick
        )
    }
}

@Composable
private fun PlaylistContent(
    shuffleModeEnabled: State<Boolean>,
    repeatMode: State<Int>,
    thisMusicPlaying: MutableState<YosMediaItem?>
) {
    Column(
        Modifier
            .fillMaxSize()
            .clickable(enabled = false, onClick = {})
    ) {
        PlayingBar(
            modifier = Modifier,
            albumUrlLambda = { thisMusicPlaying.value?.thumb },
            musicPlayingLambda = { thisMusicPlaying.value }
        ) { /* Not used here */ }
        
        PlayingList(
            shuffleModeEnabledLambda = { shuffleModeEnabled.value },
            shuffleModeOnChanged = { mediaControl?.shuffleModeEnabled = it },
            repeatModeLambda = { repeatMode.value },
            repeatModeOnChanged = { mediaControl?.repeatMode = it },
            thisMusicPlayingLambda = { thisMusicPlaying.value }
        )
    }
}

@Composable
private fun PlayerControls(
    showControl: Boolean,
    isPlayingStatusLambda: () -> Boolean,
    isPlayingOnChanged: (Boolean) -> Unit,
    translation: Boolean,
    translationEnabled: Boolean,
    nowPage: () -> String,
    nowPageOnChanged: (String) -> Unit,
    lastClickTime: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            Modifier
                .fillMaxHeight(0.437f)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = showControl,
                enter = fadeIn() + expandVertically(
                    expandFrom = Alignment.Top,
                    initialHeight = { (it / 1.4).toInt() }
                ),
                exit = fadeOut() + shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    targetHeight = { (it / 1.4).toInt() }
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp, bottom = 15.dp)
                        .padding(horizontal = 25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Translation button
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .graphicsLayer { compositingStrategy = CompositingStrategy.ModulateAlpha },
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .overlayEffect()
                                .alpha(0.4f)
                                .clickable(
                                    enabled = translationEnabled,
                                    onClick = {
                                        Vibrator.click(context)
                                        SettingsLibrary.NowPlayingTranslation = !translation
                                        lastClickTime()
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(targetState = translation) { trans ->
                                Icon(
                                    painter = painterResource(
                                        id = if (trans) R.drawable.ic_nowplaying_translateon 
                                        else R.drawable.ic_nowplaying_translate
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    // Player control core
                    PlayerControlCore(
                        isPlayingLambda = isPlayingStatusLambda,
                        isPlayingOnChanged = isPlayingOnChanged,
                        onPrevious = {
                            mediaControl?.seekToPreviousMediaItem()
                            lastClickTime()
                        },
                        onStatus = { status ->
                            if (status) mediaControl?.fadePlay() else mediaControl?.fadePause()
                            lastClickTime()
                        },
                        onNext = {
                            mediaControl?.seekToNextMediaItem()
                            lastClickTime()
                        },
                        onSeek = { mediaControl?.seekTo(it.toLong()) },
                        onLyrics = {
                            if (nowPage() == Lyric) nowPageOnChanged(Album) 
                            else nowPageOnChanged(Lyric)
                        },
                        onPlaylist = {
                            if (nowPage() == PlayingList) nowPageOnChanged(Album) 
                            else nowPageOnChanged(PlayingList)
                        },
                        nowPage = nowPage,
                        onSlider = { lastClickTime() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.Album(
    modifier: Modifier,
    albumUrl: () -> Uri?,
    isPlaying: () -> Boolean
) = Box(
    Modifier
        .weight(1f)
        .padding(top = 20.dp, bottom = 33.dp)
        .padding(horizontal = 15.dp),
    contentAlignment = Alignment.BottomCenter
) {
    val springSpec = remember { SpringSpec<Float>(stiffness = 300f, dampingRatio = 1f) }
    val tweenSpec = remember { TweenSpec<Float>(durationMillis = 350, easing = EaseOutQuart) }

    val scale = animateFloatAsState(
        targetValue = if (isPlaying()) 0f else 1f,
        animationSpec = if (isPlaying()) springSpec else tweenSpec,
        label = "albumScale"
    )

    ShadowImageWithCache(
        dataLambda = albumUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = CompositingStrategy.ModulateAlpha }
            .padding(
                start = (7 + (27 * scale.value)).dp,
                end = (7 + (27 * scale.value)).dp,
                bottom = (7 + (27 * scale.value)).dp
            )
            .then(modifier),
        imageQuality = ImageQuality.RAW,
        shadowOverlay = true
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayingList(
    shuffleModeEnabledLambda: () -> Boolean,
    shuffleModeOnChanged: (Boolean) -> Unit,
    repeatModeLambda: () -> Int,
    repeatModeOnChanged: (Int) -> Unit,
    thisMusicPlayingLambda: () -> YosMediaItem?
) {
    val context = LocalContext.current
    val musicList = remember { playingMusicList }
    val hide = derivedStateOf { 
        musicList.value.isNullOrEmpty() || shuffleModeEnabledLambda() 
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.545f)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(top = 10.dp)
                .height(65.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.page_library_playlists),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        id = R.string.page_library_playlists_music_total,
                        musicList.value?.size ?: 0
                    ),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .overlayEffect()
                        .alpha(0.35f)
                )
            }

            Row(modifier = Modifier.overlayEffect().alpha(0.6f)) {
                // Shuffle button
                val shuffleBackgroundAlpha = animateFloatAsState(
                    targetValue = if (shuffleModeEnabledLambda()) 0.9f else 0f,
                    label = "shuffleAlpha"
                )
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                Vibrator.click(context)
                                shuffleModeOnChanged(!shuffleModeEnabledLambda())
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = shuffleBackgroundAlpha.value),
                            shape = YosRoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val shuffleIconTint = animateColorAsState(
                        targetValue = if (shuffleModeEnabledLambda()) Color.Black else Color.White,
                        label = "shuffleTint"
                    )
                    Icon(
                        painterResource(id = R.drawable.ic_nowplaying_shuffle),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = shuffleIconTint.value
                    )
                }

                // Repeat button
                val repeatHighlight = repeatModeLambda() == REPEAT_MODE_ALL || 
                                      repeatModeLambda() == REPEAT_MODE_ONE
                val repeatBackgroundAlpha = animateFloatAsState(
                    targetValue = if (repeatHighlight) 0.9f else 0f,
                    label = "repeatAlpha"
                )
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                Vibrator.click(context)
                                val targetMode = when (repeatModeLambda()) {
                                    REPEAT_MODE_OFF -> REPEAT_MODE_ALL
                                    REPEAT_MODE_ALL -> REPEAT_MODE_ONE
                                    else -> REPEAT_MODE_OFF
                                }
                                repeatModeOnChanged(targetMode)
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(start = 10.dp)
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = repeatBackgroundAlpha.value),
                            shape = YosRoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = repeatModeLambda(),
                        transitionSpec = { fadeIn() togetherWith fadeOut() }
                    ) { mode ->
                        val iconTint = animateColorAsState(
                            targetValue = if (repeatHighlight) Color.Black else Color.White,
                            label = "repeatTint"
                        )
                        Icon(
                            painter = painterResource(
                                id = when (mode) {
                                    REPEAT_MODE_ONE -> R.drawable.ic_nowplaying_repeatone
                                    else -> R.drawable.ic_nowplaying_repeat
                                }
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = iconTint.value
                        )
                    }
                }
            }
        }

        if (hide.value) {
            EmptyPlaylistMessage()
        } else {
            PlaylistItems(musicList, thisMusicPlayingLambda)
        }
    }
}

@Composable
private fun EmptyPlaylistMessage() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_uitabbar_library),
            contentDescription = null,
            modifier = Modifier
                .overlayEffect()
                .size(70.dp)
                .alpha(0.6f)
        )
        Text(
            text = stringResource(id = R.string.playlist_unavailable_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 18.dp)
        )
        Text(
            text = stringResource(
                id = if (playingMusicList.value.isNullOrEmpty()) 
                    R.string.playlist_unavailable_desc 
                else 
                    R.string.playlist_shuffle_desc
            ),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.overlayEffect().alpha(0.4f)
        )
    }
}

@Composable
private fun PlaylistItems(
    musicList: State<List<YosMediaItem>?>,
    thisMusicPlayingLambda: () -> YosMediaItem?
) {
    val scope = rememberCoroutineScope()
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = (musicList.value?.indexOf(musicPlaying.value) ?: 0) + 1,
        initialFirstVisibleItemScrollOffset = -15
    )

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black,
                                    Color.Black,
                                    Color.Transparent
                                )
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
                }
        ) {
            item("blank_before") { Spacer(Modifier.height(12.dp)) }
            
            items(
                items = musicList.value ?: emptyList(),
                key = { it.id ?: it.hashCode() }
            ) { music ->
                SmallMusicListItem(music) {
                    scope.launch(Dispatchers.IO) {
                        MediaController.prepare(music, musicList.value ?: emptyList())
                    }
                }
            }
            
            item("blank_after") { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun LazyItemScope.SmallMusicListItem(
    music: YosMediaItem,
    itemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .clickable(onClick = itemClick)
            .padding(horizontal = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShadowImageWithCache(
            dataLambda = { music.thumb },
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            cornerRadius = 4.dp,
            shadowAlpha = 0f,
            imageQuality = ImageQuality.LOW
        )

        Column(Modifier.padding(start = 14.dp)) {
            Text(
                text = music.title ?: defaultTitle,
                modifier = Modifier.padding(bottom = 1.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                lineHeight = 16.sp,
            )
            Text(
                text = music.artistsName ?: defaultArtistsName,
                modifier = Modifier.alpha(0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.5.sp,
                lineHeight = 11.5.sp,
            )
        }
    }
}

@Composable
private fun Lyric(
    lrcEntries: () -> List<List<Pair<Float, String>>>,
    weightLambda: () -> Boolean,
    translationLambda: () -> Boolean,
    mainViewModel: MainViewModel,
    mediaViewModel: MediaViewModel,
    onBackClick: () -> Unit
) = Column(Modifier.fillMaxSize()) {
    Spacer(modifier = Modifier.statusBarsHeight(110.dp))

    YosLyricView(
        lrcEntriesLambda = lrcEntries,
        liveTimeLambda = { (mediaControl?.currentPosition ?: 0).toInt() },
        mediaEvent = object : YosMediaEvent {
            override fun onSeek(position: Int) {
                mediaControl?.seekTo(position.toLong())
            }
        },
        translationLambda = translationLambda,
        blurLambda = { SettingsLibrary.LyricBlurEffect },
        uiConfig = YosUIConfig(noLrcText = stringResource(id = R.string.tip_no_lyrics)),
        weightLambda = weightLambda,
        modifier = Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x59000000),
                                Color.Black,
                                Color.Black,
                                Color(0x59000000),
                                Color(0x21000000),
                                Color.Transparent
                            )
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
            },
        onBackClick = onBackClick
    )
}

@Composable
private fun ActionButtonsRow(musicPlayingLambda: () -> YosMediaItem?) {
    val context = LocalContext.current
    val isFavorite = remember(musicPlayingLambda()) {
        derivedStateOf { musicPlayingLambda()?.let { FavPlayListLibrary.isFavorite(it) } ?: false }
    }

    Row(modifier = Modifier.overlayEffect(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clickable(
                    onClick = {
                        musicPlayingLambda()?.let { music ->
                            Vibrator.click(context)
                            if (FavPlayListLibrary.isFavorite(music)) {
                                FavPlayListLibrary.removeMusic(music)
                            } else {
                                FavPlayListLibrary.addMusic(music)
                            }
                        }
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = isFavorite.value, animationSpec = TweenSpec(300)) { favorited ->
                Icon(
                    painter = painterResource(
                        id = if (favorited) R.drawable.ic_nowplaying_favorited 
                        else R.drawable.ic_nowplaying_favorite
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .graphicsLayer { rotationZ = 90f }
                .clickable(
                    onClick = { /* TODO */ },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(id = R.drawable.ic_nowplaying_more),
                contentDescription = null,
                modifier = Modifier.size(28.dp).overlayEffect()
            )
        }
    }
}

@Composable
private fun PlayingBar(
    modifier: Modifier,
    albumUrlLambda: () -> Uri?,
    musicPlayingLambda: () -> YosMediaItem?,
    onAlbumClick: () -> Unit
) = Row(
    Modifier
        .fillMaxWidth()
        .padding(horizontal = 28.5.dp)
        .padding(top = 22.dp)
        .height(70.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    ShadowImageWithCache(
        dataLambda = albumUrlLambda,
        contentDescription = null,
        modifier = modifier
            .size(69.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onAlbumClick
            ),
        cornerRadius = 5.dp,
        imageQuality = ImageQuality.LOW,
        shadowType = ShadowType.Small,
        shadowOverlay = true
    )
    
    Column(
        Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(start = 12.dp, end = 15.dp)
    ) {
        Text(
            text = musicPlayingLambda()?.title ?: defaultTitle,
            fontSize = 16.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.5.sp
        )
        Text(
            text = musicPlayingLambda()?.artistsName ?: defaultArtistsName,
            fontSize = 15.sp,
            modifier = Modifier.overlayEffect(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White.copy(alpha = 0.35f)
        )
    }

    ActionButtonsRow(musicPlayingLambda)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerControlCore(
    isPlayingLambda: () -> Boolean,
    isPlayingOnChanged: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onStatus: (Boolean) -> Unit,
    onNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onLyrics: () -> Unit,
    onPlaylist: () -> Unit,
    nowPage: () -> String,
    onSlider: () -> Unit
) {
    val context = LocalContext.current
    val playingDuration = remember { produceState(0L) {
        while (true) {
            value = mediaControl?.duration ?: 0L
            delay(700)
        }
    } }
    
    val playingPosition = remember { produceState(0L) {
        while (true) {
            value = mediaControl?.currentPosition ?: 0L
            delay(700)
        }
    } }
    
    val (sliderPosition, setSliderPosition) = remember { mutableFloatStateOf(0f) }
    val isSliding = remember { mutableStateOf(false) }
    
    // Derived time values
    val (playedTime, remainingTime) = remember(playingPosition.value, playingDuration.value, isSliding.value) {
        val totalSeconds = playingPosition.value / 1000
        val played = formatTime(totalSeconds)
        val remaining = formatTime((playingDuration.value - playingPosition.value) / 1000)
        played to "-$remaining"
    }

    // Update slider when not sliding
    LaunchedEffect(playingPosition.value, isSliding.value) {
        if (!isSliding.value) {
            setSliderPosition(playingPosition.value.toFloat())
        }
    }

    Column(Modifier.fillMaxWidth()) {
        // Progress slider
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                isSliding.value = true
                setSliderPosition(newValue)
                onSlider()
            },
            onValueChangeFinished = {
                Vibrator.longClick(context)
                onSeek(sliderPosition)
                isSliding.value = false
            },
            valueRange = 0f..playingDuration.value.toFloat().coerceAtLeast(0f),
            colors = SliderDefaults.colors(
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0x0DFFFFFF)
            ),
            modifier = Modifier
                .overlayEffect()
                .alpha(0.45f)
                .height(14.dp),
            thumb = { },
            track = { positions ->
                Track(
                    sliderPositions = positions,
                    height = 7.dp
                )
            }
        )

        // Time indicators
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 7.dp)
                .height(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = playedTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.overlayEffect()
                )
                Text(
                    text = remainingTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.overlayEffect()
                )
            }
            MusicQualityIndicator()
        }

        // Control buttons
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControlButton(
                    iconRes = R.drawable.ic_nowplaying_rewind,
                    onClick = { onPrevious() },
                    size = 61.dp
                )
                
                Spacer(Modifier.width(43.dp))
                
                PlayPauseButton(
                    isPlaying = isPlayingLambda(),
                    onStatusChange = { 
                        isPlayingOnChanged(!isPlayingLambda())
                        onStatus(isPlayingLambda())
                    },
                    size = 58.5.dp
                )
                
                Spacer(Modifier.width(43.dp))
                
                ControlButton(
                    iconRes = R.drawable.ic_nowplaying_fforward,
                    onClick = { onNext() },
                    size = 61.dp
                )
            }
        }

        // Bottom controls
        Row(
            modifier = Modifier
                .overlayEffect()
                .fillMaxWidth()
                .alpha(0.4f),
            horizontalArrangement = Arrangement.Center
        ) {
            // Lyrics button
            ControlToggleButton(
                iconRes = R.drawable.ic_nowplaying_lyrics,
                activeIconRes = R.drawable.ic_nowplaying_lyricson,
                isActive = { nowPage() == Lyric },
                onClick = onLyrics,
                size = 32.dp
            )

            Spacer(modifier = Modifier.weight(0.1f))

            AirPlay()

            Spacer(modifier = Modifier.weight(0.1f))

            // Playlist button
            ControlToggleButton(
                iconRes = R.drawable.ic_nowplaying_queue,
                activeIconRes = R.drawable.ic_nowplaying_queueon,
                isActive = { nowPage() == PlayingList },
                onClick = onPlaylist,
                size = 32.dp
            )
        }
    }
}

@Composable
private fun ControlButton(
    iconRes: Int,
    onClick: () -> Unit,
    size: Dp
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = {
                    Vibrator.click(context)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(10.dp)
        )
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    onStatusChange: (Boolean) -> Unit,
    size: Dp
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onClick = {
                    Vibrator.click(context)
                    onStatusChange(!isPlaying)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isPlaying,
            animationSpec = TweenSpec(200),
            modifier = Modifier.fillMaxSize()
        ) { playing ->
            if (playing) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nowplaying_pause),
                    contentDescription = "Pause",
                    modifier = Modifier.padding(10.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_nowplaying_play),
                    contentDescription = "Play",
                    modifier = Modifier.padding(9.dp)
                )
            }
        }
    }
}

@Composable
private fun ControlToggleButton(
    iconRes: Int,
    activeIconRes: Int,
    isActive: () -> Boolean,
    onClick: () -> Unit,
    size: Dp
) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .weight(1f)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isActive(),
            animationSpec = TweenSpec(300)
        ) { active ->
            Icon(
                painter = painterResource(id = if (active) activeIconRes else iconRes),
                contentDescription = null,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
private fun Track(
    sliderPositions: SliderPositions,
    modifier: Modifier = Modifier,
    height: Dp
) {
    val inactiveTrackColor = Color.White.copy(alpha = 0.5f)
    val activeTrackColor = Color.White
    val trackStrokeWidth = height.toPx()

    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderStart = if (isRtl) Offset(size.width, center.y) else Offset(0f, center.y)
        val sliderEnd = if (isRtl) Offset(0f, center.y) else Offset(size.width, center.y)
        
        // Inactive track
        drawLine(
            inactiveTrackColor,
            sliderStart,
            sliderEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
        
        // Active track
        val activeEnd = Offset(
            sliderStart.x + (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive,
            center.y
        )
        drawLine(
            activeTrackColor,
            sliderStart,
            activeEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
    }
}

// Helper function
private fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "$minutes:${if (secs < 10) "0$secs" else "$secs"}"
}

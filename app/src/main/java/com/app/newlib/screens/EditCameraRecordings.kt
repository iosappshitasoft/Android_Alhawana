package com.app.newlib.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ly.img.editor.EditorConfiguration
import ly.img.editor.EditorDefaults
import ly.img.editor.EditorUiMode
import ly.img.editor.EngineConfiguration
import ly.img.editor.VideoEditor
import ly.img.editor.core.UnstableEditorApi
import ly.img.editor.rememberForVideo

@OptIn(UnstableEditorApi::class)
@Composable
fun EditCameraRecordings(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        val engineConfig = EngineConfiguration.remember(
            license = "AoihtQwVuv8bKo1npDGkh0tqOGwfWh7fmJb9uoVMzMji5wZN7AnwldHpX9OpOjJE",
            onCreate = {
                EditorDefaults.onCreate(
                    engine = editorContext.engine,
                    eventHandler = editorContext.eventHandler,
                    sceneUri = EngineConfiguration.defaultVideoSceneUri,
                )
            },
        )
        val editorConfig = EditorConfiguration.rememberForVideo(
            uiMode = EditorUiMode.DARK ,
        )
        VideoEditor(
            engineConfiguration = engineConfig,
            editorConfiguration = editorConfig,
        ) {
            onBack()
        }
    }
}

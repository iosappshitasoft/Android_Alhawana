package com.app.alhawanajd.provider

import androidx.core.content.FileProvider

/**
 * Dedicated FileProvider so third-party libraries can ship their own providers
 * without hitting manifest merge conflicts on the shared androidx class name.
 */
class GenericFileProvider : FileProvider()


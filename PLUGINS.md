# Plugins

Vimax allows adding plugins by placing a KotlinScript inside a `plugins` directory in the working directory of Vimax. The idea
 being a plugin is to provide a predefined set of metadata and also automatically update the value for them of a video. A very
 simple plugin can be found under `src/main/resources/plugins/VideoVersionProvider.kts`, which provides a version for videos.

## Structure

A plugin has three main parts:

```kotlin
registerPlugin("Name") { // <--- 1
    val versionMetadata = int("Version", ASC, 1) // <--- 2
    
    onUpdate { // <--- 3
        it[versionMetadata] = (it[versionMetadata] ?: 0) + 1
    }
}
```

1) The plugin registration with a name
2) Definition of the metadata provided by the plugin, these can multiple (or none)
3) Event handlers to react on changes in the system, which can then be used to update the plugins' metadata

A more complex plugin is the `MetadataInfoPlugin` which uses the provided `ffmpeg`/`ffprobe` to provide video file metadata
 information.

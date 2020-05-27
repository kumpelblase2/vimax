# Plugins

Vimax allows adding plugins by placing a KotlinScript file inside a `plugins` directory in the working directory of Vimax. The 
idea behind a plugin is to provide a predefined set of metadata and manage them for each video. A 
very simple plugin can be found under [`src/main/resources/plugins/VideoVersionProvider.kts`](src/main/resources/plugins/VideoVersionProvider.kts), 
which provides a version for videos. In other words, it will increase a counter every time a video gets updated.

## Structure

A plugin has four main parts:

```kotlin
PluginRegistration.register("Name") { // <--- 1
    description = "Some description of this plugin." // <--- 2
    author = "Who?"
    version = "1.0"

    val versionMetadata = number("Version", ASC, 1) // <--- 3
    
    onUpdate { // <--- 4
        it[versionMetadata] = (it[versionMetadata] ?: 0) + 1
    }
}
```

1) The plugin registration with the name of the plugin
2) Information about the plugin itself
3) Definition of the metadata provided by the plugin, these can be multiple (or none)
4) Event handlers to react on changes in the system, which can then be used to update the plugins' metadata

A more complex plugin is the `MetadataInfoPlugin` which uses the provided `ffmpeg`/`ffprobe` to provide video file metadata
 information.
 
### Possible metadata types

The metadata types available to plugins are the same a user can use to create their own metadata. Thus they are the following:

- `number`
- `text`
- `taglist`
- `selection`
- `duration`
- `switch`
- `float`
- `range`

### Possible callbacks

To actually make changes to a video, there are only a few ways for a plugin to be called. These are:

- `onCreate` - When the video gets added to the library
- `onUpdate` - When the video gets modified (e.g. metadata changes)
- `onStartWatching` - When the user starts playing the video
- `onFinishWatching` - When the user finishes watching the video

You can register as many of these as often as you like.

## Possibilities

Since a plugin is a kotlin script, you can program any kind of logic as well as fetch external resources or other things. The 
only limitation is you cannot access Vima internals nor add your own dependencies. Apart from that, there are no restrictions 
to what java apis you can use.

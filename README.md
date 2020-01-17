# vimax

Vimax is the successor to [Vima](https://github.com/kumpelblase2/vima/), but with a few different ideas in mind while the main
 use case stayed the same. To quote what Vima wanted to do:
 
> Vima is a video manager or video collection. It manages a set of videos and allows the user to define custom metadata for each video. The user can then search for specific videos via the specified metadata using a custom query language, create playlists the same way and obviously play the videos in their browser.

All of which is still true for Vimax. While I'm still OK with the decisions I've made, I wouldn't make them again with what I know
 now. And sadly, these changes cannot be completely implemented in Vima, which is why we now have Vimax.

The reasons boil down to mostly big setup cost for users and bad maintainability of configuration. Ruby on rails is a pain to
 deploy and run for a normal user compared to a simple binary or jar. This seems to be a general issue with interpreted 
languages and cannot really be fixed apart from providing packages for everyone. 

Vima also required an existing mongodb database which is, again, annoying setup a user would have to do beforehand not to 
mention that they'd have to maintain it. While mongo itself isn't the issue (I think it's one of the few use-cases where mongo
 _does_ make sense), there's no alternative document store that can run in-memory or can be started inside the application.

Lastly, the configuration wasn't really pleasant to work with. It was neither nice to work with from the user side nor was 
reacting to changes to it on the server side. A simple file doesn't provide the user any help with how it should be 
structured, what values can be used or what options he had available. It provided a lot of flexibility when initially developing
 the application but that came at a price.

### So how is Vimax gonna fix these?

Vimax is written in a compiled language and will be provided as a single artifact (in this case: Jar). Secondly, it uses SQLite to
 store the data which doesn't require any setup from the user. And there will be no need to edit a config file as it would all
be done in the client.

## Plugins

To allow for providing automated metadata values as well as allowing the user to generate his own metadata values, Vimax - just 
like Vima - allows for adding plugins. Plugins in Vimax can do two things: declare their provided metadata and react to events 
to change the values of that metadata. Vimax already ships with three plugins: `Watched`, `Metadata` & `Version`. Plugins are
disabled by default and have to be enabled in the "Settings" page in the client. Once enabled, they'll take care of their 
metadata on their own and the metadata can be used to filter just like any other metadata.

You can also create your own plugins. Check out the [plugin information](PLUGINS.md) file in the repository for more information.

## Requirements

Java 8 (or later) needs to be installed on the system to run the application.
 
You need FFmpeg installed in the `PATH` of your system. FFmpeg is used to generate the thumbnails for the videos and to provide
 metadata like resolution or bitrate if the `MetadataInfoPlugin` is enabled.
 
## How to start (user instructions)

Simply place the provided jar in any folder you like and start it using the following command (adjust the filename accordingly):
```shell script
java -jar vima-0.0.1-all.jar
```

This will start the application and once startup has finished, you can access the application via `http://localhost:8080`

## How to start (developer instructions)

To start the backend you can run:

```
./gradlew bootRun
```

or

```
gradlew.bat bootRun
```

if you're on windows.

To then start the ui you have do multiple steps.
First, change into the `src/frontend` directory and then run the following:

```
npm install
npm start
```

You can then access the UI on `localhost:8081`

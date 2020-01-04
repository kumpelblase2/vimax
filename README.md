# vimax

Vimax is the successor to [Vima](https://github.com/kumpelblase2/vima/), but with a few different ideas in mind while the main
 use case stayed the same. To quote what Vima wanted to do:
 
> Vima is a video manager or video collection. It manages a set of videos and allows the user to define custom metadata for each video. The user can then search for specific videos via the specified metadata using a custom query language, create playlists the same way and obviously play the videos in their browser.

which is still true for Vimax. While I'm still OK with the decisions I made, I wouldn't make them again with what I know now.
 And sadly, these changes cannot be completely implemented in Vima, which is why we now have Vimax.

They boil down to mostly big setup cost for users and bad maintainability of configuration. Ruby on rails is a pain to deploy 
and run for a normal user compared to a simple binary or jar. This seems to be a general issue with interpreted languages and 
cannot really be fixed - just worked around. Vima also required an existing mongodb database which is, again, annoying setup a 
user would have to do beforehand. While mongo itself isn't the issue (I think it's one of the few use-cases where mongo _does_ 
make sense), there's no alternative document store that can run in-memory or can be started inside the application. Lastly, the 
configuration wasn't really pleasant to work with. It was neither nice to work with from the user side nor was reacting to changes 
to it on the server side. A simple file doesn't provide the user any help with how it should be structured, what values can be 
used or whatnot. It provided a lot of flexibility when initially developing the application but that came at a price.

So how is Vimax gonna fix these?

Vimax is written in a compiled language and will be provided as a single artifact (in this case: Jar). Secondly, it uses SQLite to
 store the data which doesn't require any setup from the user. And there will be no need to edit a config file as it would all
be done in the client.

## Requirements

You need FFmpeg installed in the `PATH` of your system. FFmpeg is used to generate the thumbnails for the videos and to provide
 metadata like resolution or bitrate if the `MetadataInfoPlugin` is enabled.

## How to use (developer instructions)

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

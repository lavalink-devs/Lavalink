# Lavalink Plugins
**Warning:** The plugin system is still in beta. Breaking changes may still be made to the plugin API if deemed necessary.

Lavalink supports third-party plugins to add additional functionality such as custom audio sources, custom filters,
WebSocket handling, REST endpoints, and much more. 

List of plugins:
- [Google Cloud TTS](https://github.com/DuncteBot/tts-plugin) A text to speech plugin using the [google cloud tts api](https://cloud.google.com/text-to-speech/docs)
- [SponsorBlock](https://github.com/Topis-Lavalink-Plugins/Sponsorblock-Plugin) for skipping sponsor segments in YouTube videos
- [Topis Source Managers](https://github.com/Topis-Lavalink-Plugins/Topis-Source-Managers-Plugin) adds Spotify and Apple Music support
- [DuncteBot plugin](https://github.com/DuncteBot/skybot-lavalink-plugin) adds additional source managers that are not widely used

Lavalink loads all .jar files placed in the `plugins` directory, which you may need to create yourself. Lavalink can
also download plugin .jar files automatically by editing the configuration file. See the respective plugin repository
for instructions.

You can add your own plugin by submitting a pull-request to this file.

## Developing your own plugin
Follow these steps to quickly get started with plugin development:
1. Create a copy of https://github.com/freyacodes/lavalink-plugin-template
2. Rename the directories `org/example/plugin/` under `src/main/java/` to something more specific like
`io/github/yourusername/yourplugin`
3. Rename `src/main/resources/lavalink-plugins/your-plugin.properties` to the name of your plugin. The file must end
with `.properties`. This is the plugin manifest.
4. Fill out the name, path, and version properties in the manifest. The path is where your classes will be loaded from,
e.g. `io.github.yourusername.yourplugin`.

Now you can start writing your plugin. You can test your plugin against Lavalink by running Gradle with the
`:run` Gradle task. The [Gradle documentation](https://docs.gradle.org/current/userguide/userguide.html) might be helpful.

Lavalink has a [plugin API](plugin-api/src/main/java/dev/arbjerg/lavalink/api) which you can integrate with. The API is
provided as an artifact and is used by the template. It is also possible to integrate with internal parts og Lavalink,
but this is not recommended. Instead, open an issue or pull-request to change the API.

Lavalink is configured by plugins using the Spring Boot framework using Spring annotations. For instance, you could define
an extension to the WebSocket API by exposing a Spring bean like this:

```java
@Service
class MyExtension implements WebSocketExtension {
    // ...
} 
```

You can also define custom REST endpoints and configuration file properties. See the Spring Boot documentation for
[Spring Web MVC](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#web.servlet) and
[type-safe configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.external-config.typesafe-configuration-properties).

If you simply want to add a Lavaplayer `AudioSourceManager` to Lavalink it is sufficient to simply provide it as a bean.
For example:

```java
import org.springframework.stereotype.Service;

@Service
class MyAudioSourceManager implements AudioSourceManager {
    // ...
} 
```

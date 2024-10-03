---
description: Lavalink Sources API documentation.
---

# Sources

Below is all the default sources that come with Lavalink. Make sure to have them enabled!

## YouTube

!!! note
    Make sure you have the YouTube plugin added!

    You can find it [here](https://github.com/lavalink-devs/youtube-source).

Enabling this option:

```yml title="application.yml"
server:
    sources:
        youtube: true
```

### Searching {: #youtube-searching }

Searching using Youtube.

 - `ytsearch`: This will search `youtube.com` for relevant tracks/playlists.
 - `ytmsearch`: This will search `music.youtube.com` for relevant tracks/playlists.

### URLs {: #youtube-url }

Supported URL types

 - https://youtube.com/watch?v=dQw4w9WgXcQ
 - https://youtube.com/playlist?list=PLlaN88a7y2_qSLH3pLiQIQ6isY_DZTtdg
 - https://youtube.com/shorts/dQw4w9WgXcQ
 - https://youtube.com/live/dQw4w9WgXcQ
 - https://youtube.com/embed/dQw4w9WgXcQ
 - https://www.youtube.com/watch?v=dQw4w9WgXcQ
 - https://www.youtube.com/playlist?list=PLlaN88a7y2_qSLH3pLiQIQ6isY_DZTtdg
 - https://www.youtube.com/shorts/dQw4w9WgXcQ
 - https://www.youtube.com/live/dQw4w9WgXcQ
 - https://www.youtube.com/embed/dQw4w9WgXcQ
 - https://youtu.be/watch?v=dQw4w9WgXcQ
 - https://youtu.be/playlist?list=PLlaN88a7y2_qSLH3pLiQIQ6isY_DZTtdg
 - https://music.youtube.com/watch?v=dQw4w9WgXcQ
 - https://music.youtube.com/playlist?list=PLlaN88a7y2_qSLH3pLiQIQ6isY_DZTtdg
 - https://music.youtube.com/embed/dQw4w9WgXcQ

## Bandcamp

Enabling this option:

```yml title="application.yml"
server:
    sources:
        bandcamp: true
```

### Searching {: #bandcamp-searching }

Searching using Bandcamp.

 - `bcsearch`: This will search `bandcamp.com` for relevant tracks/playlists.

### URLs {: #bandcamp-url }

Supported URL types

 - FIXME: add url's

## SoundCloud

Enabling this option:

```yml title="application.yml"
server:
    sources:
        soundcloud: true
```

### Searching {: #soundcloud-searching }

Searching using SoundCloud.

 - `scsearch`: This will search `soundcloud.com` for relevant tracks/playlists.

### URLs {: #soundcloud-url }

Supported URL types

 - FIXME: add url's

## Twitch

Enabling this option:

```yml title="application.yml"
server:
    sources:
        twitch: true
```

### URLs {: #twitch-url }

Supported URL types

 - FIXME: add url's

## Vimeo

Enabling this option:

```yml title="application.yml"
server:
    sources:
        vimeo: true
```

### URLs {: #vimeo-url }

Supported URL types

 - FIXME: add url's

## Nico

Enabling this option:

```yml title="application.yml"
server:
    sources:
        nico: true
```

### URLs {: #nico-url }

Supported URL types

 - FIXME: add url's

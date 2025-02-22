import dev.arbjerg.lavalink.protocol.v4.Exception
import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.ResultStatus
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertIs

class LoadResultSerializerTest {
    @Test
    @JsName("test1")
    fun `test track loaded can be serialized`() {
        //<editor-fold desc="JSON">
        //language=json
        val json = """
            {
              "loadType": "track",
              "data": {
                  "encoded": "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA==",
                  "info": {
                    "identifier": "dQw4w9WgXcQ",
                    "isSeekable": true,
                    "author": "RickAstleyVEVO",
                    "length": 212000,
                    "isStream": false,
                    "position": 0,
                    "title": "Rick Astley - Never Gonna Give You Up",
                    "uri": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "artworkUrl": "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg",
                    "isrc": null,
                    "sourceName": "youtube"
                  },
                  "pluginInfo": {},
                  "userData": {}
              },
              "exception": null
            }
        """.trimIndent()
        //</editor-fold>

        test<LoadResult>(json) {
            loadType shouldBe ResultStatus.TRACK
            assertIs<LoadResult.TrackLoaded>(this)
            data {
                encoded shouldBe "QAAAjQIAJVJpY2sgQXN0bGV5IC0gTmV2ZXIgR29ubmEgR2l2ZSBZb3UgVXAADlJpY2tBc3RsZXlWRVZPAAAAAAADPCAAC2RRdzR3OVdnWGNRAAEAK2h0dHBzOi8vd3d3LnlvdXR1YmUuY29tL3dhdGNoP3Y9ZFF3NHc5V2dYY1EAB3lvdXR1YmUAAAAAAAAAAA=="
                info {
                    identifier shouldBe "dQw4w9WgXcQ"
                    isSeekable shouldBe true
                    author shouldBe "RickAstleyVEVO"
                    length shouldBe 212000
                    isStream shouldBe false
                    position shouldBe 0
                    title shouldBe "Rick Astley - Never Gonna Give You Up"
                    uri shouldBe "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
                    artworkUrl shouldBe "https://i.ytimg.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
                    isrc shouldBe null
                    sourceName shouldBe "youtube"
                }
            }
        }
    }

    @Test
    @JsName("test2")
    fun `test load failed can be serialized`() {
        //<editor-fold desc="JSON">
        //language=json
        val json = """
            {
              "loadType": "error",
              "data": {
                "message": "The uploader has not made this video available in your country.",
                "severity": "common",
                "cause": "com.sedmelluq.discord.lavaplayer.tools.FriendlyException: This video is not available in your country.",
                "causeStackTrace": "com.sedmelluq.discord.lavaplayer.tools.FriendlyException: This video is not available in your country.\n\nblabla"
              }
            }
        """.trimIndent()
        //</editor-fold>

        test<LoadResult>(json) {
            loadType shouldBe ResultStatus.ERROR
            assertIs<LoadResult.LoadFailed>(this)
            data {
                message shouldBe "The uploader has not made this video available in your country."
                severity shouldBe Exception.Severity.COMMON
                cause shouldBe "com.sedmelluq.discord.lavaplayer.tools.FriendlyException: This video is not available in your country."
                causeStackTrace shouldBe "com.sedmelluq.discord.lavaplayer.tools.FriendlyException: This video is not available in your country.\n\nblabla"
            }
        }
    }

    @Test
    @JsName("test3")
    fun `test no matches can be serialized`() {
        //<editor-fold desc="JSON">
        //language=json
        val json = """
            {
              "loadType": "empty",
              "data": null
            }
        """.trimIndent()
        //</editor-fold>

        test<LoadResult>(json) {
            loadType shouldBe ResultStatus.NONE
            assertIs<LoadResult.NoMatches>(this)
            data shouldBe null
        }
    }

    @Test
    @JsName("test4")
    fun `test search result loaded can be serialized`() {
        //<editor-fold desc="JSON">
        //language=json
        val json = """
            {
              "loadType": "search",
              "data": []
            }
        """.trimIndent()
        //</editor-fold>

        test<LoadResult>(json) {
            loadType shouldBe ResultStatus.SEARCH
            assertIs<LoadResult.SearchResult>(this)
            data.tracks shouldBe emptyList()
        }
    }

    @Test
    @JsName("test5")
    fun `test playlist loaded can be serialized`() {
        //<editor-fold desc="JSON">
        //language=json
        val json = """
            {
              "loadType": "playlist",
              "data": {
                "info": {
                  "name": "Example YouTube Playlist",
                  "selectedTrack": 3
                },
                "pluginInfo": {},
                "tracks": []
              }
            }
        """.trimIndent()
        //</editor-fold>

        test<LoadResult>(json) {
            loadType shouldBe ResultStatus.PLAYLIST
            assertIs<LoadResult.PlaylistLoaded>(this)
            data {
                pluginInfo shouldBe emptyMap()
                info {
                    name shouldBe "Example YouTube Playlist"
                    selectedTrack shouldBe 3
                }
                tracks shouldBe emptyList()
            }
        }
    }
}

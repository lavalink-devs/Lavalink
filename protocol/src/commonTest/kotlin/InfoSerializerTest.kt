import dev.arbjerg.lavalink.protocol.v4.Info
import kotlin.js.JsName
import kotlin.test.Test

//language=json
private const val json = """
{
  "version": {
    "semver": "3.7.0-rc.1",
    "major": 3,
    "minor": 7,
    "patch": 0,
    "preRelease": "rc.1"
  },
  "buildTime": 1664223916812,
  "git": {
    "branch": "master",
    "commit": "85c5ab5",
    "commitTime": 1664223916812
  },
  "jvm": "18.0.2.1",
  "lavaplayer": "1.3.98.4-original",
  "sourceManagers": [
    "youtube",
    "soundcloud"
  ],
  "filters": [
    "equalizer",
    "karaoke",
    "timescale",
    "channelMix"
  ],
  "plugins": [
    {
      "name": "some-plugin",
      "version": "1.0.0"
    },
    {
      "name": "foo-plugin",
      "version": "1.2.3"
    }
  ]
}
"""
class InfoSerializerTest {
    @Test
    @JsName("test1")
    fun `info can be serialized`() {
        test<Info>(json) {
            version {
                semver shouldBe "3.7.0-rc.1"
                major shouldBe 3
                minor shouldBe 7
                patch shouldBe 0
                preRelease shouldBe "rc.1"
            }
            buildTime shouldBe 1664223916812
            git {
                branch shouldBe "master"
                commit shouldBe "85c5ab5"
                commitTime shouldBe 1664223916812
            }
            jvm shouldBe  "18.0.2.1"
            lavaplayer shouldBe  "1.3.98.4-original"
        }
    }
}

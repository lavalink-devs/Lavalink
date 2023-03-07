import dev.arbjerg.lavalink.protocol.v4.Filters
import dev.arbjerg.lavalink.protocol.v4.Omissible
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertIs

//language=json
const val filters = """
{
  "volume": 1.0,
  "equalizer": [
    {
      "band": 0,
      "gain": 0.2
    }
  ],
  "karaoke": {
    "level": 1.0,
    "monoLevel": 1.0,
    "filterBand": 220.0,
    "filterWidth": 100.0
  },
  "timescale": {
    "speed": 1.0,
    "pitch": 1.0,
    "rate": 1.0
  },
  "tremolo": {
    "frequency": 2.0,
    "depth": 0.5
  },
  "vibrato": {
    "frequency": 2.0,
    "depth": 0.5
  },
  "rotation": {
    "rotationHz": 0
  },
  "distortion": {
    "sinOffset": 0.0,
    "sinScale": 1.0,
    "cosOffset": 0.0,
    "cosScale": 1.0,
    "tanOffset": 0.0,
    "tanScale": 1.0,
    "offset": 0.0,
    "scale": 1.0
  },
  "channelMix": {
    "leftToLeft": 1.0,
    "leftToRight": 0.0,
    "rightToLeft": 0.0,
    "rightToRight": 1.0
  },
  "lowPass": {
    "smoothing": 20.0
  }
}
"""

class FiltersTest {
    @Test
    @JsName("test1")
    fun `test filters serialization`() {
        test<Filters>(filters) {
            volume shouldBe 1.0f
            equalizer.requirePresent {
                onEach {
                    band shouldBe 0
                    gain shouldBe 0.2f
                }
            }
            karaoke.requirePresent {
                level shouldBe 1.0f
                monoLevel shouldBe 1.0f
                filterBand shouldBe 220.0f
                filterWidth shouldBe 100.0f
            }
            timescale.requirePresent {
                speed shouldBe 1.0
                pitch shouldBe 1.0
                rate shouldBe 1.0
            }
            tremolo.requirePresent {
                frequency shouldBe 2.0f
                depth shouldBe 0.5f
            }
            vibrato.requirePresent {
                frequency shouldBe 2.0f
                depth shouldBe 0.5f
            }
            rotation.requirePresent {
                rotationHz shouldBe 0.0
            }
            distortion.requirePresent {
                sinOffset shouldBe 0.0f
                sinScale shouldBe 1.0f
                cosOffset shouldBe 0.0f
                cosScale shouldBe 1.0f
                tanOffset shouldBe 0.0f
                tanScale shouldBe 1.0f
                offset shouldBe 0.0f
                scale shouldBe 1.0f
            }
            channelMix.requirePresent {
                leftToLeft shouldBe 1.0f
                leftToRight shouldBe 0.0f
                rightToLeft shouldBe 0.0f
                rightToRight shouldBe 1.0f
            }
            lowPass.requirePresent {
                smoothing shouldBe 20.0f
            }
        }
    }

    @Test
    @JsName("test2")
    fun `test empty filters can be serialized`() {
        //language=json
        val json = """{}"""

        test<Filters>(json) {
            assertIs<Omissible.Omitted<*>>(volume)
            assertIs<Omissible.Omitted<*>>(equalizer)
            assertIs<Omissible.Omitted<*>>(karaoke)
            assertIs<Omissible.Omitted<*>>(timescale)
            assertIs<Omissible.Omitted<*>>(tremolo)
            assertIs<Omissible.Omitted<*>>(vibrato)
            assertIs<Omissible.Omitted<*>>(distortion)
            assertIs<Omissible.Omitted<*>>(rotation)
            assertIs<Omissible.Omitted<*>>(channelMix)
            assertIs<Omissible.Omitted<*>>(lowPass)
            pluginFilters shouldBe emptyMap()
        }
    }
}

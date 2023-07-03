import dev.arbjerg.lavalink.protocol.v4.RoutePlannerFreeAddress
import dev.arbjerg.lavalink.protocol.v4.RoutePlannerStatus
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertIs

class RoutePlannerSerializerTest {
    @Test
    @JsName("test1")
    fun `test free address body serializer`() {
        //language=json
        val json = """
            {
              "address": "1.0.0.1"
            }
        """.trimIndent()

        test<RoutePlannerFreeAddress>(json) {
            address shouldBe "1.0.0.1"
        }
    }

    @Test
    @JsName("test2")
    fun `test rotating nano ip route planner serializer`() {
        //language=json
        val json = """
            {
              "class": "RotatingNanoIpRoutePlanner",
              "details": {
                "ipBlock": {
                  "type": "Inet6Address",
                  "size": "1208925819614629174706176"
                },
                "failingAddresses": [
                  {
                    "failingAddress": "/1.0.0.0",
                    "failingTimestamp": 1573520707545,
                    "failingTime": "Mon Nov 11 20:05:07 EST 2019"
                  }
                ],
                "blockIndex": "0",
                "currentAddressIndex": "36792023813"
              }
            }
        """.trimIndent()

        test<RoutePlannerStatus>(json) {
            assertIs<RoutePlannerStatus.RotatingNanoIpRoutePlannerStatus>(this)
            details {
                ipBlock {
                    type shouldBe RoutePlannerStatus.IpBlockStatus.Type.INET_6_ADDRESS
                    size shouldBe "1208925819614629174706176"
                }
                failingAddresses.forEach {
                    with(it) {
                        failingAddress shouldBe  "/1.0.0.0"
                        failingTimestamp shouldBe 1573520707545
                        failingTime shouldBe "Mon Nov 11 20:05:07 EST 2019"
                    }
                }

                blockIndex shouldBe "0"
                currentAddressIndex shouldBe "36792023813"
            }
        }
    }
    @Test
    @JsName("test3")
    fun `test balancing route planner serializer`() {
        //language=json
        val json = """
            {
              "class": "BalancingIpRoutePlanner",
              "details": {
                "ipBlock": {
                  "type": "Inet6Address",
                  "size": "1208925819614629174706176"
                },
                "failingAddresses": [
                  {
                    "failingAddress": "/1.0.0.0",
                    "failingTimestamp": 1573520707545,
                    "failingTime": "Mon Nov 11 20:05:07 EST 2019"
                  }
                ]
              }
            }
        """.trimIndent()

        test<RoutePlannerStatus>(json) {
            assertIs<RoutePlannerStatus.BalancingIpRoutePlannerStatus>(this)
            details {
                ipBlock {
                    type shouldBe RoutePlannerStatus.IpBlockStatus.Type.INET_6_ADDRESS
                    size shouldBe "1208925819614629174706176"
                }
                failingAddresses.forEach {
                    with(it) {
                        failingAddress shouldBe  "/1.0.0.0"
                        failingTimestamp shouldBe 1573520707545
                        failingTime shouldBe "Mon Nov 11 20:05:07 EST 2019"
                    }
                }
            }
        }
    }
}

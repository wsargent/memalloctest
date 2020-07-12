package simulation

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

// run with "sbt gatling:test" on another machine so you don't have resources contending.
// http://gatling.io/docs/2.2.2/general/simulation_structure.html#simulation-structure
class GatlingSpec extends Simulation {

  // change this to another machine, make sure you have Play running in producion mode
  // i.e. sbt stage / sbt dist and running the script
  val httpConf: HttpProtocolBuilder = http.baseUrl("http://localhost:9000/v1/posts")

  val indexReq = repeat(500) {
    exec(
      http("Index").get("/").check(status.is(200))
    )
  }

  val readClientsScenario = scenario("Clients").exec(indexReq).pause(1)

  setUp(
    readClientsScenario.inject(rampUsers(500).during(500 seconds)).protocols(httpConf)
  )
}

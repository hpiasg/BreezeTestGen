package de.hpi.asg.breezetestgen

import com.typesafe.config._
import de.hpi.asg.breezetestgen.domain.Netlist


class TestGenerationContext(config: Config) extends Loggable {

  config.checkValid(ConfigFactory.defaultReference(), "breeze-test-gen")

  def this() {
    this(ConfigFactory.load())
  }

  //TODO: specify proper return values and use them

  def generateTestsForFile(breezeFile: java.io.File) = {
    info(config.getString("breeze-test-gen.foo"))
    logger.info(s"Execute for file: ${breezeFile.getName}")
    val mainNetlist = BreezeTransformer.parse(breezeFile)
    mainNetlist match {
      case Some(netlist) =>
        logger.info(netlist)
        generateTestsForNetlist(netlist)
      case None => logger.error("Could not parse Netlist")
    }
  }

  def generateTestsForNetlist(mainNetlist: Netlist) = {
    import akka.actor.{ActorSystem, Props}
    import scala.concurrent.Await
    import scala.concurrent.duration._

    import actors.TestGenerationActor

    val system = ActorSystem("TestGen")
    logger.info("Start testfinding...")
    val testgen = system.actorOf(Props(classOf[TestGenerationActor], mainNetlist))
    testgen ! TestGenerationActor.Start
    Await.result(system.whenTerminated, 20 seconds)
    logger.info("testfinding finished!")
  }
}

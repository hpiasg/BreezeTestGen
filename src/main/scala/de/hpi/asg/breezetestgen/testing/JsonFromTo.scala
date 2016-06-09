package de.hpi.asg.breezetestgen.testing

import de.hpi.asg.breezetestgen.domain._
import de.hpi.asg.breezetestgen.util
import net.liftweb.json.{CustomSerializer, DefaultFormats}
import net.liftweb.json.JsonAST._
import net.liftweb.json.Extraction.decompose

import scalax.collection.GraphEdge.DiEdge
import scalax.collection.io.edge.EdgeParameters
import scalax.collection.io.json._
import scalax.collection.io.json.descriptor.predefined.Di


/** Helps transforming an instance of a test graph to a json representation and the other way round
  */
object JsonFromTo {
  def toJson(test: Test): String = {
    val testWithIds = addIDs(test)

    testWithIds.toJson(desc)
  }

  def fromJson(testString: String): Option[Test] = {
    Some(scalax.collection.Graph.fromJson[TestEventWithID,DiEdge](testString, desc)
    ).map(removeIDs)
  }

  private implicit val formats = DefaultFormats

  /** adds an ID to an IOEvent in order to be addressable by edges */
  private case class TestEventWithID(id: Int, orig: TestEvent)

  /** transform a testgraph without IDs to one with IDs */
  private def addIDs(orig: Test): scalax.collection.Graph[TestEventWithID, DiEdge] = {
    val ids = (1 to Int.MaxValue).iterator

    util.Graph.mapNodes(orig, {x: TestEvent => TestEventWithID(ids.next(), x)})
  }

  /** transform a testgraph with IDs to one without IDs */
  private def removeIDs(withIDs: scalax.collection.Graph[TestEventWithID, DiEdge]): Test = {
    util.Graph.mapNodes(withIDs, {x: TestEventWithID => x.orig})
  }

  private class TestEventWithIDSerializer extends CustomSerializer[TestEventWithID](format => (
    {
      //extract TestEventWithId from JsonAst
      case JObject(JField("type", JString("Merge")) :: JField("id", JInt(id)) :: Nil) =>
        TestEventWithID(id.intValue, new MergeEvent)

      case JObject(JField("type", JString("Request")) :: JField("id", JInt(id)) :: JField("channelId", JInt(cId)) :: Nil) =>
        TestEventWithID(id.intValue, IOEvent(Request(cId.intValue)))

      case JObject(JField("type", JString("Acknowledge")) :: JField("id", JInt(id)) :: JField("channelId", JInt(cId)) :: Nil) =>
        TestEventWithID(id.intValue, IOEvent(Acknowledge(cId.intValue)))

      case JObject(JField("type", JString("DataRequest")) :: JField("id", JInt(id)) ::
          JField("channelId", JInt(cId)) :: JField("data", JObject(data)) :: Nil) =>
        val const = JObject(data).extract[Constant]
        TestEventWithID(id.intValue, IOEvent(DataRequest(cId.intValue, const)))

      case JObject(JField("type", JString("DataAcknowledge")) :: JField("id", JInt(id)) ::
        JField("channelId", JInt(cId)) :: JField("data", JObject(data)) :: Nil) =>
        val const = JObject(data).extract[Constant]
        TestEventWithID(id.intValue, IOEvent(DataAcknowledge(cId.intValue, const)))
    },
    {
      // transform TestEventWithId to JsonAst
      case TestEventWithID(id, _:MergeEvent) =>
        JObject(JField("type", JString("Merge")) :: JField("id", JInt(id)) :: Nil)

      case TestEventWithID(id, IOEvent(s: Signal)) =>
        val d = decompose(s)
        val typeString = s match {
          case _: Request => "Request"
          case _: Acknowledge => "Acknowledge"
          case _: DataRequest => "DataRequest"
          case _: DataAcknowledge => "DataAcknowledge"
        }
        JObject(JField("type", JString(typeString)) :: JField("id", JInt(id)) :: Nil) merge d
    }
    ))

  /** format edges as array of two integers: the ids of the TestEvents. Standard behavior generated Strings here  */
  class EdgeSerializer extends CustomSerializer[EdgeParameters](format => (
    {
      // from Json
      case JArray(JInt(nodeId_1) :: JInt(nodeId_2) :: Nil) =>
        new EdgeParameters(nodeId_1.toString, nodeId_2.toString)

    }, {
      // to json
    case EdgeParameters(nodeId_1, nodeId_2) =>
      JArray(JInt(nodeId_1.toInt) :: JInt(nodeId_2.toInt) :: Nil)
    })
  )

  private val nodeDescriptor = new NodeDescriptor[TestEventWithID](
    typeId = "TestEvent",
    customSerializers = Seq(new TestEventWithIDSerializer)
  ){
    def id(node: Any) = node match {
      case TestEventWithID(id, _) => id.toString
    }
  }
  private val desc = new Descriptor[TestEventWithID](
    defaultNodeDescriptor = nodeDescriptor,
    defaultEdgeDescriptor = Di.descriptor[TestEventWithID](Some(new EdgeSerializer))
  )
}
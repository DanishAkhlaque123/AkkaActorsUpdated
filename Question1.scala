import Question1.writerInfo
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

import java.io.{BufferedWriter, File, FileWriter, PrintWriter}
import scala.util.Try
object Question1 extends App {

  object flush{
    val maxFlushCount = 2
    var countInfo = 0
    var countwarn = 0
  }

  class actorWithInfo extends Actor with ActorLogging {

    import flush._
    val logger = Logging(context.system, this)

    override def receive: Receive = {

      case message: String =>
        writeInfo(message)


      case message: Int =>
        writeWarn(message)
    }


  }

  val writerInfo: BufferedWriter = {
    val f = new File("src/main/log/applicationInfoNew.log")
    if (!f.exists()) {
      f.createNewFile()
    }
    new BufferedWriter(new FileWriter(f, true))
  }


    val writerWarn: BufferedWriter = {
      val f = new File("src/main/log/applicationWarnNew.log")
      if (!f.exists()) {
        f.createNewFile()
      }

      new BufferedWriter(new FileWriter(f, true))
    }

  val system = ActorSystem("LoggingQuestion1")
  val sampleActor = system.actorOf(Props(new actorWithInfo))


  sampleActor ! "Hi my name is Danish"
  //sampleActor ! "Hi my name is Tabish"
  //sampleActor ! "Hi my name is Shahin"
  sampleActor ! 42



  import flush._

  def writeInfo(str: String) {

    writerInfo.write(str)
    writerInfo.newLine()
    countInfo += 1
    if (countInfo > maxFlushCount) {
      writerInfo.flush()
      Try(new File("src/main/log/applicationInfoNew.log").renameTo(new File("src/main/log/applicationInfoNew1.log"))).getOrElse(false)
      countInfo = 0
    }

  }

  def writeWarn(str: Int) {

    writerWarn.write(str)
    writerWarn.newLine()
    countwarn += 1
    if (countwarn > maxFlushCount) {
      writerWarn.flush()
      Try(new File("src/main/log/applicationWarnNew.log").renameTo(new File("src/main/log/applicationWarnNew1.log"))).getOrElse(false)
      countwarn = 0
    }

  }

  Thread.sleep(5000)
  writerInfo.close
  writerWarn.close

  }


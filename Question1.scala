
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

import java.io.{BufferedWriter, File, FileWriter, PrintWriter}
import java.time.LocalDateTime
import scala.util.Try
object question1 extends App {

  object flush{
    val maxFlushCount = 2
    var countInfo = 0
    var countwarn = 0
  }

  case class Info(message: String)
  case class Warn(message: String)
  case class Error(message: String)
  class actorWithInfo extends Actor with ActorLogging {

    import flush._
    val logger = Logging(context.system, this)

    override def receive: Receive = {

      case Info(message) =>
        log.info(message)
        writeInfo(message)


      case Warn(message) => log.warning(message)
        writeWarn(message)

      case Error(message) => throw new RuntimeException(sender().toString + "error occured in this actor : message " + message)
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


  sampleActor ! Info("Hi my name is Danish")

  sampleActor ! Warn("Hi my name is Tabish")

  sampleActor ! Warn("Hi my name is Tabish1")
  sampleActor ! Warn("Hi my name is Tabish2")




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

  def writeWarn(str: String) {

    writerWarn.write(str)
    writerWarn.newLine()
    countwarn += 1
    if (countwarn > maxFlushCount) {
      writerWarn.flush()
      Try(new File("src/main/log/applicationWarnNew.log").renameTo(new File(s"src/main/log/applicationWarnNew${ LocalDateTime.now()}.log"))).getOrElse(false)
      countwarn = 0
    }

  }

  Thread.sleep(5000)
  writerInfo.close
  writerWarn.close

}
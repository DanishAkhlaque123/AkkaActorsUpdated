import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props}
import akka.event.Logging

import java.io.{FileWriter, PrintWriter}

object SupervisorSpec extends App{

  val system = ActorSystem("SampleActor")
  object Parent{
    case class startchild(value: String)
    case class stopchild(message: String)
    case object Stop
  }


  class Supervisor extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = withChildren(Map())
    def withChildren(children: Map[String, ActorRef]): Receive={
      case startchild(name)=>log.info(s"starting child ${name}")
        context.become(withChildren(children+(name->context.actorOf(Props[child],name))))

      case stopchild(name) => log.info(s"stopping child ${name}")
        val childOption = children.get(name)
        childOption.foreach((childref=>context.stop(childref)))

      case stop =>
        log.info("stopping parent")
        context.stop(self)

        def supervisorStrategy = OneForOneStrategy() {
          case _:RuntimeException=>Restart
        }
    }

  }

  class child extends Actor with ActorLogging{
    val logger = Logging(context.system,this)

    override def receive: Receive = {
      case  message: String => println("I am alive")

      case message: Int => val fileWriter = new PrintWriter(new FileWriter("src/main/log/applicationWarn.log"))
        fileWriter.write("hello warnning there" + message.toString)
        fileWriter.close()
    }

  }


  import Parent._
  val parent = system.actorOf(Props[Supervisor],"parent")
  parent ! startchild("child1")
  val child = system.actorSelection("/user/parent/child1")
  child ! 2
  parent ! Stop

}

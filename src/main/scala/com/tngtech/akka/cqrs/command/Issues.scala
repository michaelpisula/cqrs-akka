package com.tngtech.akka.cqrs.command

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import com.tngtech.akka.cqrs.command.Issues._

object Issues {

  case class CreateNewIssue(name: String)

  sealed trait Event

  case class IssueCreated(name: String) extends Event

  def props(): Props =
    Props(classOf[Issues])

  val actorName = "Issues"
}

class Issues extends PersistentActor with ActorLogging {
  override def persistenceId: String = actorName

  var existingIssues = Map.empty[String, ActorRef]

  override def receiveRecover: Receive = {
    case event: Event =>
      updateState(event)
  }

  def updateState(event: Event) = event match {
    case IssueCreated(name) =>
      existingIssues = existingIssues.updated(name, context.actorOf(Issue.props(name), Issue.actorName(name)))
  }

  override def receiveCommand: Receive = {
    case CreateNewIssue(name) if (!existingIssues.contains(name)) =>
      log.info("Issue \"{}\" added", name)
      persist(IssueCreated(name)) { evt =>
        updateState(evt)
      }
    case CreateNewIssue(name) =>
      log.error("Issue \"{}\" already exists", name)
    case cmd: Issue.Command if (existingIssues.contains(cmd.issue)) =>
      existingIssues.get(cmd.issue).get forward cmd
    case cmd: Issue.Command =>
      log.error("No issue \"{}\" found", cmd.issue)
  }

}

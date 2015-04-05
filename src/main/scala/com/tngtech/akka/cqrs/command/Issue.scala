package com.tngtech.akka.cqrs.command

import java.time.LocalDateTime

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.tngtech.akka.cqrs.command.Issue._

object Issue {

  sealed trait Command {
    def issue: String
  }

  case class AddComment(issue: String, author: String, text: String) extends Command

  case class ChangeSeverity(issue: String, severity: String) extends Command

  case class CloseIssue(issue: String, user: String, message: String) extends Command

  sealed trait Event

  case class CommentAdded(author: String, date: LocalDateTime, text: String) extends Event

  case class SeverityChanged(severity: String) extends Event

  case object IssueClosed extends Event

  def props(name: String): Props = Props(classOf[Issue], name)

  def actorName(name: String) = s"Project_$name".replace(" ", "_")
}

class Issue(name: String) extends PersistentActor with ActorLogging {
  override def persistenceId: String = actorName(name)

  var closed = false

  override def receiveRecover: Receive = {
    case event: Event =>
      updateStatus(event)
      log.info("recovering \"{}\" ", event)
  }

  def updateStatus(event: Event): Unit = event match {
    case IssueClosed => closed = true
    case e => log.debug("processing event \"{}\"",e)
  }

  override def receiveCommand: Receive = {
    case _ if(closed) => log.error("Issue \"{}\" is already closed", name)
    case AddComment(_, author, text) =>
      log.info("Adding comment \"{}\"", text)
      persist(CommentAdded(author, LocalDateTime.now(), text)) {
        updateStatus(_)
      }
    case ChangeSeverity(_, severity) =>
      persist(SeverityChanged(severity)) {
        updateStatus(_)
      }
    case CloseIssue(_, user, message) =>
      persist(CommentAdded(user, LocalDateTime.now(), message)) {
        updateStatus(_)
      }
      persist(IssueClosed) {
        updateStatus(_)
      }
    case cmd => log.info("received unknown command \"{}\"", cmd)
  }

}

package com.tngtech.akka.cqrs.query

import akka.actor.{ActorRef, ActorLogging, Props}
import akka.persistence.PersistentView
import com.tngtech.akka.cqrs.command.{Issue, Issues}
import com.tngtech.akka.cqrs.command.Issues.IssueCreated
import com.tngtech.akka.cqrs.query.IssuesView._

object IssuesView {

  case object GetAll

  case class IssueSet(issues: Iterable[String])

  def props = Props(classOf[IssuesView])

  val actorName = "IssuesView"
}

class IssuesView extends PersistentView with ActorLogging {
  override def viewId: String = actorName

  override def persistenceId: String = Issues.actorName

  var existingIssues = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case IssueCreated(name) if (!existingIssues.contains(name)) =>
      log.info("Added issue \"{}\"", name)
      existingIssues = existingIssues.updated(name, context.actorOf(IssueView.props(name), IssueView.actorName(name)))
    case GetAll =>
      log.debug("Returning issue list")
      sender ! IssueSet(existingIssues.keys)
    case e: IssueView.Query =>
      log.info("Received issue query")
      if(existingIssues.contains(e.issue)) {
        existingIssues.get(e.issue).get.forward(e)
      }
    case q => log.info("Unknown query \"{}\"", q)
  }
}

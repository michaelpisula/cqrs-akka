package com.tngtech.akka.cqrs.query

import java.time.LocalDateTime

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentView
import com.tngtech.akka.cqrs.command.Issue
import com.tngtech.akka.cqrs.query.IssueView._

object IssueView {
  def props(name: String) = Props(classOf[IssueView], name)

  def actorName(name: String) = s"View_$name".replace(' ', '_')

  case class Comment(author: String, date: LocalDateTime, text: String)

  sealed trait Query {
    def issue: String
  }
  case class GetComments(issue: String) extends Query

}

class IssueView(name: String) extends PersistentView with ActorLogging {
  override def viewId: String = actorName(name)

  override def persistenceId: String = Issue.actorName(name)

  var comments = Set.empty[Comment]

  override def receive: Receive = {
    case Issue.CommentAdded(author, date, text) =>
      log.info("Comment added \"{}\"", text)
      comments += Comment(author, date, text)
    case GetComments(_) =>
      sender ! comments
  }
}

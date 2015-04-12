package com.tngtech.akka.cqrs

import akka.actor.{ActorSystem, Inbox}
import com.tngtech.akka.cqrs.command.Issue.{CloseIssue, AddComment}
import com.tngtech.akka.cqrs.command.Issues
import com.tngtech.akka.cqrs.command.Issues.{CreateNewIssue}
import com.tngtech.akka.cqrs.query.IssueView.GetComments
import com.tngtech.akka.cqrs.query.IssuesView
import com.tngtech.akka.cqrs.query.IssuesView.GetAll

import scala.concurrent.duration._

object Main {
  def main(args: Array[String]) {
    val actorSystem = ActorSystem.create("Issues")
    val projectsActor = actorSystem.actorOf(Issues.props,"issues")
    val queryActor = actorSystem.actorOf(IssuesView.props,"issuesView")

    projectsActor ! CreateNewIssue("More unicorns needed")
    projectsActor ! CreateNewIssue("Did not work on my machine")

    projectsActor ! AddComment("More unicorns needed","Admin","Installed more unicorns")

    projectsActor ! CloseIssue("More unicorns needed","Admin","Added all the unicorns")

    Thread.sleep(10000)

    val inbox = Inbox.create(actorSystem)
    inbox.send(queryActor, GetAll)
    println("Result: " + inbox.receive(1 seconds))

    inbox.send(queryActor, GetComments("More unicorns needed"))
    println("Result: " + inbox.receive(1 seconds))

    actorSystem.shutdown()
  }
}

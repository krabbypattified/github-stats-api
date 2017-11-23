package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}
import scala.concurrent._
import ExecutionContext.Implicits.global

import sangria.schema._
import sangria.execution._
import sangria.macros._
import sangria.marshalling.playJson._

class ApplicationController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action.async { _ ⇒

    val QueryType = ObjectType("Query", fields[Unit, Unit](
      Field("hello", StringType, resolve = _ ⇒ "Hello world!")
    ))

    val schema = Schema(QueryType)

    val query = graphql"{ hello }"

    Executor.execute(schema, query).map(Ok(_))

  }

}
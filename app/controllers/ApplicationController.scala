package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}

import scalaj.http.Http
import Proxy.Proxy
import sangria.macros._
import models._

class ApplicationController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action { request =>

    val proxy = new Proxy(schema, Http("https://api.github.com/graphql").header("Authorization", "token 87c00a6918686a8c106f3c4943067bdfd998d52e"))
    val response = proxy.send(
      gql"""
      query Um {
        search(query: "react", limit: 1) {
          ...repo
        }
      }

      fragment repo on Repository {
        name
        commits {
          times
        }
      }
      """,
      Some("Um"),
      Some("{}")
    )

    println(request.queryString)
    //request.queryString.map { case (k,v) => k -> v.mkString }

    Ok(response)

  }

}
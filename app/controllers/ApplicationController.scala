package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}

import scalaj.http.Http
import Proxy.Proxy
import models._
import io.circe.parser.decode

class ApplicationController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  val proxy = new Proxy(schema, Http("https://api.github.com/graphql").header("Authorization", "token 90b87fdb8f846f2b232e67121d4c9b72c307ef31"))

  def get = Action { request =>
    val queryMap = request.queryString.map { case (k,v) => k -> v.mkString }
    Ok(proxy.send(queryMap))
  }

  def post = Action { request =>
    val postMap = decode[Map[String, String]](request.body.asText.getOrElse("[]")).getOrElse(Map())
    Ok(proxy.send(postMap))
  }

}
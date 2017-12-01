package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}

import scalaj.http.Http
import Proxy.Proxy
import models._

class ApplicationController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  val proxy = new Proxy(schema, Http("https://api.github.com/graphql").header("Authorization", "token 90b87fdb8f846f2b232e67121d4c9b72c307ef31"))

  def get = Action { request =>
    val req = request.queryString.map{ case (k,v) => k -> v.mkString }.toString
    Ok(proxy.send(req))
  }

  def post = Action { request =>
    val req = request.body.asJson.getOrElse("").toString
    Ok(proxy.send(req))
  }

}
package Proxy

import Helpers._
import sangria.ast.Document

import scalaj.http._

class Proxy(val schemaAst: Document, val endpoint: HttpRequest) {

  var proxifier: Proxifier = new Proxifier(schemaAst)

  def send(query: Document, operationName: Option[String] = None, variables: Option[String] = None): String = {
    // Handle syntax error
    val errors = proxifier.validate(query)
    if (errors.nonEmpty) return s"{errors:[${errors.toString}]}"

    // Proxify
    val postData = f"""{
      "operationName": ${operationName.getOrElse("").escape},
      "variables": ${variables.getOrElse("{}").escape},
      "query": ${proxifier.proxify(query).escape}
    }"""
    val res = endpoint.postData(postData).asString
    val body = proxifier.unproxify(res.body)

    body
  }

}

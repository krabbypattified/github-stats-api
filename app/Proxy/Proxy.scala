package Proxy

import Helpers._
import sangria.ast.Document
import sangria.parser.QueryParser

import scalaj.http._

class Proxy(val schemaAst: Document, val endpoint: HttpRequest) {

  var proxifier: Proxifier = new Proxifier(schemaAst)

  def send(parameters: Map[String, String]): String = {
    val query = parameters.get("query")
    val operationName = parameters.get("operationName")
    val variables = parameters.get("variables")

    // Parse query
    if (query.isEmpty) return """{"errors":["message":"Must supply a query."]}"""
    var queryDoc = Document.emptyStub
    val tryQuery = QueryParser.parse(query.get).toOption
    if (tryQuery.isEmpty) return """{"errors":["message":"Unable to parse query. Check your syntax."]}"""
    else queryDoc = tryQuery.get

    // Validate query
    val errors = proxifier.validate(queryDoc)
    if (errors.nonEmpty) return s"""{"errors":["message":${errors.toString.escape}]}"""

    // Proxify
    var postData = "{"
    if (operationName.isDefined) postData += s""" "operationName": ${operationName.getOrElse("").escape}, """
    if (variables.isDefined) postData += s""" "variables": ${variables.getOrElse("{}").escape}, """
    postData += s""" "query": ${proxifier.proxify(queryDoc).escape} """
    postData += "}"

    // Fetch
    val res = endpoint.postData(postData).asString

    // Unproxify
    proxifier.unproxify(res.body)
  }

}

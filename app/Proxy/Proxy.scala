package Proxy

import Helpers._
import io.circe.Json
import sangria.ast.Document
import sangria.parser.QueryParser
import io.circe.parser._

import scalaj.http._
import io.circe.optics.JsonPath._

class Proxy(val schemaAst: Document, val endpoint: HttpRequest) {

  var proxifier: Proxifier = new Proxifier(schemaAst)

  def send(request: String): String = {
    val json = parse(request).getOrElse(Json.obj())
    val query = root.query.string.getOption(json)
    val operationName = root.operationName.string.getOption(json)
    val variables = root.variables.json.getOption(json).getOrElse(Json.Null)

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
    if (variables != Json.Null) postData += s""" "variables": ${variables.toString}, """
    postData += s""" "query": ${proxifier.proxify(queryDoc).escape} """
    postData += "}"

    // Fetch
    val res = endpoint.postData(postData).asString

    // Unproxify
    proxifier.unproxify(res.body)
  }

}

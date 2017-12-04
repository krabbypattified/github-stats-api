package Proxy

import Helpers._
import io.circe.Json
import sangria.ast.Document
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

    // Validate query
    if (query.isEmpty) return """{"errors":["message":"Must supply a query."]}"""
    val parsedQuery = proxifier.parseQuery(query.get)
    if (parsedQuery.isLeft) return parsedQuery.left.get

    // Proxify
    var postData = "{"
    if (operationName.isDefined) postData += s""" "operationName": ${operationName.getOrElse("").escape}, """
    if (variables != Json.Null) postData += s""" "variables": ${variables.toString}, """
    postData += s""" "query": ${proxifier.proxify(parsedQuery.right.get).escape} """
    postData += "}"

    // Fetch
    val res = endpoint.postData(postData).asString

    // Unproxify
    proxifier.unproxify(res.body)
  }

}

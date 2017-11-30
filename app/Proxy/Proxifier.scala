package Proxy

import Helpers._
import sangria.ast._
import sangria.validation.{QueryValidator, Violation}
import io.circe._
import io.circe.parser._
import io.circe.syntax._

class Proxifier(val schemaAst: Document) {

  val proxy = new ProxySchema(schemaAst)


  def validate(query: Document): Vector[Violation] = {
    QueryValidator.default.validateQuery(proxy.schema, query)
  }

  def proxify(query: Document): String = {
    var string = ""

    for (_d <- query.definitions) _d match {

      case quer @ OperationDefinition(_,_,_,_,_,_,_,_) =>
        if (quer.name.isDefined) string += s"query ${quer.name.get} {\n"
        else string += "{\n"
        for (field <- quer.selections) string += _proxify(field, quer.operationType.toString)
        string += "}\n"

      case frag @ FragmentDefinition(_,_,_,_,_,_,_) =>
        string += s"fragment ${frag.name} on ${frag.typeCondition.name} {\n"
        for (field <- frag.selections) string += _proxify(field, frag.typeCondition.name)
        string += "}\n"

      case _ => ; // TODO double underscore to schema builder ie __schema
    }

    string
  }


  def _proxify(field: Any, onType: String): String = field match {

    case FragmentSpread(name,_,_,_) => s"...$name" + "\n"

    case f @ Field(_,_,_,_,_,_,_,_) =>
      val proxifier = proxy.proxifier(f, onType)
      var string = ""

      if (!f.hasChildren) string += proxifier.foldersToValue

      else {
        val (left, right) = proxifier.foldersToWrapper
        string += left
        for (_field <- f.children) string += _proxify(_field, proxifier.ofType)
        string += right
      }

      string

    case _ => "";
  }


  def unproxify(_data: String): String = {
    parse(_data).getOrElse(Json.Null).hcursor.downField("data").withFocus(data => {
      if (data.asObject.isDefined) data.asObject.get.toMap.map(f => f._2.remap(f._1, "Query", proxy.unproxifier)).asJson
      else data
    }).top.get.toString
  }

}

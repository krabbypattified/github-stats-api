package Proxy

import io.circe.Json
import sangria.ast._
import sangria.schema.{ListType, ObjectType, OptionType, ScalarType, Schema}

class ProxySchema(schemaAst: Document) {

  val schema: Schema[Any, Any] = Schema.buildFromAst(schemaAst)


  case class SType(name: String, fields: Vector[SField])
  case class SField(name: String, ofType: String, proxy: Option[String], fragment: Boolean = false) {
    val proxyFolders: Option[Array[String]] =
      if (proxy.isDefined) Some(proxy.get.replaceAll("\\(.*?\\)", "").split("\\."))
      else None
  }
  private def getSType(name: String): Option[SType] = simpleSchema.find(_.name == name)
  private def getSField(stype: SType, name: String): Option[SField] = stype.fields.find(_.name == name)
  def getField(field: String, onType: String): Option[SField] = {
    val sType = getSType(onType)
    if (sType.isEmpty) None
    else getSField(sType.get, field)
  }
  def getFieldByProxyRoot(proxyRoot: String, onType: String): Option[SField] = {
    val sType = getSType(onType)
    if (sType.isDefined) {
      val sField = sType.get.fields.find(_.name == proxyRoot)
      val betterSField = sType.get.fields.find(p => p.proxyFolders.isDefined && p.proxyFolders.get.head == proxyRoot)
      if (betterSField.isDefined) betterSField
      else if (sField.isDefined) sField
      else None
    }
    else None
  }


  def aType(x: Any): String = x match {
    case ScalarType(name, _, _, _, _, _, _, _) => name
    case ObjectType(name, _, _, _, _, _) => name
    case list @ ListType(_) => lisType(list)
    case opt @ OptionType(_) => optType(opt)
    case otr => otr.toString
  }
  private def lisType(x: ListType[Any]): String = aType(x.ofType)
  private def optType(x: OptionType[Any]): String = aType(x.ofType)


  // Create a Vector[SType(fields)] simple schema
  private val simpleSchema: Vector[SType] = {
    var _schema: Vector[SType] = Vector()
    for (_stype @ ObjectType(_, _, _, _, _, _) <- schema.typeList) {
      val STYPE = _stype
      _schema = _schema :+ SType(name = STYPE.name, fields = {
        var _fields: Vector[SField] = Vector()
        for (_sfield <- STYPE.fields) {
          val SFIELD = _sfield
          _fields = _fields :+ SField(
            name = SFIELD.name,
            ofType = aType(SFIELD.fieldType),
            proxy = {
              val func = SFIELD.astDirectives.find(_.name == "Proxy")
              if (func.isEmpty) None
              else Some(func.get.arguments.find(_.name == "route").get.value match {
                case _val: StringValue => _val.value
                case _val => _val.toString
              })
            },
            fragment = {
              val func = SFIELD.astDirectives.find(_.name == "Proxy")
              if (func.isEmpty) false
              else {
                val fragment = func.get.arguments.find(_.name == "fragment")
                if (fragment.isEmpty) false
                else fragment.get.value match {
                  case _val: BooleanValue => _val.value
                  case _ => false
                }
              }
            }
          )
        }
        _fields
      })
    }
    _schema
  }


  def proxifier(field: Field, onType: String): Unmapper = {
    val sfield = getField(field.name, onType)
    if (sfield.isEmpty) Unmapper(field.name, onType)
    else if (sfield.get.proxy.isEmpty) Unmapper(field.name, sfield.get.ofType)
    else {
      val arguments = field.arguments.map(f => (f.name, f.value.renderPretty)).toMap
      Unmapper(sfield.get.proxy.get, sfield.get.ofType, arguments, sfield.get.fragment)
    }
  }

  def unproxifier(value: Json, proxyRoot: String, onType: String): Remapper = {
    // value: 202, key: blahNum, onType: Actor
    val field = getFieldByProxyRoot(proxyRoot, onType)
    if (field.isEmpty) Remapper(value, proxyRoot, proxyRoot, onType)
    else if (field.get.proxy.isEmpty) Remapper(value, proxyRoot, proxyRoot, field.get.ofType)
    else Remapper(value, field.get.proxyFolders.get.mkString("."), field.get.name, field.get.ofType)
  }

}

package it.softfork.bitcoin4s

import it.softfork.bitcoin4s.script._
import julienrf.json.derived.flat.owrites
import it.softfork.bitcoin4s.Utils._
import play.api.libs.json._

object ApiModels {

  sealed trait InterpreterResultOut

  object InterpreterResultOut {
    case object NoResult extends InterpreterResultOut
    case class Result(value: Boolean) extends InterpreterResultOut

    def fromInterpreterResult(result: Option[Boolean]) = {
      result.map(Result.apply).getOrElse(NoResult)
    }
  }

  case class InterpreterStateOut(
    scriptPubKey: Seq[ScriptElement],
    scriptSig: Seq[ScriptElement],
    currentScript: Seq[ScriptElement],
    scriptP2sh: Option[Seq[ScriptElement]] = None,
    scriptWitness: Option[Seq[ScriptElement]] = None,
    scriptWitnessStack: Option[Seq[ScriptElement]] = None,
    stack: Seq[ScriptElement] = Seq.empty,
    altStack: Seq[ScriptElement] = Seq.empty,
    stage: ScriptExecutionStage
  )

  object InterpreterStateOut {

    def fromInterpreterState(interpreterState: InterpreterState) = {
      InterpreterStateOut(
        scriptPubKey = interpreterState.scriptPubKey,
        scriptSig = interpreterState.scriptSig,
        currentScript = interpreterState.currentScript,
        scriptP2sh = interpreterState.scriptP2sh,
        scriptWitness = interpreterState.scriptWitness,
        scriptWitnessStack = interpreterState.scriptWitnessStack,
        stack = interpreterState.stack,
        altStack = interpreterState.altStack,
        stage = interpreterState.scriptExecutionStage
      )
    }
  }

  case class InterpreterOutcome(
    result: InterpreterResultOut,
    state: InterpreterStateOut
  )

  case object TransactionInputNotFound extends RuntimeException("Transaction input not found")

  implicit val interpretResultOutWriter: OWrites[InterpreterResultOut] = {
    owrites[InterpreterResultOut]((JsPath \ "type").format[String])
  }

  implicit val scriptElementWriter: Writes[ScriptElement] = Writes[ScriptElement] {
    case scriptNum: ScriptNum =>
      Json.obj("type" -> "ScriptNum", "value" -> scriptNum.value)
    case scriptConstant: ScriptConstant =>
      Json.obj("type" -> "ScriptConstant", "value" -> s"0x${scriptConstant.bytes.toHex}")
    case scriptOpcode: ScriptOpCode =>
      Json.obj("type" -> scriptOpcode.name, "value" -> scriptOpcode.value)
  }


  implicit val scriptElementReader: Reads[ScriptElement] = Reads[ScriptElement] { in =>
    (in \ "type").validate[String].flatMap {
      case "ScriptNum" =>
        (in \ "value").validate[String].map { x =>
          ScriptNum.apply(x.toLong)
        }
      case _ =>
        throw new NotImplementedError("Script element reader not implemented")
    }
  }

  implicit val scriptElementFormat: Format[ScriptElement] = Format(scriptElementReader, scriptElementWriter)

  implicit val scriptExecutionStageWriter: OWrites[ScriptExecutionStage] = {
    owrites[ScriptExecutionStage]((JsPath \ "type").format[String])
  }

  implicit val InterpreterStateWriter: Writes[InterpreterStateOut] = Json.writes[InterpreterStateOut]

  implicit val InterpretOutcome: Writes[InterpreterOutcome] = Json.writes[InterpreterOutcome]
}

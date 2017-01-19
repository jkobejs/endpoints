package cqrs.publicserver

import java.net.URLEncoder
import java.util.UUID

import cats.Traverse
import cqrs.queries._
import cqrs.commands.{AddRecord, CreateMeter, MeterCreated, StoredEvent}
import play.api.libs.ws.WSClient
import play.api.routing.{Router => PlayRouter}
import cats.instances.option._
import cats.instances.future._
import endpoints.play.routing.{CirceEntities, Endpoints, OptionalResponses}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Implementation of the public API based on our “commands” and “queries” microservices.
  */
class PublicServer(
  commandsBaseUrl: String,
  queriesBaseUrl: String,
  wsClient: WSClient)(implicit
  ec: ExecutionContext
) extends PublicEndpoints
  with Endpoints
  with CirceEntities
  with OptionalResponses {

  private val commandsClient = new CommandsClient(commandsBaseUrl, wsClient)
  private val queriesClient = new QueriesClient(queriesBaseUrl, wsClient)

  val routes: PlayRouter.Routes =
    routesFromEndpoints(

      createMeter.implementedByAsync { createData =>
        for {
          maybeEvent <- commandsClient.command(CreateMeter(createData.label))
          maybeMeter <- Traverse[Option].flatSequence(
            maybeEvent.collect {
              case StoredEvent(t, MeterCreated(id, _)) =>
                queriesClient.query(FindById(id, after = Some(t)))(circeJsonEncoder(QueryReq.queryEncoder), circeJsonDecoder(QueryResp.queryDecoder))
                  .map(_.value)
            }
          )
          meter <- maybeMeter.fold[Future[Meter]](Future.failed(new NoSuchElementException))(Future.successful)
        } yield meter
      },

      addRecord.implementedByAsync { case (id, addData) =>
        commandsClient.command(AddRecord(id, addData.date, addData.value))
          .flatMap(_.fold[Future[Unit]](Future.failed(new NoSuchElementException))(_ => Future.successful(())))
      },

      listMeters.implementedByAsync { _ =>
        queriesClient.query(FindAll)(circeJsonEncoder(QueryReq.queryEncoder), circeJsonDecoder(QueryResp.queryDecoder))
          .map(_.value)
      }

    )

  implicit def uuidSegment: Segment[UUID] =
    new Segment[UUID] {
      def decode(segment: String): Option[UUID] = Try(UUID.fromString(segment)).toOption
      def encode(uuid: UUID): String = URLEncoder.encode(uuid.toString, utf8Name)
    }

}

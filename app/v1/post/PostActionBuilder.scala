package v1.post

import com.tersesystems.blindsight._
import javax.inject.Inject
import nl.grons.metrics4.scala.DefaultInstrumented
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * A wrapped request for post resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait PostRequestHeader
    extends MessagesRequestHeader
    with PreferredMessagesProvider
class PostRequest[A](request: Request[A], val messagesApi: MessagesApi)
    extends WrappedRequest(request)
    with PostRequestHeader

/**
  * The action builder for the Post resource.
  *
  * This is the place to put logging, metrics, to augment
  * the request with contextual data, and manipulate the
  * result.
  */
class PostActionBuilder @Inject()(messagesApi: MessagesApi,
                                  playBodyParsers: PlayBodyParsers)(
    implicit val executionContext: ExecutionContext)
    extends ActionBuilder[PostRequest, AnyContent]
    with HttpVerbs with DefaultInstrumented {

  // should be a way to get flowlogger working better with Future[Result]
  //private val flowlogger = LoggerFactory.getLogger.flow
  private val logger = LoggerFactory.getLogger

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type PostRequestBlock[A] = PostRequest[A] => Future[Result]

  override def invokeBlock[A](request: Request[A],
                              block: PostRequestBlock[A]): Future[Result] = {
    val start = System.currentTimeMillis()
    val future = block(new PostRequest(request, messagesApi))

    future.map { result =>
      import DSL._
      val durationMs = System.currentTimeMillis() - start
      val snapshot = metrics.registry.histogram("jvm_alloc_rate").getSnapshot
      val mean = snapshot.getMean
      val resultTag = bobj(
        "result.status" -> result.header.status,
        "duration_ms" -> durationMs,
        "alloc_rate.mean_bsec" -> mean,
        "alloc_rate.mean_mbsec" -> mean / 1e6,
      )
      logger.info(Markers(resultTag))
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}

/**
  * Packages up the component dependencies for the post controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class PostControllerComponents @Inject()(
    postActionBuilder: PostActionBuilder,
    postResourceHandler: PostResourceHandler,
    actionBuilder: DefaultActionBuilder,
    parsers: PlayBodyParsers,
    messagesApi: MessagesApi,
    langs: Langs,
    fileMimeTypes: FileMimeTypes,
    executionContext: scala.concurrent.ExecutionContext)
    extends ControllerComponents

/**
  * Exposes actions and handler to the PostController by wiring the injected state into the base class.
  */

class PostBaseController @Inject()(@noinline pcc: PostControllerComponents) extends BaseController {
  override protected def controllerComponents: ControllerComponents = pcc

  def PostAction: PostActionBuilder = pcc.postActionBuilder

  def postResourceHandler: PostResourceHandler = pcc.postResourceHandler
}

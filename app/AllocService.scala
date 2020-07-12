import com.tersesystems.blindsight._
import javax.inject._
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import jvm_alloc_rate_meter.MeterThread
import nl.grons.metrics4.scala._

@Singleton
class AllocService @Inject()(lifecycle: ApplicationLifecycle) extends DefaultInstrumented  {

  private val logger = LoggerFactory.getLogger

  import com.codahale.metrics.Histogram

  import com.codahale.metrics.SlidingTimeWindowArrayReservoir
  import java.util.concurrent.TimeUnit

  val hist = new Histogram(new SlidingTimeWindowArrayReservoir(10, TimeUnit.SECONDS))
  metrics.registry.register("jvm_alloc_rate", hist)

  private val t = new MeterThread(hist.update(_))
  t.start()

  logger.info("New allocation thread created")

  lifecycle.addStopHook { () =>
    logger.info("Terminating allocation meter thread")
    Future.successful(t.terminate())
  }
}

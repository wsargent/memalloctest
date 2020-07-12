import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import v1.post._

class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with ScalaModule {

  override def configure(): Unit = {
    bind[PostRepository].to[PostRepositoryImpl].asEagerSingleton()
    bind[AllocService].asEagerSingleton()
  }
}

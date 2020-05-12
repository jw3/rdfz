package rdf

import java.io.FileInputStream
import java.nio.file.Path

import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.impl.{LinkedHashModelFactory, SimpleValueFactory}
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.eclipse.rdf4j.rio.{RDFFormat, Rio}
import zio._

object io extends scala.App {
  type RdfIO = Has[RdfIO.Service]
  object RdfIO {

    trait Service {
      implicit val valueFactory: ValueFactory = SimpleValueFactory.getInstance
      implicit val modelFactory: ModelFactory = new LinkedHashModelFactory

      def read(path: Path): IO[Exception, Model]
    }

    def make(): ZLayer.NoDeps[Nothing, RdfIO] = ZLayer.succeed(
      new Service {
        def read(path: Path): IO[Exception, Model] = ZIO.succeed(rdf.model()).map { m =>
          val reader = Rio.createParser(RDFFormat.TURTLE)
          reader.setRDFHandler(new StatementCollector(m))
          reader.parse(new FileInputStream(path.toFile), defaultNamespace)
          m
        }
      }
    )
    def read(path: Path): ZIO[RdfIO, Exception, Model] = ZIO.accessM(_.get.read(path))
  }
}

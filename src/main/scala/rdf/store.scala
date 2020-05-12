package rdf

import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.query.{GraphQueryResult, QueryLanguage}
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.eclipse.rdf4j.sail.memory.MemoryStore
import zio._

import scala.collection.JavaConverters._

object store {
  type RdfStore = Has[RdfStore.Service]
  object RdfStore {
    trait Service {
      def ask(sparql: String): IO[Throwable, Boolean]
      def count(sparql: String): IO[Throwable, Long]
      def select(sparql: String): Task[GraphQueryResult]
      def selectModel(sparql: String): Task[Model]
      def selectModel(s: Resource, p: IRI, o: Value, g: Resource*): Task[Model]

      def add(m: Model): IO[Throwable, Unit]
      def add(stmt: Statement): IO[Throwable, Unit]
      def add(s: S, p: P, o: O, g: Option[G]): IO[Throwable, Unit]
      def update(sparql: String): IO[Throwable, Unit]

      def begin(): IO[Throwable, Unit]
      def commit(): IO[Throwable, Unit]
      def rollback(): IO[Throwable, Unit]
    }

    def make(): ZLayer.NoDeps[Throwable, RdfStore] =
      ZLayer.fromEffect(IO.effect(new SailRepository(new MemoryStore()).getConnection).map { implicit connection =>
        new Service {
          import rdf.implicits._

          def ask(sparkql: String): IO[Throwable, Boolean] = IO.fromTry(query.booleanQuery(sparkql))
          def count(sparql: String): IO[Throwable, Long] =
            IO.fromTry(query.tupleQuery(sparql))
              .map(v => v.headOption.map(vv => literal(vv.getValue.stringValue()).longValue()).getOrElse(-1L))

          def select(sparql: String): Task[GraphQueryResult] =
            IO.fromTry(query.graphQuery(sparql))

          def selectModel(sparql: String): Task[Model] = IO.succeed(rdf.model()).tap { m =>
            IO.effect(query.graphQuery(sparql, new StatementCollector(m))).as(m)
          }

          def selectModel(s: Resource, p: IRI, o: Value, g: Resource*): Task[Model] =
            IO.effect(connection.getStatements(s, p, o, false, g: _*)).map(_.asScala).map(rdf.model)

          def add(stmt: Statement): IO[Throwable, Unit] =
            IO.fromFunction { _ =>
              connection.add(stmt)
            }

          def add(m: Model): IO[Throwable, Unit] =
            IO.fromFunction { _ =>
              connection.add(m)
            }

          def add(s: S, p: P, o: O, g: Option[G]): IO[Throwable, Unit] =
            IO.fromFunction { _ =>
              connection.add(s, p, o, g.getOrElse(defaultGraph))
            }
          def update(sparql: String): IO[Throwable, Unit] =
            IO.fromFunction(_ =>
              connection.prepareUpdate(QueryLanguage.SPARQL, sparql, defaultNamespace).execute()
            )

          def begin(): IO[Throwable, Unit] = IO.fromFunction { _ =>
            connection.begin()
          }
          def commit(): IO[Throwable, Unit] = IO.fromFunction { _ =>
            connection.commit()
          }

          def rollback(): IO[Throwable, Unit] = IO.fromFunction { _ =>
            connection.rollback()
          }
        }
      })

    def ask(sparql: String): ZIO[RdfStore, Throwable, Boolean] = ZIO.accessM(_.get.ask(sparql))
    def count(sparql: String): ZIO[RdfStore, Throwable, Long] = ZIO.accessM(_.get.count(sparql))
    def select(sparql: String): ZIO[RdfStore, Throwable, GraphQueryResult] = ZIO.accessM(_.get.select(sparql))
    def selectModel(sparql: String): ZIO[RdfStore, Throwable, Model] = ZIO.accessM(_.get.selectModel(sparql))
    def selectModel(s: Resource, p: IRI, o: Value, g: Resource*): ZIO[RdfStore, Throwable, Model] =
      ZIO.accessM(_.get.selectModel(s, p, o, g: _*))

    def add(m: Model): ZIO[RdfStore, Throwable, Unit] = ZIO.accessM(_.get.add(m))
    def add(stmt: Statement): ZIO[RdfStore, Throwable, Unit] = ZIO.accessM(_.get.add(stmt))
    def add(s: S, p: P, o: O, g: Option[G] = None): ZIO[RdfStore, Throwable, Unit] = ZIO.accessM(_.get.add(s, p, o, g))
    def update(sparql: String): ZIO[RdfStore, Throwable, Unit] = ZIO.accessM(_.get.update(sparql))

    def begin(): ZIO[RdfStore, Throwable, Unit] = ZIO.accessM(_.get.begin())
    def commit(): ZIO[RdfStore, Throwable, Unit] = ZIO.accessM(_.get.commit())
    def rollback(): ZIO[RdfStore, Throwable, Unit] = ZIO.accessM(_.get.rollback())
  }
}

package rdf

import org.eclipse.rdf4j.model.ValueFactory
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.scalatest.{Matchers, OptionValues, WordSpec}
import rdf.store.RdfStore
import zio._

class TransactionSpec extends WordSpec with Matchers with OptionValues {
  implicit val valueFactory: ValueFactory = SimpleValueFactory.getInstance

  private val store = rdf.store.RdfStore.make()
  private def iri() = rdf.iri(rdf.random(4))

  "transaction" should {
    "commit" in {
      val addStatement: ZIO[RdfStore, Throwable, (Long, Long)] = for {
        pre <- rdf.store.RdfStore.count("select (count(*) AS ?count) {?s ?p ?o}")
        _ <- rdf.store.RdfStore.begin()
        _ <- rdf.store.RdfStore.add(iri(), iri(), iri())
        _ <- rdf.store.RdfStore.add(iri(), iri(), iri())
        _ <- rdf.store.RdfStore.commit()
        post <- rdf.store.RdfStore.count("select (count(*) AS ?count) {?s ?p ?o}")
      } yield (pre, post)
      Runtime.unsafeFromLayer(store).unsafeRun(addStatement)

      val (pre, post) = Runtime.unsafeFromLayer(store).unsafeRun(addStatement)
      pre shouldBe 0
      post shouldBe 2
    }

    "rollback" in {
      val addStatement: ZIO[RdfStore, Throwable, (Long, Long)] = for {
        pre <- rdf.store.RdfStore.count("select (count(*) AS ?count) {?s ?p ?o}")
        _ <- rdf.store.RdfStore.begin()
        _ <- rdf.store.RdfStore.add(iri(), iri(), iri())
        _ <- rdf.store.RdfStore.add(iri(), iri(), iri())
        _ <- rdf.store.RdfStore.rollback()
        post <- rdf.store.RdfStore.count("select (count(*) AS ?count) {?s ?p ?o}")
      } yield (pre, post)

      val (pre, post) = Runtime.unsafeFromLayer(store).unsafeRun(addStatement)
      pre shouldBe 0
      post shouldBe 0
    }
  }
}

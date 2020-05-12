package rdf

import org.eclipse.rdf4j.query.{Binding, GraphQueryResult, QueryLanguage}
import org.eclipse.rdf4j.repository.RepositoryConnection
import org.eclipse.rdf4j.rio.RDFHandler

import scala.collection.JavaConverters._
import scala.util.Try

object query {
  def booleanQuery(sparql: String)(implicit conn: RepositoryConnection): Try[Boolean] = Try {
    conn.prepareBooleanQuery(QueryLanguage.SPARQL, sparql, rdf.defaultNamespace).evaluate
  }

  def graphQuery(sparql: String)(implicit conn: RepositoryConnection): Try[GraphQueryResult] = Try {
    conn.prepareGraphQuery(QueryLanguage.SPARQL, sparql, rdf.defaultNamespace).evaluate
  }

  def graphQuery(sparql: String, handler: RDFHandler)(implicit conn: RepositoryConnection): Unit = Try {
    conn.prepareGraphQuery(QueryLanguage.SPARQL, sparql, rdf.defaultNamespace).evaluate(handler)
  }

  def tupleQuery(sparql: String)(implicit conn: RepositoryConnection): Try[Iterable[Binding]] = Try {
    conn
      .prepareTupleQuery(QueryLanguage.SPARQL, sparql, defaultNamespace)
      .evaluate
      .asScala
      .flatMap(_.iterator().asScala.toSeq)
  }
}

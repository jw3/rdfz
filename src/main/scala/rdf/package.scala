import org.eclipse.rdf4j.model._
import org.eclipse.rdf4j.model.impl.{LinkedHashModelFactory, SimpleValueFactory}
import org.eclipse.rdf4j.model.vocabulary.XMLSchema._

import scala.util.Random

package object rdf {
  type S = Resource
  type P = IRI
  type O = Value
  type G = Resource

  private val baseUri: String = "urn:xx"
  val defaultNamespace: String = baseUri + "#"
  val nullGraph: G = null
  val defaultGraph: G = nullGraph
  val allGraphs: Seq[G] = Seq.empty

  def iri(s: String)(implicit f: ValueFactory): IRI =
    if (s.contains('#')) f.createIRI(s)
    else f.createIRI(defaultNamespace, s)

  def iri(ns: String, ln: String)(implicit f: ValueFactory): IRI = f.createIRI(ns, ln)

  def bnode()(implicit f: ValueFactory): BNode = f.createBNode()
  def bnode(id: String)(implicit f: ValueFactory): BNode = f.createBNode(id)

  def literal(label: String)(implicit f: ValueFactory): Literal = f.createLiteral(label)
  def literal(label: String, lang: String)(implicit f: ValueFactory): Literal = f.createLiteral(label, lang)
  def literal(label: String, datatype: IRI)(implicit f: ValueFactory): Literal = f.createLiteral(label, datatype)

  def value(v: Any)(implicit f: ValueFactory): Value = v match {
    case v: Value          => v
    case v: String         => f.createLiteral(v)
    case v: java.util.Date => f.createLiteral(v)
    case v: Boolean        => f.createLiteral(v)
    case v: Byte           => f.createLiteral(v)
    case v: Short          => f.createLiteral(v)
    case v: Int            => f.createLiteral(v)
    case v: Long           => f.createLiteral(v)
    case v: Float          => f.createLiteral(v)
    case v: Double         => f.createLiteral(v)
  }

  def devalue[V](v: Value): V = v match {
    case v: Literal =>
      v.getDatatype match {
        case BYTE    => v.byteValue.asInstanceOf[V]
        case SHORT   => v.shortValue.asInstanceOf[V]
        case INT     => v.intValue.asInstanceOf[V]
        case LONG    => v.longValue.asInstanceOf[V]
        case FLOAT   => v.floatValue.asInstanceOf[V]
        case DOUBLE  => v.doubleValue.asInstanceOf[V]
        case STRING  => v.stringValue.asInstanceOf[V]
        case BOOLEAN => v.booleanValue.asInstanceOf[V]
      }
    case _ => v.asInstanceOf[V]
  }

  def statement(s: Resource, p: IRI, o: Value, g: Resource)(implicit f: ValueFactory): Statement =
    statement(s, p, o, Some(g))

  def statement(s: Resource, p: IRI, o: Value, g: Option[Resource] = None)(implicit f: ValueFactory): Statement =
    f.createStatement(s, p, o, g.orNull)

  def model(stmts: Iterable[Statement] = Seq.empty)(implicit m: ModelFactory): Model = {
    import scala.jdk.CollectionConverters._
    val model = m.createEmptyModel()
    model.addAll(stmts.asJavaCollection)
    model
  }

  def random(): String = random(4)
  def random(len: Int): String = Random.alphanumeric.take(len).mkString

  object implicits {
    implicit val valueFactory: ValueFactory = SimpleValueFactory.getInstance
    implicit val modelFactory: ModelFactory = new LinkedHashModelFactory
    implicit def string2iri(s: String)(implicit f: ValueFactory): IRI = f.createIRI(s)

    implicit class RichModel(m: Model) {
      def query(
                 s: Option[Resource] = None,
                 p: Option[IRI] = None,
                 o: Option[Value] = None,
                 c: Option[Resource] = None
               ): Model =
        m.filter(s.orNull, p.orNull, o.orNull, c.orNull)
    }

    /*
     * implicits
     */

    implicit def singleValToSeq[V <: Value](value: V): Seq[V] = Seq(value)

    implicit def singleStmtToSeq(stmt: Statement): Seq[Statement] = Seq(stmt)

    implicit def varStmtToSeq(stmt: Statement*): Seq[Statement] = stmt

    implicit def stmtToModel(stmt: Statement): Model = model(singleStmtToSeq(stmt))

    implicit def valueToOption[V <: Value](v: V): Option[V] = Option(v)

    implicit def valueCollectionToScala[V <: Value](c: java.util.Collection[V]): Iterable[V] = {
      import scala.jdk.CollectionConverters._
      c.asScala
    }

    implicit def statementCollectionToScala[V <: Statement](c: java.util.Collection[V]): Iterable[V] = {
      import scala.jdk.CollectionConverters._
      c.asScala
    }
  }
}

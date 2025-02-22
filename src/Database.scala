package qk

import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource
import org.apache.commons.dbcp2.BasicDataSource
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

case class Database(
  driverClass: String,
  url:         String,
  userName:    String,
  password:    Option[String] = None,
  initScript:  Option[String] = None
):
  lazy val dataSource =
    Class.forName(driverClass)
    val ds = BasicDataSource()
    ds.setUrl(url)
    ds.setUsername(userName)
    password.foreach(ds.setPassword)
    initScript.foreach: script =>
      val steps = script.split(";\\s*\n").toList
      ds.setConnectionInitSqls(steps.asJava)
    ds

  def connect() = dataSource.getConnection()
end Database

object Database:
  extension(conn: Connection)
    def executeQuery(
      sql: String,
      params: List[Any | Null] = List.empty
    ): Try[List[Map[String, Any | Null]]] =

      Using(conn.prepareStatement(sql)): ps =>
        val md = ps.getParameterMetaData()
        require(
          md.getParameterCount() == params.size,
          s"Parameter count mismatch: ${md.getParameterCount}, not ${params.size}"
        )

        params.indices.foreach: i =>
          val paramValue = params(i)
          if paramValue == null then
            ps.setNull(i + 1, md.getParameterType(i + 1))
          else
            ps.setObject(i + 1, paramValue)

        LazyList
          .iterate(ps.executeQuery())(identity)
          .takeWhile(_.next())
          .map: rs =>
            (1 to rs.getMetaData().getColumnCount())
              .map: i =>
                rs.getMetaData().getColumnLabel(i).toLowerCase()
                  -> rs.getObject(i)
              .toMap
          .toList
    end executeQuery
end Database

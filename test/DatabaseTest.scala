package qk

import Database.executeQuery
import scala.util.{Failure, Success, Try, Using}

class DatabaseTest extends munit.FunSuite:
  val initScript = """
    create table dept (
      deptno integer     not null primary key,
      dname  varchar(14) not null,
      loc    varchar(13) null
    );
    
    create table emp (
      empno    integer       not null  primary key,
      ename    varchar(10)   not null,
      job      varchar(9)    not null,
      mgr      integer                 references emp,
      hiredate date          not null,
      sal      numeric(7, 2) not null,
      comm     numeric(7, 2),
      deptno   integer       not null   references dept
    );
    
    insert into dept
      values (10, 'ACCOUNTING', 'NEW YORK'),
             (20, 'RESEARCH', 'DALLAS'),
             (30, 'SALES', 'CHICAGO'),
             (40, 'OPERATIONS', 'BOSTON');
    
    insert into emp
    values (7839, 'KING', 'PRESIDENT', null, '2011-11-17', 15000, null, 10),
           (7566, 'JONES', 'MANAGER', 7839, '2011-04-02', 14875, null, 20),
           (7788, 'SCOTT', 'ANALYST', 7566, '2012-12-09', 15000, null, 20),
           (7876, 'ADAMS', 'CLERK', 7788, '2013-01-12', 5500, null, 20),
           (7902, 'FORD', 'ANALYST', 7566, '2011-12-03', 15000, null, 20),
           (7369, 'SMITH', 'CLERK', 7902, '2010-12-17', 14250, null, 20),
           (7698, 'BLAKE', 'MANAGER', 7839, '2011-05-01', 14250, null, 30),
           (7499, 'ALLEN', 'SALESMAN', 7698, '2011-02-20', 8000, 1500, 30),
           (7521, 'WARD', 'SALESMAN', 7698, '2011-02-22', 6250, 2500, 30),
           (7654, 'MARTIN', 'SALESMAN', 7698, '2011-09-28', 6250, 7000, 30),
           (7844, 'TURNER', 'SALESMAN', 7698, '2011-09-08', 6000, 0, 30),
           (7900, 'JAMES', 'CLERK', 7698, '2011-12-03', 4750, null, 30),
           (7782, 'CLARK', 'MANAGER', 7839, '2011-06-09', 12250, null, 10),
           (7934, 'MILLER', 'CLERK', 7782, '2012-01-23', 6500, null, 10);
  """

  val database = Database(
    driverClass = "org.h2.Driver",
    url = "jdbc:h2:mem:test",
    userName = "sa",
    initScript = Some(initScript)
  )

  test("Executes parameterized query"):
    val sql = """
      SELECT COUNT(*) AS count
      FROM   emp
      WHERE  deptno = ?
    """
    val result = executeQuery(sql, List(10))
    assertEquals(result.size, 1)
    assertEquals(
      result,
      List(Map("count" -> 3))
    )

  test("Executes non-parameterized query"):
    val sql = """
      SELECT COUNT(*) AS count
      FROM   emp
    """
    val result = executeQuery(sql)
    assertEquals(result.size, 1)
    assertEquals(
      result,
      List(Map("count" -> 14))
    )

  test("Runs action given a connection"):
    database.run: connection =>
      val sql = """
        SELECT COUNT(*) AS count
        FROM   dept
      """
      val result = executeQuery(sql)
      assertEquals(result.size, 1)
      assertEquals(
        result,
        List(Map("count" -> 4))
      )

  test("Executes name-parameterized query"):

    database.run: connection =>
      val sql = """
        SELECT COUNT(*) AS count
        FROM   emp
        WHERE  deptno = :deptno
           OR  empno = :empno
      """
      val result = connection
        .executeQuery(
          sql,
          Map[String, Any | Null](
            "empno" -> 7839,
            "deptno" -> 20
          )
        )
        .get
      assertEquals(result.size, 1)
      assertEquals(
        result,
        List(Map("count" -> 5))
      )

  test("Executes name-parameterized query"):

    database.run: connection =>
      val sql = """
        SELECT COUNT(*) AS count
        FROM   emp
        WHERE  deptno = :deptno
           OR  empno = :empno
      """
      val result = connection
        .executeQuery(
          sql,
          Map[String, Any | Null](
            "empno" -> 7839,
            "deptno" -> 20
          )
        )
        .get
      assertEquals(result.size, 1)
      assertEquals(
        result,
        List(Map("count" -> 5))
      )

  test("Validates param names in parameterized sql"):
    val result = Using(database.connect()): connection =>
      connection
        .executeQuery(
          """
            SELECT *
            FROM   emp
            WHERE  empno = :empno
               OR  deptno = :deptno
               OR  sal > :sal
          """,
          Map("deptno" -> 10)
        )
        .get
    assert(result.isFailure)
    assertEquals(
      result.failed.get.getMessage(),
      "requirement failed: Missing names: [empno, sal]"
    )

  def executeQuery(
      sql: String,
      params: List[Any | Null] = List.empty
  ): List[Map[String, Any | Null]] =
    val tryResult = Using(database.connect()): connection =>
      val result = connection.executeQuery(sql, params)
      assert(result.isInstanceOf[Success[List[Map[String, Any | Null]]]])
      result.get
    tryResult.get
  end executeQuery

  def normalizeSpace(s: String) = s.trim().split("\\s+").mkString(" ")

end DatabaseTest

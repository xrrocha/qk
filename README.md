# QK: A Query Web Publisher

A dead-simple way to publish your database query results on the web with
(almost) no programming:

```yaml
# Build JS parameter object from request
request: |
  ({ deptno: parseInt(param('deptno')) })

# Run parameterized SQL query
sql: |
  SELECT   e.empno,
           e.ename,
           e.job,
           TO_CHAR(e.hiredate, 'DD Mon YYYY') AS hiredate,
           CAST(e.sal AS MONEY)               AS sal,
           e.mgr,
           m.ename AS mgrName
  FROM     emp e
           LEFT OUTER JOIN emp m ON e.mgr = m.empno
  WHERE    e.deptno = :deptno
  ORDER BY empno

# Render HTML fragment in Pug
pug: |
  table(style='margin: 0 auto;')
      caption
          span Employees
          hr
      tr
          th Code
          th Name
          th Job
          th Hiredate
          th Salary
          th Manager
      for row in rows
          tr
              td
                  a(href='emp.yml?empno=' + empno) #{empno}
              td #{row.ename}
              td #{row.job}
              td #{row.hiredate}
              td(style='text-align: right;') #{row.sal}
              td
                  a(href='emp.yml?empno=' + mgr) #{mgrName}
```

package com.bobantalevski.courses.dao;

import static org.junit.Assert.*;

import com.bobantalevski.courses.model.Course;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Sql2oCourseDaoTest {

  private CourseDao dao;
  private Connection conn;

  @Before
  public void setUp() throws Exception {
    String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
    Sql2o sql2o = new Sql2o(connectionString, "", "");
    dao = new Sql2oCourseDao(sql2o);
    // Keep connection open through entire test so that it isn't wiped out.
    // This is per test method, not using the same connection for all the tests
    // cause then we could have an already filled database by addition tests and not
    // getting a proper state when checking for an empty list if we haven't added any courses
    // With in memory database, when all connections to it are closed, it gets erased
    conn = sql2o.open();
  }

  @After
  public void tearDown() throws Exception {
    conn.close();
  }

  @Test
  public void addingCourseSetsId() throws Exception {
    Course course = newTestCourse();
    int originalCourseId = course.getId();

    dao.add(course);

    assertNotEquals(originalCourseId, course.getId());
  }

  @Test
  public void addedCoursesAreReturnedFromFindAll() throws Exception {
    Course course = newTestCourse();

    dao.add(course);

    assertEquals(1, dao.findAll().size());
  }

  @Test
  public void noCoursesReturnsEmptyList() throws Exception{
    assertEquals(0, dao.findAll().size());
  }

  @Test
  public void existingCoursesCanBeFoundById() throws Exception {
    Course course = newTestCourse();
    dao.add(course);

    Course foundCourse = dao.findById(course.getId());

    assertEquals(course, foundCourse);
  }

  private Course newTestCourse() {
    return new Course("Test", "http://test.com");
  }
}
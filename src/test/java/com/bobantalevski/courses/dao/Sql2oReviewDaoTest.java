package com.bobantalevski.courses.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.bobantalevski.courses.exc.DaoException;
import com.bobantalevski.courses.model.Course;
import com.bobantalevski.courses.model.Review;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Sql2oReviewDaoTest {

  private Connection conn;
  private ReviewDao reviewDao;
  private CourseDao courseDao;
  private Course testCourse;

  @Before
  public void setUp() throws Exception {
    String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
    Sql2o sql2o = new Sql2o(connectionString, "", "");
    reviewDao = new Sql2oReviewDao(sql2o);
    courseDao = new Sql2oCourseDao(sql2o);
    // Keep connection open through entire test so that it isn't wiped out.
    // This is per test method, not using the same connection for all the tests
    // cause then we could have an already filled database by addition tests and not
    // getting a proper state when checking for an empty list if we haven't added any courses
    // With in memory database, when all connections to it are closed, it gets erased
    conn = sql2o.open();
    testCourse = Course.newTestCourse();
    courseDao.add(testCourse);
  }

  @After
  public void tearDown() throws Exception {
    conn.close();
  }

  @Test
  public void addingReviewSetsId()throws Exception {
    Review review = new Review(testCourse.getId(), 3, "Mediocre");
    int originalReviewId = review.getId();

    reviewDao.add(review);

    assertNotEquals(originalReviewId, review.getId());
  }

  @Test
  public void multipleReviewsAreFoundWhenTheyExistForACourse() throws Exception {
    reviewDao.add(new Review(testCourse.getId(), 1, "Very bad"));
    reviewDao.add(new Review(testCourse.getId(), 5, "Great"));

    List<Review> reviews = reviewDao.findByCourseId(testCourse.getId());

    assertEquals(2, reviews.size());
  }

  @Test(expected = DaoException.class)
  public void addingAReviewToANonExistingCourseFails() throws Exception {
    Review review = new Review(42, 5, "Test comment");

    reviewDao.add(review);
  }

  @Test
  public void noReviewsForAnExistingCourseReturnsEmptyList() throws Exception{
    assertEquals(0, reviewDao.findByCourseId(testCourse.getId()).size());
  }
}
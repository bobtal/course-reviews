package com.bobantalevski.courses;

import static org.junit.Assert.assertEquals;

import com.bobantalevski.courses.dao.CourseDao;
import com.bobantalevski.courses.dao.ReviewDao;
import com.bobantalevski.courses.dao.Sql2oCourseDao;
import com.bobantalevski.courses.dao.Sql2oReviewDao;
import com.bobantalevski.courses.model.Course;
import com.bobantalevski.courses.model.Review;
import com.bobantalevski.testing.ApiClient;
import com.bobantalevski.testing.ApiResponse;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import spark.Spark;

public class ApiTest {

  public static final String PORT = "4568";
  public static final String TEST_DATA_SOURCE = "jdbc:h2:mem:testing";
  private Connection connection;
  private ApiClient client;
  private Gson gson;
  private CourseDao courseDao;
  private ReviewDao reviewDao;

  @BeforeClass
  public static void startServer() {
    String[] args = {PORT, TEST_DATA_SOURCE};
    Api.main(args);
  }

  @AfterClass
  public static void stopServer() {
    Spark.stop();
  }

  @Before
  public void setUp() throws Exception {
    Sql2o sql2o = new Sql2o(TEST_DATA_SOURCE + ";INIT=RUNSCRIPT from 'classpath:/db/init.sql'", "", "");
    courseDao = new Sql2oCourseDao(sql2o);
    reviewDao = new Sql2oReviewDao(sql2o);
    connection = sql2o.open();
    client = new ApiClient("http://localhost:" + PORT);
    gson = new Gson();
  }

  @After
  public void tearDown() throws Exception {
    connection.close();
  }

  @Test
  public void addingCoursesReturnsCreatedStatus() {
    Map<String, String> values = new HashMap<>();
    values.put("name", "Test");
    values.put("url", "http://test.com");

    ApiResponse res = client.request("POST", "/courses", gson.toJson(values));

    assertEquals(201, res.getStatus());
  }

  @Test
  public void addingReviewReturnsCreatedStatus() throws Exception{
    Course course = Course.newTestCourse();
    courseDao.add(course);
    Map<String, Object> values = new HashMap<>();
    values.put("rating", 5);
    values.put("comment", "Purely Awesome");

    ApiResponse res = client.request(
        "POST",
        "/courses/" + course.getId() + "/reviews",
        gson.toJson(values)
    );

    assertEquals(201, res.getStatus());
  }

  @Test
  public void addingReviewToUnknownCourseThrowsError() throws Exception {
    Map<String, Object> values = new HashMap<>();
    values.put("rating", 5);
    values.put("comment", "Purely Awesome");

    ApiResponse res = client.request(
        "POST",
        "/courses/42/reviews",
        gson.toJson(values)
    );

    assertEquals(500, res.getStatus());
  }

  @Test
  public void multipleReviewsReturnedForCourse() throws Exception {
    Course course = Course.newTestCourse();
    courseDao.add(course);
    reviewDao.add(new Review(course.getId(), 5, "Awesome"));
    reviewDao.add(new Review(course.getId(), 4, "Pretty Good"));

    ApiResponse res = client.request("GET", "/courses/" + course.getId() + "/reviews");
    Review[] reviews = gson.fromJson(res.getBody(), Review[].class);

    assertEquals(2, reviews.length);
  }

  @Test
  public void coursesCanBeAccessedById() throws Exception {
    Course course = Course.newTestCourse();
    courseDao.add(course);

    ApiResponse res = client.request("GET", "/courses/" + course.getId());
    Course retrievedCourse = gson.fromJson(res.getBody(), Course.class);

    assertEquals(course, retrievedCourse);
  }

  @Test
  public void missingCoursesReturnNotFoundStatus() throws Exception {
    ApiResponse res = client.request("GET", "/courses/42");

    assertEquals(404, res.getStatus());
  }

  @Test
  public void missingReviewsForACourseReturnNotFoundStatus() throws Exception {
    Course course = Course.newTestCourse();
    ApiResponse res = client.request("GET", "/courses/" + course.getId() + "/reviews");

    assertEquals(404, res.getStatus());
  }

}
package com.bobantalevski.courses;

import static spark.Spark.after;
import static spark.Spark.exception;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

import com.bobantalevski.courses.dao.CourseDao;
import com.bobantalevski.courses.dao.ReviewDao;
import com.bobantalevski.courses.dao.Sql2oCourseDao;
import com.bobantalevski.courses.dao.Sql2oReviewDao;
import com.bobantalevski.courses.exc.ApiError;
import com.bobantalevski.courses.exc.DaoException;
import com.bobantalevski.courses.model.Course;
import com.bobantalevski.courses.model.Review;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sql2o.Sql2o;
import spark.Route;

public class Api {
  public static void main(String[] args) {
    String dataSource = "jdbc:h2:~/reviews.db";
    if (args.length > 0) {
      if (args.length != 2) {
        System.out.println("java Api <port> <datasource>");
        System.exit(0);
      }
      port(Integer.parseInt(args[0]));
      dataSource = args[1];
    }

    Sql2o sql2o = new Sql2o(dataSource + ";INIT=RUNSCRIPT from 'classpath:db/init.sql'", "", "");
    CourseDao courseDao = new Sql2oCourseDao(sql2o);
    ReviewDao reviewDao = new Sql2oReviewDao(sql2o);
    Gson gson = new Gson();

    // Creating a course
    // According to REST standards, this means making a
    // POST request to the plural name of the resource
    post("/courses", "application/json", (req, res) -> {
      Course course = gson.fromJson(req.body(), Course.class);
      courseDao.add(course);
      res.status(201);
      return course;
    }, gson::toJson);

    get("/courses", "application/json",
        (req, res) -> courseDao.findAll(), gson::toJson);

    get("/courses/:id", "application/json", (req, res) -> {
      int id = Integer.parseInt(req.params("id"));
      Course course = courseDao.findById(id);
      if (course == null) {
        throw new ApiError(404,"Could not find course with id " + id);
      }
      return course;
    }, gson::toJson);

    post("/courses/:courseId/reviews", "application/json", (req, res) -> {
      int courseId = Integer.parseInt(req.params("courseId"));
      Review review = gson.fromJson(req.body(), Review.class);
      review.setCourseId(courseId);
      try {
        reviewDao.add(review);
      } catch (DaoException ex) {
        throw new ApiError(500, ex.getMessage());
      }
      res.status(201);
      return review;
    }, gson::toJson);

    get("/courses/:courseId/reviews", "application/json", (req, res) -> {
      int courseId = Integer.parseInt(req.params("courseId"));
      List<Review> reviews = reviewDao.findByCourseId(courseId);
      if (reviews.size() > 0) {
        res.status(200);
      } else {
        res.status(404);
      }
      return reviews;
    }, gson::toJson);

    exception(ApiError.class, (exc, req, res) -> {
      ApiError err = (ApiError) exc;
      Map<String, Object> jsonMap = new HashMap<>();
      jsonMap.put("status", err.getStatus());
      jsonMap.put("errorMessage", err.getMessage());
      // after doesn't run in the exception handler
      res.type("application/status");
      res.status(err.getStatus());
      res.body(gson.toJson(jsonMap));
    });

    after((req, res) -> res.type("application/json"));
  }

}

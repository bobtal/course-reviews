package com.bobantalevski.courses;

import static spark.Spark.after;
import static spark.Spark.post;
import static spark.Spark.get;

import com.bobantalevski.courses.dao.CourseDao;
import com.bobantalevski.courses.dao.Sql2oCourseDao;
import com.bobantalevski.courses.model.Course;
import com.google.gson.Gson;
import org.sql2o.Sql2o;
import spark.Route;

public class Api {
  public static void main(String[] args) {
    Sql2o sql2o = new Sql2o("jdbc:h2:~/reviews.db;INIT=RUNSCRIPT from 'classpath:db/init.sql'", "", "");
    CourseDao courseDao = new Sql2oCourseDao(sql2o);
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
      // TODO: what if this is not found
      Course course = courseDao.findById(id);
      return course;
    }, gson::toJson);

    after((req, res) -> res.type("application/json"));
  }

}

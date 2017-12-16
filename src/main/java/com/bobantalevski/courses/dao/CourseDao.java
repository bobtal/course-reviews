package com.bobantalevski.courses.dao;

import com.bobantalevski.courses.exc.DaoException;
import com.bobantalevski.courses.model.Course;
import java.util.List;

public interface CourseDao {
  void add(Course course) throws DaoException;

  List<Course> findAll() throws DaoException;

  Course findById(int id) throws DaoException;
}

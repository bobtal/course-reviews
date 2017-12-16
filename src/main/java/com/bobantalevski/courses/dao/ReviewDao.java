package com.bobantalevski.courses.dao;

import com.bobantalevski.courses.exc.DaoException;
import com.bobantalevski.courses.model.Review;
import java.util.List;

public interface ReviewDao {
  void add(Review review) throws DaoException;

  List<Review> findAll() throws DaoException;

  List<Review> findByCourseId(int courseId) throws DaoException;
}

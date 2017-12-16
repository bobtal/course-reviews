package com.bobantalevski.courses.dao;

import com.bobantalevski.courses.exc.DaoException;
import com.bobantalevski.courses.model.Review;
import java.util.List;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

public class Sql2oReviewDao implements ReviewDao {

  private final Sql2o sql2o;

  public Sql2oReviewDao(Sql2o sql2o) {
    this.sql2o = sql2o;
  }

  @Override
  public void add(Review review) throws DaoException {
    String sql = "INSERT INTO reviews (course_id, rating, comment) "
        + "VALUES (:courseId, :rating, :comment)";
    try (Connection connection = sql2o.open()) {
      int id = (int)connection.createQuery(sql)
          .bind(review)
          .executeUpdate()
          .getKey();
      review.setId(id);
    } catch (Sql2oException ex) {
      throw new DaoException(ex, "Problem adding review");
    }
  }

  @Override
  public List<Review> findAll() throws DaoException {
    try (Connection connection = sql2o.open()) {
      return connection.createQuery("SELECT * FROM reviews")
          .executeAndFetch(Review.class);
    } catch (Sql2oException ex) {
      throw new DaoException(ex, "Problem retrieving all reviews");
    }
  }

  @Override
  public List<Review> findByCourseId(int courseId) throws DaoException {
    try (Connection connection = sql2o.open()) {
      return connection.createQuery("SELECT * FROM reviews WHERE course_id = :courseId")
          .addColumnMapping("COURSE_ID", "courseId")
          .addParameter("courseId", courseId)
          .executeAndFetch(Review.class);
    } catch (Sql2oException ex) {
      throw new DaoException(ex, "Problem retrieving all reviews");
    }
  }

}

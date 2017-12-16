package com.bobantalevski.courses.exc;

public class ApiError extends RuntimeException {
  private final int status;

  public ApiError(int status, String message) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}

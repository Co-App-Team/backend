package com.backend.coapp.model.enumeration;

/** All possible error codes returned from Authentication Service. */
public enum AuthErrorCodeEnum {
  INVALID_EMAIL,
  EMAIL_NOT_REGISTERED,
  EXIST_ACCOUNT_WITH_EMAIL,
  INVALID_CONFIRMATION_CODE
}

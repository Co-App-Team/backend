package com.backend.coapp.model.enumeration;

/** All possible error codes returned from Authentication Service. */
public enum AuthErrorCode {
  INVALID_EMAIL,
  EMAIL_NOT_REGISTERED,
  EXIST_ACCOUNT_WITH_EMAIL,
  INVALID_CONFIRMATION_CODE,
  ACCOUNT_NOT_ACTIVATED,
  ACCOUNT_ALREADY_VERIFIED,
  INVALID_EMAIL_OR_PASSWORD,
  TOKEN_EXPIRE,
  INVALID_TOKEN,
  MISING_ACCESS_TOKEN
}

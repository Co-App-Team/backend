package com.backend.coapp.model.enumeration;

import com.backend.coapp.exception.ConcurrencyException;

public enum GenAIErrorCode {
    OVER_LIMIT_CHARACTER,
    OVER_LIMIT_CHATBOT_REQUEST,
    OTHER_REQUEST_IN_PROGRESS
}

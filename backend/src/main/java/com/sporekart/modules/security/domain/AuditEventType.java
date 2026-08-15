package com.sporekart.modules.security.domain;

public enum AuditEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    LOGOUT_ALL,
    TOKEN_ROTATION,
    TOKEN_REUSE_DETECTED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    PASSWORD_CHANGED,
    UNAUTHORIZED_ACCESS
}

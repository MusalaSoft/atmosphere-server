package com.musala.atmosphere.server.data.db.ormlite;
/**
 * Enumerates all actions that can be invoked on a query builder instance in the {@link DeviceDao} selection of a wanted devices.
 * 
 * @author denis.bialev
 */
public enum WhereClauseOperator {
    EQUAL,
    GREATER_OR_EQUAL,
    LESS_OR_EQUAL,
    BETWEEN;
}

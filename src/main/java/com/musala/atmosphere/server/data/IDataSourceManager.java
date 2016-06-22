package com.musala.atmosphere.server.data;

/**
 * Common interface containing basic methods of the data source lifecycle.
 *
 * @author filareta.yordanova
 *
 */
public interface IDataSourceManager {
    /**
     * Initializes a source for storing and querying data on the server.
     */
    void initialize();
}

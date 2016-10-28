package com.musala.atmosphere.server.eventservice.event.datasource.create;

import com.musala.atmosphere.server.eventservice.event.datasource.DataSourceEvent;

/**
 * This event is published when data source is created to inform all subscribers that data access objects are available
 * for executing operations.
 * 
 * @author filareta.yordanova
 * 
 */
public interface DataSourceCreatedEvent extends DataSourceEvent {

}

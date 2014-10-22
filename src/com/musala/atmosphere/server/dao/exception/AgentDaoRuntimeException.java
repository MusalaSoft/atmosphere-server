package com.musala.atmosphere.server.dao.exception;

/**
 * 
 * Class for all Runtime exceptions that might be thrown form the agent data access objects.
 * 
 * @author filareta.yordanova
 * 
 */
public class AgentDaoRuntimeException extends DaoRuntimeException {
    private static final long serialVersionUID = 3805178138627496757L;

    /**
     * Creates new {@link AgentDaoRuntimeException AgentDaoRuntimeException} with the given message.
     * 
     * @param message
     *        - message representing the error that occurred
     */
    public AgentDaoRuntimeException(String message) {
        super(message);
    }

    /**
     * Creates new {@link AgentDaoRuntimeException AgentDaoRuntimeException} with the given message and
     * {@link Throwable throwable}.
     * 
     * @param message
     *        - message representing the error that occurred
     * @param throwable
     *        - the cause for the exception
     */
    public AgentDaoRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

}

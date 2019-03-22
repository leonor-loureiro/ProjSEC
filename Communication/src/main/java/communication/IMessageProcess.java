package communication;

/**
 * Wrapper to allow a function to be injected when a message is received
 */
interface IMessageProcess {

    Message process(Message message);
}

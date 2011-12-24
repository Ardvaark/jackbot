package net.ardvaark.jackbot;

public class JackBotException extends Exception
{
    private static final long serialVersionUID = 1L;

    public JackBotException()
    {
        super();
    }

    public JackBotException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JackBotException(String message)
    {
        super(message);
    }

    public JackBotException(Throwable cause)
    {
        super(cause);
    }
}

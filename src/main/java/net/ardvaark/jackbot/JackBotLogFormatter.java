/*
 * This file is part of JackBot IRC Bot (JackBot).
 * 
 * JackBot is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * JackBot is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * JackBot; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package net.ardvaark.jackbot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formats log messages by JackBot.
 * 
 * @author Brian Vargas
 * @since JackBot v1.1
 * @version $Revision: 55 $ $Date: 2008-04-13 14:36:35 -0400 (Sun, 13 Apr 2008) $
 */
public class JackBotLogFormatter extends Formatter
{
    static
    {
        JackBotLogFormatter.format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSSS Z");
    }

    /**
     * Default constructor.
     */
    public JackBotLogFormatter()
    {
    }

    /**
     * Formats a <CODE>LogRecord</CODE> into a string.
     * 
     * @param logRecord The <CODE>LogRecord</CODE> to format.
     * @return The formatted form of the <CODE>LogRecord</CODE>.
     */
    @Override
    public String format(LogRecord logRecord)
    {
        String message = logRecord.getMessage();
        String time = JackBotLogFormatter.format.format(new Date(logRecord.getMillis()));
        String level = logRecord.getLevel().toString();
        String srcClassName = logRecord.getSourceClassName();
        String srcMethodName = logRecord.getSourceMethodName();
        Object[] params = logRecord.getParameters();
        int bufferLength = message.length() + time.length() + level.length() + srcClassName.length() + srcMethodName.length() + 20
                + (params == null ? 0 : params.length * 30 + 2);
        StringBuffer buffer = new StringBuffer(bufferLength);

        buffer.append(time);
        buffer.append(' ');
        buffer.append(level);
        buffer.append(' ');
        buffer.append(Thread.currentThread().toString());
        buffer.append(' ');
        buffer.append(srcClassName);
        buffer.append('.');
        buffer.append(srcMethodName);

        if (params != null)
        {
            buffer.append('(');

            for (int i = 0; i < params.length; i++)
            {
                buffer.append(params[i].toString());

                if (i != params.length - 1)
                {
                    buffer.append(", ");
                }
            }

            buffer.append(')');
        }

        buffer.append(' ');
        buffer.append(message);
        buffer.append('\n');

        if (logRecord.getThrown() != null)
        {
            buffer.append(logRecord.getThrown().toString());
        }

        return buffer.toString();
    }

    /**
     * Used to format the dates in the log.
     */
    private static SimpleDateFormat format;
}

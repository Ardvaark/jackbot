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

package net.ardvaark.jackbot.scripting.ecma;

import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ScriptException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.util.HashMap;

/**
 * The base class of all Host Objects in the JackBot ECMAScript engine. This
 * class provides basic event handler functionality.
 * 
 * @author Brian Vargas
 * @since JackBot v1.1
 * @version $Revision: 66 $ $Date: 2008-07-04 14:29:04 -0400 (Fri, 04 Jul 2008) $
 */
abstract class HostObject extends ScriptableObject
{
    private static final Log log = Log.getLogger(HostObject.class);
    
    /**
     * Default constructor.
     */
    protected HostObject()
    {
        this.eventHandlers = new HashMap<String, Object>(4);
    }

    /**
     * Gets the <code>ECMAEngine</code> under which this
     * <code>HostObject</code> is running. Note that the engine must be set
     * correctly during object initialization using the
     * {@link #setEngine(ECMAEngine) setEngine(ECMAEngine)} method.
     * 
     * @return The engine under which this object is running.
     * @see #setEngine(ECMAEngine)
     */
    ECMAEngine getEngine()
    {
        return this.engine;
    }

    /**
     * Sets the <code>ECMAEngine</code> in which this object is running.
     * Access the engine using the {@link #getEngine() getEngine()} method.
     * 
     * @param engine The engine for this object.
     * @see #getEngine()
     */
    void setEngine(ECMAEngine engine)
    {
        this.engine = engine;
    }

    /**
     * Sets the parent host object of this host object. The parent must be set
     * correctly during object initialization using the
     * {@link #setParent(HostObject) setParent(HostObject)} method.
     * 
     * @return The parent of this object.
     * @see #setParent(HostObject)
     */
    HostObject getParent()
    {
        return this.parent;
    }

    /**
     * Sets the parent of this object.
     * 
     * @param parent The parent of this object.
     * @see #getParent()
     */
    void setParent(HostObject parent)
    {
        this.parent = parent;
    }

    /**
     * Gets an ECMAScript property from this object. This overridden version
     * checks an internal hashtable of event handlers to see if the property
     * being accessed is a known event handler. If so, it returns the event
     * handler; otherwise it returns the super-class' version of this method.
     * 
     * @param name The name of the ECMAScript property to fetch.
     * @param start The scope in which the search was started.
     * @return The event handler given by <code>name</code>, or else the
     *         result of the super-classes' version of this method.
     */
    @Override
    public Object get(String name, Scriptable start)
    {
        Object ret = null;

        if (this.eventHandlers.containsKey(name))
        {
            ret = this.eventHandlers.get(name);
        }
        else
        {
            ret = super.get(name, start);
        }

        return ret;
    }

    /**
     * Puts an ECMAScript property into this object. This version checks an
     * internal hashtable of known event handlers. If the property name matches
     * a known event handler, and it is either <code>null</code> or an
     * ECMAScript <code>Function</code>, then it is added to that collection.
     * If it is not, nothing is done. If the name is not of a known event
     * handler, then this calls the super-class' version of this method.
     * 
     * @param name The name of the property to add.
     * @param start The scope in which the add started.
     * @param value The value to add to the property.
     */
    @Override
    public void put(String name, Scriptable start, Object value)
    {
        if (this.eventHandlers.containsKey(name))
        {
            if (value == null || value != null && value instanceof Function)
            {
                this.eventHandlers.put(name, value);
            }
        }
        else
        {
            super.put(name, start, value);
        }
    }

    /**
     * Fires the ECMAScript handler with the given name and the given arguments.
     * Handlers that have been added using the
     * {@link #get(String, Scriptable) get(String, Scriptable)} method are
     * obtained from the internal hashtable and called.
     * 
     * @param handler The name of the handler to fire.
     * @param args An array of arguments to pass to the handler.
     * @throws ScriptException When any other exception occurs.
     */
    protected void fireHandler(String handler, Object...args) throws ScriptException
    {
        Object func = this.eventHandlers.get(handler);

        if (func != null)
        {
            try
            {
                log.trace("Firing handler {0}.{1}", this.getClass().getName(), handler);
                ScriptableObject.callMethod(this, handler, args);
            }
            catch (Exception e)
            {
                throw new ScriptException("Caught exception while firing event hander for " + handler, e);
            }
        }
    }

    /**
     * Determines if a handler with the given name exists on this object.
     *
     * @param handlerName The name of the handler.
     * @return Returns true if the handler exists on this host object; false otherwise.
     */
    protected boolean hasHandler(String handlerName) {
        return this.eventHandlers.containsKey(handlerName);
    }

    /**
     * Adds an event name to the internal hashtable. This is primarily for
     * internal use.
     * 
     * @param eventName The name of the event to add.
     */
    void addEvent(String eventName)
    {
        this.eventHandlers.put(eventName, null);
    }

    /**
     * The engine with which this object is associated.
     */
    private ECMAEngine              engine;

    /**
     * The parent of this object.
     */
    private HostObject              parent;

    /**
     * The event handlers that this object will handle.
     */
    private HashMap<String, Object> eventHandlers;
}

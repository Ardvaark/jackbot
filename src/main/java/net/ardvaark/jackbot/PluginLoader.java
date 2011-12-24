package net.ardvaark.jackbot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import net.ardvaark.jackbot.logging.Log;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Loads {@link JackBotPlugin plugins} for the JackBot runtime.

 * @version $Revision$ $Date$
 */
class PluginLoader
{
    private static final Log log = Log.getLogger(PluginLoader.class);
    
    private ClassLoader loader;
    private List<JackBotPlugin> plugins = new ArrayList<JackBotPlugin>();
    
    public Collection<JackBotPlugin> getPlugins()
    {
        return this.plugins;
    }
    
    public PluginLoader(ClassLoader loader, NodeList pluginNodes) throws JackBotException
    {
        ServiceLoader<JackBotPlugin> serviceLoader = ServiceLoader.load(JackBotPlugin.class, loader);
        
        for (JackBotPlugin plugin : serviceLoader)
        {
            this.plugins.add(plugin);
        }
        
        this.loader = loader;
        
        this.loadByName(pluginNodes);
        this.makeReadOnly();
        this.configure(pluginNodes);
    }
    
    private void makeReadOnly()
    {
        this.plugins = Collections.unmodifiableList(this.plugins);
    }
    
    private void loadByName(NodeList pluginNodes) throws JackBotException
    {
        for (int i = 0; i < pluginNodes.getLength(); i++)
        {
            Element curElement = (Element)pluginNodes.item(i);
            String pluginName = curElement.getAttribute("name");

            try
            {
                this.loadByName(pluginName);
            }
            catch (Exception e)
            {
                log.error("Error loading plugin: {0}", e, pluginName);
            }
        }
    }
    
    private void loadByName(String name) throws JackBotException
    {
        try
        {
            log.trace("Loading plugin: {0}", name);
            Object obj = this.loader.loadClass(name).newInstance();
            
            if (obj instanceof JackBotPlugin)
            {
                this.plugins.add((JackBotPlugin)obj);
            }
            else
            {
                log.warn(
                        "Skipped plugin class {0} because it is not an " +
                		"instance of {1}.",
                        name,
                        JackBotPlugin.class.getCanonicalName());
            }
        }
        catch (ClassNotFoundException e)
        {
            log.error("Plugin class not found: {0}", e, name);
        }
        catch (InstantiationException e)
        {
            log.error("Unable to instantiate plugin class: {0}", e, name);
        }
        catch (IllegalAccessException e)
        {
            log.error("Unable to instantiate plugin class: {0}", e, name);
        }
    }
    
    private void configure(NodeList pluginNodes) throws JackBotConfigurationException
    {
        Map<String, Element> configs = this.mapConfigs(pluginNodes);
        
        for (JackBotPlugin pluginObject : this.plugins)
        {
            if (pluginObject instanceof Configurable)
            {
                Element configElement = configs.get(pluginObject.getClass().getName());
                
                if (configElement != null)
                {
                    log.info("Configuring plugin " + pluginObject.getClass().getName() + "...");

                    try
                    {
                        ((Configurable)pluginObject).configure(configElement);
                        log.info("Plugin successfully configured!");
                    }
                    catch (Exception e)
                    {
                        log.warn("Error configuring plugin: {0}", e, pluginObject.getClass().getName());
                    }
                }
            }
        }
    }
    
    private Map<String, Element> mapConfigs(NodeList nodes)
    {
        HashMap<String, Element> map = new HashMap<String, Element>(nodes.getLength());
        
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Element element = (Element)nodes.item(i);
            String name = element.getAttribute("name");
            map.put(name, element);
        }
        
        return map;
    }
}

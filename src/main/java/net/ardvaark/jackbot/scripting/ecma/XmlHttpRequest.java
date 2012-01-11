package net.ardvaark.jackbot.scripting.ecma;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xmlimpl.XMLLibImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import net.ardvaark.jackbot.logging.Log;
import net.ardvaark.jackbot.scripting.ScriptException;

/**
 * A client for doing synchronous and asynchronous HTTP requests from within
 * JackBot scripts.  This class implements the {@linkplain http://www.w3.org/TR/XMLHttpRequest/ XMLHttpRequest}
 * specification.
 * 
 * @version $Id$
 * @since 2.0
 * @see http://www.w3.org/TR/XMLHttpRequest/
 */
@SuppressWarnings("serial")
public class XmlHttpRequest extends HostObject
{
    private static final Log log = Log.getLogger(XmlHttpRequest.class);
    private static final long serialVersionUID = 1L;

    /**
     * The ECMAScript class name for this class. This is the name by which the
     * script will refer to the class.
     */
    public static final String ECMA_CLASS_NAME  = "XMLHttpRequest";
    
    /**
     * The name of the handler to be fired for the <tt>onReadyStateChange</tt>
     * event.  This event is fired at specific times, according to the
     * {@link http://www.w3.org/TR/XMLHttpRequest/ W3C spec}.
     */
    public static final String HANDLER_ON_READY_STATE_CHANGE = "onReadyStateChange";
    
    /**
     * The undefined value.
     */
    private static final Object JS_UNDEFINED = Context.getUndefinedValue();
    
    /**
     * The set of valid HTTP methods, as defined by the XMLHttpRequest spec.
     * @see http://www.w3.org/TR/XMLHttpRequest/#open
     */
    private static final Set<String> VALID_METHODS = new HashSet<String>() {{
        add("DELETE"); add("GET"); add("HEAD");
        add("OPTIONS"); add("POST"); add("PUT");
    }};
    
    /**
     * The set of valid schemes supported.  This is pretty much just HTTP and
     * HTTPS.
     */
    private static final Set<String> VALID_SCHEMES = new HashSet<String>() {{
        add("http"); add("https");
    }};
    
    /**
     * The set of invalid header values, as specified by
     * {@link http://www.w3.org/TR/XMLHttpRequest/#setrequestheader}.  They
     * are uppercased to make the case-insensitive matching easier.
     */
    private static final Set<String> INVALID_HEADERS = new HashSet<String>() {{
        add("ACCEPT-CHARSET"); add("ACCEPT-ENCODING"); add("CONNECTION");
        add("CONTENT-LENGTH"); add("CONTENT-TRANSFER-ENCODING");
        add("DATE"); add("EXPECT"); add("HOST"); add("KEEP-ALIVE");
        add("REFERER"); add("TE"); add("TRAILER"); add("TRANSFER-ENCODING");
        add("UPGRADE"); add("VIA");
    }};
    
    /**
     * The set of known XML content types.  Also, content types ending in
     * <tt>+xml</tt> are also valid, but that is checked imperitively in the
     * code.
     */
    private static final Set<String> XML_CONTENT_TYPES = new HashSet<String>() {{
        add("text/xml"); add("application/xml");
    }};

    /**
     * A {@link DocumentBuilderFactory} used for parsing the
     * {@link #jsGet_responseXML() responseXML}.
     */
    private static DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    
    /**
     * The standard parameters used by this client.
     */
    private static HttpClientParams standardParmaters = new HttpClientParams() {{
        setAuthenticationPreemptive(false);
        setParameter(USER_AGENT, "JackBot IRC Bot ($Id$)");
    }};
    
    static
    {
        docBuilderFactory.setNamespaceAware(true);
        docBuilderFactory.setCoalescing(true);
        docBuilderFactory.setValidating(false);
        docBuilderFactory.setXIncludeAware(false);
    }
    
    // State
    private boolean aborted;
    private RequestState currentState;
    
    // Request info
    private String storedMethod;
    private URI storedUri;
    private boolean async;
    private String storedUsername;
    private String storedPassword;
    private List<Header> requestHeaders;

    // Response info
    private Map<String, List<Header>> responseHeaders;
    private byte[] responseData;
    
    /**
     * Gets the ECMAScript class name for this object.
     * 
     * @return The ECMAScript class name for this object.
     */
    @Override
    public String getClassName()
    {
        return ECMA_CLASS_NAME;
    }
    
    /**
     * Default prototype constructor.
     */
    public XmlHttpRequest()
    {
        ECMAEngine engine = (ECMAEngine)Context.getCurrentContext().getThreadLocal(ECMAEngine.class);
        this.setEngine(engine);
        
        this.putConst("UNSENT", this, RequestState.UNSENT);
        this.putConst("OPENED", this, RequestState.OPENED);
        this.putConst("HEADERS_RECEIVED", this, RequestState.HEADERS_RECEIVED);
        this.putConst("LOADING", this, RequestState.LOADING);
        this.putConst("DONE", this, RequestState.DONE);
    }
    
    /**
     * Default instance constructor.
     */
    public void jsConstructor()
    {
        this.currentState = new UnsentState(); // As per the spec.
        this.requestHeaders = new ArrayList<Header>();
        this.responseHeaders = new HashMap<String, List<Header>>();
        
        this.addEvent(HANDLER_ON_READY_STATE_CHANGE);
        this.addEvent(HANDLER_ON_READY_STATE_CHANGE.toLowerCase()); // Some stupid scripts assume an all-lowercase event name.
    }
    
    /**
     * Gets the current ready state of the object.
     * 
     * @return The current state as a short.
     * @see State
     * @see http://www.w3.org/TR/XMLHttpRequest/#xmlhttprequest
     */
    public synchronized int jsGet_readyState()
    {
        return this.currentState.getValue();
    }
    
    /**
     * Gets the response from the server as a text string.
     * 
     * @return The response text
     * @throws ScriptException
     */
    public synchronized String jsGet_responseText() throws ScriptException
    {
        return this.currentState.getResponseText();
    }
    
    /**
     * Gets the response from the server as a {@see Document}.  If there
     * is no response, or it cannot be parsed as XML, this method will
     * return <tt>null</tt>.
     * 
     * @return The XML document, or <tt>null</tt> if there is no response,
     * or if the response cannot be parsed as XML.
     * @throws ScriptException
     */
    public synchronized Document jsGet_responseXML() throws ScriptException
    {
        return this.currentState.getResponseXml();
    }
    
    /**
     * Gets the status text from the server.
     * @throws ScriptException
     */
    public synchronized String jsGet_statusText() throws ScriptException
    {
        return this.currentState.getStatusText();
    }

    /**
     * Gets the status code returned by the server.
     *
     * @return Returns the current HTTP status code.
     * @throws ScriptException Thrown if the status code cannot be obtained.
     */
    public synchronized int jsGet_status() throws ScriptException {
        return this.currentState.getStatusCode();
    }
    
    /**
     * Opens the connection to the server.  For more information see the
     * {@link http://www.w3.org/TR/XMLHttpRequest/#open W3C specification}.
     * 
     * @param method The HTTP method.  Required.
     * @param url The URL to access.  Required.
     * @param async Whether the request should be asynchronous.  Defaults to <tt>true</tt>.
     * @param username Optional.
     * @param password Optional.
     * @throws ScriptException If an error occurs.
     */
    public synchronized void jsFunction_open(String method, String url, Object async, Object username, Object password) throws ScriptException
    {
        boolean asyncSet = !(async == null || async.equals(JS_UNDEFINED));
        boolean usernameSet = !(username == null || username.equals(JS_UNDEFINED));
        boolean passwordSet = !(password == null || password.equals(JS_UNDEFINED));
        boolean asyncVal = asyncSet? Boolean.parseBoolean(async.toString()) : true;
               
        this.currentState.open(method, url, asyncVal, usernameSet, String.valueOf(username), passwordSet, String.valueOf(password));
    }
    
    public synchronized void jsFunction_setRequestHeader(String header, String value) throws ScriptException
    {
        this.currentState.setRequestHeader(header, value);
    }
    
    public synchronized void jsFunction_send(Object data) throws ScriptException
    {
        RequestEntity entity;
        
        if (data == null || data.equals(JS_UNDEFINED))
        {
            entity = null;
        }
        else if (data instanceof XMLObject)
        {
            // This is very imlementation-specific to the current
            // Rhino build.
            Node node = XMLLibImpl.toDomNode(data);
            
            try
            {
                Transformer tx = TransformerFactory.newInstance().newTransformer();

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(byteStream, "UTF-8");
                
                tx.transform(new DOMSource(node), new StreamResult(writer));
                
                entity = new ByteArrayRequestEntity(byteStream.toByteArray());
            }
            catch (TransformerConfigurationException e)
            {
                throw new ScriptException(e);
            }
            catch (TransformerException e)
            {
                throw new ScriptException(e);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new ScriptException(e);
            }
        }
        else
        {
            // Convert everything else to a string.
            
            try
            {
                entity = new StringRequestEntity(data.toString(), null, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new ScriptException(e);
            }
        }
        
        this.send(entity);
    }
    
    public synchronized void jsFunction_abort() throws ScriptException
    {
        this.abort();
    }
    
    private void send(RequestEntity entity) throws ScriptException
    {
        this.currentState.send(entity);
    }
    
    private void abort() throws ScriptException
    {
        try
        {
            aborted = true;
            responseData = null;
            this.abortExistingRequests();
        }
        finally
        {
        }
    }
    
    private void abortExistingRequests() throws ScriptException
    {
        this.clearRequestHeaders();
        currentState.abort();
    }
    
    private void clearRequestHeaders()
    {
        this.requestHeaders.clear();
    }
    
    private void fireReadyStateChanged() throws ScriptException
    {
        this.fireHandler(HANDLER_ON_READY_STATE_CHANGE);
        this.fireHandler(HANDLER_ON_READY_STATE_CHANGE.toLowerCase());
    }
    
    private synchronized boolean changeState(RequestState newState)
    {
        log.trace("Changing to state: {0}", newState.getValue());

        boolean stateChanged;
        short oldState = currentState.getValue();
        
        if (newState.getValue() != oldState)
        {
            currentState = newState;
            stateChanged = true;
        }
        else
        {
            stateChanged = false;
        }
        
        return stateChanged;
    }
    
    private boolean changeToUnsentState()
    {
        return this.changeState(new UnsentState());
    }
    
    private boolean changeToOpenedState()
    {
        return this.changeState(new OpenedState());
    }
    
    private boolean changeToHeadersReceivedState(int statusCode, String statustext)
    {
        HeadersReceivedState newState = new HeadersReceivedState();
        newState.setStatusCode(statusCode);
        newState.setStatusText(statustext);
        return this.changeState(newState);
    }
    
    private boolean changeToLoadingState()
    {
        LoadingState newState = new LoadingState();
        
        if (currentState instanceof HeadersReceivedState)
        {
            newState.setStatusText(((HeadersReceivedState)currentState).statusText);
        }
        
        return this.changeState(newState);
    }
    
    private boolean changeToDoneState(boolean errorFlag)
    {
        DoneState newState = new DoneState();
        newState.setErrorFlag(errorFlag);

        if (currentState instanceof HeadersReceivedState)
        {
            newState.setStatusText(((HeadersReceivedState)currentState).statusText);
        }

        return this.changeState(newState);
    }
    
    private String normalizeHeaderName(String name)
    {
        if (name != null)
        {
            name = name.toUpperCase();
        }
        
        return name;
    }
    
    private String concatHeaders(List<Header> headers)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < headers.size(); i++)
        {
            sb.append(headers.get(i).getValue());
            
            if (i < headers.size() - 1)
            {
                sb.append(", ");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * <p>The base class for the state machine states.  By default, most of the
     * state machine methods will throw an {@link XmlHttpRequestException}
     * complaining of an invalid state, allowing the state subclasses
     * to only override the methods that are relevant to their own operation.</p>
     * 
     * <p>The states are defined by the
     * {@link http://www.w3.org/TR/XMLHttpRequest/#xmlhttprequest XMLHttpRequest}
     * spec.</p>
     */
    private abstract class RequestState
    {
        public static final short UNSENT = 0;
        public static final short OPENED = 1;
        public static final short HEADERS_RECEIVED = 2;
        public static final short LOADING = 3;
        public static final short DONE = 4;
        
        private short value;
        
        protected RequestState(short value)
        {
            this.value = value;
        }
        
        public short getValue()
        {
            return this.value;
        }
        
        // Request
        public void open(String method, String url, boolean async, boolean usernameSet, String username, boolean passwordSet, String password) throws ScriptException
        {
            this.throwInvalidStateException();
        }
        
        public void setRequestHeader(String header, String value) throws ScriptException
        {
            this.throwInvalidStateException();
        }
        
        public void send(RequestEntity entity) throws ScriptException
        {
            this.throwInvalidStateException();
        }
        
        public void abort() throws ScriptException
        {
            this.throwInvalidStateException();
        }
        
        // Response
        public String getAllResponseHeaders() throws ScriptException
        {
            this.throwInvalidStateException();
            return null; // Will never happen
        }
        
        public String getResponseHeader(String header) throws ScriptException
        {
            this.throwInvalidStateException();
            return null; // Will never happen
        }

        public String getResponseText() throws ScriptException
        {
            this.throwInvalidStateException();
            return null; // Will never happen
        }

        public Document getResponseXml() throws ScriptException
        {
            this.throwInvalidStateException();
            return null; // Will never happen
        }
        
        public int getStatusCode() throws ScriptException
        {
            this.throwInvalidStateException();
            return 0; // Will never happen
        }

        public String getStatusText() throws ScriptException
        {
            this.throwInvalidStateException();
            return null; // Will never happen
        }
        
        protected void throwInvalidStateException() throws ScriptException
        {
            String msg = MessageFormat.format("Invalid state: {0}", this.getValue());
            throw new XmlHttpRequestException(msg);
        }
    }
    
    private class UnsentState extends RequestState
    {
        public UnsentState()
        {
            super(UNSENT);
        }
        
        @Override
        public void abort() throws ScriptException
        {
            // Do nothing.  We're already in the unsent state.
        }
        
        @Override
        public void open(String method, String url, boolean async, boolean usernameSet, String username, boolean passwordSet, String password) throws ScriptException
        {
            storedMethod = method.toUpperCase();

            log.trace("Opening XmlHttpRequest (async={2}): {0} {1}", storedMethod, url, async);

            // If stored method does not match the Method production,
            // defined in section 5.1.1 of RFC 2616, raise a SYNTAX_ERR
            // exception and terminate these steps.
            checkMethodValid();
            setStoredUri(url);
            checkUriValid();
            setCredentialsFromUri();
            XmlHttpRequest.this.async = async;
            
            if (usernameSet)
            {
                storedUsername = username;
            }
            
            if (passwordSet)
            {
                storedPassword = password;
            }
            
            abortExistingRequests();
            changeToOpenedState();
            fireReadyStateChanged();
        }
        
        private void checkMethodValid() throws ScriptException
        {
            if (!VALID_METHODS.contains(storedMethod))
            {
                throw new XmlHttpRequestException(MessageFormat.format("Invalid method: {0}", storedMethod));
            }
        }

        private void setStoredUri(String url) throws ScriptException
        {
            try
            {
                URI uri = new URI(url);
                
                if (uri.isOpaque())
                {
                    throw new XmlHttpRequestException(MessageFormat.format("Invalid URL: {0}", url));
                }
                
                storedUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null);
            }
            catch (URISyntaxException e)
            {
                throw new XmlHttpRequestException(MessageFormat.format("Invalid URL: {0}", url), e);
            }
        }

        private void checkUriValid() throws ScriptException
        {
            if (!storedUri.isAbsolute())
            {
                throw new XmlHttpRequestException(MessageFormat.format("Relative URLs are not supported: {0}", storedUri));
            }
            
            String scheme = storedUri.getScheme().toLowerCase();

            if (!VALID_SCHEMES.contains(scheme))
            {
                throw new XmlHttpRequestException(MessageFormat.format("Scheme not supported: {0}", scheme));
            }
        }

        private void setCredentialsFromUri() throws ScriptException
        {
            String userInfo = storedUri.getUserInfo();
            
            if (userInfo != null && userInfo.length() > 0)
            {
                if (userInfo.contains(":"))
                {
                    String[] parts = userInfo.split(":", 1);
                    storedUsername = parts[0];
                    storedPassword = parts[1];
                }
                else
                {
                    storedUsername = userInfo;
                }
            }
        }
    }

    private class OpenedState extends RequestState
    {
        private boolean sendFlag;
        private HttpMethod runningMethod;
        
        public OpenedState()
        {
            super(OPENED);
            this.sendFlag = false;
        }
        
        @Override
        public void abort() throws ScriptException
        {
            if (!sendFlag)
            {
                changeToUnsentState();
            }
            else
            {
                if (runningMethod != null)
                {
                    runningMethod.abort();
                }
                
                sendFlag = false;
                changeToDoneState(true);
                fireReadyStateChanged();
            }
        }
        
        @Override
        public void setRequestHeader(String header, String value) throws ScriptException
        {
            if (header == null)
            {
                throw new NullPointerException("Header is null");
            }
            
            String uHeader = header.toUpperCase();
            
            if (INVALID_HEADERS.contains(uHeader)
                    || uHeader.startsWith("PROXY-")
                    || uHeader.startsWith("SEC-"))
            {
                throw new XmlHttpRequestException(MessageFormat.format("Setting of header is disallowed for security reasons: {0}", header));
            }
            
            log.trace("Setting header: {0}: {1}", header, value);
            requestHeaders.add(new Header(header, value));
        }
        
        @Override
        public void send(RequestEntity entity) throws ScriptException
        {
            log.trace("Send: {0}", storedUri);
            
            // 1. If the state of the object is not OPENED raise an
            // INVALID_STATE_ERR exception and terminate these steps.
            //
            // No code is needed to do that here.  It's handled by the
            // base class.
            
            // 2. If the send() flag is "true" raise an INVALID_STATE_ERR
            // exception and terminate these steps. 
            if (this.sendFlag)
            {
                throw new XmlHttpRequestException("Request has already been sent.");
            }
            
            // 3. If async is true set the send() flag to "true". 
            if (async)
            {
                this.sendFlag = true;
            }
            
            // 4. If stored method is GET act as if the data argument is null.
            // (Handled in the createRequest() method)
            final HttpMethod method = runningMethod = this.createRequest(entity);
            final HttpClient client = new HttpClient();
            final HttpState state = new HttpState();

            // Useful for debugging with Charles
            client.getHostConfiguration().setProxy("localhost", 8889);

            HttpClientParams params = new HttpClientParams(standardParmaters);
            
            if (storedUsername != null)
            {
                log.trace("Setting credentials: {0}/{1}", storedUsername, "xxxx");
                state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(storedUsername, storedPassword));
                params.setAuthenticationPreemptive(true);
            }

            method.setParams(params);

            // Set the request headers.
            for (Header requestHeader : requestHeaders) {
                method.addRequestHeader(requestHeader);
            }

            // 6. Synchronously dispatch a readystatechange event on the object.
            // Note: The state of the object does not change. The event is dispatched for historical reasons.
            fireReadyStateChanged();
            
            // 5. Make a request to stored url, using HTTP method stored
            // method, user stored user (if provided) and password stored
            // password (if provided), taking into account the entity body,
            // list of request headers and the rules listed directly after
            // this set of steps.
            //
            // Since the request/response happens atomically, step #6
            // is done before step #5.
            Runnable steps = new Runnable() {
                public void run()
                {
                    log.trace("Running: {0}", storedUri);
                    
                    try
                    {
                        if (!aborted)
                        {
                            client.executeMethod(HostConfiguration.ANY_HOST_CONFIGURATION, method, state);
                        }
                        
                        if (!aborted)
                        {
                            saveResponseHeaders(method.getResponseHeaders());
                            changeToHeadersReceivedState(method.getStatusCode(), method.getStatusText());

                            log.trace("Response: {0} {1}", method.getStatusCode(), method.getStatusText());

                            fireReadyStateChanged();
                        }

                        if (!aborted)
                        {
                            changeToLoadingState();
                            fireReadyStateChanged();
                        }

                        if (!aborted)
                        {
                            responseData = method.getResponseBody();

                            if (log.isTraceEnabled()) {
                                String responseBody = new String(responseData, "UTF-8");
                                log.trace("Response body: {0}", responseBody);
                            }
                        }
                        
                        if (!aborted)
                        {
                            changeToDoneState(false);
                            fireReadyStateChanged();
                        }
                    }
                    catch (HttpException e)
                    {
                        this.handleException(e);
                    }
                    catch (IOException e)
                    {
                        this.handleException(e);
                    }
                    catch (ScriptException e)
                    {
                        this.handleException(e);
                    }
                }
                
                private void saveResponseHeaders(Header[] headers)
                {
                    for (Header header : headers)
                    {
                        String headerName = header.getName();
                        List<Header> headerList;
                        
                        if (!responseHeaders.containsKey(headerName))
                        {
                            headerList = new ArrayList<Header>(1);
                            responseHeaders.put(headerName, headerList);
                        }
                        else
                        {
                            headerList = responseHeaders.get(headerName);
                        }
                        
                        headerList.add(header);
                    }
                }
                
                private void handleException(Exception e)
                {
                    log.error("An error occurred while executing an " +
                    		"asynchronous HTTP request: {0}", e, storedUri);
                    responseData = null;
                    
                    try
                    {
                        changeToDoneState(true);
                        fireReadyStateChanged();
                    }
                    catch (ScriptException e2)
                    {
                        log.error("An error occurred while switching to the " +
                        		"error state while handling an error " +
                        		"in an asynchronous HTTP request: {0}", e2, storedUri);
                    }
                }
            };
            
            if (!async)
            {
                steps.run();
            }
            else
            {
                getEngine().runAsync(steps);
            }
        }

        private HttpMethod createRequest(RequestEntity entity) throws ScriptException
        {
            HttpMethod result;
            String uri = storedUri.toString();
            
            if (storedMethod.equals("GET"))
            {
                GetMethod request = new GetMethod(uri);
                request.setFollowRedirects(true);
                result = request;
            }
            else if (storedMethod.equals("DELETE"))
            {
                DeleteMethod request = new DeleteMethod(uri);
                request.setFollowRedirects(true);
                result = request;
            }
            else if (storedMethod.equals("HEAD"))
            {
                HeadMethod request = new HeadMethod(uri);
                request.setFollowRedirects(true);
                result = request;
            }
            else if (storedMethod.equals("OPTIONS"))
            {
                OptionsMethod request = new OptionsMethod(uri);
                request.setFollowRedirects(true);
                result = request;
            }
            else if (storedMethod.equals("POST"))
            {
                PostMethod request = new PostMethod(uri);
                request.setRequestEntity(entity);
                request.setFollowRedirects(false);
                result = request;
            }
            else if (storedMethod.equals("PUT"))
            {
                PutMethod request = new PutMethod(uri);
                request.setRequestEntity(entity);
                request.setFollowRedirects(false);
                result = request;
            }
            else
            {
                // Should never happen.
                throw new ScriptException("Invalid method.");
            }
            
            return result;
        }
    }

    private class HeadersReceivedState extends RequestState
    {
        private int statusCode;
        private String statusText;
        
        public HeadersReceivedState()
        {
            super(HEADERS_RECEIVED);
        }
        
        protected HeadersReceivedState(short state)
        {
            super(state);
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public String getStatusText() throws ScriptException
        {
            return this.statusText;
        }
        
        public void setStatusText(String statusText)
        {
            this.statusText = statusText;
        }
        
        @Override
        public String getAllResponseHeaders() throws ScriptException
        {
            StringBuilder sb = new StringBuilder();
            
            for (String name : responseHeaders.keySet())
            {
                String value = this.getResponseHeader(name);
                sb.append(name).append(": ");
                
                if (value != null)
                {
                    sb.append(value);
                }
                
                sb.append("\r\n");
            }
            
            return sb.toString();
        }
        
        @Override
        public String getResponseHeader(String headerName) throws ScriptException
        {
            String result;
            headerName = normalizeHeaderName(headerName);
            
            if (!responseHeaders.containsKey(headerName))
            {
                result = null;
            }
            else
            {
                List<Header> headers = responseHeaders.get(headerName);

                switch (headers.size())
                {
                case 0: result = null; break;
                case 1: result = headers.get(0).getValue(); break;
                default: result = concatHeaders(headers); break;
                }
            }
            
            return result;
        }
    }

    private class LoadingState extends HeadersReceivedState
    {
        public LoadingState()
        {
            super(LOADING);
        }
        
        protected LoadingState(short state)
        {
            super(state);
        }
        
        @Override
        public void abort() throws ScriptException
        {
            changeToDoneState(true);
            fireReadyStateChanged();
        }
        
        @Override
        public String getResponseText() throws ScriptException
        {
            // Don't cache this guy, since we're not actually done loading data
            // yet.  Why does the spec even allow this?!
            return this.buildResponseString();
        }

        protected String buildResponseString()
        {
            // HACK: We should really follow the encoding rules
            // specified by http://www.w3.org/TR/XMLHttpRequest/#text-response-entity-body
            return new String(responseData, Charset.forName("UTF-8"));
        }
    }

    private class DoneState extends LoadingState
    {
        private boolean errorFlag = false;
        private String responseString;
        private Document responseXml;
        
        public DoneState()
        {
            super(DONE);
        }
        
        public boolean getErrorFlag()
        {
            return this.errorFlag;
        }
        
        public void setErrorFlag(boolean flag)
        {
            this.errorFlag = flag;
        }
        
        @Override
        public void abort() throws ScriptException
        {
            changeToUnsentState();
        }
        
        @Override
        public String getAllResponseHeaders() throws ScriptException
        {
            String result;
            
            if (this.errorFlag)
            {
                result = null;
            }
            else
            {
                result = super.getAllResponseHeaders();
            }
            
            return result;
        }
        
        @Override
        public String getResponseHeader(String headerName) throws ScriptException
        {
            String result;
            
            if (this.errorFlag)
            {
                result = null;
            }
            else
            {
                result = super.getResponseHeader(headerName);
            }
            
            return result;
        }
        
        @Override
        public Document getResponseXml() throws ScriptException
        {
            buildResponseXml();
            return responseXml;
        }
        
        @Override
        protected String buildResponseString()
        {
            if (responseString == null)
            {
                if (responseData == null)
                {
                    responseString = "";
                }
                else
                {
                    responseString = super.buildResponseString();
                }
            }
            
            return this.responseString;
        }
        
        private void buildResponseXml() throws ScriptException
        {
            if (responseXml == null)
            {
                if (responseData != null)
                {
                    String contentType = this.getResponseHeader("Content-Type");
                    
                    if (contentType == null || contentType.length() == 0)
                    {
                        contentType = "text/xml";
                    }
                    
                    if (XML_CONTENT_TYPES.contains(contentType) || contentType.endsWith("+xml"))
                    {
                        try
                        {
                            DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
                            responseXml = builder.parse(new ByteArrayInputStream(responseData), storedUri.toString());
                        }
                        catch (SAXException e)
                        {
                            log.warn("Unable to parse response as XML: {0}", e, storedUri);
                        }
                        catch (ParserConfigurationException e)
                        {
                            log.warn("Unable to parse response as XML: {0}", e, storedUri);
                        }
                        catch (IOException e)
                        {
                            log.warn("Unable to parse response as XML: {0}", e, storedUri);
                        }
                    }
                }
            }
        }
    }
    
    public static class XmlHttpRequestException extends ScriptException
    {
        private static final long serialVersionUID = 1L;

        public XmlHttpRequestException(Exception inner)
        {
            super(inner);
        }

        public XmlHttpRequestException(String msg, Exception innerException)
        {
            super(msg, innerException);
        }

        public XmlHttpRequestException(String msg)
        {
            super(msg);
        }
    }
}

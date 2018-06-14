package com.microsoft.azure.servicebus.primitives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RequestResponseLinkCache
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(RequestResponseLinkCache.class);
    
    private Object lock = new Object();
    private final MessagingFactory underlyingFactory;    
    private HashMap<String, RequestResponseLinkWrapper> pathToRRLinkMap;
    
    public RequestResponseLinkCache(MessagingFactory underlyingFactory)
    {
        this.underlyingFactory = underlyingFactory;
        this.pathToRRLinkMap = new HashMap<>();
    }
    
    public CompletableFuture<RequestResponseLink> obtainRequestResponseLinkAsync(String entityPath, String transferEntityPath)
    {
        RequestResponseLinkWrapper wrapper;
        String mapKey;
        if (transferEntityPath != null)
        {
            mapKey = entityPath + ":" + transferEntityPath;
        }
        else
        {
            mapKey = entityPath;
        }
        
        synchronized (lock)
        {
            wrapper = this.pathToRRLinkMap.get(mapKey);
            if(wrapper == null)
            {
                wrapper = new RequestResponseLinkWrapper(this.underlyingFactory, entityPath, transferEntityPath);
                this.pathToRRLinkMap.put(mapKey, wrapper);
            }
        }
        return wrapper.acquireReferenceAsync();
    }
    
    public void releaseRequestResponseLink(String entityPath, String transferEntityPath)
    {
        String mapKey;
        if (transferEntityPath != null)
        {
            mapKey = entityPath + ":" + transferEntityPath;
        }
        else
        {
            mapKey = entityPath;
        }

        RequestResponseLinkWrapper wrapper;
        synchronized (lock)
        {
            wrapper = this.pathToRRLinkMap.get(mapKey);
        }
        if(wrapper != null)
        {
            wrapper.releaseReference();
        }
    }
    
    public CompletableFuture<Void> freeAsync()
    {
        TRACE_LOGGER.info("Closing all cached request-response links");
        ArrayList<CompletableFuture<Void>> closeFutures = new ArrayList<>();
        for(RequestResponseLinkWrapper wrapper : this.pathToRRLinkMap.values())
        {
            closeFutures.add(wrapper.forceCloseAsync());
        }
        
        this.pathToRRLinkMap.clear();
        return CompletableFuture.allOf(closeFutures.toArray(new CompletableFuture[0]));
    }
    
    private void removeWrapperFromCache(String entityPath)
    {
        synchronized (lock)
        {
            this.pathToRRLinkMap.remove(entityPath);
        }
    }
    
    private class RequestResponseLinkWrapper
    {
        private Object lock = new Object();
        private final MessagingFactory underlyingFactory;
        private final String entityPath;
        private final String transferEntityPath;
        private RequestResponseLink requestResponseLink;
        private int referenceCount;
        private ArrayList<CompletableFuture<RequestResponseLink>> waiters;
        
        public RequestResponseLinkWrapper(MessagingFactory underlyingFactory, String entityPath, String transferEntityPath)
        {
            this.underlyingFactory = underlyingFactory;
            this.entityPath = entityPath;
            this.transferEntityPath = transferEntityPath;
            this.requestResponseLink = null;
            this.referenceCount = 0;
            this.waiters = new ArrayList<>();
            this.createRequestResponseLinkAsync();
        }
        
        private void createRequestResponseLinkAsync()
        {
            String requestResponseLinkPath = RequestResponseLink.getManagementNodeLinkPath(this.entityPath);
            String sasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, this.underlyingFactory.getHostName(), this.entityPath);

            String transferDestinationSasTokenAudienceURI = null;
            Map<Symbol, Object> additionalProperties = null;
            if (this.transferEntityPath != null) {
                transferDestinationSasTokenAudienceURI = String.format(ClientConstants.SAS_TOKEN_AUDIENCE_FORMAT, this.underlyingFactory.getHostName(), this.transferEntityPath);
                additionalProperties = new HashMap<>();
                additionalProperties.put(ClientConstants.LINK_TRANSFER_DESTINATION_PROPERTY, this.transferEntityPath);
            }

            TRACE_LOGGER.debug("Creating requestresponselink to '{}'", requestResponseLinkPath);
            RequestResponseLink.createAsync(
                    this.underlyingFactory,
                    StringUtil.getShortRandomString() + "-RequestResponse",
                    requestResponseLinkPath,
                    sasTokenAudienceURI,
                    transferDestinationSasTokenAudienceURI,
                    additionalProperties)
                    .handleAsync((rrlink, ex) ->
            {
                synchronized (this.lock)
                {
                    if(ex == null)
                    {
                        TRACE_LOGGER.info("Created requestresponselink to '{}'", requestResponseLinkPath);
                        this.requestResponseLink = rrlink;
                        for(CompletableFuture<RequestResponseLink> waiter : this.waiters)
                        {
                            this.referenceCount++;
                            waiter.complete(this.requestResponseLink);
                        }
                    }
                    else
                    {
                        Throwable cause = ExceptionUtil.extractAsyncCompletionCause(ex);
                        TRACE_LOGGER.error("Creating requestresponselink to '{}' failed.", requestResponseLinkPath, cause);
                        RequestResponseLinkCache.this.removeWrapperFromCache(this.entityPath);
                        for(CompletableFuture<RequestResponseLink> waiter : this.waiters)
                        {
                            waiter.completeExceptionally(cause);
                        }
                    }
                }
                
                return null;
            });
        }
        
        public CompletableFuture<RequestResponseLink> acquireReferenceAsync()
        {
            synchronized (this.lock)
            {
                if(this.requestResponseLink == null)
                {
                    CompletableFuture<RequestResponseLink> waiter = new CompletableFuture<>();
                    this.waiters.add(waiter);
                    return waiter;
                }
                else
                {
                    this.referenceCount++;
                    return CompletableFuture.completedFuture(this.requestResponseLink);
                }
            }
        }
        
        public void releaseReference()
        {            
            synchronized (this.lock)
            {
                if(--this.referenceCount == 0)
                {
                    RequestResponseLinkCache.this.removeWrapperFromCache(this.entityPath);
                    TRACE_LOGGER.info("Closing requestresponselink to '{}'", this.requestResponseLink.getLinkPath());
                    this.requestResponseLink.closeAsync();
                }
            }
        }
        
        public CompletableFuture<Void> forceCloseAsync()
        {
            TRACE_LOGGER.info("Force closing requestresponselink to '{}'", this.requestResponseLink.getLinkPath());
            return this.requestResponseLink.closeAsync();
        }
    }
}

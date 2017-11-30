// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 *
 *
 */
final public class Message implements Serializable, IMessage {
	private static final long serialVersionUID = 7849508139219590863L;
	
	private static final String DEFAULT_CONTENT_TYPE = null;
	
	private static final MessageBody DEFAULT_CONTENT = new MessageBody(new byte[0]);

	private long deliveryCount;
	
	private String messageId;
	
	private Duration timeToLive;
	
	private MessageBody body;
	
	private String contentType;
	
	private String sessionId;
	
	private long sequenceNumber;
	
	private Instant enqueuedTimeUtc;
	
	private Instant scheduledEnqueueTimeUtc;
	
	private Instant lockedUntilUtc;
	
	private Map<String, Object> properties;
	
	private String correlationId;
	
	private String replyToSessionId;
	
	private String label;
	
	private String to;
	
	private String replyTo;
	
	private String partitionKey;
	
	private String deadLetterSource;
	
	private UUID lockToken;
	
	private byte[] deliveryTag;
	
	public Message()
	{
		this(DEFAULT_CONTENT);
	}
	
	public Message(String content)
	{
		this(new MessageBody(content));
	}
	
	public Message(MessageBody body)
	{
		this(body, DEFAULT_CONTENT_TYPE);
	}
	
	public Message(String content, String contentType)
	{
		this(new MessageBody(content), contentType);
	}
	
	public Message(MessageBody body, String contentType)
	{
		this(UUID.randomUUID().toString(), body, contentType);
	}
	
	public Message(String messageId, String content, String contentType)
	{
		this(messageId, new MessageBody(content), contentType);
	}

	public Message(String messageId, MessageBody body, String contentType)
	{
		this.messageId = messageId;
		this.body = body;
		this.contentType = contentType;
		this.properties = new HashMap<>();
	}

	@Override
	public long getDeliveryCount() {
		return deliveryCount;
	}

	void setDeliveryCount(long deliveryCount) {
		this.deliveryCount = deliveryCount;
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@Override
	public Duration getTimeToLive() {
		return timeToLive;
	}

	@Override
	public void setTimeToLive(Duration timeToLive) {
		this.timeToLive = timeToLive;
	}	

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;		
	}

	@Override
	public Instant getExpiresAtUtc() {
		return this.enqueuedTimeUtc.plus(this.timeToLive);
	}

	@Override
	public Instant getLockedUntilUtc() {
		return this.lockedUntilUtc;
	}
	
	public void setLockedUntilUtc(Instant lockedUntilUtc) {
		this.lockedUntilUtc = lockedUntilUtc;
	}

	@Override
	public Instant getEnqueuedTimeUtc() {
		return this.enqueuedTimeUtc;
	}
	
	void setEnqueuedTimeUtc(Instant enqueuedTimeUtc) {
		this.enqueuedTimeUtc = enqueuedTimeUtc;
	}

	@Override
	public long getSequenceNumber() {
		return this.sequenceNumber;
	}
	
	void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public String getSessionId() {
		return this.sessionId;
	}

	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;		
	}
	
	@Override
	public MessageBody getBody() {
		return this.body;
	}

	@Override
	public void setBody(MessageBody body) {
		this.body = body;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return this.properties;
	}

	@Override
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;				
	}

	@Override
	public String getCorrelationId() {
		return this.correlationId;
	}

	@Override
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public String getTo() {
		return this.to;
	}

	@Override
	public void setTo(String to) {
		this.to= to;		
	}

	@Override
	public String getReplyTo() {
		return this.replyTo;
	}

	@Override
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;		
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;		
	}

	@Override
	public String getReplyToSessionId() {
		return this.replyToSessionId;
	}

	@Override
	public void setReplyToSessionId(String replyToSessionId) {
		this.replyToSessionId = replyToSessionId;		
	}

	@Override
	public Instant getScheduledEnqueuedTimeUtc() {
		return this.scheduledEnqueueTimeUtc;
	}

	@Override
	public void setScheduledEnqueuedTimeUtc(Instant scheduledEnqueueTimeUtc) {
		this.scheduledEnqueueTimeUtc = scheduledEnqueueTimeUtc;		
	}	

	@Override
	public String getPartitionKey() {
		return this.partitionKey;
	}

	@Override
	public void setPartitionKey(String partitionKey) {
		this.partitionKey = partitionKey;		
	}	

	@Override
	public String getDeadLetterSource() {
		return this.deadLetterSource;
	}
	
	void setDeadLetterSource(String deadLetterSource) {
		this.deadLetterSource = deadLetterSource;
	}

	@Override
	public UUID getLockToken() {
		return this.lockToken;
	}
	
	void setLockToken(UUID lockToken){
		this.lockToken = lockToken;
	}
	
	byte[] getDeliveryTag()
	{
		return this.deliveryTag;
	}
	
	void setDeliveryTag(byte[] deliveryTag)
	{
		this.deliveryTag = deliveryTag;
	}
	
}

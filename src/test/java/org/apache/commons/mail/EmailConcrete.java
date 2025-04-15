package org.apache.commons.mail;

import java.util.Map;

public class EmailConcrete extends Email{
	
	@Override
    public Email setMsg(String msg) throws EmailException {
        if (msg == null || msg.trim().isEmpty()) {
            throw new EmailException("Message content cannot be null or empty.");
        }
        this.content = msg;
        this.contentType = "text/plain";
        return this;
    }
	
	/**
	 * @return headers
	 */
	public Map<String, String> getHeaders()
	{
		return this.headers;
	}
	
	public String getContentType()
	{
		return this.contentType;
	}
}

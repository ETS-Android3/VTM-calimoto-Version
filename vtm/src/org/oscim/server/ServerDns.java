package org.oscim.server;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * Simple item with a {@link List} of all URL pointing to this server.
 * @author Luca Osten
 */
public class ServerDns
{
	/**
	 * This is a {@link List} of all URL pointing to this server.
	 * <p>
	 * Multiple URLs might be pointing to the same server to allow server scaling without 
	 * the need to perform an app update.
	 */
	@NonNull
	private final List<String> listServerUrl;
	
	/**
	 * Create a new {@link ServerDns} instance.
	 * @param listServerUrl
	 * 			{@link List} of all URL pointing to this server.
	 */
	public ServerDns(@NonNull final List<String> listServerUrl)
	{
		this.listServerUrl = Collections.unmodifiableList(listServerUrl);
	}
	
	String getUrl()
	{
		return this.listServerUrl.get((int) (Math.random() * this.listServerUrl.size()));
	}
	
}

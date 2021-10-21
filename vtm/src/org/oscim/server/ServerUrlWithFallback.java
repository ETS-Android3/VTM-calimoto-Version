package org.oscim.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Simple item containing a {@link ServerDns} and a fallback {@link ServerDns} if multiple servers 
 * are available.
 * @author Luca Osten
 * @author Mareike Wendtland
 */
public class ServerUrlWithFallback
{
	/**
	 * Get a random {@link ServerDns} from the given {@link List} trying to avoid the given 
	 * {@link ServerDns} (tries 50 iterations to find a different one).
	 * @param listServerDns
	 * 			{@link List} of available {@link ServerDns}
	 * @param avoidServerUrl
	 * 			{@link ServerDns} to avoid
	 * @return random {@link ServerDns}
	 */
	private static ServerDns random(
			@NonNull final List<ServerDns> listServerDns,
			@Nullable final ServerDns avoidServerUrl)
	{
		ServerDns randomServerUrl = random(listServerDns);
		for (int i = 0; i < 50; i++)
		{
			if (randomServerUrl != avoidServerUrl)
			{
				break;
			}
			randomServerUrl = random(listServerDns);
		}
		return randomServerUrl;
	}
	
	/**
	 * Get a random {@link ServerDns} from the given {@link List}.
	 * @param listServerDns
	 * 			{@link List} of available {@link ServerDns}
	 * @return random {@link ServerDns}
	 */
	private static ServerDns random(@NonNull final List<ServerDns> listServerDns)
	{
		return listServerDns.get((int) (Math.random() * listServerDns.size()));
	}
	
	// attributes
	@NonNull
	public final String defaultServerUrl;
	@Nullable
	public final String fallbackServerUrl;
	
	/**
	 * Create a new {@link ServerUrlWithFallback} instance.
	 * <p>
	 * Will have no fallback if the {@link List} of available {@link ServerDns} only has 1 element.
	 * @param listServerDns
	 * 			{@link List} of available {@link ServerDns}
	 */
	public ServerUrlWithFallback(@NonNull final List<ServerDns> listServerDns)
	{
		if (listServerDns.isEmpty())
		{
			throw new IllegalArgumentException();
		}
		if (listServerDns.size() == 1)
		{
			final ServerDns defaultServer = listServerDns.get(0);
			this.defaultServerUrl = defaultServer.getUrl();
			this.fallbackServerUrl = null;
		}
		else
		{
			final ServerDns defaultServer = random(listServerDns);
			this.defaultServerUrl = defaultServer.getUrl();
			final ServerDns serverFallback = random(listServerDns, defaultServer);
			this.fallbackServerUrl = serverFallback.getUrl();
		}
	}
	
}

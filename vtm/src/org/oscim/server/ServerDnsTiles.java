package org.oscim.server;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.oscim.GlobalConfig;

import java.util.Collections;

/**
 * All server DNS of tile servers are found here.
 * @author Luca Osten
 * @author Mareike Neumann
 */
public class ServerDnsTiles
{
	// attributes
	@NonNull
	private final ServerDns server;
	
	/**
	 * Create a new {@link ServerDnsTiles} instance.
	 */
	public ServerDnsTiles()
	{
		this.server = new ServerDns(Collections.singletonList(GlobalConfig.TILE_SERVER_DNS));
	}
	
	@NonNull
	private String getTilesUrl(@NonNull final String serverUrl)
	{
		return serverUrl + "/maps/osm";
	}
	
	@NonNull
	public String getTilesUrl()
	{
		return getTilesUrl(this.server.getUrl());
	}
	
	@Nullable
	public String getTilesUrlFallback()
	{
		return null;
	}
	
	@NonNull
	@Override
	public String toString()
	{
		return this.server.getUrl();
	}
	
}

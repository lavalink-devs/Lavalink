package lavalink.client.io;

/**
 * Copyright (C) Fabricio20 2017 - All Rights Reserved.
 * Created by Fabricio20 on 2017-10-21.
 */
public interface PenaltyProvider {

    // Using the penalties class allows for fetching default ones like CPU or Players.
    int getPenalty(LavalinkLoadBalancer.Penalties balancer);

}

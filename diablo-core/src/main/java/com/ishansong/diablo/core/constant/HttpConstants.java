package com.ishansong.diablo.core.constant;

import java.util.concurrent.TimeUnit;

public final class HttpConstants {

    public static final long CLIENT_POLLING_READ_TIMEOUT = TimeUnit.SECONDS.toMillis( 90 );

    public static final long SERVER_MAX_HOLD_TIMEOUT = TimeUnit.SECONDS.toMillis( 60 );

 }

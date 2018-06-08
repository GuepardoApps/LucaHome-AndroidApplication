package guepardoapps.lucahome.common.handler;

import guepardoapps.lucahome.common.utils.Logger;

@SuppressWarnings({"unused"})
public class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String Tag = LoggingExceptionHandler.class.getSimpleName();

    private final Thread.UncaughtExceptionHandler _rootUncaughtExceptionHandler;

    public LoggingExceptionHandler() {
        Logger.Companion.getInstance().debug(Tag, "Constructor");
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        _rootUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable exception) {
        Logger.Companion.getInstance().error(Tag, exception.toString());
        _rootUncaughtExceptionHandler.uncaughtException(thread, exception);
    }
}

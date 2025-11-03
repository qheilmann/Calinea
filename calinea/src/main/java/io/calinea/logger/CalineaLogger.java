package io.calinea.logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

import io.calinea.Calinea;

// pick from CommandAPI's logger interface implementation
// https://github.com/CommandAPI/CommandAPI/blob/a96131d1d1a45cab6348ac48e240c566a2531d66/commandapi-core/src/main/java/dev/jorel/commandapi/CommandAPILogger.java

public interface CalineaLogger {
    static CalineaLogger fromJavaLogger(java.util.logging.Logger logger) {
		return withHeader("[" + Calinea.LIBRARY_NAME + "] ", bindToMethods(
			logger::info,
			logger::warning,
			logger::severe,
			(message, ex) -> logger.log(Level.SEVERE, message, ex))
		);
	}

    static CalineaLogger fromApacheLog4jLogger(org.apache.logging.log4j.Logger logger) {
		return withHeader("[" + Calinea.LIBRARY_NAME + "] ", bindToMethods(logger::info, logger::warn, logger::error, logger::error));
	}

	static CalineaLogger fromSlf4jLogger(org.slf4j.Logger logger) {
		return withHeader("[" + Calinea.LIBRARY_NAME + "] ", bindToMethods(logger::info, logger::warn, logger::error, logger::error));
	}

	static CalineaLogger silent() {
		return bindToMethods(msg -> {}, msg -> {}, msg -> {}, (msg, ex) -> {});
	}

	static CalineaLogger withHeader(String header, CalineaLogger baseLogger) {
		return bindToMethods(
			msg -> baseLogger.info(header + msg),
			msg -> baseLogger.warning(header + msg),
			msg -> baseLogger.severe(header + msg),
			(msg, ex) -> baseLogger.severe(header + msg, ex)
		);
	}

	static CalineaLogger bindToMethods(Consumer<String> info, Consumer<String> warning, Consumer<String> severe, BiConsumer<String, Throwable> severeException) {
		return new CalineaLogger() {
			@Override
			public void info(String message) {
				info.accept(message);
			}

			@Override
			public void warning(String message) {
				warning.accept(message);
			}

			@Override
			public void severe(String message) {
				severe.accept(message);
			}
			
			@Override
			public void severe(String message, Throwable exception) {
				severeException.accept(message, exception);
			}
		};
	}

	void info(String message);

	void warning(String message);

	void severe(String message);
	
	void severe(String message, Throwable exception);
}

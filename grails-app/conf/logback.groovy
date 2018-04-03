import ch.qos.logback.classic.encoder.PatternLayoutEncoder

GString loggingPattern = "%d{HH:mm:ss.SSS} %-5level [${hostname}] %logger - %msg%n"

// For logging to console.
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = loggingPattern
    }
}

root(ERROR, ['STDOUT'])

logger('grails.app', DEBUG, ['STDOUT'], false)
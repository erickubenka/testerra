/*
 * (C) Copyright T-Systems Multimedia Solutions GmbH 2018, ..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Peter Lehmann <p.lehmann@t-systems.com>
 *     pele <p.lehmann@t-systems.com>
 */
package eu.tsystems.mms.tic.testframework.common;

import eu.tsystems.mms.tic.testframework.constants.RTConstants;
import eu.tsystems.mms.tic.testframework.constants.fennecProperties;
import eu.tsystems.mms.tic.testframework.exceptions.FileNotFoundException;
import eu.tsystems.mms.tic.testframework.exceptions.fennecSystemException;
import eu.tsystems.mms.tic.testframework.utils.FileUtils;
import eu.tsystems.mms.tic.testframework.utils.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by pele on 05.02.2015.
 */
public class fennecCommons {

    private static final Logger LOGGER = LoggerFactory.getLogger(fennecCommons.class);

    private static final String p = "eu.tsystems.mms.tic.testframework";

    private static boolean loggingInitialized = false;

    public static final String DEFAULT_PACKAGE_NAME = "eu.tsystems.mms.tic";

    private static final String SYSTEM_PROPERTIES_FILE = "system.properties";

    private fennecCommons() {}

    /**
     * If the System Property fennec.loglevel is set, this method tries to change the appropriate Log4j Level.
     */
    public static void setfennecLogLevel(Level level) {
        org.apache.log4j.Logger fennecLogger = org.apache.log4j.Logger.getLogger(p);
        fennecLogger.setLevel(level);
    }

    public static void setfennecLogLevel() {
        /*
        Try to get fennec log level from system or test.properties
         */
        Properties properties = new Properties();
        String propertyFile = RTConstants.getfennecTestPropertiesFile();
        try {
            InputStream is = FileUtils.getLocalOrResourceFileAsStream(propertyFile);
            properties.load(is);
        } catch (Exception e) {
            throw new fennecSystemException("test.properties not found", e);
        }

        // load from file
        String fennecLogLevelString = properties.getProperty("fennec.loglevel", null);
        // overload from system
        fennecLogLevelString = System.getProperty("fennec.loglevel", fennecLogLevelString);

        /*
        Patch log level
         */
        if (fennecLogLevelString != null) {
            org.apache.log4j.Logger fennecLogger = org.apache.log4j.Logger.getLogger(p);
            fennecLogLevelString = fennecLogLevelString.trim().toUpperCase();

            Level level = Level.toLevel(fennecLogLevelString); // is debug when conversion fails
            fennecLogger.setLevel(level);
        }
    }

    /**
     * If no log4j configuration is set. We try to set it with the file test-log4j or through the BasicConfigurator.
     * Another Method is called, which reads the fennec.loglevel from Systemproperties and may overrides an existing
     * value.
     *
     * @param basicConfigFallback If true, BasicConfigurator is called when no logging config is found.
     */
    private static void initializeLogging(final boolean basicConfigFallback) {
        if (!loggingInitialized) {
            final String loggerDefinitionsFilename = "test-log4j.xml";
            final URL log4jConfig = ClassLoader.getSystemResource(loggerDefinitionsFilename);
            if (log4jConfig != null) {
                System.setProperty("log4j.configuration", loggerDefinitionsFilename);
                DOMConfigurator.configure(log4jConfig);
            } else {
                if (basicConfigFallback) {
                    BasicConfigurator.configure();
                }
            }
            loggingInitialized = true;

            /*
            Logging is basically initialized here. We CAN now overwrite the fennec log level.
             */
            setfennecLogLevel();
        }
    }

    /**
     * Loads proxy settings from a file.
     */
    private static void initializeProxySettings() {
        final boolean loadProxySettings = PropertyManager.getBooleanProperty(fennecProperties.fennec_PROXY_SETTINGS_LOAD, true);
        if (!loadProxySettings) {
            LOGGER.info("Skipping loading of fennec Proxy Settings.");
            return;
        }

        final String filename = PropertyManager.getProperty(fennecProperties.fennec_PROXY_SETTINGS_FILE, "proxysettings.properties");

        final InputStream inputStream = FileUtils.getLocalOrResourceFileAsStream(filename);

        if (inputStream == null) {
            LOGGER.warn("File " + filename + " not found. No proxy settings loaded.");
            return;
        }

        final Properties props = new Properties();

        try {
            props.load(inputStream);
        } catch (Exception e) {
            LOGGER.warn("Not loaded: " + filename);
            return;
        }

        for (String property : props.stringPropertyNames()) {
            final String systemPropertyValue = System.getProperty(property);
            if (StringUtils.isStringEmpty(systemPropertyValue)) {
                final String propertyValue = props.getProperty(property);
                System.setProperty(property, propertyValue);
                LOGGER.info("Setting system property " + property + " = " + propertyValue);
            }
            else {
                LOGGER.warn("System property " + property + " is NOT set because it was already set to "
                        + systemPropertyValue);
            }
        }
    }

    private static void initializeSystemProperties() {
        final Properties systemProperties = new Properties();
        try {
            InputStream is = FileUtils.getLocalOrResourceFileAsStream(SYSTEM_PROPERTIES_FILE);

            if (is == null) {
                LOGGER.warn("Not loaded: " + SYSTEM_PROPERTIES_FILE);
                return;
            }

            systemProperties.load(is);
            systemProperties.stringPropertyNames().forEach(key -> {
                final String value = System.getProperty(key);
                final String newValue = "" + systemProperties.get(key);
                if (!StringUtils.isStringEmpty(value)) {
                    LOGGER.warn("SystemProperty - Overwriting " + key + "=" + value + " << " + newValue);
                }
                else {
                    LOGGER.info("SystemProperty - Setting " + key + "=" + newValue);
                }
                System.setProperty(key, newValue);
            });
        } catch (Exception e) {
            LOGGER.warn("Not loaded: " + SYSTEM_PROPERTIES_FILE, e);
        }
    }

    public static void init() {
        initializeLogging(true);
        initializeSystemProperties();
        initializeProxySettings();
    }
}

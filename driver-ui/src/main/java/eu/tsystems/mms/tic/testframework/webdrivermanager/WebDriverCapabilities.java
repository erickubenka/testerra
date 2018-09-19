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
package eu.tsystems.mms.tic.testframework.webdrivermanager;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WebDriverCapabilities {

    /**
     * Extra capabilities.
     */
    protected static final Map<String, Object> GLOBALCAPABILITIES = new HashMap<>();
    protected static final ThreadLocal<Map<String, Object>> THREAD_CAPABILITIES = new ThreadLocal<Map<String, Object>>();

    protected static final Logger LOGGER = LoggerFactory.getLogger(WebDriverCapabilities.class);

    /**
     * Adds a capability.
     *
     * @param key   The key of the capability to set.
     * @param value The value of the capability to set.
     */
    static void addGlobalCapability(String key, Object value) {
        if (CapabilityType.BROWSER_NAME.equals(key)) {
            LOGGER.warn("Skipping extra desired capability " + key);
            return;
        }
        GLOBALCAPABILITIES.put(key, value);
    }

    private static void checkThreadCapabilities() {
        if (THREAD_CAPABILITIES.get() == null) {
            THREAD_CAPABILITIES.set(new HashMap<String, Object>());
        }
    }

    static void addThreadCapability(String key, Object value) {
        if (CapabilityType.BROWSER_NAME.equals(key)) {
            LOGGER.warn("Skipping extra desired capability " + key);
            return;
        }

        checkThreadCapabilities();
        THREAD_CAPABILITIES.get().put(key, value);
    }

    public static Map<String, Object> getThreadCapabilities() {
        checkThreadCapabilities();
        return THREAD_CAPABILITIES.get();
    }

    /**
     * Remove extra capability from capabilities.
     *
     * @param key The key of the capability to remove.
     */
    static void removeGlobalExtraCapability(final String key) {
        GLOBALCAPABILITIES.remove(key);
    }

    /**
     * Logs selenium proxy settings.
     *
     * @param proxy .
     */
    static void logProxySettings(Proxy proxy) {
        LOGGER.info("Proxy settings: " + "http: " + proxy.getHttpProxy() + " ssl: " + proxy.getSslProxy());
    }

    /**
     * Clear capabilities.
     */
    static void clearGlobalCapabilities() {
        GLOBALCAPABILITIES.clear();
    }

    static void clearThreadCapabilities() {
        THREAD_CAPABILITIES.remove();
    }

    /**
     * Adds extra capabilities to desired capabilities.
     *
     * @param desiredCapabilities .
     */
    static void setGlobalExtraCapabilities(final DesiredCapabilities desiredCapabilities) {
        Map<String, ?> map = desiredCapabilities.asMap();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            addGlobalCapability(key, value);
        }
    }

    static Map<String, Object> getGlobalExtraCapabilities() {
        return GLOBALCAPABILITIES;
    }

}
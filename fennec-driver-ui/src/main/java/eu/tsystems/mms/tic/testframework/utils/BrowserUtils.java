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
package eu.tsystems.mms.tic.testframework.utils;

import org.openqa.selenium.WebDriver;

/**
 * Created by pele on 14.04.2015.
 */
public final class BrowserUtils {

    /**
     * Clicks on the overridelink on the cert error page in ie.
     *
     * @param driver your driver.
     * @param handleAlert true to accept the following alert.
     */
    public static void skipIESecurityWarning(WebDriver driver, boolean handleAlert) {
        driver.navigate().to("javascript:document.getElementById('overridelink').click()");
        if (handleAlert) {
            driver.switchTo().alert().accept();
        }
    }

}

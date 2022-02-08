/*
 * Testerra
 *
 * (C) 2021, Mike Reiche,  T-Systems MMS GmbH, Deutsche Telekom AG
 *
 * Deutsche Telekom AG and all other contributors /
 * copyright owners license this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.testerra.report.test;

import eu.tsystems.mms.tic.testframework.utils.TimerUtils;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;
import io.testerra.report.test.AbstractReportTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ReportUnderTest extends AbstractReportTest {

    @Test
    public void test_visitReportDefault() {
        this.visitTestPage(WebDriverManager.getWebDriver());

        Assert.assertTrue(true);
        TimerUtils.sleep(100000);
    }

    @Test
    public void test_visitReportDirectory() {
        this.visitTestPage(WebDriverManager.getWebDriver(), "report-testsundertest-01/report-ng");

        Assert.assertTrue(true);
        TimerUtils.sleep(100000);
    }

}

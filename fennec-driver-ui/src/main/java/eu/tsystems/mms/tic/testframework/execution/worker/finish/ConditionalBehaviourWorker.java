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
package eu.tsystems.mms.tic.testframework.execution.worker.finish;

import eu.tsystems.mms.tic.testframework.common.PropertyManager;
import eu.tsystems.mms.tic.testframework.constants.fennecProperties;
import eu.tsystems.mms.tic.testframework.execution.testng.worker.MethodWorker;
import eu.tsystems.mms.tic.testframework.report.TestStatusController;
import eu.tsystems.mms.tic.testframework.report.fennecListener;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;

/**
 * Created by pele on 19.01.2017.
 */
public class ConditionalBehaviourWorker extends MethodWorker {

    @Override
    public void run() {
        if (isTest() && isFailed() && TestStatusController.areAllTestsPassedYet()) {

            // check state condition: shutdown
            boolean skipShutdown = PropertyManager.getBooleanProperty(
                    fennecProperties.ON_STATE_TESTFAILED_SKIP_SHUTDOWN, false);
            if (skipShutdown) {
                LOGGER.debug("ON_STATE_TESTFAILED_SKIP_SHUTDOWN: true");
                // leave all windows open when this condition is true (except you call forceShutdown)
                WebDriverManager.config().executeCloseWindows = false;
            }

            // check state condition: skip test methods
            boolean skipFollowingTests = PropertyManager.getBooleanProperty(
                    fennecProperties.ON_STATE_TESTFAILED_SKIP_FOLLOWING_TESTS, false);
            if (skipFollowingTests) {
                LOGGER.debug("ON_STATE_TESTFAILED_SKIP_FOLLOWING_TESTS: true");
                fennecListener.skipAllTests();
            }
        }

    }
}

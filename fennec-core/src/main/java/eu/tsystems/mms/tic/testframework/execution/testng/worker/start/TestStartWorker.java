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
package eu.tsystems.mms.tic.testframework.execution.testng.worker.start;

import eu.tsystems.mms.tic.testframework.events.fennecEvent;
import eu.tsystems.mms.tic.testframework.events.fennecEventDataType;
import eu.tsystems.mms.tic.testframework.events.fennecEventService;
import eu.tsystems.mms.tic.testframework.events.fennecEventType;
import eu.tsystems.mms.tic.testframework.execution.testng.worker.MethodWorker;
import eu.tsystems.mms.tic.testframework.report.fennecListener;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;

/**
 * Created by pele on 19.01.2017.
 */
public class TestStartWorker extends MethodWorker {

    @Override
    public void run() {
        // set StartTime at very first test invocation.
        synchronized (fennecListener.class) {
            if (wasMethodInvoked()) {
                // fire event for test start
                fennecEventService.getInstance().fireEvent(new fennecEvent(fennecEventType.TEST_START)
                        .addUserData()
                        .addData(fennecEventDataType.TIMESTAMP, ExecutionContextController.RUN_CONTEXT.startTime.getTime())
                        .addData(fennecEventDataType.ITestResult, testResult)
                        .addData(fennecEventDataType.IInvokedMethod, method)
                );
            }
        }

        // set the thread name
        methodContext.setThreadName();

        // Thread visualizer and method timer start.
        fennecListener.startMethodTimer();

    }
}

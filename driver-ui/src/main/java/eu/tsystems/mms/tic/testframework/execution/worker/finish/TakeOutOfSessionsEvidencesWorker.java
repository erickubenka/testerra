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

import eu.tsystems.mms.tic.testframework.execution.testng.worker.MethodWorker;
import eu.tsystems.mms.tic.testframework.interop.TestEvidenceCollector;
import eu.tsystems.mms.tic.testframework.report.TestStatusController;
import eu.tsystems.mms.tic.testframework.report.model.context.Screenshot;
import eu.tsystems.mms.tic.testframework.report.model.context.Video;
import eu.tsystems.mms.tic.testframework.webdrivermanager.WebDriverManager;

import java.util.List;

/**
 * Created by pele on 19.01.2017.
 */
public class TakeOutOfSessionsEvidencesWorker extends AbstractEvidencesWorker {

    void collect() {
        if (isTest() && WebDriverManager.config().areSessionsClosedAfterTestMethod()) {
            /*
            videos are now fetched only after test methods
             */
            List<Video> videos = TestEvidenceCollector.collectVideos();
            LOGGER.info("Evidence Videos: " + videos);
            if (videos != null) {
                videos.forEach(v -> v.errorContextId = methodContext.id);
                methodContext.videos.addAll(videos);
            }
        }
    }

}
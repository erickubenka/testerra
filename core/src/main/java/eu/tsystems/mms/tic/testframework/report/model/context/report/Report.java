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
package eu.tsystems.mms.tic.testframework.report.model.context.report;

import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraRuntimeException;
import eu.tsystems.mms.tic.testframework.exceptions.TesterraSystemException;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.IReport;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.model.context.Screenshot;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import eu.tsystems.mms.tic.testframework.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Report implements IReport, Loggable {

    public static final File REPORT_DIRECTORY;

    public static final String FRAMES_FOLDER_NAME = "frames";
    public static final String METHODS_FOLDER_NAME = "methods";
    public static final String SCREENSHOTS_FOLDER_NAME = "screenshots";
    public static final String VIDEO_FOLDER_NAME = "videos";
    public static final String XML_FOLDER_NAME = "xml";

    private final static IReport report = Testerra.ioc().getInstance(IReport.class);

    static {
        /*
        Initialize report directory
         */
        final String relativeReportDir = Properties.BASE_DIR.asString();
        REPORT_DIRECTORY = new File(relativeReportDir);

        // cleanup
        try {
            FileUtils.deleteDirectory(REPORT_DIRECTORY);
        } catch (IOException e) {
            throw new TesterraRuntimeException("Could not clean report dir.", e);
        }
        if (!REPORT_DIRECTORY.mkdirs()) {
            throw new TesterraSystemException(
                "Error cleaning report dir: " + REPORT_DIRECTORY +
                    "\nCheck consoles or other directory and file accesses for locks.");
        }
    }

    public static final File FRAMES_DIRECTORY = new File(REPORT_DIRECTORY, FRAMES_FOLDER_NAME);
    public static final File METHODS_DIRECTORY = new File(FRAMES_DIRECTORY, METHODS_FOLDER_NAME);
    public static final File SCREENSHOTS_DIRECTORY = new File(REPORT_DIRECTORY, SCREENSHOTS_FOLDER_NAME);
    public static final File VIDEO_DIRECTORY = new File(REPORT_DIRECTORY, VIDEO_FOLDER_NAME);
    public static final File XML_DIRECTORY = new File(REPORT_DIRECTORY, XML_FOLDER_NAME);

    static {
        /*
        Initialize report sub directories
         */
        FRAMES_DIRECTORY.mkdirs();
        METHODS_DIRECTORY.mkdirs();
        SCREENSHOTS_DIRECTORY.mkdirs();
        VIDEO_DIRECTORY.mkdirs();
        XML_DIRECTORY.mkdirs();
    }

    /**
     * Adds a screenshot to the report
     */
    @Override
    public IReport addScreenshot(Screenshot screenshot, Mode mode) {
        /*
        provide screenshot
         */
        final File targetScreenshotFile = new File(SCREENSHOTS_DIRECTORY, screenshot.filename);
        try {
            switch (mode) {
                case COPY:
                    FileUtils.copyFile(screenshot.getScreenshotFile(), targetScreenshotFile, true);
                    break;
                case MOVE:
                    FileUtils.moveFile(screenshot.getScreenshotFile(), targetScreenshotFile);
                    break;
            }
        } catch (IOException e) {
            log().error(e.getMessage());
        }

        final File targetSourceFile = new File(SCREENSHOTS_DIRECTORY, screenshot.sourceFilename);
        try {
            switch (mode) {
                case COPY:
                    FileUtils.copyFile(screenshot.getPageSourceFile(), targetSourceFile, true);
                    break;
                case MOVE:
                    FileUtils.moveFile(screenshot.getPageSourceFile(), targetSourceFile);
                    break;
            }
        } catch (IOException e) {
            log().error(e.getMessage());
        }

        List<Screenshot> screenshots = new ArrayList<>();
        screenshots.add(screenshot);

        MethodContext methodContext = ExecutionContextController.getCurrentMethodContext();
        methodContext.addScreenshots(screenshots);

        return this;
    }

    /**
     * @deprecated This method does nothing
     */
    @Deprecated
    public static Screenshot provideScreenshot(
        File screenshotFile,
        File screenshotSourceFileOrNull,
        Mode mode
    ) throws IOException {
        return new Screenshot();
    }

//
//    public static Video provideVideo(
//        File file,
//        Mode mode
//    ) throws IOException {
//        if (!file.exists()) {
//            LOGGER.error("Cannot provide video: " + file + " does not exist");
//            return null;
//        }
//
//        final String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getName());
//
//        File targetFile = new File(VIDEO_DIRECTORY, fileName);
//        switch (mode) {
//            case COPY:
//                FileUtils.copyFile(file, targetFile, true);
//                break;
//            case MOVE:
//                FileUtils.moveFile(file, targetFile);
//                break;
//        }
//
//        Video video = new Video();
//        video.filename = fileName;
//
//        LOGGER.info("Provided video " + file + " as " + targetFile);
//
//        return video;
//    }
}
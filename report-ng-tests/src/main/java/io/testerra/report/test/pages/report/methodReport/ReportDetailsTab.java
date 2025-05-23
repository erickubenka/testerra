/*
 * Testerra
 *
 * (C) 2022, Marc Dietrich, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
 *
 */

package io.testerra.report.test.pages.report.methodReport;

import eu.tsystems.mms.tic.testframework.internal.asserts.StringAssertion;
import eu.tsystems.mms.tic.testframework.pageobjects.Check;
import eu.tsystems.mms.tic.testframework.pageobjects.PreparedLocator;
import eu.tsystems.mms.tic.testframework.pageobjects.TestableUiElement;
import eu.tsystems.mms.tic.testframework.pageobjects.UiElement;
import eu.tsystems.mms.tic.testframework.report.Status;
import io.testerra.report.test.pages.utils.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReportDetailsTab extends AbstractReportMethodPage {
    @Check
    private final UiElement pageContent = find(By.xpath("//router-view[contains(@class,'au-target')]"));
    @Check
    private final UiElement testFailureAspect = pageContent.find(By.xpath("//div[contains(@class,'p1') and contains(@class,'status')]"));
    PreparedLocator failureAspectLocator = LOCATE.prepare("//div[contains(@class,'p1') and contains(@class,'status') %s]");
    //TODO: mandatory?
    private final UiElement testOriginCard = pageContent.find(By.xpath("//div[./div[contains(text(), 'Origin')]]"));
    @Check
    private final UiElement testStacktraceCard = pageContent.find(By.xpath("//div[./div[contains(text(), 'Stacktrace')]]"));

    private final String failureAspectCodeLineXPath = "//div[contains(@class,'line') and contains(@class,'error')]/span[@class='au-target']";
    private final UiElement layoutComparison = find(By.xpath("//layout-comparison"));
    PreparedLocator imgTitleLocator = LOCATE.prepare("//img[contains(@title,'%s')]");

    // Root element of every overly component
    private final UiElement dialogRoot = find(By.xpath("//div[@class = 'mdc-dialog__container']"));

    public ReportDetailsTab(WebDriver driver) {
        super(driver);
    }

    // Returns the first failure aspect box
    public TestableUiElement getFailureAspect() {
        return getFailureAspect("");
    }

    public TestableUiElement getFailureAspect(final String assertMessage) {
        return this.getFailureAspectElement(assertMessage);
    }

    public TestableUiElement getComparisonImgElement(String assertMessage, String type) {
        PreparedLocator imgLocator = LOCATE.prepare("//layout-comparison//img[contains(@title, '%s')]");
        return this.getFailureAspectElement(assertMessage).find(imgLocator.with(type));
    }

    private UiElement getFailureAspectElement(final String assertMessage) {
        String messagePart = String.format("and .//span[contains(text(), '%s')]", assertMessage);
        if (StringUtils.isNotEmpty(assertMessage)) {
            return this.pageContent.find(failureAspectLocator.with(messagePart));
        } else {
            return this.pageContent.find(failureAspectLocator.with(""));
        }
    }

    public void assertFailureAspectsCorrespondsToCorrectStatus(final String expectedStatusTitle) {
        String expectedStatusTitleFormatted = expectedStatusTitle.toLowerCase();

        if (expectedStatusTitleFormatted.equals(Status.FAILED_EXPECTED.title.toLowerCase(Locale.ROOT)))
            expectedStatusTitleFormatted = "failed-expected";

        final StringAssertion attributeClass = testFailureAspect.expect().attribute("class");
        attributeClass.contains(expectedStatusTitleFormatted).is(true,
                String.format("Failure Aspect status [%s] should correspond to method state [%s]",
                        attributeClass.getActual(),
                        expectedStatusTitleFormatted));
    }

    public void assertTestMethodContainsCorrectFailureAspect(final String... expectedFailureAspects) {
        final List<String> actualFailureAspects = testOriginCard.list()
                .stream()
                .map(uiElement -> uiElement.find(By.xpath(failureAspectCodeLineXPath)))
                .map(uiElement -> uiElement.waitFor().text().getActual())
                .collect(Collectors.toList());

        for (String failureAspect : expectedFailureAspects) {
            final String assertMessage = String.format("Given failure aspect '%s' should match the code-line in origin-card!", failureAspect);

            Assert.assertTrue(actualFailureAspects.stream().anyMatch(actualFailureAspect -> actualFailureAspect.contains(failureAspect)),
                    assertMessage);
        }
    }

    public void assertSkippedTestContainsCorrespondingFailureAspect() {
        boolean skippedTestContainsCorrespondingFailureAspect = skippedTestDependsOnFailedMethod() || skippedTestContainsSkipException() || skippedTestFailsInBeforeMethod() || skippedTestFailsInDataProvider();
        //at least one condition should be true
        Assert.assertTrue(skippedTestContainsCorrespondingFailureAspect, "One skipped condition should correspond to skipped test!");
    }

    private boolean skippedTestContainsSkipException() {
        UiElement failureAspectCodeLine = testOriginCard.find(By.xpath(failureAspectCodeLineXPath));
        if (failureAspectCodeLine.waitFor().displayed(true))
            return failureAspectCodeLine.expect().text().getActual().contains("SkipException");
        return false;
    }

    private boolean skippedTestFailsInDataProvider() {
        //check whether code contains '@DataProvider'
        UiElement dataProviderCode = testOriginCard.find(By.xpath("//div[contains(@class,'line')]//span[contains(text(),'@DataProvider')]"));
        return dataProviderCode.waitFor().displayed(true);
    }

    private boolean skippedTestFailsInBeforeMethod() {
        //check whether code contains '@BeforeMethod()'
        UiElement beforeMethodCode = testOriginCard.find(By.xpath("//div[contains(@class,'line')]//span[contains(text(),'@BeforeMethod(')]"));
        return beforeMethodCode.waitFor().displayed(true);
    }

    private boolean skippedTestDependsOnFailedMethod() {
        String failureAspect = testFailureAspect.expect().text().getActual();
        String expectedContainedText = "depends on not successfully finished methods";
        return failureAspect.contains(expectedContainedText);
    }

    public void assertPageIsValid() {
        expandStacktrace();
        assertStacktraceIsDisplayed();
        assertContainsCodeLines();
        assertFailLureLineIsMarked();
    }

    private void expandStacktrace() {
        UiElement expander = testStacktraceCard.find(By.xpath("//mdc-expandable"));
        expander.expect().displayed().is(true);
        expander.click();
    }

    private void assertStacktraceIsDisplayed() {
        UiElement stacktrace = testStacktraceCard.find(By.xpath("//mdc-expandable//div[@class='code-view']"));
        stacktrace.expect().displayed().is(true);
        Assert.assertTrue(stacktrace.list().size() > 0, "There should be some line of stack trace displayed!");
    }

    private void assertContainsCodeLines() {
        UiElement failureOrigin = testOriginCard.find(By.xpath("//div[@class='code-view']"));
        failureOrigin.expect().displayed().is(true);
        Assert.assertTrue(failureOrigin.list().size() > 0, "There should be some line of code displayed!");
    }

    private void assertFailLureLineIsMarked() {
        UiElement markedFailureCodeLine = testOriginCard.find(By.xpath("//div[@class='code-view']//div[contains(@class,'error')]"));
        markedFailureCodeLine.expect().displayed().is(true);
    }

    public void assertStacktraceContainsExpectedFailureAspects(String... expectedFailureAspects) {
        final List<String> actualFailureAspects = testStacktraceCard.list()
                .stream()
                .map(uiElement -> uiElement.waitFor().text().getActual())
                .collect(Collectors.toList());

        for (String failureAspect : expectedFailureAspects) {
            Assert.assertTrue(actualFailureAspects.stream().anyMatch(actualFailureAspect -> actualFailureAspect.contains(failureAspect)),
                    "Actual failure aspects should match the expected ones");
        }
    }

    public void assertDurationIsNotValid(int lowerBound) {
        String duration = getTestDuration();
        String secondsString = RegExUtils.getRegExpResultOfString(RegExUtils.RegExp.DIGITS_ONLY, duration);
        int seconds = Integer.parseInt(secondsString.trim());
        Assert.assertFalse(lowerBound > seconds, "Run duration should not be in valid interval");
    }

    public void assertFailsAnnotationMessage(String expectedTicketString) {
        UiElement failsAnnotationSegment = find(By.xpath("//div[./span[contains(text(),'@Fails')]]"));
        failsAnnotationSegment.expect().text().contains(expectedTicketString).is(true);
    }

    public ReportDetailsTab navigateToPreviousFailedMethod() {
        UiElement previousFailedMethod = find(By.xpath("//div[./span[text()='Previous failed method']]"));
        previousFailedMethod.find(By.xpath("//a")).click();

        return createPage(ReportDetailsTab.class);
    }

    public ReportDetailsTab navigateToNextFailedMethod() {
        UiElement previousFailedMethod = find(By.xpath("//div[./span[text()='Next failed method']]"));
        previousFailedMethod.find(By.xpath("//a")).click();

        return createPage(ReportDetailsTab.class);
    }

    public boolean hasFailureAspectAScreenshotComparison(String failureMessage) {
        return this.getFailureAspectElement(failureMessage).find(By.xpath("//layout-comparison")).waitFor().present(true);
    }

    public ComparisonDialogOverlay openComparisonDialogByClickingOnScreenShot(final ScreenshotType screenshotType) {
        UiElement image = layoutComparison.find(imgTitleLocator.with(screenshotType.toString()));
        image.click();
        return createComponent(ComparisonDialogOverlay.class, this.dialogRoot);
    }

    public enum ScreenshotType {
        Actual,
        Difference,
        Expected;
    }
}

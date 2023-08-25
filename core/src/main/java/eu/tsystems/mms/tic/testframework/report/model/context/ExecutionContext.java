/*
 * Testerra
 *
 * (C) 2020, Mike Reiche, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
package eu.tsystems.mms.tic.testframework.report.model.context;

import com.google.common.eventbus.EventBus;
import eu.tsystems.mms.tic.testframework.common.Testerra;
import eu.tsystems.mms.tic.testframework.events.ContextUpdateEvent;
import eu.tsystems.mms.tic.testframework.internal.TimingCollector;
import eu.tsystems.mms.tic.testframework.report.model.timings.TimingType;
import eu.tsystems.mms.tic.testframework.report.utils.TestNGContextNameGenerator;
import org.apache.commons.lang3.time.StopWatch;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class ExecutionContext extends AbstractContext {
    private final Queue<SuiteContext> suiteContexts = new ConcurrentLinkedQueue<>();
    /**
     * @deprecated Use {@link #getRunConfig()} instead
     */
    public final RunConfig runConfig = new RunConfig();
    /**
     * @deprecated Use {@link #isCrashed()} instead
     */
    public boolean crashed = false;
    private Queue<SessionContext> exclusiveSessionContexts;
    /**
     * @deprecated Use {@link #getEstimatedTestMethodCount()} instead
     */
    public int estimatedTestMethodCount;
    private final ConcurrentLinkedQueue<LogMessage> methodContextLessLogs = new ConcurrentLinkedQueue<>();

    // Additional timing information
    private Map<AbstractContext, Map<TimingType, StopWatch>> contextTimings = new ConcurrentHashMap<>();
    // page timings

    public ExecutionContext() {
        setName(runConfig.RUNCFG);
        Testerra.getEventBus().post(new ContextUpdateEvent().setContext(this));
    }

    public Stream<SuiteContext> readSuiteContexts() {
        return this.suiteContexts.stream();
    }

    public Stream<SessionContext> readExclusiveSessionContexts() {
        if (this.exclusiveSessionContexts == null) {
            return Stream.empty();
        } else {
            return exclusiveSessionContexts.stream();
        }
    }

    public ExecutionContext addExclusiveSessionContext(SessionContext sessionContext) {
        if (this.exclusiveSessionContexts == null) {
            this.exclusiveSessionContexts = new ConcurrentLinkedQueue<>();
        }
        if (!this.exclusiveSessionContexts.contains(sessionContext)) {
            this.exclusiveSessionContexts.add(sessionContext);
            sessionContext.setParentContext(this);
        }
        return this;
    }

    public ExecutionContext addLogMessage(LogMessage logMessage) {
        this.methodContextLessLogs.add(logMessage);
        return this;
    }

    public Stream<LogMessage> readMethodContextLessLogs() {
        return this.methodContextLessLogs.stream();
    }

    public SuiteContext getSuiteContext(ITestResult testResult) {
        return getSuiteContext(Testerra.getInjector().getInstance(TestNGContextNameGenerator.class).getSuiteContextName(testResult));
    }

    private synchronized SuiteContext getSuiteContext(String suiteContextName) {
        return getOrCreateContext(
                suiteContexts,
                suiteContextName,
                () -> new SuiteContext(this),
                suiteContext -> {
                    EventBus eventBus = Testerra.getEventBus();
                    eventBus.post(new ContextUpdateEvent().setContext(this));
                });
    }

    public SuiteContext getSuiteContext(ITestContext testContext) {
        return getSuiteContext(Testerra.getInjector().getInstance(TestNGContextNameGenerator.class).getSuiteContextName(testContext));
    }

    public RunConfig getRunConfig() {
        return this.runConfig;
    }

    public boolean isCrashed() {
        return this.crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public int getEstimatedTestMethodCount() {
        return this.estimatedTestMethodCount;
    }

    public void setEstimatedTestMethodCount(int count) {
        this.estimatedTestMethodCount = count;
    }

    public void addContextTiming(AbstractContext context, TimingType type, StopWatch stopWatch) {
        Map<TimingType, StopWatch> mapEntry;
        if (this.contextTimings.containsKey(context)) {
            mapEntry = this.contextTimings.get(context);
        } else {
            mapEntry = new HashMap<>();
            this.contextTimings.put(context, mapEntry);
        }
        mapEntry.put(type, stopWatch);
    }

    public Map<AbstractContext, Map<TimingType, StopWatch>> getContextTimings() {
        return this.contextTimings;
    }
}

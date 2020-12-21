/*
 * Testerra
 *
 * (C) 2020,  Peter Lehmann, T-Systems Multimedia Solutions GmbH, Deutsche Telekom AG
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
 package eu.tsystems.mms.tic.testframework.report.model.context;

import com.google.common.eventbus.EventBus;
import eu.tsystems.mms.tic.testframework.annotations.TestClassContext;
import eu.tsystems.mms.tic.testframework.events.ContextUpdateEvent;
import eu.tsystems.mms.tic.testframework.internal.Flags;
import eu.tsystems.mms.tic.testframework.report.FailureCorridor;
import eu.tsystems.mms.tic.testframework.report.TestStatusController;
import eu.tsystems.mms.tic.testframework.report.TesterraListener;
import eu.tsystems.mms.tic.testframework.report.utils.TestNGHelper;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.core.LogEvent;
import org.testng.ITestContext;
import org.testng.ITestResult;

public class ExecutionContext extends AbstractContext implements SynchronizableContext {

    /**
     * @deprecated Use {@link #readSuiteContexts()} instead
     */
    public final Queue<SuiteContext> suiteContexts = new ConcurrentLinkedQueue<>();
    @Deprecated
    public Map<String, List<MethodContext>> failureAspects;
    @Deprecated
    public Map<String, List<MethodContext>> exitPoints;
    public final RunConfig runConfig = new RunConfig();

    /**
     * @deprecated Use {@link #getMetaData()} instead
     */
    public final Map<String, String> metaData = new LinkedHashMap<>();
    public boolean crashed = false;

    private Queue<SessionContext> exclusiveSessionContexts;

    public int estimatedTestMethodCount;

    private final ConcurrentLinkedQueue<LogEvent> methodContextLessLogs = new ConcurrentLinkedQueue<>();

    public ExecutionContext() {
        name = runConfig.RUNCFG;
        TesterraListener.getEventBus().post(new ContextUpdateEvent().setContext(this));
    }

    public Map<String, String> getMetaData() {
        return metaData;
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
            sessionContext.parentContext = this;
        }
        return this;
    }

    public ExecutionContext addLogEvent(LogEvent logEvent) {
        this.methodContextLessLogs.add(logEvent);
        return this;
    }

    public Stream<LogEvent> readMethodContextLessLogs() {
        return this.methodContextLessLogs.stream();
    }

    public synchronized SuiteContext getSuiteContext(ITestResult testResult, ITestContext iTestContext) {
        final String suiteName = TestNGHelper.getSuiteName(testResult, iTestContext);
        return getOrCreateContext(
                suiteContexts,
                suiteName,
                () -> new SuiteContext(this),
                suiteContext -> {
                    EventBus eventBus = TesterraListener.getEventBus();
                    eventBus.post(new ContextUpdateEvent().setContext(this));
                });
    }

    public SuiteContext getSuiteContext(final ITestContext iTestContext) {
        return this.getSuiteContext(null, iTestContext);
    }

    @Override
    public TestStatusController.Status getStatus() {
        if (Flags.FAILURE_CORRIDOR_ACTIVE) {
            if (FailureCorridor.isCorridorMatched()) {
                return TestStatusController.Status.PASSED;
            } else {
                return TestStatusController.Status.FAILED;
            }
        } else {
            return getStatusFromContexts(suiteContexts.stream());
        }
    }

    /**
     * Used in dashboard.vm
     */
    @Deprecated
    public long getNumberOfRepresentationalTests() {
        AtomicLong i = new AtomicLong();
        i.set(0);
        suiteContexts.forEach(suiteContext -> {
            suiteContext.readTestContexts().forEach(testContext -> {
                testContext.readClassContexts().forEach(classContext -> {
                    i.set(i.get() + classContext.getRepresentationalMethods().count());
                });
            });
        });
        return i.get();
    }

    /**
     * Used in dashboard.vm and methodsDashboard.vm
     */
    @Deprecated
    public Map<TestStatusController.Status, Integer> getMethodStats(boolean includeTestMethods, boolean includeConfigMethods) {
        Map<TestStatusController.Status, Integer> counts = new LinkedHashMap<>();

        // initialize with 0
        Arrays.stream(TestStatusController.Status.values()).forEach(status -> counts.put(status, 0));

        suiteContexts.forEach(suiteContext -> {
            suiteContext.readTestContexts().forEach(testContext -> {
                testContext.readClassContexts().forEach(classContext -> {
                    Map<TestStatusController.Status, Integer> methodStats = classContext.getMethodStats(includeTestMethods, includeConfigMethods);
                    methodStats.keySet().forEach(status -> {
                        Integer oldValue = counts.get(status);
                        int newValue = oldValue + methodStats.get(status);
                        counts.put(status, newValue);
                    });
                });
            });
        });

        return counts;
    }

    /**
     * Get method statistics for all effective classes.
     * The classes are all classContext from the context tree (without merged ones) AND mergedClassContexts from executionContext.
     *
     * @param includeTestMethods   .
     * @param includeConfigMethods .
     * @return a map
     * Use in dashboard.vm and classesStatistics.vm
     */
    @Deprecated
    public Map<ClassContext, Map> getMethodStatsPerClass(boolean includeTestMethods, boolean includeConfigMethods) {
        Map<String, ClassContext> mergedClassContexts = new HashMap<>();
        final Map<ClassContext, Map> methodStatsPerClass = new LinkedHashMap<>();
        suiteContexts.forEach(suiteContext -> {
            suiteContext.readTestContexts().forEach(testContext -> {
                testContext.readClassContexts().forEach(classContext -> {
                    Optional<TestClassContext> optionalTestClassContext = classContext.getTestClassContext();
                    if (optionalTestClassContext.isPresent()) {
                        TestClassContext testClassContext = optionalTestClassContext.get();
                        log().info("Merge class context " + classContext.getTestClass().getSimpleName() + " to " + testClassContext.name());
                        ClassContext mergedClassContext;
                        if (!mergedClassContexts.containsKey(testClassContext.name())) {
                            mergedClassContext = new ClassContext(classContext.getTestClass(), testContext);
                            mergedClassContext.setName(testClassContext.name());
                            mergedClassContexts.put(testClassContext.name(), mergedClassContext);
                        } else {
                            mergedClassContext = mergedClassContexts.get(testClassContext.name());
                        }
                        mergedClassContext.methodContexts.addAll(classContext.readMethodContexts().collect(Collectors.toList()));
                    } else {
                        Map<TestStatusController.Status, Integer> methodStats = classContext.getMethodStats(includeTestMethods, includeConfigMethods);
                        methodStatsPerClass.put(classContext, methodStats);
                    }
                });
            });
        });

        mergedClassContexts.values().forEach(classContext -> methodStatsPerClass.put(classContext, classContext.getMethodStats(includeTestMethods, includeConfigMethods)));

        /*
        sort
         */
        final AtomicReference<Integer> i1 = new AtomicReference<>();
        final AtomicReference<Integer> i2 = new AtomicReference<>();
        Comparator<? super Map> comp = (Comparator<Map>) (m1, m2) -> {
            i1.set(0);
            m1.keySet().forEach(status -> i1.set(i1.get() + ((int) m1.get(status))));

            i2.set(0);
            m2.keySet().forEach(status -> i2.set(i2.get() + ((int) m2.get(status))));

            return i2.get() - i1.get();
        };
        final Map sortedMap = methodStatsPerClass.entrySet().stream().sorted(Map.Entry.comparingByValue(comp))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return sortedMap;
    }

    public TestStatusController.Status[] getAvailableStatuses() {
        return TestStatusController.Status.values();
    }

    public TestStatusController.Status[] getAvailableStatus() {
        return TestStatusController.Status.values();
    }
}

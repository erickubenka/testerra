package eu.tsystems.mms.tic.testframework.adapters;

import eu.tsystems.mms.tic.testframework.report.TestStatusController;
import eu.tsystems.mms.tic.testframework.report.model.ContextValues;
import eu.tsystems.mms.tic.testframework.report.model.ExecStatusType;
import eu.tsystems.mms.tic.testframework.report.model.ResultStatusType;
import eu.tsystems.mms.tic.testframework.report.model.context.AbstractContext;
import eu.tsystems.mms.tic.testframework.report.model.context.ExecutionContext;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.report.utils.ExecutionContextController;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractContextExporter {

    private static final Map<TestStatusController.Status, ResultStatusType> STATUS_MAPPING = new LinkedHashMap<>();

    public AbstractContextExporter() {
        for (TestStatusController.Status status : TestStatusController.Status.values()) {
            /*
            Status
             */
            ResultStatusType resultStatusType = ResultStatusType.valueOf(status.name());

            // add to map
            STATUS_MAPPING.put(status, resultStatusType);
        }
    }

    ResultStatusType getMappedStatus(TestStatusController.Status status) {
        return STATUS_MAPPING.get(status);
    }

    /**
     * Applies a value if not null
     */
    protected <T, R> void apply(T value, Function<T, R> function) {
        if (value != null) {
            function.apply(value);
        }
    }

    /**
     * Maps and applies a value if not null
     */
    protected<T, M, R> void map(T value, Function<T, M> map, Function<M, R> function) {
        if (value != null) {
            M m = map.apply(value);
            function.apply(m);
        }
    }

    /**
     * Iterates if not null
     */
    protected <T extends Iterable<C>, C> void forEach(T value, Consumer<C> function) {
        if (value != null) {
            value.forEach(function);
        }
    }

    protected ContextValues createContextValues(AbstractContext context) {
        ContextValues.Builder builder = ContextValues.newBuilder();

        apply(context.id, builder::setId);
        apply(context.swi, builder::setSwi);
        apply(System.currentTimeMillis(), builder::setCreated);
        apply(context.name, builder::setName);
        map(context.startTime, Date::getTime, builder::setStartTime);
        map(context.endTime, Date::getTime, builder::setEndTime);

        if (context instanceof MethodContext) {
            MethodContext methodContext = (MethodContext) context;

            // result status
            map(methodContext.status, this::getMappedStatus, builder::setResultStatus);

            // exec status
            if (methodContext.status == TestStatusController.Status.NO_RUN) {
                builder.setExecStatus(ExecStatusType.RUNNING);
            } else {
                builder.setExecStatus(ExecStatusType.FINISHED);
            }
        } else if (context instanceof ExecutionContext) {
            ExecutionContext executionContext = (ExecutionContext) context;
            if (executionContext.crashed) {
                /*
                crashed state
                 */
                builder.setExecStatus(ExecStatusType.CRASHED);
            } else {
                if (TestStatusController.getTestsSkipped() == executionContext.estimatedTestMethodCount) {
                    builder.setResultStatus(ResultStatusType.SKIPPED);
                    builder.setExecStatus(ExecStatusType.VOID);
                } else if (TestStatusController.getTestsFailed() + TestStatusController.getTestsSuccessful() == 0) {
                    builder.setResultStatus(ResultStatusType.NO_RUN);
                    builder.setExecStatus(ExecStatusType.VOID);

                } else {
                    ResultStatusType resultStatusType = STATUS_MAPPING.get(executionContext.getStatus());
                    builder.setResultStatus(resultStatusType);

                    // exec status
                    if (ExecutionContextController.testRunFinished) {
                        builder.setExecStatus(ExecStatusType.FINISHED);
                    } else {
                        builder.setExecStatus(ExecStatusType.RUNNING);
                    }
                }
            }
        }
        return builder.build();
    }
}
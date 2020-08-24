package eu.tsystems.mms.tic.testframework.listener;

import eu.tsystems.mms.tic.testframework.events.ITesterraEventDataType;
import eu.tsystems.mms.tic.testframework.events.TesterraEvent;
import eu.tsystems.mms.tic.testframework.events.TesterraEventDataType;
import eu.tsystems.mms.tic.testframework.events.TesterraEventListener;
import eu.tsystems.mms.tic.testframework.events.TesterraEventType;
import eu.tsystems.mms.tic.testframework.logging.Loggable;
import eu.tsystems.mms.tic.testframework.report.events.TesterraReportEventDataTypes;
import eu.tsystems.mms.tic.testframework.report.model.ReportingData;
import eu.tsystems.mms.tic.testframework.report.model.context.MethodContext;
import eu.tsystems.mms.tic.testframework.utils.ReportUtils;
import java.util.Map;

public class GenerateReportListener implements TesterraEventListener, Loggable {

    @Override
    public void fireEvent(final TesterraEvent testerraEvent) {

        /*
         GENERATE REPORT FOR EACH METHODCONTEXT
         */
        if (testerraEvent.getTesterraEventType() == TesterraEventType.GENERATE_METHOD_REPORT) {

            try {
                final Map<ITesterraEventDataType, Object> eventData = testerraEvent.getData();
                final MethodContext methodContext = (MethodContext) eventData.get(TesterraEventDataType.CONTEXT);
                ReportUtils.createMethodDetailsStepsView(methodContext);
            } catch (Throwable e) {
                log().error("FATAL: Could not create html", e);
            }
        }

        /*
         GENERATE REPORT AT THE END OF TEST RUN
         */
        if (testerraEvent.getTesterraEventType() == TesterraEventType.GENERATE_REPORT) {
            final Map<ITesterraEventDataType, Object> eventData = testerraEvent.getData();
            ReportUtils.createReport((ReportingData) eventData.get(TesterraReportEventDataTypes.REPORT_DATA));
        }

        /*
         GENERATE MINIMAL REPORT WHEN TEST RUN ABORTED
         */
        if (testerraEvent.getTesterraEventType() == TesterraEventType.TEST_RUN_ABORT) {
            ReportUtils.generateReportEssentials();
        }

    }
}

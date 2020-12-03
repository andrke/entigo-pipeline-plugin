package io.jenkins.plugins.entigo.pipeline.argocd.service;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.entigo.pipeline.argocd.client.ArgoCDClient;
import io.jenkins.plugins.entigo.pipeline.argocd.model.*;
import io.jenkins.plugins.entigo.pipeline.argocd.process.*;
import io.jenkins.plugins.entigo.pipeline.argocd.process.Process;
import io.jenkins.plugins.entigo.pipeline.util.ListenerUtil;

import java.util.concurrent.TimeoutException;

/**
 * Author: Märt Erlenheim
 * Date: 2020-08-18
 */
public class ArgoCDService {

    private final ArgoCDClient argoCDClient;
    private final TaskListener listener;
    private final long timeout;
    private volatile TimeoutExecution<?> processExecution = null;

    public ArgoCDService(ArgoCDClient argoCDClient, TaskListener listener, long timeout) {
        this.argoCDClient = argoCDClient;
        this.listener = listener;
        this.timeout = timeout;
    }

    public Application getApplication(String applicationName, String projectName) throws AbortException,
            ProcessException {
        ListenerUtil.println(listener, String.format("Getting ArgoCD application %s, timeout: %d seconds",
                applicationName, timeout));
        GetApplicationProcess process = new GetApplicationProcess(listener, argoCDClient, applicationName, projectName);
        return (Application) getResult(process);
    }

    public void syncApplication(String applicationName) throws AbortException, ProcessException {
        ListenerUtil.println(listener, String.format("Syncing ArgoCD application %s, timeout: %d seconds",
                applicationName, timeout));
        SyncApplicationProcess process = new SyncApplicationProcess(listener, argoCDClient, applicationName,
                createSyncRequest(applicationName));
        getResult(process);
    }

    private ApplicationSyncRequest createSyncRequest(String applicationName) {
        SyncStrategy syncStrategy = new SyncStrategy();
        syncStrategy.setApply(new SyncStrategyApply(true));
        ApplicationSyncRequest syncRequest = new ApplicationSyncRequest();
        syncRequest.setName(applicationName);
        syncRequest.setPrune(true);
        syncRequest.setStrategy(syncStrategy);
        return syncRequest;
    }

    public void waitApplicationStatus(String applicationName, boolean waitFailure) throws AbortException,
            ProcessException {
        ListenerUtil.println(listener, "Waiting for application to sync, timeout: " + timeout + " seconds");
        WaitApplicationProcess process = new WaitApplicationProcess(this.listener, argoCDClient, applicationName);
        this.processExecution = new TimeoutExecution<>(this.listener, process, this.timeout);
        try {
            ProcessResult<?> waitResult = this.processExecution.run();
            waitResult.get();
        } catch (TimeoutException exception) {
            if (waitFailure) {
                throw new AbortException("Process timed out");
            } else {
                ListenerUtil.println(listener, "waitFailure was False, continuing build");
            }
        }
    }

    public void deleteApplication(String applicationName, boolean cascade) throws AbortException, ProcessException {
        ListenerUtil.println(listener, String.format("Deleting ArgoCD application %s, cascade: %s, timeout: %d seconds",
                applicationName, cascade, timeout));
        DeleteApplicationProcess process = new DeleteApplicationProcess(listener, argoCDClient, applicationName,
                cascade);
        getResult(process);
    }

    private <T> Object getResult(Process<T> process) throws AbortException, ProcessException {
        this.processExecution = new TimeoutExecution<>(this.listener, process, this.timeout);
        try {
            ProcessResult<?> result = this.processExecution.run();
            return result.get();
        } catch (TimeoutException exception) {
            throw new AbortException("Process timed out");
        }
    }

    public void stop() {
        if (this.processExecution != null) {
            this.processExecution.stop();
        }
    }

}

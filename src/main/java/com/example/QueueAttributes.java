package com.example;

/**
 * Created by sipham on 15/12/16.
 */
public class QueueAttributes {

    private String queueName; // [a-zA-Z-_]{1,75}.fifo
    private String queueUrl; // url
    private int delaySeconds; // [0..900]
    private int visibilityTimeoutSeconds; // [0..43200]
    private int redrivePolicyMaxRecieveCount; // [1..1000]
    private String redrivePolicyDeadLetterTargetUrl; // url

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public int getVisibilityTimeoutSeconds() {
        return visibilityTimeoutSeconds;
    }

    public void setVisibilityTimeoutSeconds(int visibilityTimeoutSeconds) {
        this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
    }

    public int getRedrivePolicyMaxRecieveCount() {
        return redrivePolicyMaxRecieveCount;
    }

    public void setRedrivePolicyMaxRecieveCount(int redrivePolicyMaxRecieveCount) {
        this.redrivePolicyMaxRecieveCount = redrivePolicyMaxRecieveCount;
    }

    public String getRedrivePolicyDeadLetterTargetUrl() {
        return redrivePolicyDeadLetterTargetUrl;
    }

    public void setRedrivePolicyDeadLetterTargetUrl(String redrivePolicyDeadLetterTargetUrl) {
        this.redrivePolicyDeadLetterTargetUrl = redrivePolicyDeadLetterTargetUrl;
    }
}

package com.ibm.watsonwork.model;

import lombok.Data;

@Data
public class PersistObject {

    private String messageId;
    private WebhookEvent webhookEvent;
    private AnnotationPayload annotationPayload;
}

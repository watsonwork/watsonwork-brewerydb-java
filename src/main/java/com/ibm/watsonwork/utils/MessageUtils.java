package com.ibm.watsonwork.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.watsonwork.MessageTypes;
import com.ibm.watsonwork.model.Annotation;
import com.ibm.watsonwork.model.AnnotationPayload;
import com.ibm.watsonwork.model.Message;
import com.ibm.watsonwork.model.Persist;
import com.ibm.watsonwork.model.PersistObject;
import com.ibm.watsonwork.model.WebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ibm.watsonwork.MessageTypes.ACTION_SELECTED;
import static com.ibm.watsonwork.MessageTypes.FOCUS_ANNOTATION;

public class MessageUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtils.class);


    public static Message buildMessage(String messageTitle, String messageText) {
        Annotation annotation = new Annotation();
        annotation.setType(MessageTypes.GENERIC_ANNOTATION);
        annotation.setVersion(1.0);
        annotation.setColor("#1DB954");
        annotation.setTitle(messageTitle);
        annotation.setText(messageText);

//        Actor actor = new Actor();
//        actor.setUrl("");
//        actor.setAvatar("");
//        actor.setName("");
//        annotation.setActor(actor);

        Message message = new Message();
        message.setType(MessageTypes.APP_MESSAGE);
        message.setVersion(1.0);
        message.setAnnotations(Collections.singletonList(annotation));

        return message;
    }

    public static AnnotationPayload mapAnnotationPayload(WebhookEvent webhookEvent){
        String annotationPayload = webhookEvent.getAnnotationPayload();
        AnnotationPayload payload = null;
        try {
            payload = objectMapper.readValue(annotationPayload, AnnotationPayload.class);
        } catch (IOException e) {
            LOGGER.error("Failed to read the annotation", e);
        }
        return payload;
    }

    public static Persist storeObject(WebhookEvent webhookEvent, Persist persist) {

        AnnotationPayload annotationPayload = MessageUtils.mapAnnotationPayload(webhookEvent);

        ArrayList<PersistObject> persistObjects = persist.getPersistObjects();
        if (persistObjects==null) {
            persistObjects = new ArrayList<>();
        }

        PersistObject persistObject = new PersistObject();
        String messageId = null;
        if (FOCUS_ANNOTATION.equalsIgnoreCase(webhookEvent.getAnnotationType())) {
            messageId = webhookEvent.getMessageId();
        }

        persistObject.setWebhookEvent(webhookEvent);
        persistObject.setMessageId(messageId);
        persistObject.setAnnotationPayload(annotationPayload);
        persistObjects.add(persistObject);
        persist.setPersistObjects(persistObjects);
        return persist;
    }

    public static PersistObject retreiveObject(WebhookEvent webhookEvent, Persist persist) throws NullPointerException{
        AnnotationPayload annotationPayload = MessageUtils.mapAnnotationPayload(webhookEvent);
        ArrayList<PersistObject> persistObjects = persist.getPersistObjects();

        for (PersistObject checkPersistObject : persistObjects) {
            if (ACTION_SELECTED.equalsIgnoreCase(webhookEvent.getAnnotationType())) {
                if (checkPersistObject.getMessageId().equals(annotationPayload.getReferralMessageId())) {
                    return checkPersistObject;
                }
            }
        }

        return new PersistObject();

    }

}

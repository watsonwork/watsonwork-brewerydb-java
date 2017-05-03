package com.ibm.watsonwork.controller;

import com.ibm.watsonwork.WatsonWorkProperties;
import com.ibm.watsonwork.model.AnnotationPayload;
import com.ibm.watsonwork.model.Persist;
import com.ibm.watsonwork.model.PersistObject;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.service.BreweryDBService;
import com.ibm.watsonwork.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.ibm.watsonwork.MessageTypes.ACTION_SELECTED;
import static com.ibm.watsonwork.MessageTypes.FOCUS_ANNOTATION;
import static com.ibm.watsonwork.MessageTypes.MESSAGE_ANNOTATION_ADDED;
import static com.ibm.watsonwork.MessageTypes.VERIFICATION;
import static com.ibm.watsonwork.WatsonWorkConstants.X_OUTBOUND_TOKEN;

@RestController
public class WatsonWorkController {

    @Autowired
    private WatsonWorkProperties watsonWorkProperties;

    @Autowired
    private BreweryDBService breweryDBService;

    @Autowired
    private AuthService authService;

    @Autowired
    private Persist persist;

    private static final Logger LOGGER = LoggerFactory.getLogger(WatsonWorkController.class);


    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity home(){
        String responseBody = "Brewing the action fulfillment app.";
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }


    @RequestMapping(value = "webhook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity webhookCallback(@RequestHeader(X_OUTBOUND_TOKEN) String outboundToken, @RequestBody WebhookEvent webhookEvent){

        // Verify the challenge to the webhook
        if(VERIFICATION.equalsIgnoreCase(webhookEvent.getType()) && authService.isValidVerificationRequest(webhookEvent, outboundToken)) {
            return buildVerificationResponse(webhookEvent);
        }

        // Get the action in the annotationPayload of message-annotation-added.
        if(!watsonWorkProperties.getAppId().equals(webhookEvent.getUserId())
                && MESSAGE_ANNOTATION_ADDED.equalsIgnoreCase(webhookEvent.getType())
                && FOCUS_ANNOTATION.equalsIgnoreCase(webhookEvent.getAnnotationType())) {
            persist = MessageUtils.storeObject(webhookEvent,persist);
        }

        // Process the message annotation added for actionSelected
        if(!watsonWorkProperties.getAppId().equals(webhookEvent.getUserId())
                && MESSAGE_ANNOTATION_ADDED.equalsIgnoreCase(webhookEvent.getType())
                && ACTION_SELECTED.equalsIgnoreCase(webhookEvent.getAnnotationType())) {

            PersistObject persistObject;
            try {
                persistObject = MessageUtils.retreiveObject(webhookEvent, persist);
            } catch (NullPointerException e){
                return ResponseEntity.ok().build();
            }

            LOGGER.info("Message id [{}]", webhookEvent.getMessageId());
            LOGGER.info("Returned  [{}]", persistObject.getMessageId());

            AnnotationPayload payload = MessageUtils.mapAnnotationPayload(webhookEvent);
            AnnotationPayload payloadMessageFocus = MessageUtils.mapAnnotationPayload(persistObject.getWebhookEvent());


            if (payload.getTargetDialogId().equals(payload.getActionId())) {
                // Make a brewerydb call and return a targetedResponse to respond as a targeted Message to the user.
                breweryDBService.findBreweries(webhookEvent, payloadMessageFocus);
            } else {
                if (payload.getActionId().contains("Share")) {
                    breweryDBService.shareBrewery(webhookEvent);
                } else {
                    breweryDBService.getBrewery(webhookEvent);
                }
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private ResponseEntity buildVerificationResponse(WebhookEvent webhookEvent) {
        String responseBody = String.format("{\"response\": \"%s\"}", webhookEvent.getChallenge());

        String verificationHeader = authService.createVerificationHeader(responseBody);
        LOGGER.info("Building verification response [{}]",responseBody);
        return ResponseEntity.status(HttpStatus.OK)
                .header(X_OUTBOUND_TOKEN, verificationHeader)
                .body(responseBody);
    }

}

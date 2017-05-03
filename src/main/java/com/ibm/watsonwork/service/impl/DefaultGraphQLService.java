package com.ibm.watsonwork.service.impl;

import java.util.List;

import com.ibm.watsonwork.client.GraphQLClient;
import com.ibm.watsonwork.model.Annotation;
import com.ibm.watsonwork.model.Button;
import com.ibm.watsonwork.model.GraphQLQuery;
import com.ibm.watsonwork.model.GraphQLResponse;
import com.ibm.watsonwork.model.TargetedMessage;
import com.ibm.watsonwork.model.WebhookEvent;
import com.ibm.watsonwork.service.AuthService;
import com.ibm.watsonwork.service.GraphQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Service
public class DefaultGraphQLService implements GraphQLService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGraphQLService.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private GraphQLClient graphQLClient;

    @Override
    public void createMessage(WebhookEvent webhookEvent, TargetedMessage targetedMessage) {
        String body = "mutation {" +
                "createMessage(input: {conversationId: \"" + targetedMessage.getConversationId() + "\"," +
                "annotations: [{genericAnnotation: {title:\"" + targetedMessage.getAnnotation().getTitle() + "\",text:\"" + targetedMessage.getAnnotation().getText() + "\"" +
                "}}]}) {" +
                "message {id}" +
                "}}";

        GraphQLQuery graphqlQuery = new GraphQLQuery();
        graphqlQuery.setQuery(body);

        Call<GraphQLResponse> call = graphQLClient.createMessage(authService.getAppAuthToken(), graphqlQuery);

        call.enqueue(new Callback<GraphQLResponse>() {
            @Override
            public void onResponse(Call<GraphQLResponse> call, Response<GraphQLResponse> response) {
                LOGGER.info("GraphQL call createTargetedMessage successful.");
            }

            @Override
            public void onFailure(Call<GraphQLResponse> call, Throwable t) {
                LOGGER.error("GraphQL call createTargetedMessage failed.", t);
            }
        });

    }

    @Override
    public void createTargetedMessage(WebhookEvent webhookEvent, TargetedMessage targetedMessage) {
        Annotation annotation = targetedMessage.getAnnotation();
        List<Button> buttons = annotation.getButtons();

        StringBuilder buttonString = new StringBuilder();
        if (buttons != null) {
            buttonString = new StringBuilder(",buttons:[");
            for (Button button : buttons) {
                buttonString.append("{ postbackButton: {id:\"").append(button.getId()).append("\",title:\"").append(button.getTitle()).append("\",style:PRIMARY} }");
            }
            buttonString.append("]");
        }

        System.out.println("targetedMessage=" + targetedMessage.toString());
        String body = "mutation {" +
                "createTargetedMessage(input: {conversationId: \"" + targetedMessage.getConversationId() + "\"," +
                "targetDialogId: \"" + targetedMessage.getTargetDialogId() + "\"," +
                "targetUserId: \"" + targetedMessage.getTargetUserId() + "\"," +
                "annotations: [{genericAnnotation: {title:\"" + targetedMessage.getAnnotation().getTitle() + "\",text:\"" + targetedMessage.getAnnotation().getText() + "\"" +
                buttonString +
                "}}]}) {" +
                "successful" +
                "}}";

        GraphQLQuery graphqlQuery = new GraphQLQuery();
        graphqlQuery.setQuery(body);
        LOGGER.info("final mutation: " + graphqlQuery.getQuery());

        Call<GraphQLResponse> call = graphQLClient.createTargetedMessage(authService.getAppAuthToken(), graphqlQuery);

        call.enqueue(new Callback<GraphQLResponse>() {
            @Override
            public void onResponse(Call<GraphQLResponse> call, Response<GraphQLResponse> response) {
                LOGGER.info("GraphQL call createTargetedMessage successful.");
            }

            @Override
            public void onFailure(Call<GraphQLResponse> call, Throwable t) {
                LOGGER.error("GraphQL call createTargetedMessage failed.", t);
            }
        });

    }

}

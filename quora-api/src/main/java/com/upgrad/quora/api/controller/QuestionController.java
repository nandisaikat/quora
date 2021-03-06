package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController
{

    @Autowired
    private QuestionBusinessService questionBusinessService;

    @Autowired
    private UserBusinessService userBusinessService;

    @Autowired
    private QuestionDao questionDao;

    //Method to create questions in the application and uses RequestMethod.POST
    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        UserAuthEntity user = userBusinessService.getUserByToken(authorization);
        final QuestionEntity questionEntity = new QuestionEntity();

        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setUserId(user.getUserId());
        final QuestionEntity createdQuestionEntity = questionBusinessService.create(questionEntity);
        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    //Method to view all question based on the request from the API and uses RequestMethod.GET
    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException  {
        UserAuthEntity user = userBusinessService.getUserByToken(authorization);

        List<QuestionEntity> questionEntities= questionBusinessService.getAllQuestions(authorization);
        return getListResponseEntity(questionEntities);
    }
    //Method to Delete question based on the request from the API and uses RequestMethod.DELETE
    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@RequestHeader("authorization") final String authorization,@PathVariable("questionId") final String questionId) throws AuthorizationFailedException, InvalidQuestionException   {

        questionBusinessService.deleteQuestion(questionId, authorization);
        QuestionDeleteResponse response = new QuestionDeleteResponse().id(questionId).status("QUESTION DELETED");

        return new ResponseEntity<QuestionDeleteResponse>(response, HttpStatus.OK);
    }

    //Method to Edit question based on the request from the API and uses RequestMethod.PUT
    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@RequestHeader("authorization") final String authorization, @PathVariable("questionId") final String questionId,final QuestionEditRequest questionEditRequest) throws AuthorizationFailedException, InvalidQuestionException {

        questionBusinessService.editQuestion(questionId, questionEditRequest.getContent(), authorization);
        QuestionEditResponse response = new QuestionEditResponse().id(questionId).status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(response, HttpStatus.OK);
    }

    //Method to Get question based on the request from the API for specific userid and uses RequestMethod.GET
    @RequestMapping(method = RequestMethod.GET, path = "/question/all/{userId}")
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@RequestHeader("authorization") final String authorization, @PathVariable("userId") final String userId ) throws AuthorizationFailedException, UserNotFoundException {

        UserEntity userEntity = userBusinessService.getUserById(userId);
        userBusinessService.getUserByToken(authorization);

        List<QuestionEntity> allQuestions = questionBusinessService.getAllQuestionsByUser(userEntity, authorization);

        return getListResponseEntity(allQuestions);

    }

    // Method reads the list of questionentity through loop.
    private ResponseEntity<List<QuestionDetailsResponse>> getListResponseEntity(List<QuestionEntity> allQuestions) {
        List<QuestionDetailsResponse> questionDetailsResponses=new ArrayList<>();
        for (QuestionEntity questionEntity : allQuestions) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.setId(questionEntity.getUuid());
            questionDetailsResponse.setContent(questionEntity.getContent());
            questionDetailsResponses.add(questionDetailsResponse);
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(questionDetailsResponses, HttpStatus.OK);
    }

}

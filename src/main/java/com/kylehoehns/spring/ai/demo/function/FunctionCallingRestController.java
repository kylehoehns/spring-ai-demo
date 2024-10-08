package com.kylehoehns.spring.ai.demo.function;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/function")
public class FunctionCallingRestController {

    private final ChatModel chatModel;

    @PostMapping
    public String askSportsQuestion(@RequestBody SportsQuestionRequest request) {

        String systemMessageText = """
            You are an expert college football analyst answering questions about college football.
            If the question provided is not about college football, simply state that you don't know.
        """;

        UserMessage userMessage = new UserMessage(request.question());
        SystemMessage systemMessage = new SystemMessage(systemMessageText);
        Prompt prompt = new Prompt(
                List.of(userMessage, systemMessage),
                OpenAiChatOptions.builder().withFunctions(Set.of("bestTeam", "liveScore")).build()
        );

        var response = chatModel.call(prompt);
        return response.getResult().getOutput().getContent();
    }

    public record SportsQuestionRequest(String question) {}

}

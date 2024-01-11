package com.kylehoehns.spring.ai.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.prompt.Prompt;
import org.springframework.ai.prompt.SystemPromptTemplate;
import org.springframework.ai.prompt.messages.Message;
import org.springframework.ai.prompt.messages.UserMessage;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@Slf4j
public class StatRestController {

  private final ChatClient chatClient;
  private final ResourceLoader resourceLoader;
  private final SimpleVectorStore simpleVectorStore;

  @Value("classpath:/vector-storage/vector-store.json")
  private Resource vectorStoreResource;


  @PostMapping("/embeddings")
  void generateEmbeddings() throws IOException {
    // load all our markdown files
    Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
        .getResources("classpath:/documents/*.md");

    // turn them into spring ai "documents"
    List<Document> documents = new ArrayList<>();
    for (var resource : resources) {
      TextReader textReader = new TextReader(resource);
      documents.addAll(textReader.get());
    }

    // add these to our vector store
    simpleVectorStore.add(documents);


    // persist to file
    simpleVectorStore.save(vectorStoreResource.getFile());
  }

  @PostMapping
  StatResponse getStats(@RequestBody StatRequest request) {
    log.info("\nRequest\n {}", request);

    UserMessage userMessage = new UserMessage(request.question());

    var outputParser = new BeanOutputParser<>(StatResponse.class);

    List<Document> similarDocuments = simpleVectorStore.similaritySearch(request.question());

    String documentText = similarDocuments.stream()
        .map(Document::getContent)
        .collect(Collectors.joining("\n"));

    String systemMessageText = """
      You are an expert {sport} analyst answering questions about {sport}.
      If the question provided is not about {sport}, simply state that you don't know.
      Please utilize the information in the documents section to answer any questions.
      
      Documents:
      {documents}
      
      {format}
    """;
    SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemMessageText);
    Message systemMessage = systemPromptTemplate.createMessage(
        Map.of(
            "sport", request.sport(),
            "format", outputParser.getFormat(),
            "documents", documentText
        )
    );

    Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

    log.info("\nPrompt\n {}", prompt);

    ChatResponse chatResponse = chatClient.generate(prompt);

    log.info("Total Tokens {}", chatResponse.getGenerationMetadata().getUsage().getTotalTokens());

    String response = chatResponse.getGeneration().getContent();

    log.info("\nResponse\n {}", response);
    return outputParser.parse(response);
  }


  record StatRequest(String question, String sport) {}

  record StatResponse(String teamName, List<StatItem> item){}

  record StatItem(String statName, int year, float value, String playerName){}
}

# Spring AI Example

This is a Spring Boot web application that uses [Spring AI](https://docs.spring.io/spring-ai/reference/) to talk with OpenAi models. It utilizes [retrieval augmented generation](https://ai.meta.com/blog/retrieval-augmented-generation-streamlining-the-creation-of-intelligent-natural-language-processing-models/) to answer questions about the [Iowa Hawkeyes football team for the 2023-24 season](https://hawkeyesports.com/sports/football/cumestats/season/2023-24/).

This repository was used as reference during a talk for the [Central Iowa Java Users Group](https://www.meetup.com/central-iowa-java-users-group/) in [January 2024](https://www.meetup.com/central-iowa-java-users-group/events/298188550/). Slides for the talk can be found in this repository at [Spring AI.pdf](Spring%20AI.pdf)

## Setup

* Create an [OpenAI](https://openai.com/) account and [get an API key](https://platform.openai.com/api-keys).
* Set up [billing settings](https://platform.openai.com/account/billing/overview) for your account with a small spending limit.
* Create an environment variable named `SPRING_AI_OPENAI_API_KEY` that is set to your OpenAI API key, or populate the key value in ['application.yaml`](/src/main/resources/application.yaml).
* Install Java 21 or later.
* Run `./gradlew bootRun` to start the application.

## Populating Vector Storage
* Once the application is running, you can populate the vector storage by running `curl -X POST http://localhost:8080/stats/embeddings`.
* This will create embeddings for all the data in `src/main/resources/documents` and store them in the vector storage file called `vector-storage.json` in your project's `target/classes/vector-storage` directory, so you only have to generate them once.

## Using the API
* Once the vector storage is populated, you can use the API to get predictions from OpenAi using the embeddings in the vector storage.
* Run the following to get a response about the number of passing touchdowns.

```shell
curl --request POST \
  --url 'http://localhost:8080/stats' \
  --header 'Content-Type: application/json' \
  --data '{
     "question": "Who were the players with the most passing yards for Iowa in 2023?",
     "sport": "college football"
    }'
```

You'll receive a response like this:
```json
{
  "teamName": "Iowa",
  "item": [
    {
      "statName": "Passing Yards",
      "year": 2023,
      "value": 1096.0,
      "playerName": "Deacon Hill"
    },
    {
      "statName": "Passing Yards",
      "year": 2023,
      "value": 505.0,
      "playerName": "Cade McNamara"
    }
  ]
}
```

## Tech Stack

* Spring Boot 3.3
* Spring Boot Starter Web
* Spring AI
  * With Spring Boot Spring AI OpenAI Starter
* Java 21
* Lombok
* Gradle

## Topics

- [ ] Tech Stack
- [ ] Simple Interactions
- [ ] Prompts and Templates
- [ ] Output Parsing
- [ ] RAG
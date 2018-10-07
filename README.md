# ALVA ~ Asynchronous Link Visitor Application

## Description

This Spring Boot application visits a specified URI and extracts from its HTML content any hyperlinks that comply with certain criteria.
Each found link is logged and its occurrence counted. The procedure is applied recursively to each found link.

URL Filtering Criteria

|URI type|logged & counted (y/n)|recursive examination (y/n)|
|---|---|---|
|anchors containing `rel="nofollow"` attribute|n|n|
|internal anchors|n|n|
|URIs which do not return valid HTML content|y|n|
|URIs with fragments*|y|y|

\* URIs with fragments are reduced to their simple path and then logged and counted

## How To Run Locally

1. Check out via git
0. build via mvn clean install
0. run the application locally  
(either by executing `com.example.alva.AlvaApplication` in your IDE, or by executing `mvn spring-boot:run`)
0. without further modification, your instance should be available at `localhost:8080`

## How To Use

### Start A LinkVisit

In order to start a link visit use following convenience path:  
`http://localhost:8080/new?url=<YOUR_URL>`  
, while <YOUR_URL> is the URL you would like to visit.

This is a convenience mapping which calls the REST API under `POST localhost:8080/api/v1/visitors` where `url` is an expected POST parameter.

### Check Your LinkVisit's Status

As a response to your call to `http://localhost:8080/new?url=<YOUR_URL>` you should receive a JSON which tells you about the newly created process (similar to this).
```
{
  "process_id" : "f34925fb-7548-4c9b-91db-216e02ebe0ac",
  "process_status" : "ACTIVE",
  "update_link" : "http://localhost:8080/api/v1/visitors/f34925fb-7548-4c9b-91db-216e02ebe0ac",
  "result_link" : "http://localhost:8080/api/v1/visitors/f34925fb-7548-4c9b-91db-216e02ebe0ac/result",
  "base_uri" : "<YOUR_URL>"
}
```

It points you to the associated `update_link` and `result_link`.
The `update_link` can be used to check the `process_status`; the `result_link` will contain the end result but *only if the `process_status` is set to "DONE"*.

So, refresh your `update_link` until the `process_status` changes.

### Retrieving Your LinkVisit's Result

After the `process_status` changes to "DONE", visit the `result_link` and you will receive a JSON response containing found links and their associated count.
```
{
  "numberOfUniqueURIs" : 2695,
  "process_id" : "f34925fb-7548-4c9b-91db-216e02ebe0ac",
  "visited_urls" : {
    <A_URL> : 1,
    <B_URL> : 325,
    <C_URL> : 231,
    <D_URL> : 1,
    <E_URL> : 34,
    <F_URL> : 1,
    <G_URL> : 5,
    <H_URL> : 8,
    <I_URL> : 87,
    <J_URL> : 1,
    ...
  }
}
```

### Retention Policy

The current implementation holds all data in-memory; an entity will be deleted after 1 (one) hour or when the server restarts.

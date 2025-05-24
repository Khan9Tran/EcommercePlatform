package com.hkteam.ecommerce_platform.configuration;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.host:localhost}")  // default localhost nếu biến env không có
    private String esHost;

    @Value("${spring.data.elasticsearch.port:9200}")       // default 9200 nếu biến env không có
    private int esPort;

    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(esHost, esPort, "http")).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        ObjectMapper mapper =
                JsonMapper.builder().addModule(new JavaTimeModule()).build();
        RestClientTransport transport = new RestClientTransport(restClient(), new JacksonJsonpMapper(mapper));
        return new ElasticsearchClient(transport);
    }
}

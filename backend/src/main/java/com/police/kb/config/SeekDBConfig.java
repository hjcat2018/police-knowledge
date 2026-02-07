package com.police.kb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OceanBase SeekDB配置类
 * <p>
 * 配置SeekDB向量数据库的连接参数和AI模型参数。
 * 用于文档向量的存储和检索，支持RAG检索增强生成功能。
 * </p>
 *
 * @author 黄俊
 * @date 2026-01-23 12:00:00
 */
@Configuration
@ConfigurationProperties(prefix = "seekdb")
public class SeekDBConfig {

  private String host = "localhost";

  private int port = 2881;

  private String username = "root@test";

  private String password = "root123";

  private String database = "police_kb";

  private String collection = "document_vectors";

  private int dimension = 1024;

  private String metric = "COSINE";

  private int efConstruction = 200;

  private int efSearch = 100;

  private int m = 16;

  private boolean hybridSearchEnabled = true;

  private float vectorWeight = 0.7f;

  private float fulltextWeight = 0.3f;

  private double hybridScoreThreshold = 0.5;

  private AiConfig ai = new AiConfig();

  private RagConfig rag = new RagConfig();

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public int getDimension() {
    return dimension;
  }

  public void setDimension(int dimension) {
    this.dimension = dimension;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public int getEfConstruction() {
    return efConstruction;
  }

  public void setEfConstruction(int efConstruction) {
    this.efConstruction = efConstruction;
  }

  public int getEfSearch() {
    return efSearch;
  }

  public void setEfSearch(int efSearch) {
    this.efSearch = efSearch;
  }

  public int getM() {
    return m;
  }

  public void setM(int m) {
    this.m = m;
  }

  public boolean isHybridSearchEnabled() {
    return hybridSearchEnabled;
  }

  public void setHybridSearchEnabled(boolean hybridSearchEnabled) {
    this.hybridSearchEnabled = hybridSearchEnabled;
  }

  public float getVectorWeight() {
    return vectorWeight;
  }

  public void setVectorWeight(float vectorWeight) {
    this.vectorWeight = vectorWeight;
  }

  public float getFulltextWeight() {
    return fulltextWeight;
  }

  public void setFulltextWeight(float fulltextWeight) {
    this.fulltextWeight = fulltextWeight;
  }

  public double getHybridScoreThreshold() {
    return hybridScoreThreshold;
  }

  public void setHybridScoreThreshold(double hybridScoreThreshold) {
    this.hybridScoreThreshold = hybridScoreThreshold;
  }

  public AiConfig getAi() {
    return ai;
  }

  public void setAi(AiConfig ai) {
    this.ai = ai;
  }

  public RagConfig getRag() {
    return rag;
  }

  public void setRag(RagConfig rag) {
    this.rag = rag;
  }

  public static class AiConfig {
    private String embedModel = "ob_embed";

    private String embedEndpoint = "ob_embed_endpoint";

    private String provider = "siliconflow";

    private String apiUrl = "https://api.siliconflow.cn/v1/embeddings";

    private String apiKey = "your-api-key";

    private String modelName = "BAAI/bge-m3";

    private String rerankModel = "BAAI/bge-reranker-base";

    private String rerankApiUrl = "https://api.siliconflow.cn/v1/rerank";

    private String rerankApiKey;

    public String getEmbedModel() {
      return embedModel;
    }

    public void setEmbedModel(String embedModel) {
      this.embedModel = embedModel;
    }

    public String getEmbedEndpoint() {
      return embedEndpoint;
    }

    public void setEmbedEndpoint(String embedEndpoint) {
      this.embedEndpoint = embedEndpoint;
    }

    public String getProvider() {
      return provider;
    }

    public void setProvider(String provider) {
      this.provider = provider;
    }

    public String getApiUrl() {
      return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
      this.apiUrl = apiUrl;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public String getModelName() {
      return modelName;
    }

    public void setModelName(String modelName) {
      this.modelName = modelName;
    }

    public String getRerankModel() {
      return rerankModel;
    }

    public void setRerankModel(String rerankModel) {
      this.rerankModel = rerankModel;
    }

    public String getRerankApiUrl() {
      return rerankApiUrl;
    }

    public void setRerankApiUrl(String rerankApiUrl) {
      this.rerankApiUrl = rerankApiUrl;
    }

    public String getRerankApiKey() {
      return rerankApiKey;
    }

    public void setRerankApiKey(String rerankApiKey) {
      this.rerankApiKey = rerankApiKey;
    }
  }

  public static class RagConfig {
    private int topK = 20;

    private double similarityThreshold = 0.3;

    private boolean enableReranker = false;

    private int rerankCandidateSize = 20;

    private boolean enableHybridSearch = true;

    private int maxContextTokens = 4000;

    private boolean enableContextCompaction = true;

    private String chunkingStrategy = "smart";

    private int chunkSize = 512;

    private int chunkOverlap = 150;

    private int minChunkSize = 100;

    private int maxChunkSize = 1000;

    private int maxTokens = 8000;

    private double contextTriggerThreshold = 0.8;

    public int getTopK() {
      return topK;
    }

    public void setTopK(int topK) {
      this.topK = topK;
    }

    public double getSimilarityThreshold() {
      return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
      this.similarityThreshold = similarityThreshold;
    }

    public boolean isEnableReranker() {
      return enableReranker;
    }

    public void setEnableReranker(boolean enableReranker) {
      this.enableReranker = enableReranker;
    }

    public int getRerankCandidateSize() {
      return rerankCandidateSize;
    }

    public void setRerankCandidateSize(int rerankCandidateSize) {
      this.rerankCandidateSize = rerankCandidateSize;
    }

    public boolean isEnableHybridSearch() {
      return enableHybridSearch;
    }

    public void setEnableHybridSearch(boolean enableHybridSearch) {
      this.enableHybridSearch = enableHybridSearch;
    }

    public int getMaxContextTokens() {
      return maxContextTokens;
    }

    public void setMaxContextTokens(int maxContextTokens) {
      this.maxContextTokens = maxContextTokens;
    }

    public boolean isEnableContextCompaction() {
      return enableContextCompaction;
    }

    public void setEnableContextCompaction(boolean enableContextCompaction) {
      this.enableContextCompaction = enableContextCompaction;
    }

    public String getChunkingStrategy() {
      return chunkingStrategy;
    }

    public void setChunkingStrategy(String chunkingStrategy) {
      this.chunkingStrategy = chunkingStrategy;
    }

    public int getChunkSize() {
      return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
      this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
      return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
      this.chunkOverlap = chunkOverlap;
    }

    public int getMinChunkSize() {
      return minChunkSize;
    }

    public void setMinChunkSize(int minChunkSize) {
      this.minChunkSize = minChunkSize;
    }

    public int getMaxChunkSize() {
      return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
      this.maxChunkSize = maxChunkSize;
    }

    public int getMaxTokens() {
      return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
      this.maxTokens = maxTokens;
    }

    public double getContextTriggerThreshold() {
      return contextTriggerThreshold;
    }

    public void setContextTriggerThreshold(double contextTriggerThreshold) {
      this.contextTriggerThreshold = contextTriggerThreshold;
    }
  }
}

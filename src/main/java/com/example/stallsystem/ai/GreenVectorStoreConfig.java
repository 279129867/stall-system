package com.example.stallsystem.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 绿色版专用：内置一个纯内存的“本地知识库”，替代 Chroma。
 *
 * 背景：
 *   spring-ai-chroma 的自动配置在容器启动时会去连 localhost:8000，
 *   绿色版不装 Chroma，就会直接启动失败。
 *
 * 做法：
 *   在 config/application.yml 里关掉 Chroma 自动配置（spring.autoconfigure.exclude），
 *   并把 app.ai.local-vector-store 设为 true，由本类提供 VectorStore Bean。
 *
 * 效果：
 *   - AI 对话（DeepSeek）照常可用；
 *   - 政策/办证类问答的“参考知识”改为本地关键词检索，完全离线、不需要向量模型；
 *   - 开发环境（有 Chroma）不加这些配置时，本类不会生效，行为与原来一致。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.ai.local-vector-store", havingValue = "true")
public class GreenVectorStoreConfig {

    @Bean
    @Primary
    public VectorStore localVectorStore() {
        log.info("=== 使用内置本地知识库（无需 Chroma / 无需向量模型） ===");
        return new KeywordVectorStore();
    }

    /**
     * 极简知识库：文档全部放在内存，检索用“字符二元组重合度”打分。
     * 中文没有空格，按 2 字滑窗切分比按空格分词靠谱，且零依赖、零网络。
     */
    static class KeywordVectorStore implements VectorStore {

        private final Map<String, Document> store = new LinkedHashMap<>();
        private long seq = 0;

        @Override
        public void add(List<Document> documents) {
            if (documents == null) {
                return;
            }
            for (Document doc : documents) {
                store.put("doc-" + (seq++), doc);
            }
        }

        @Override
        public Optional<Boolean> delete(List<String> idList) {
            if (idList != null) {
                idList.forEach(store::remove);
            }
            return Optional.of(true);
        }

        @Override
        public List<Document> similaritySearch(SearchRequest request) {
            String query = request == null ? null : request.getQuery();
            if (query == null || query.isBlank() || store.isEmpty()) {
                return new ArrayList<>();
            }

            int topK = request.getTopK() > 0 ? request.getTopK() : 3;
            List<String> grams = bigrams(query);

            List<Scored> hits = new ArrayList<>();
            for (Document doc : store.values()) {
                double score = score(grams, doc.getContent());
                if (score > 0) {
                    hits.add(new Scored(doc, score));
                }
            }
            hits.sort(Comparator.comparingDouble(Scored::score).reversed());

            List<Document> result = new ArrayList<>();
            for (Scored hit : hits) {
                if (result.size() >= topK) {
                    break;
                }
                result.add(hit.doc());
            }
            return result;
        }

        private static List<String> bigrams(String text) {
            String t = text.replaceAll("\\s+", "");
            List<String> grams = new ArrayList<>();
            for (int i = 0; i + 1 < t.length(); i++) {
                grams.add(t.substring(i, i + 2));
            }
            if (grams.isEmpty() && !t.isEmpty()) {
                grams.add(t);
            }
            return grams;
        }

        private static double score(List<String> grams, String content) {
            if (content == null || content.isEmpty() || grams.isEmpty()) {
                return 0;
            }
            int hit = 0;
            for (String g : grams) {
                if (content.contains(g)) {
                    hit++;
                }
            }
            return (double) hit / grams.size();
        }

        record Scored(Document doc, double score) {
        }
    }
}

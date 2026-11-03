package com.example.stallsystem.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 知识初始化器：应用启动时把本地摆摊知识录入 Chroma 向量库
 *
 * 只在向量库为空时执行一次，避免重复插入。
 */
@Slf4j
@Component
public class KnowledgeLoader implements CommandLineRunner {

    private final VectorStore vectorStore;

    public KnowledgeLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {
        // 预设的本地知识库（摆摊管理系统相关）
        List<Document> documents = List.of(
            new Document(
                "摆摊办证流程：1. 先到辖区街道办或城管部门提交摆摊申请；2. 提交身份证复印件、健康证复印件、摊位位置选择意向；3. 工作人员现场勘查确认位置可用；4. 缴纳摊位管理费后领取临时摆摊许可证；5. 许可证有效期一般为3-6个月，到期前15天可申请续期。",
                Map.of("category", "办证流程", "tag", "license")
            ),
            new Document(
                "摊位租赁费用标准：根据摊位位置和面积不同，收费标准如下：主街临街摊位 30元/天，次街摊位 20元/天，夜市集中区 25元/天。月租可享受8折优惠，季租7折优惠。费用按周或按月缴纳，逾期3天未缴纳将收回摊位使用权。",
                Map.of("category", "收费标准", "tag", "fee")
            ),
            new Document(
                "摆摊卫生要求：1. 摊主必须自备垃圾桶，随时保持摊位周围3米内干净；2. 餐饮类摊主必须配备隔油桶，不得将油污直接倒入下水道；3. 每日收摊后必须清理个人摊位及周边垃圾，否则扣除卫生押金50元；4. 连续三次卫生不达标将取消摆摊资格。",
                Map.of("category", "卫生管理", "tag", "hygiene")
            ),
            new Document(
                "食品安全规定：1. 餐饮类摊主必须持有有效健康证，有效期一年；2. 食材必须从正规渠道采购，保留进货凭证；3. 生熟食品必须分开存放，刀具案板生熟分开；4. 从业人员操作时必须穿戴工作服、工作帽、口罩；5. 禁止售卖过期变质食品，违者罚款500-2000元并停业整顿。",
                Map.of("category", "食品安全", "tag", "food_safety")
            ),
            new Document(
                "营业时间规定：日间摊位营业时间为早上6点至晚上10点；夜市摊位营业时间为下午5点至凌晨1点。摊主必须在规定时间内出摊和收摊，不得提前占道或延迟收摊。周末和法定节假日可延长营业时间2小时。",
                Map.of("category", "时间管理", "tag", "business_hours")
            ),
            new Document(
                "噪音管理规定：1. 摊位不得使用高音喇叭、音响等扩音设备招揽顾客；2. 餐饮摊位的抽油烟机噪音不得超过60分贝；3. 下午12点至下午2点、晚上10点后禁止产生噪音的操作；4. 违反规定者首次警告，第二次罚款100元，第三次取消摆摊资格。",
                Map.of("category", "噪音管理", "tag", "noise")
            ),
            new Document(
                "摊位申请条件：1. 具有完全民事行为能力，年龄在18-60周岁之间；2. 有合法身份证明和居住证明；3. 无违法犯罪记录；4. 餐饮类需提供健康证；5. 同意遵守摊位管理各项规定；6. 外地户籍人员需提供本地居住证。",
                Map.of("category", "申请条件", "tag", "apply")
            ),
            new Document(
                "投诉处理流程：1. 消费者或其他摊主可通过系统在线提交投诉，上传相关证据照片；2. 管理员在24小时内受理投诉并核实情况；3. 核实属实后对被投诉摊主进行相应处罚（警告/罚款/停业整顿）；4. 处理结果在3个工作日内反馈给投诉人；5. 对处理结果不满意可申请复核。",
                Map.of("category", "投诉处理", "tag", "complaint")
            )
        );

        try {
            // 先尝试查询一下向量库是否已有数据
            List<Document> existing = vectorStore.similaritySearch(
                SearchRequest.query("测试")
                        .withTopK(1)
            );

            if (existing == null || existing.isEmpty()) {
                log.info("=== 向量库为空，开始录入 {} 条本地知识 ===", documents.size());
                vectorStore.add(documents);
                log.info("=== 知识录入完成，共 {} 条 ===", documents.size());
            } else {
                log.info("=== 向量库已有数据，跳过知识初始化 ===");
            }
        } catch (Exception e) {
            log.error("=== 知识初始化失败，请确认 Chroma 向量数据库已启动在 localhost:8000 ===", e);
        }
    }
}

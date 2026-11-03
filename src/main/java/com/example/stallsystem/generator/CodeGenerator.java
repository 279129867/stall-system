package com.example.stallsystem.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 * 一键生成 9 张表的 Entity / Mapper / Service / Controller
 *
 * 运行方式：右键 main 方法 → Run
 */
public class CodeGenerator {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/stall_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "AAaa135@";

        FastAutoGenerator.create(url, username, password)
                .globalConfig(g -> g
                        .author("example")
                        .outputDir(System.getProperty("user.dir") + "/src/main/java")
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(p -> p
                        .parent("com.example.stallsystem")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                System.getProperty("user.dir") + "/src/main/resources/mapper"))
                )
                .strategyConfig(s -> s
                        // 要生成的表（9张）
                        .addInclude(
                                "admin", "stall_owner", "consumer", "stall",
                                "product_category", "product", "orders", "order_item", "review"
                        )
                        // 实体类策略
                        .entityBuilder()
                        .enableLombok()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
                        // Controller 策略
                        .controllerBuilder()
                        .enableRestStyle()
                        // Service 策略
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                )
                .execute();

        System.out.println("===========================================");
        System.out.println("  代码生成完成！");
        System.out.println("  刷新项目目录，查看 entity / mapper / service / controller");
        System.out.println("===========================================");
    }
}

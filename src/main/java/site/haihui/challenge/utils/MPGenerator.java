/**
 * MybatisPlus 代码生成
 * make codeGenerate
 */
package site.haihui.challenge.utils;

import java.util.Collections;
import java.util.Scanner;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

public class MPGenerator {

    public static String getTableNames(Scanner scanner) {
        System.out.println("请输入表名，多张表以英文逗号隔开:");
        if (scanner.hasNext()) {
            String tableName = scanner.next();
            if (!tableName.isEmpty()) {
                return tableName;
            }
        }
        throw new MybatisPlusException("请输入表名");
    }

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        String userName = System.getProperty("user.name");
        Scanner scanner = new Scanner(System.in);
        String[] tableNames = getTableNames(scanner).split(",");
        scanner.close();
        FastAutoGenerator.create(
                "jdbc:mysql://127.0.0.1:33060/challenge?useSSL=false&autoReconnect=true&characterEncoding=utf8",
                "root", "123456")
                .globalConfig(builder -> {
                    builder.author(userName)
                            .disableOpenDir()
                            .enableSpringdoc()
                            .outputDir(projectPath + "/src/main/java");
                })
                .packageConfig(builder -> {
                    builder.parent("site.haihui")
                            .moduleName("challenge")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper/"));
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tableNames);
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}

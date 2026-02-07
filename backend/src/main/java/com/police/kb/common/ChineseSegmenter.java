package com.police.kb.common;

import lombok.extern.slf4j.Slf4j;
import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 中文分词器
 * <p>
 * 使用Ansj库实现中文文本的分词功能。
 * 支持停用词过滤，返回分词结果列表。
 * </p>
 * <p>
 * 功能特性：
 * <ul>
 *   <li>智能分词 - 基于词典和语法分析</li>
 *   <li>停用词过滤 - 过滤常见无意义词汇</li>
 *   <li>词性标注 - 返回词语的词性信息</li>
 * </ul>
 * </p>
 *
 * @author Police KB System
 * @version 1.0.0
 * @since 2026-02-05
 */
@Slf4j
public class ChineseSegmenter {

    /**
     * 停用词集合
     */
    private static final Set<String> STOP_WORDS = new HashSet<>();

    /**
     * 停用词性集合
     */
    private static final Set<String> STOP_NATURES = new HashSet<>();

    static {
        STOP_WORDS.add("的");
        STOP_WORDS.add("了");
        STOP_WORDS.add("是");
        STOP_WORDS.add("在");
        STOP_WORDS.add("我");
        STOP_WORDS.add("有");
        STOP_WORDS.add("和");
        STOP_WORDS.add("就");
        STOP_WORDS.add("不");
        STOP_WORDS.add("人");
        STOP_WORDS.add("都");
        STOP_WORDS.add("一");
        STOP_WORDS.add("一个");
        STOP_WORDS.add("上");
        STOP_WORDS.add("也");
        STOP_WORDS.add("很");
        STOP_WORDS.add("到");
        STOP_WORDS.add("说");
        STOP_WORDS.add("要");
        STOP_WORDS.add("去");
        STOP_WORDS.add("你");
        STOP_WORDS.add("会");
        STOP_WORDS.add("着");
        STOP_WORDS.add("没有");
        STOP_WORDS.add("看");
        STOP_WORDS.add("好");
        STOP_WORDS.add("自己");
        STOP_WORDS.add("这");
        STOP_WORDS.add("那");
        STOP_WORDS.add("么");
        STOP_WORDS.add("呢");
        STOP_WORDS.add("吧");
        STOP_WORDS.add("啊");
        STOP_WORDS.add("哦");
        STOP_WORDS.add("嗯");
        STOP_WORDS.add("还");
        STOP_WORDS.add("又");
        STOP_WORDS.add("但");
        STOP_WORDS.add("但是");
        STOP_WORDS.add("而且");
        STOP_WORDS.add("所以");
        STOP_WORDS.add("因为");
        STOP_WORDS.add("如果");
        STOP_WORDS.add("虽然");
        STOP_WORDS.add("然后");
        STOP_WORDS.add("接着");
        STOP_WORDS.add("再");
        STOP_WORDS.add("又");
        STOP_WORDS.add("被");
        STOP_WORDS.add("让");
        STOP_WORDS.add("给");
        STOP_WORDS.add("把");
        STOP_WORDS.add("被");

        STOP_NATURES.add("w");
        STOP_NATURES.add("p");
        STOP_NATURES.add("r");
        STOP_NATURES.add("u");
        STOP_NATURES.add("c");
        STOP_NATURES.add("y");
        STOP_NATURES.add("e");
        STOP_NATURES.add("o");
        STOP_NATURES.add("i");
        STOP_NATURES.add("q");
        STOP_NATURES.add("x");
        STOP_NATURES.add("d");
        STOP_NATURES.add("t");
        STOP_NATURES.add("s");
        STOP_NATURES.add("f");
        STOP_NATURES.add("vg");
        STOP_NATURES.add("vd");
        STOP_NATURES.add("vx");
        STOP_NATURES.add("vl");
        STOP_NATURES.add("vu");
        STOP_NATURES.add("vn");
    }

    /**
     * 标点符号模式
     */
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{P}\\p{S}\\p{C}]+");

    static {
        STOP_WORDS.addAll(List.of("第", "条", "款", "项", "节", "章", "卷", "部", "编", "附"));
    }

    /**
     * 中文分词
     * <p>
     * 对输入的中文文本进行分词处理，
     * 返回去除停用词后的词语列表。
     * </p>
     *
     * @param text 输入文本
     * @return 分词后的词语列表
     */
    public static List<String> segment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Result result = ToAnalysis.parse(text);
        List<Term> terms = result.getTerms();

        List<String> tokens = new ArrayList<>();
        for (Term term : terms) {
            String word = term.getName();
            String nature = term.getNatureStr();

            if (STOP_WORDS.contains(word) ||
                STOP_NATURES.contains(nature) ||
                word.trim().isEmpty() ||
                PUNCTUATION_PATTERN.matcher(word).matches()) {
                continue;
            }

            tokens.add(word);
        }

        return tokens;
    }

    /**
     * 分词并返回原始词列表
     *
     * @param text 输入文本
     * @return 原始分词列表（不过滤）
     */
    public static List<String> segmentRaw(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Result result = ToAnalysis.parse(text);
        List<Term> terms = result.getTerms();

        List<String> tokens = new ArrayList<>();
        for (Term term : terms) {
            String word = term.getName();
            if (!word.trim().isEmpty()) {
                tokens.add(word);
            }
        }

        return tokens;
    }

    /**
     * 获取词性标注
     *
     * @param text 输入文本
     * @return 词性标注列表
     */
    public static List<String> segmentWithNature(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Result result = ToAnalysis.parse(text);
        List<Term> terms = result.getTerms();

        List<String> tokens = new ArrayList<>();
        for (Term term : terms) {
            String word = term.getName();
            String nature = term.getNatureStr();
            tokens.add(word + "/" + nature);
        }

        return tokens;
    }
}

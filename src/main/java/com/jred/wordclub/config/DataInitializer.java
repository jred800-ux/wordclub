package com.jred.wordclub.config;

import com.jred.wordclub.entity.Word;
import com.jred.wordclub.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final WordRepository wordRepository;

    @Override
    public void run(String... args) {
        if (wordRepository.count() > 0) {
            log.info("词库已有 {} 条数据，跳过初始化", wordRepository.count());
            return;
        }

        List<Word> words = List.of(
                createWord("persistent", "坚持不懈的；执着的", "/pərˈsɪstənt/", "adjective", "sist", "per-", "-ent"),
                createWord("elaborate", "精心制作的；详细阐述", "/ɪˈlæbərət/", "adjective, verb", "labor", "e-", "-ate"),
                createWord("substantial", "大量的；实质的", "/səbˈstænʃəl/", "adjective", "stant", "sub-", "-ial"),
                createWord("consequence", "结果；重要性", "/ˈkɑːnsɪkwens/", "noun", "sequ", "con-", "-ence"),
                createWord("predominant", "主要的；占优势的", "/prɪˈdɑːmɪnənt/", "adjective", "domin", "pre-", "-ant"),
                createWord("unprecedented", "史无前例的", "/ʌnˈpresɪdentɪd/", "adjective", "cede", "un-pre-", "-ed"),
                createWord("comprehensive", "全面的；综合的", "/ˌkɑːmprɪˈhensɪv/", "adjective", "prehens", "com-", "-ive"),
                createWord("extraordinary", "非凡的；特别的", "/ɪkˈstrɔːrdəneri/", "adjective", "ordin", "extra-", "-ary"),
                createWord("controversial", "有争议的", "/ˌkɑːntrəˈvɜːrʃəl/", "adjective", "vers", "contro-", "-ial"),
                createWord("indispensable", "不可或缺的", "/ˌɪndɪˈspensəbl/", "adjective", "pens", "in-dis-", "-able"),
                createWord("sophisticated", "复杂精密的；老练的", "/səˈfɪstɪkeɪtɪd/", "adjective", "soph", "", "-ated"),
                createWord("spontaneous", "自发的；自然的", "/spɑːnˈteɪniəs/", "adjective", "spont", "", "-aneous"),
                createWord("deteriorate", "恶化；退化", "/dɪˈtɪriəreɪt/", "verb", "deterior", "", "-ate"),
                createWord("exaggerate", "夸张；夸大", "/ɪɡˈzædʒəreɪt/", "verb", "agger", "ex-", "-ate"),
                createWord("accumulate", "积累；积聚", "/əˈkjuːmjəleɪt/", "verb", "cumul", "ac-", "-ate"),
                createWord("unanimous", "一致同意的", "/juˈnænɪməs/", "adjective", "anim", "un-", "-ous"),
                createWord("vulnerable", "脆弱的；易受伤的", "/ˈvʌlnərəbl/", "adjective", "vulner", "", "-able"),
                createWord("inevitable", "不可避免的", "/ɪnˈevɪtəbl/", "adjective", "evit", "in-", "-able"),
                createWord("phenomenon", "现象；杰出人才", "/fɪˈnɑːmɪnən/", "noun", "phen", "", "-on"),
                createWord("simultaneously", "同时地", "/ˌsaɪmlˈteɪniəsli/", "adverb", "simul", "", "-ously")
        );

        wordRepository.saveAll(words);
        log.info("词库初始化完成，共 {} 条数据", words.size());
    }

    private Word createWord(String spelling, String meaning, String phonetic,
                            String partOfSpeech, String root, String prefix, String suffix) {
        Word word = new Word();
        word.setSpelling(spelling);
        word.setMeaning(meaning);
        word.setPhonetic(phonetic);
        word.setPartOfSpeech(partOfSpeech);
        word.setRoot(root);
        word.setPrefix(prefix);
        word.setSuffix(suffix);
        return word;
    }
}

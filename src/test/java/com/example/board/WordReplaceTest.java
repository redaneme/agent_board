package com.example.board;

import com.example.board.entity.Post;
import com.example.board.repository.PostRepository;
import com.example.board.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:wordtestdb;DB_CLOSE_DELAY=-1")
class WordReplaceTest {

    @Autowired
    PostService service;

    @Autowired
    PostRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private Long save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p).getId();
    }

    @Test
    void 대소문자가_달라_바뀌지_않은_글은_변경_건수에서_제외된다() {
        Long same = save("kfc 후기", "맛있다");
        Long differentCase = save("KFC 후기", "맛있다");

        int changed = service.replaceWord(List.of(same, differentCase), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
        assertThat(repository.findById(same).orElseThrow().getTitle()).isEqualTo("bhc 후기");
        assertThat(repository.findById(differentCase).orElseThrow().getTitle()).isEqualTo("KFC 후기");
    }

    @Test
    void 제목이나_내용_중_하나만_바뀌어도_변경으로_센다() {
        Long onlyContent = save("제목", "kfc 치킨");

        int changed = service.replaceWord(List.of(onlyContent), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
        assertThat(repository.findById(onlyContent).orElseThrow().getContent()).isEqualTo("bhc 치킨");
    }

    @Test
    void 없는_id는_건너뛴다() {
        Long id = save("kfc", "kfc");

        int changed = service.replaceWord(List.of(id, 999_999L), "kfc", "bhc");

        assertThat(changed).isEqualTo(1);
    }
}

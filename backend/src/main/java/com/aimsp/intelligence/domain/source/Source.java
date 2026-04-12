package com.aimsp.intelligence.domain.source;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 500)
    private String url;

    @Column(length = 50)
    private String type; // NEWS|HOMEPAGE|SNS|IDC

    @Column(length = 50)
    private String competitor; // LG_CNS|SK_AX|BESPIN|PWC|GENERAL

    private Boolean active = true;

    private LocalDateTime lastCrawledAt;

    private Integer crawlCount = 0;
    private Integer errorCount = 0;
}

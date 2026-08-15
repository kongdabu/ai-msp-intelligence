package com.aimsp.intelligence.domain.radar;

import java.util.List;

public final class RadarCatalog {

    private RadarCatalog() {
    }

    public record Lens(String code, String label, String description) {
    }

    public record PlayerSeed(String name, String layer, String country, String website, int watchPriority) {
    }

    public static final List<Lens> LENSES = List.of(
            new Lens("AI_AGENT", "AI Agent", "에이전트 제품·플랫폼과 자율 업무 실행 구조"),
            new Lens("FRONTIER_LABS", "Frontier Labs", "모델 경쟁력과 생태계 지배력의 변화"),
            new Lens("PARTNERSHIP", "Partnership", "모델사·클라우드·SI 간 결합 및 판매 구조"),
            new Lens("DEPLOYMENT_MODEL", "FDE·RDE·ODE", "현장 투입형 AI 딜리버리 모델과 전문 역할"),
            new Lens("AI_PRICING", "AI Pricing", "인력 투입형에서 사용량·성과형으로의 과금 전환"),
            new Lens("AGENTIC_OPERATIONS", "Agentic ITO", "AIOps·운영 자동화와 관리형 서비스의 재편")
    );

    public static final List<PlayerSeed> WATCHLIST = List.of(
            new PlayerSeed("OpenAI", "FRONTIER_LAB", "미국", "https://openai.com", 1),
            new PlayerSeed("Anthropic", "FRONTIER_LAB", "미국", "https://www.anthropic.com", 1),
            new PlayerSeed("Google DeepMind", "FRONTIER_LAB", "미국", "https://deepmind.google", 1),
            new PlayerSeed("Meta", "FRONTIER_LAB", "미국", "https://ai.meta.com", 2),
            new PlayerSeed("xAI", "FRONTIER_LAB", "미국", "https://x.ai", 2),
            new PlayerSeed("Mistral AI", "FRONTIER_LAB", "프랑스", "https://mistral.ai", 2),
            new PlayerSeed("Microsoft", "CSP_PLATFORM", "미국", "https://www.microsoft.com", 1),
            new PlayerSeed("AWS", "CSP_PLATFORM", "미국", "https://aws.amazon.com", 1),
            new PlayerSeed("Google Cloud", "CSP_PLATFORM", "미국", "https://cloud.google.com", 1),
            new PlayerSeed("Oracle", "CSP_PLATFORM", "미국", "https://www.oracle.com", 2),
            new PlayerSeed("Salesforce", "CSP_PLATFORM", "미국", "https://www.salesforce.com", 2),
            new PlayerSeed("ServiceNow", "CSP_PLATFORM", "미국", "https://www.servicenow.com", 2),
            new PlayerSeed("Accenture", "CONSULTING", "아일랜드", "https://www.accenture.com", 1),
            new PlayerSeed("McKinsey", "CONSULTING", "미국", "https://www.mckinsey.com", 2),
            new PlayerSeed("BCG", "CONSULTING", "미국", "https://www.bcg.com", 2),
            new PlayerSeed("Deloitte", "CONSULTING", "영국", "https://www.deloitte.com", 1),
            new PlayerSeed("PwC", "CONSULTING", "영국", "https://www.pwc.com", 1),
            new PlayerSeed("EY", "CONSULTING", "영국", "https://www.ey.com", 2),
            new PlayerSeed("KPMG", "CONSULTING", "네덜란드", "https://kpmg.com", 2),
            new PlayerSeed("IBM", "GLOBAL_SI_MSP", "미국", "https://www.ibm.com", 1),
            new PlayerSeed("Capgemini", "GLOBAL_SI_MSP", "프랑스", "https://www.capgemini.com", 2),
            new PlayerSeed("NTT DATA", "GLOBAL_SI_MSP", "일본", "https://www.nttdata.com", 1),
            new PlayerSeed("TCS", "GLOBAL_SI_MSP", "인도", "https://www.tcs.com", 2),
            new PlayerSeed("Infosys", "GLOBAL_SI_MSP", "인도", "https://www.infosys.com", 2),
            new PlayerSeed("Cognizant", "GLOBAL_SI_MSP", "미국", "https://www.cognizant.com", 2),
            new PlayerSeed("Fujitsu", "GLOBAL_SI_MSP", "일본", "https://www.fujitsu.com", 2),
            new PlayerSeed("LG CNS", "KOREA_SI_MSP", "대한민국", "https://www.lgcns.com", 1),
            new PlayerSeed("SK AX", "KOREA_SI_MSP", "대한민국", "https://www.skax.co.kr", 1),
            new PlayerSeed("Samsung SDS", "KOREA_SI_MSP", "대한민국", "https://www.samsungsds.com", 1),
            new PlayerSeed("KT DS", "KOREA_SI_MSP", "대한민국", "https://www.ktds.com", 2),
            new PlayerSeed("Hyundai AutoEver", "KOREA_SI_MSP", "대한민국", "https://www.hyundai-autoever.com", 2),
            new PlayerSeed("POSCO DX", "KOREA_SI_MSP", "대한민국", "https://www.poscodx.com", 2),
            new PlayerSeed("Lotte Innovate", "KOREA_SI_MSP", "대한민국", "https://www.lotteinnovate.com", 3),
            new PlayerSeed("MegazoneCloud", "KOREA_SI_MSP", "대한민국", "https://www.megazone.com", 1),
            new PlayerSeed("Bespin Global", "KOREA_SI_MSP", "대한민국", "https://www.bespinglobal.com", 1)
    );
}

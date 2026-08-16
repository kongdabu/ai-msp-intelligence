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
            new PlayerSeed("Bain & Company", "CONSULTING", "미국", "https://www.bain.com", 1),
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

    public record SignalSeed(
            String title,
            String fact,
            String sourceUrl,
            String sourceTier,
            String signalType,
            int confidenceScore,
            int impactScore,
            List<String> lenses,
            List<String> playerNames,
            String whatChanged,
            String industryStructureImpact,
            String mspOpportunity,
            String mspThreat,
            String structuralRisk,
            String recommendedAction,
            String deliveryModel,
            String pricingModel
    ) {
    }

    public static final List<SignalSeed> SIGNAL_SEEDS = List.of(
            new SignalSeed(
                    "OpenAI, Batch API 50% 할인 및 GPT-4o 토큰 단가 인하로 사용량 기반 과금 경쟁 가속",
                    "OpenAI가 비동기 대량 처리를 위한 Batch API를 발표하고 50% 요금 할인을 적용했으며, GPT-4o 출시와 함께 입력 및 출력 토큰 단가를 대폭 인하했다.",
                    "https://openai.com/index/batch-api/",
                    "TIER_1",
                    "PRICING",
                    95,
                    90,
                    List.of("AI_PRICING", "FRONTIER_LABS"),
                    List.of("OpenAI"),
                    "비동기 워크로드에 대한 50% 할인 Batch API 제공 및 최신 플래그십 모델의 토큰 단가 급격한 인하",
                    "엔터프라이즈 AI 워크로드 비용 장벽이 낮아지며 실시간성 요구 여부에 따른 이원화 과금 체계가 표준으로 안착",
                    "고객사의 AI 파이프라인 비용 최적화(FinOps for AI) 컨설팅 및 배치 처리 전환 아키텍처 구축 수요 증가",
                    "단순 토큰 리셀링 마진 축소로 인한 고부가가치 관리형 서비스 전환 압박",
                    "모델사 간 급격한 단가 인하 경쟁으로 인한 솔루션 마진 변동성",
                    "고객사 배치 워크로드 분리 및 FinOps 비용 절감 패키지를 선제적으로 제안하고 과금 최적화 솔루션 내재화",
                    "MANAGED_SERVICE",
                    "USAGE_BASED"
            ),
            new SignalSeed(
                    "Anthropic, Prompt Caching 도입으로 입력 토큰 비용 최대 90% 절감 지원",
                    "Anthropic이 Claude API에 프롬프트 캐싱(Prompt Caching) 기능을 도입하여 반복 컨텍스트에 대해 최대 90% 비용 절감과 응답 지연 시간 단축을 제공한다.",
                    "https://www.anthropic.com/news/prompt-caching",
                    "TIER_1",
                    "PRICING",
                    95,
                    88,
                    List.of("AI_PRICING", "AI_AGENT"),
                    List.of("Anthropic"),
                    "긴 시스템 프롬프트 및 문서 기반 질의에 대해 캐시된 토큰에 대해 대폭 할인된 과금 정책 적용",
                    "Agentic 워크플로우 및 대규모 문서 검색(RAG) 시 반복 발생하는 토큰 비용이 획기적으로 절감되어 엔터프라이즈 에이전트 도입 비용 장벽 해소",
                    "프롬프트 캐싱 최적화 아키텍처 설계 및 에이전트 파이프라인 성능/비용 튜닝 엔지니어링 서비스 제공",
                    "기존 단순 RAG 구축 프로젝트의 단가 하락 압력",
                    "각 모델사별 캐시 유지 시간 및 과금 구조 상이함에 따른 아키텍처 복잡도 증가",
                    "엔터프라이즈 Agentic ITO 및 RAG 프로젝트에 프롬프트 캐싱 구조를 기본 탑재해 TCO 절감 효과를 입증",
                    "MANAGED_SERVICE",
                    "USAGE_BASED"
            )
    );
}

# StudyFlow Architecture

## Otimizações Realizadas
*   **Performance**: Todos os serviços de IA agora envelopados na classe `AIHelper.kt`, que implementa retentativas com *exponential backoff*. As chamadas utilizam um pool assíncrono padrão do Kotlin Coroutines sem bloquear a Main Thread e com reaproveitamento de instâncias da API.
*   **Cache Avançado**: O `AICacheManager.kt` permite armazenamento flexível para queries estáticas baseadas em *hash* do texto e dificuldade. Isso minimiza requisições repetidas para resumos ou listas de *flashcards* já geradas.
*   **Gerenciamento de Tokens e Telemetria**: Adicionada a classe `AITelemetry.kt` que armazena localmente o uso em memória (latência média, sucessos, erros, re-tentativas e estimativa de tokens). Este log pode ser consultado via painel em "Configurações".
*   **Monitoramento**: Painel de visualização interno (SettingsScreen) de estatísticas.
*   **Segurança**: O `BuildConfig` é utilizado restritamente para carregar `API_KEY`. Além disso, todos os construtores de IA foram forçados a realizar limpeza via parsing e sanitização de dados com `Json.parseToJsonElement` (evitando injection na UI).

## Fluxo dos Módulos (Estudo, IA e Gamificação)
1.  **Dashboard**: Entrada principal. Acompanhamento rápido das *Tasks* ativas.
2.  **StudyFlow AI (Material Generator e Document Analyzer)**: Módulo responsável por interagir via `NvidiaApiClient` para criar Flashcards, Resumos, Questões e extrair metadados dos conteúdos dos estudantes (Livros, Editais).
3.  **Mentor AI**: Análise holística baseada no histórico do aluno (Tasks completadas, Metas, Provas e Estatísticas). Produz missões diárias com base nas proficiências do banco `Room`.
4.  **Flashcards / Simulados**: Entidades que salvam o progresso em `Room` para consumo cíclico.
5.  **Autenticação**: Integrada com Firebase Auth + Cloud Sync.

## Arquitetura
*   **Design Pattern**: Clean Architecture (Presentation, Domain e Data Layers).
*   **Padrão UI/UX**: MVVM com *StateFlow* + Jetpack Compose (M3).
*   **Concorrência**: Coroutines para tarefas I/O, `Flow` para banco de dados local (Room) em tempo real.
*   **Persistência**: Room para uso offline nativo, Firebase para sync (quando ativo).

O projeto é mantido sob os padrões de tipagem segura do Kotlin.

## Modificações Recentes
*   **Cache Inteligente com Room + DataStore**: Os conteúdos das respostas geradas e material de estudo denso (resumos, flashcards) foram movidos para uma nova tabela nativa no Room (`ai_cache`), melhorando enormemente o uso da RAM (que antes utilizava `ConcurrentHashMap`).
*   **Controle e Metadados**: O `DataStore` (preferences) foi introduzido em `AIMetadataManager` para administrar preferências finas de estado e flags de ativação do cache, assim como carimbo de tempo da última limpeza e métricas.
*   **Limpeza Inteligente de Cache (Time-To-Live)**: Uma política de expiração baseada em TTL temporal (`CACHE_EXPIRATION_MS`) e remoção assíncrona foi implementada.

# StudyFlow — Documentação de Arquitetura

O **StudyFlow** é um aplicativo Android nativo para planejamento, execução e otimização de rotinas de estudo voltadas ao ENEM, Concursos Públicos e Vestibulares, integrando gamificação e inteligência artificial generativa.

---

## 1. Stack Tecnológica Real

* **Linguagem:** Kotlin 2.x
* **Interface do Usuário:** Jetpack Compose com Material Design 3 (M3), suporte a temas dinâmicos e layout adaptativo.
* **Injeção de Dependências:** **Koin 4.0** (não utiliza Hilt). Módulos definidos de forma centralizada em `AppModule.kt`.
* **Arquitetura de Software:** MVVM (Model-View-ViewModel) direto. **Não há camada intermediária de Use Cases**: os `ViewModels` comunicam-se diretamente com os `Repositories`, `Managers` (ex: `GamificationManager`) e `AI Services`.
* **Banco de Dados Local:** **Room Database v14**. Todas as migrações estruturais (desde 3→4 até 13→14) estão consolidadas e versionadas em `data/local/Migrations.kt`, sem fallbacks destrutivos silenciosos.
* **Inteligência Artificial:** **Google Gemini** através do SDK oficial do **Firebase AI Logic** (`com.google.firebase.ai` com backend Google AI).
  * **Modelo Oficial Unificado:** `gemini-2.5-flash`, centralizado em `AIConfig.kt`.
  * **Tratamento de Exceções:** Hierarquia `sealed class AIError` (`ModelNotFound`, `PermissionDenied`, `Network`, `Unknown`), garantindo que erros de infraestrutura ou rede nunca sejam persistidos no banco ou tratados como texto de resposta da IA.
* **Manipulação de Documentos:** `PdfBox-Android` para extração de texto de PDFs selecionados via Storage Access Framework (SAF), com validação de limites de tamanho (15 MB), barreira contra PDFs escaneados sem OCR e validação estrita de JSONs de editais.
* **Autenticação e Nuvem:** Firebase Auth (Email/Senha) e Firebase Firestore para sincronização remota opcional.

---

## 2. Camadas do Projeto

```
com.studyflow.app/
├── data/
│   ├── local/              # Room Entities, DAOs, Database v14 e Migrations.kt
│   └── repository/         # Implementações dos repositórios de dados locais e nuvem
├── domain/
│   ├── ai/                 # GeminiClient, AIConfig, AIError, Serviços de IA e DTOs
│   ├── manager/            # GamificationManager (cálculo de XP, Níveis, Streaks e Conquistas)
│   ├── repository/         # Interfaces dos repositórios
│   └── util/               # DocumentTextExtractor e utilitários de arquivos
├── presentation/
│   ├── components/         # Componentes Compose reutilizáveis (Cards, TopBar, Gráficos)
│   ├── navigation/         # NavGraph e rotas de navegação do app
│   ├── screens/            # Telas Compose por funcionalidade
│   └── viewmodel/          # ViewModels gerenciando StateFlow e eventos de UI
└── di/
    └── AppModule.kt        # Injeção de dependências unificada via Koin
```

---

## 3. Status das Funcionalidades e Módulos

| Módulo | Descrição | Status | Evidência |
| :--- | :--- | :--- | :--- |
| **Autenticação & Perfil** | Cadastro e login por E-mail/Senha, edição de dados e preferências. | **[Implementado, sem teste]** | `AuthScreens.kt`, `AuthViewModel.kt`, `AuthRepositoryImpl.kt` |
| **Dashboard Principal** | Visão geral diária, resumo de tarefas, status de metas e componente "Dica do Dia" com Gemini. | **[Implementado, sem teste]** | `HomeScreen.kt`, `HomeViewModel.kt` |
| **Dica do Dia (IA)** | Sugestões diárias de neurociência e métodos de estudo via Gemini com cache diário e fallback resiliente. | **[Implementado, sem teste]** | `StudyTipService.kt` |
| **Gestão de Disciplinas & Tarefas** | CRUD de disciplinas com cores/ícones, pesos, alvos de horas e agendamento de tarefas. | **[Implementado, sem teste]** | `SubjectRepositoryImpl.kt`, `TaskRepositoryImpl.kt` |
| **Metas de Estudo** | Metas personalizadas para ENEM, Concursos, Vestibulares e Faculdades com data e nota alvo. | **[Implementado, sem teste]** | `StudyGoalsScreen.kt`, `StudyGoalDao.kt` |
| **Sessões de Estudo & Pomodoro** | Cronômetro de foco (25/5/15), contagem de ciclos, cálculo de tempo estudado e premiação de XP/Streak. | **[Implementado e testado]** | `GamificationSessionTest.kt` |
| **Simulados** | Registro de provas realizadas, acertos/erros e cálculo de nota na escala ENEM (0 a 1000 pontos). | **[Implementado e testado]** | `SimulationScoreTest.kt` |
| **Módulo de Redação** | Editor com contagem de palavras/tempo, banco de temas e correção estruturada pelas 5 competências do ENEM via IA. | **[Implementado e testado]** | `EssayScoreTest.kt` |
| **Tutor Inteligente (AI Tutor)** | Chat interativo com histórico persistente no Room, controle de parâmetros e botão funcional de "Regenerar". | **[Implementado, sem teste]** | `AITutorService.kt`, `AITutorViewModel.kt` |
| **Mentor Inteligente (AI Mentor)** | Análise holística de desempenho, diagnóstico de matérias negligenciadas e geração de missões inteligentes. | **[Implementado, sem teste]** | `MentorService.kt`, `MentorViewModel.kt` |
| **Gerador de Materiais** | Síntese de resumos (curto, completo, tópicos), geração de flashcards e questões de múltipla escolha via IA. | **[Implementado, sem teste]** | `MaterialGeneratorService.kt`, `MaterialGeneratorViewModel.kt` |
| **Analisador de Documentos/Editais** | Importação via SAF, leitura com PdfBox-Android, detecção de formato/binários e análise por blocos. | **[Implementado e testado]** | `DocumentTextExtractorTest.kt` |
| **Gamificação** | Sistema de XP, cálculo de níveis, progressão de streaks diários, medalhas (Bronze/Prata/Ouro) e desafios semanais. | **[Implementado e testado]** | `GamificationSessionTest.kt` |
| **Estatísticas & Gráficos** | Dashboard visual de tempo estudado (Hoje, Semana, Mês, Total) e progresso de metas diárias. | **[Implementado, sem teste]** | `StatisticsScreen.kt`, `StatisticsViewModel.kt` |
| **Banco de Dados Local & Migrações** | Room Database v14 e migrações estruturais seguras de v3 até v14 sem perda de dados. | **[Implementado e testado]** | `RoomMigrationTest.kt` |
| **Sincronização em Nuvem (Sync)** | Backup e restore bidirecional com Firebase Firestore quando conectado a uma conta ativa. | **[Implementado, sem teste]** | `SyncRepositoryImpl.kt`, `SyncViewModel.kt` |
| **Exportação de Relatórios PDF** | Exportação física do histórico e relatórios completos em arquivo PDF. | **[Planejado]** | Funcionalidade planejada (sem código ou telas ativas) |

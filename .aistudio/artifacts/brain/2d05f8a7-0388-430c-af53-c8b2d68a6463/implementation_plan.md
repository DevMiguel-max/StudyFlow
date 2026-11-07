# Plano de Implementação: Consentimento Prévio & Verificação Etária (ECA Digital e LGPD)

Garante que a verificação de faixa etária (ECA Digital — Lei 15.211/2025) e o consentimento aos Termos de Uso e Política de Privacidade (LGPD — Lei 13.709/2018) sejam rigorosamente cumpridos **antes de qualquer acesso** ao StudyFlow, abrangendo tanto a criação de conta, login com Google/e-mail, quanto a navegação em modo offline.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Decisões confirmadas pelo usuário:**
> 1. **Ponto de Apresentação:** Tela inicial dedicada de boas-vindas e consentimento apresentada logo na abertura do aplicativo, antes de qualquer opção de login, cadastro ou acesso offline.
> 2. **Modo Offline:** O acesso ao modo offline só é liberado se a idade for válida (≥ 12 anos) e os termos tiverem sido expressamente aceitos.
> 3. **Gestão em Configurações:** A faixa etária informada e a versão/data do consentimento serão exibidas na tela de Configurações, com opção para revogação dos termos.
> 4. **ECA Digital (< 12 anos):** Bloqueio estrito de acesso para crianças menores de 12 anos sem representação parental, com aviso educativo em conformidade com a Lei 15.211/2025.

---

## 1. Overview & Core Concept

- **O que faz:** Introduz uma barreira prévia de conformidade legal na inicialização do app (`WelcomeConsentScreen`). Ninguém consegue acessar telas de estudo, criar conta, fazer login ou usar o modo offline sem antes declarar sua idade e aceitar ativamente os Termos de Uso e Política de Privacidade.
- **Público-alvo:** Estudantes e concurseiros de todas as idades, assegurando proteção integral a crianças e adolescentes.
- **Valor Principal:** Cumprimento rigoroso da legislação brasileira (ECA Digital, LGPD e Marco Civil), impedindo que usuários transitem pelo aplicativo ou pelo modo offline de maneira anônima ou sem consentimento verificado.

---

## 2. User Experience & Visual Design

### Fluxo de Usuário (Zero-State ao Acesso)

1. **Abertura do Aplicativo:**
   - O aplicativo verifica se o usuário já possui consentimento e faixa etária válidos registrados no `PrivacyManager`.
   - Se pendente: direciona obrigatoriamente para a rota `welcome_consent`.
   - Se já aprovado: segue normalmente para a tela inicial (`home`) ou `login`.

2. **Tela de Boas-Vindas e Consentimento (`WelcomeConsentScreen`):**
   - **Cabeçalho:** Logotipo e identidade visual do StudyFlow ("Boas-vindas ao StudyFlow").
   - **Seletor de Faixa Etária (ECA Digital):**
     - Três opções selecionáveis em chips/cartões:
       - `< 12 anos` (Criança)
       - `12 a 17 anos` (Adolescente)
       - `18 anos ou mais` (Adulto)
     - **Regra < 12 anos:** Exibição de card de bloqueio com aviso educativo: *"Em conformidade com o ECA Digital (Lei 15.211/2025), o acesso direto para menores de 12 anos é bloqueado sem consentimento e representação formal de pais ou responsáveis."* Botão de avanço desabilitado permanentemente.
     - **Regra 12 a 17 anos:** Exibição de aviso protetivo: *"Conta protegida para adolescentes: telemetria e coleta de dados analíticos desativadas permanentemente."*
   - **Aceite dos Termos de Uso e Política de Privacidade (LGPD):**
     - Checkbox não pré-marcado: *"Li e concordo com os Termos de Uso e a Política de Privacidade"*.
     - Links clicáveis com abertura direta para `terms_of_service` e `privacy_policy`.
   - **Ação Principal:**
     - Botão "Continuar" habilitado apenas quando uma faixa etária válida (≥ 12 anos) estiver selecionada e o checkbox de termos estiver marcado.
     - Ao clicar em "Continuar": persiste os dados no `PrivacyManager` (e sincroniza com o perfil local e nuvem caso autenticado).

3. **Transição para Autenticação ou Modo Offline:**
   - Após a aprovação inicial, o usuário é conduzido à tela `login`, onde pode escolher:
     - Fazer login com e-mail/senha;
     - Fazer login com conta Google;
     - Criar uma nova conta;
     - **"Continuar Offline"**: agora respaldado pela validação legal prévia.

4. **Gerenciamento em Configurações (`SettingsScreen`):**
   - Exibição da faixa etária confirmada (ex: "Faixa etária: 12 a 17 anos" ou "18+ anos").
   - Indicador de consentimento ativo com data e versão (`v1.0_2026-11`).
   - Botão para **"Revogar consentimento dos Termos"**: apaga as preferências legais e reconduz o aplicativo à tela de boas-vindas inicial.

---

## 3. Key Product Decisions & Trade-Offs

- **Decisão 1: Tela de Entrada Única (`welcome_consent`) vs. Múltiplos Diálogos Espalhados:**
  - *Abordagem Escolhida:* Uma tela inicial limpa e bloqueante como rota de entrada raiz do aplicativo.
  - *Motivo:* Garante conformidade uniforme e auditável antes de qualquer ponto de entrada (login, cadastro, recuperação de senha ou modo offline), sem poluir a tela de login nem criar redundâncias.
- **Decisão 2: Persistência no `PrivacyManager` (DataStore) e sincronização com `UserProfile` (Room):**
  - *Abordagem Escolhida:* Armazenar o estado imediatamente no DataStore seguro e gravar no `UserProfile` local (e Firestore ao autenticar).
  - *Motivo:* Permite validar a elegibilidade no primeiro frame do aplicativo mesmo antes de qualquer tabela ou sessão do usuário ser carregada.
- **Decisão 3: Manutenção do Schema do Room:**
  - *Abordagem Escolhida:* Não alterar a versão do banco Room (v14) e reutilizar os campos de auditoria e perfil já disponíveis.

---

## 4. Arquitetura Técnica & Estratégia de Dados

### Diagrama do Fluxo de Navegação e Consentimento

```
                ┌─────────────────────────────────┐
                │   StudyFlow Launch / Restart    │
                └────────────────┬────────────────┘
                                 │
                   ¿Consentimento & Idade Válidos?
                                 │
                     ┌───────────┴───────────┐
                     ▼                       ▼
                   [NÃO]                   [SIM]
                     │                       │
          ┌─────────────────────┐            │
          │ WelcomeConsentScreen│            │
          │ (ECA + LGPD Gate)   │            │
          └──────────┬──────────┘            │
                     │                       │
      Idade < 12?    │  Idade ≥ 12 + Termos? │
      ┌──────────────┴───────────────┐       │
      ▼                              ▼       │
 [BLOQUEADO]                 [Grava Consent] │
 (Aviso ECA)                         │       │
                                     ▼       ▼
                            ┌────────────────┐
                            │  LoginScreen   │
                            └────────┬───────┘
                                     │
           ┌─────────────────────────┼─────────────────────────┐
           ▼                         ▼                         ▼
     [Login Online]           [Criar Conta]           [Continuar Offline]
           │                         │                         │
           └─────────────────────────┼─────────────────────────┘
                                     ▼
                            ┌────────────────┐
                            │   HomeScreen   │
                            └────────┬───────┘
                                     │
                            ┌────────▼───────┐
                            │ SettingsScreen │
                            │ (Ver / Revogar)│
                            └────────────────┘
```

### Componentes Envolvidos

1. `PrivacyManager.kt`:
   - Adicionar método `isConsentAndAgeCompleted(): Boolean` e `revokeTermsConsent()`.
2. `WelcomeConsentScreen.kt` (Novo Composable):
   - Apresentação visual acolhedora com os seletores de idade e aceite dos termos.
   - Navegação direta para leitura da Política de Privacidade e Termos de Uso.
3. `AppNavigation.kt`:
   - Adicionar rota `welcome_consent`.
   - Ajustar `startDestination` condicional para `welcome_consent` quando pendente.
4. `LoginScreen.kt` & `SignUpScreen.kt`:
   - Atualizar `SignUpScreen` para simplificar a criação de conta (já que a verificação etária e os termos foram previamente validados na entrada).
   - Manter a opção de leitura e revisão dos termos no rodapé do login.
5. `SettingsScreen.kt`:
   - Adicionar cartão informativo detalhando a faixa etária atual e o botão para revogar consentimento e reiniciar as preferências legais.

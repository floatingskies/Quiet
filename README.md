# 🛡️ Quiet

App Android que **bloqueia 100% das chamadas** que não estão na sua **lista de confiança** (whitelist). Feito para o povo brasileiro cansado de golpes de ligação, ligações mudas que gravam sua voz, e spam telefônico.

> **Proteção vitalícia contra golpes.** Quem não está na sua lista é **desligado na cara** — sem toque, sem chamada perdida, sem notificação, sem susto.

**Application ID:** `org.floatingskies.Quiet`

---

## ✨ O que o app faz

- ✅ **Whitelist de confiança**: só quem você autorizar pode te ligar
- ✅ **Bloqueio silencioso**: a chamada é recusada e desligada sem você saber
- ✅ **Sem chamada perdida**: não aparece no registro de chamadas
- ✅ **Sem notificação**: nada de "fulano tentou te ligar"
- ✅ **Bloqueio de números ocultos/privados** (configurável)
- ✅ **Modo paranóia**: se a lista estiver vazia, **TODAS** as chamadas são bloqueadas
- ✅ **Log de chamadas bloqueadas**: veja quem tentou e quantas vezes
- ✅ **Exportar CSV**: para denúncia na Anatel/Polícia Civil
- ✅ **Ativação vitalícia**: 1 pagamento, código de 9 linhas, funciona para sempre
- ✅ **Compartilhável**: o código pode ser enviado a familiares
- ✅ **Funciona offline**: a validação do código é 100% local (SHA-256)

---

## 🌍 Compatibilidade

| Android | Versão | Status | Método de bloqueio |
|---------|--------|--------|---------------------|
| 5.0-5.1 | Lollipop | ✅ Funciona com limitações | `ITelephony.endCall()` via reflexão |
| 6.0 | Marshmallow | ✅ Funciona | `ITelephony.endCall()` via reflexão |
| 7.0-9.0 | Nougat-Pie | ✅ **100% silencioso** | `CallScreeningService` |
| 10-14 | Q-UpsideDownCake | ✅ **100% silencioso** | `CallScreeningService` (requer ser app de telefone padrão) |

> **Nota sobre Android 4.4 (KitKat)**: descontinuado pelas bibliotecas modernas do AndroidX. Representa <0.5% do mercado brasileiro em 2024. Para suporte legado, seria necessário rebaixar todas as bibliotecas para versões de 2018-2019 (não recomendado).

### ⚠️ Limitações conhecidas (Android 5.0-6.0)

Nestas versões antigas, o Android não tem API oficial para bloqueio silencioso. O app usa reflexão para chamar `endCall()` do `ITelephony`. Pode ocorrer:

- 🟡 A chamada tocar 1 ring antes de ser desligada (depende do fabricante)
- 🟡 Aparecer como "chamada perdida" em alguns aparelhos (LG, Xiaomi antigos)
- 🔴 Em alguns aparelhos com root/ROM custom, pode não funcionar

**Para Android 7+ o bloqueio é 100% silencioso e oficial via `CallScreeningService`.**

### 📱 Suporte a fabricantes

Testado e compatível com:
- ✅ **Android puro** (Pixel, Nexus, Motorola, Android One)
- ✅ **MIUI** (Xiaomi, Redmi, Poco) — exige "Auto-iniciar" ligado
- ✅ **OneUI** (Samsung Galaxy) — funciona perfeitamente
- ✅ **LG UI** (LG K-series, G-series) — funciona
- ✅ **EMUI** (Huawei) — exige "Proteção de bateria" desligada para o app
- ✅ **ColorOS** (Oppo, Realme) — exige "Iniciar em segundo plano" permitido

---

## 📲 Como compilar o APK

### Pré-requisitos

1. **Android Studio Hedgehog ou superior** (download: https://developer.android.com/studio)
2. **JDK 17** (instalado junto com Android Studio)
3. **Android SDK Platform 34** (Android 14) — Android Studio instala automaticamente
4. **Build Tools 34.0.0**
5. Internet para baixar as dependências do Gradle pela primeira vez

### Passo a passo

```bash
# 1. Copie a pasta BloqueadorChamadasBR para seu computador
# 2. Abra o Android Studio → "Open" → selecione a pasta BloqueadorChamadasBR

# 3. Aguarde o Gradle sincronizar (5-15 min na primeira vez)

# 4. Para gerar o APK de debug (para testar):
#    Menu Build → Build Bundle(s)/APK(s) → Build APK(s)

# 5. Para gerar o APK de release (para distribuir):
#    Menu Build → Generate Signed Bundle / APK → APK
#    Crie uma keystore (primeira vez) e preencha os dados
```

O APK será gerado em:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Compilar via linha de comando (alternativa)

```bash
# Linux/Mac
./gradlew assembleRelease

# Windows
gradlew.bat assembleRelease
```

---

## 🔑 Sistema de ativação (códigos de 9 linhas)

### Como funciona

1. O app gera um código de 9 linhas no formato `XXXX-XXXX-XXXX` por linha
2. As 8 primeiras linhas são aleatórias (corpo)
3. A 9ª linha é a **assinatura** = SHA-256(corpo + segredo) → 12 chars → formatados
4. O app valida offline: recalcula a assinatura e compara com a 9ª linha
5. **Sem internet necessária** para validar

### Gerar códigos para clientes pagantes

Use o script Python na pasta `ferramentas/`:

```bash
cd ferramentas

# Gera 1 código
python3 gerar_codigo.py

# Gera 5 códigos
python3 gerar_codigo.py -n 5

# Gera 10 códigos e salva em arquivo
python3 gerar_codigo.py -n 10 -o codigos_clientes.txt

# Formato compacto (com pipes | para enviar por WhatsApp)
python3 gerar_codigo.py --compacto
```

**Exemplo de código gerado:**
```
0Z1R-7SR1-Y0U0
CUQF-L80H-QUSI
2EWN-BF7C-P21Z
96UU-X0ZV-MZZE
4O8R-3ERD-OYOT
TXDY-E03L-K3RV
6D55-BPVC-DRFQ
AY70-Y8QI-DL4L
6435-4860-50F0
```

### Fluxo de pagamento (recomendado)

1. Cliente paga R$ 39,90 via PIX (QR Code no app)
2. Cliente informa email + ID do comprovante no app
3. Você (desenvolvedor) recebe a notificação do PIX
4. Gera um código com `gerar_codigo.py`
5. Envia o código por email ao cliente
6. Cliente cola o código na tela de ativação → pronto!

> **Importante**: O segredo `BloqueadorBR-2024-Lifetime-Protect-Key` está embarcado no APK. Um usuário técnico poderia extraí-lo (fazendo engenharia reversa) e gerar códigos próprios. Para máxima segurança, em uma versão futura, considere adicionar validação online (backend) que verifique o código contra um banco de dados de códigos emitidos.

---

## 🏗️ Estrutura do projeto

```
BloqueadorChamadasBR/
├── app/
│   ├── build.gradle                          # Configurações do módulo + dependências
│   ├── proguard-rules.pro                    # Regras de ofuscação
│   └── src/main/
│       ├── AndroidManifest.xml               # Permissões + declaração de serviços
│       ├── java/com/brazil/bloqueador/
│       │   ├── App.kt                        # Application class (init)
│       │   ├── MainActivity.kt               # Dashboard principal
│       │   ├── data/                         # Camada Room (Whitelist + BlockedCalls)
│       │   ├── service/
│       │   │   └── CallBlockerService.kt     # ⭐ CallScreeningService (Android 7+)
│       │   ├── receiver/
│       │   │   ├── CallReceiver.kt           # Receiver legado (Android 4.4-6)
│       │   │   └── BootReceiver.kt           # Reativa no boot
│       │   ├── ui/
│       │   │   ├── onboarding/               # Tela inicial + permissões
│       │   │   ├── whitelist/                # Lista de confiança
│       │   │   ├── payment/                  # Pagamento PIX
│       │   │   ├── activation/               # Ativação por código
│       │   │   ├── blocked/                  # Log de bloqueadas
│       │   │   └── settings/                # Configurações
│       │   └── util/
│       │       ├── PhoneUtils.kt             # Normalização de números BR
│       │       ├── ActivationValidator.kt    # Validação SHA-256
│       │       ├── PermissionHelper.kt       # Permissões
│       │       └── PrefsManager.kt           # SharedPreferences criptografados
│       └── res/
│           ├── layout/                       # Telas XML
│           ├── values/                       # Cores, strings (PT-BR), estilos
│           ├── drawable/                     # Ícones vetoriais
│           └── mipmap-anydpi-v26/            # Ícone do launcher
├── ferramentas/
│   └── gerar_codigo.py                       # Gerador de códigos de ativação
├── build.gradle                              # Top-level
├── settings.gradle
└── gradle.properties
```

---

## 🔧 Tecnologias

- **Linguagem**: Kotlin 1.9.20
- **minSdk**: 21 (Android 5.0 Lollipop)
- **targetSdk**: 34 (Android 14)
- **UI**: Material Design 3 (escuro, foco em acessibilidade)
- **DB**: Room 2.6.1
- **QR Code**: ZXing + zxing-android-embedded
- **Build**: Gradle 8.2 + Android Gradle Plugin 8.1.4

---

## ⚖️ Aviso legal

Este app é uma **ferramenta de proteção pessoal**. Não substitui:

- 🚨 **Denúncia formal** na Anatel (https://www.anatel.gov.br/consumidor)
- 🚨 **Boletim de ocorrência** na Polícia Civil em caso de golpe consumado
- 🚨 **Notificação** ao Procon em caso de cobrança indevida

O app bloqueia chamadas baseado **apenas no número de telefone**. Não identifica a intenção do chamador. Cabe ao usuário manter sua whitelist atualizada com números legítimos (banco, médico, familiares, etc.).

> **Dica**: Ao adicionar o número do seu banco à whitelist, confirme o número oficial no site ou app do banco. Nunca adicione números recebidos por SMS ou WhatsApp suspeitos.

---

## 🆘 Solução de problemas

| Problema | Solução |
|----------|---------|
| "O app não bloqueia nada" | Verifique se concedeu todas as permissões (telefone, contatos, overlay, bateria) |
| "Aparece chamada perdida mesmo assim" | Você está em Android 4.4-6.0 (limitação do sistema). Em 7+ funciona 100% silencioso |
| "O app para de funcionar depois de um tempo" | Ative "Ignorar otimização de bateria" nas permissões |
| "MIUI desliga o app em segundo plano" | Ative "Auto-iniciar" nas configurações do MIUI → Apps → Bloqueador BR |
| "Não consigo receber ligação do banco" | Adicione o número do banco na whitelist (use o número oficial, não o que aparece no SMS) |
| "Comprei mas não recebi o código" | Verifique o spam do email. Se não chegou em 30min, contate o suporte |

---

## 📞 Suporte

Para reportar bugs ou sugerir melhorias, abra uma issue no repositório do projeto.

**Made com ❤️ para o Brasil.**

# Core Reasoning & Project Constitution

- **Understand Before Changing**: Reverse-engineer existing code, constraints, and conventions before altering behavior. Gather just enough evidence to decide safely, then act; stop when further analysis yields diminishing returns.
- **First Principles & Root Cause**: Reduce problems to facts, constraints, and causal invariants. Fix underlying causes rather than accumulating symptom patches.
- **Simplify Aggressively**: Choose the simplest solution meeting requirements. Eliminate unnecessary abstractions, indirection, branching, hidden state, and cognitive overhead.
- **Focus on High-Impact Scope**: Solve strictly what the current goal requires. Never introduce speculative features or unrequested enhancements.
- **Preserve System Invariants**: Identify and protect data invariants, contracts, ownership, boundaries, and externally relied-upon semantics.
- **Explicit & Consistent Design**: Make ownership, dependencies, contracts, and data flows explicit. Follow established codebase conventions and domain vocabulary.
- **Protect Structural Integrity**: Never treat changes as isolated patches. When structural decay appears (boundary leaks, dependency drift, duplicated concepts, recurring workarounds), resolve the root cause at the proper layer.
- **Smallest Complete Change**: Implement the minimal complete change that solves the issue and validates core assumptions. Prioritize safe, reversible steps.
- **Prevent Recurrence**: When fixing structural issues, add the minimal invariant, guard, contract, or test needed to prevent identical regression.
- **Validate With Evidence**: Verify assumptions, runtime behavior, and state changes with concrete evidence rather than inference.
- **Observe and Iterate**: Inspect execution results against intended invariants after every change, then adjust the next action.

# Architectural Principles & Boundaries

- **Separation of Concerns (SoC)**: Strictly isolate responsibilities; never mix business rules, application orchestration, infrastructure, and UI within the same module or layer.
- **High Cohesion, Low Coupling**: Keep related logic concentrated within its owning module. Expose minimal public interfaces and forbid sharing internal implementation details.
- **Explicit Boundaries**: Every module, package, and bounded context must maintain defined ownership, clean public contracts, and inviolable boundaries.
- **Unidirectional Dependencies**: Enforce strictly unidirectional dependencies. Forbid circular references, reverse dependencies, and core domain logic depending on upper-level implementations.
- **Reversible Design**: Prioritize designs that are easy to alter, replace, or roll back. Avoid premature, hard-to-reverse architectural commitments.
- **YAGNI (You Aren't Gonna Need It)**: Never introduce abstractions, extension points, dependencies, or architectural complexity for unvalidated or future requirements.

# Testing & Mocking Standards

- **測試替身統一使用 MockK**。
- **不要新增 Mockito、Mockito-Kotlin 或其他平行 mocking framework**。
- **Coroutine / suspend function 使用 coEvery、coVerify**。
- **能使用 fake 或真實 domain object 驗證時，不要為了方便過度 mock**。
- **只 mock 外部邊界與不可控依賴，不 mock 純 domain logic**。

# Detekt 靜態程式碼分析與風格規範 (Static Analysis Guidelines)

- **定位與職責**：專案透過 Detekt 與 Ktlint Wrapper（`detekt-rules-ktlint-wrapper`）進行 Kotlin 靜態分析、程式碼風格一致性檢查與潛在代碼異味（Code Smells）防護。
- **設定檔位置**：
  - 主要規則設定檔：`config/detekt/detekt.yml`
  - 歷史已知例外 Baseline：`config/detekt/baseline.xml`
- **執行指令**：
  - 執行全專案 Detekt 檢查：`gradle :app:detekt`（或 `gradle detekt`）
  - 建立或更新 Baseline：`gradle :app:detektBaseline`
- **開發守則**：
  - **不得隨意關閉規則或盲目增加 `@Suppress`**：遇有違規，應先從架構與程式碼簡化著手修復根本原因。
  - **自動修正支援**：Detekt 配置已開啟 `autoCorrect = true`，對於格式問題會自動修正，修改完畢後應重新檢查 Git diff。
  - **新功能與重構提交標準**：所有新增與修改的 Kotlin 原始碼均須通過 `gradle :app:detekt` 檢查，保持零告警、零失敗。

# Konsist 架構守衛與模組邊界測試 (Architecture Fitness Tests)

- **定位與職責**：使用 Konsist 將「架構原則」轉化為「可自動執行的單元測試」，防止分層破壞、依賴洩漏與產品語意偏移。
- **測試檔案位置**：`app/src/test/java/com/example/ArchitectureKonsistTest.kt`
- **執行指令**：
  - 單獨執行 Konsist 架構測試：`gradle :app:testDebugUnitTest --tests "com.example.ArchitectureKonsistTest"`
  - 執行全體單元測試（包含 Konsist）：`gradle :app:testDebugUnitTest --no-configuration-cache`
- **核心架構不變量守衛規範**：
  1. **純淨 Domain 層**：`com.example.domain..` 嚴禁依賴 UI、Room、Android SDK 或 Compose（確保商業邏輯可獨立移植與高可測性）。
  2. **單向分層依賴**：
     - `Application` 層僅依賴 `Domain`，嚴禁反向依賴 `UI` 或 `androidx.room` 具體實作。
     - `UI`（`screens`, `components`）嚴禁直接跨層 import `data.local` 或 `data.repository` 具體實作，一律透過 ViewModel / Application 介面進行狀態驅動。
  3. **代碼整潔與一致性**：全專案禁止使用 `import alias`（`as`），維持命名透明與搜尋一致性。
  4. **無程式碼協作語意邊界**：嚴禁在 Repository、Issue、Artifact 等業務邏輯與介面中混入 Git、Commit、Branch、PR、CI/CD 等開發者底層語意。
- **維護與擴充守則**：
  - 當新增 package、層級或進行重大重構時，必須同步檢視並補充相應的 Konsist 測試案例，守護架構邊界不衰退。

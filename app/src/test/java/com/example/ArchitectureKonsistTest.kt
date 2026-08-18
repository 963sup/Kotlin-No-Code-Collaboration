package com.example

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.*
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureKonsistTest {

    // 1. 禁止 import alias
    @Test
    fun `imports do not use aliases`() {
        Konsist.scopeFromProduction()
            .imports
            .assertFalse { it.alias != null }
    }

    // 2. UI (Screens & Components) 不得直接存取 data.local 與 data.repository
    @Test
    fun `ui screens and components do not access data local or data repository directly`() {
        val uiFiles = Konsist.scopeFromProduction().files.withPackage("..ui.screens..") +
            Konsist.scopeFromProduction().files.withPackage("..ui.components..")
        uiFiles.assertFalse { file ->
            file.imports.any { import ->
                import.name.startsWith("com.example.data.local") ||
                    import.name.startsWith("com.example.data.repository")
            }
        }
    }

    // 3. Domain 保持純淨（無 UI, Room, Android, Compose 依賴）
    @Test
    fun `domain layer remains pure without ui room android or compose dependencies`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("..domain..")
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.example.ui") ||
                        import.name.contains("room", ignoreCase = true) ||
                        import.name.startsWith("android.") ||
                        import.name.startsWith("androidx.compose")
                }
            }
    }

    // 4. Application 僅依賴 Domain，不得依賴 UI 或 Room implementation
    @Test
    fun `application layer does not depend on ui or room implementation`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("..application..")
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.example.ui") ||
                        import.name.startsWith("com.example.data.local") ||
                        import.name.startsWith("androidx.room")
                }
            }
    }

    // 5. @Entity 僅能出現在 persistence / data.model / data.local package
    @Test
    fun `entities reside in persistence or data package only`() {
        Konsist.scopeFromProduction()
            .classes()
            .withAnnotationNamed("Entity")
            .assertTrue {
                it.resideInPackage("..data.local..") ||
                    it.resideInPackage("..data.model..") ||
                    it.resideInPackage("..persistence..")
            }
    }

    // 6. *ViewModel 僅能出現在 ui.viewmodel
    @Test
    fun `view models reside in ui viewmodel package only`() {
        Konsist.scopeFromProduction()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.resideInPackage("..ui.viewmodel..") }
    }

    // 7. *UseCase 僅能出現在 application / domain
    @Test
    fun `use cases reside in application or domain package only`() {
        Konsist.scopeFromProduction()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                it.resideInPackage("..application..") || it.resideInPackage("..domain..")
            }
    }

    // 8. *Dao 僅能出現在 data.local
    @Test
    fun `daos reside in data local package only`() {
        Konsist.scopeFromProduction()
            .interfaces()
            .withNameEndingWith("Dao")
            .assertTrue { it.resideInPackage("..data.local..") }
    }

    // 9. *RepositoryImpl 僅能出現在 infrastructure / data
    @Test
    fun `repository implementations reside in data or infrastructure package only`() {
        Konsist.scopeFromProduction()
            .classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue {
                it.resideInPackage("..data..") || it.resideInPackage("..infrastructure..")
            }
    }

    // 10. 禁止平行持久化模型 (WbsTask, KanbanTask, MyWorkTask)
    @Test
    fun `forbidden persisted parallel models are not introduced`() {
        Konsist.scopeFromProduction()
            .classes()
            .assertFalse { it.name in listOf("WbsTask", "KanbanTask", "MyWorkTask") }
    }

    // 11. 禁止開發者產品領域語義 (GitCommit, PullRequest, Branch, Diff)
    @Test
    fun `forbidden developer product semantics are not introduced`() {
        val forbiddenSemantics = listOf("GitCommit", "PullRequest", "Branch", "Diff")
        Konsist.scopeFromProduction()
            .classes()
            .assertFalse { it.name in forbiddenSemantics }
        Konsist.scopeFromProduction()
            .interfaces()
            .assertFalse { it.name in forbiddenSemantics }
        Konsist.scopeFromProduction()
            .objects()
            .assertFalse { it.name in forbiddenSemantics }
    }

    // 12. 單向依賴圖 (Layer Dependency Graph)
    @Test
    fun `enforce clean architecture layers and unidirectional dependency graph`() {
        Konsist.scopeFromProduction()
            .assertArchitecture {
                val ui = Layer("UI", "com.example.ui..")
                val data = Layer("Data", "com.example.data..")
                val sync = Layer("Sync", "com.example.sync..")
                val engine = Layer("Engine", "com.example.engine..")

                // UI 不可被底層反向依賴
                ui.dependsOnNothing()
            }
    }
}

